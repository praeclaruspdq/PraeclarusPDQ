"""
P-BEAR-RL: A tool to repair anomalous traces in an event log.
"""
__author__ = "Jonghyeon Ko"
__version__ = "0.1"
__date__ = "03/10/2025"


# =============================================================================
# 1. SETUP & IMPORTS
# =============================================================================

# Ignore warnings to keep the output clean.
import warnings
warnings.filterwarnings(action='ignore')

# Standard library imports for operating system interaction, data manipulation, and timing.
import os
import pandas as pd
import numpy as np
import time
import sys
import io

if __name__ == "__main__":

    # Get the absolute path of the current script to build relative paths.
    org_path = os.path.abspath(__file__)
    parent_path = os.path.dirname(org_path)

    # Multiprocessing utilities for parallel execution.
    from multiprocessing import Pool, cpu_count
    from functools import partial

    # --- Import custom utility functions ---
    # These modules contain helper functions for Reinforcement Learning (RL) logic
    # and data preprocessing specific to the P-BEAR algorithm.
    from utils.func_RL import discover_NBG, process_case_id_wrapper
    from utils.preprocess_pbear import *


    # =============================================================================
    # 2. PATH & PARAMETER CONFIGURATION
    # =============================================================================


    # --- Command-line arguments ---
    num_epi =int(sys.argv[1])  # The number of episodes for the RL agent training.
    alpha = float(sys.argv[2]) # The alpha parameter, likely for the learning rate or exploration/exploitation balance.
    DATA_DELIMITER = "---PBEAR_DATA_SEPARATOR---"


    try:
        full_input_string = sys.stdin.read()
        csv_parts = full_input_string.split(f"\n{DATA_DELIMITER}\n")
        if len(csv_parts) != 2:
            print(f"Error: Expected 2 datasets separated by delimiter, but found {len(csv_parts)}", file=sys.stderr)
            sys.exit(1)
            
        anomalous_csv_data = csv_parts[0]
        normal_csv_data = csv_parts[1]
        
        # anomalous data
        data = pd.read_csv(io.StringIO(anomalous_csv_data))
        # normal data
        event_log = pd.read_csv(io.StringIO(normal_csv_data))
        
    except Exception as e:
        print(f"Error reading from stdin: {e}", file=sys.stderr)
        sys.exit(1)



    # =============================================================================
    # 3. DATA LOADING & PREPROCESSING
    # =============================================================================


    event_log = event_log.rename(columns={"Case ID": "case:concept:name", "Activity": "concept:name",
                            "Start.Timestamp": "time:timestamp"}, copy=False)


    data = data.rename(columns={"Case ID": "case:concept:name", "Activity": "concept:name",
                            "Order": "time:timestamp"}, copy=False)


    # Preprocess both logs by adding 'Start' and 'End' activities to each case.
    # This helps in defining clear boundaries for each process instance.
    clean = add_startend(event_log)
    anomaly = add_startend(data)

    # --- Create utility variables from the clean log ---
    # These will be used as a reference model of normal behavior.
    actset = clean['concept:name'].unique()
    # Filter out the 'Start' and 'End' activities to focus on core process steps.
    filtered_actset = [item for item in actset if item not in ['Start', 'End']]
    # Calculate the frequency of each activity in the clean log.
    act_freq = freq_act(clean, filtered_actset)

    # Start the master timer to measure total execution time.
    start_time = time.time()

    # Discover Neighboring Behavior Graphs (NBGs) from the clean event log.
    # The NBG is a key data structure representing normal process flows.
    NBGs = discover_NBG(clean)


    # =============================================================================
    # 4. PARALLEL PROCESSING OF ANOMALOUS LOGS
    # =============================================================================

    # Use the standard '__main__' guard to ensure the multiprocessing code
    # runs only when the script is executed directly.

    # Start a separate timer for the core parallel processing task.
    processing_start_time = time.time()
    print('Start training P-BEAR-RL (Parallelized)', file=sys.stderr)

    # Prepare the anomalous data by selecting relevant columns.
    anomaly_nolabel = anomaly[['case:concept:name', 'concept:name', 'order']]
    caseids = anomaly_nolabel['case:concept:name'].unique().tolist()

    # --- Optimization: Group identical traces (variants) ---
    # To avoid redundant computations, we process each unique trace once and
    # then map the results back to all cases with that trace.
    traces = anomaly_nolabel.groupby('case:concept:name')['concept:name'].apply(lambda x: '>>'.join(x))
    traces = traces.rename_axis('case:concept:name').reset_index(name='variant_id')
    traces = traces.groupby('variant_id', observed=True)['case:concept:name'].apply(lambda x: list(x))
    traces = traces.rename_axis('variant_id').reset_index(name='caseids')

    # 'casezip' contains one representative case ID for each unique variant.
    casezip = [cid[0] for cid in traces['caseids']]
    # 'anomaly_nolabel_zip' is a reduced dataframe containing only the representative cases.
    anomaly_nolabel_zip = anomaly_nolabel.loc[anomaly_nolabel['case:concept:name'].isin(casezip)].reset_index(drop=True)

    # --- Prepare the function for the worker pool ---
    # `functools.partial` freezes the arguments of the wrapper function that are
    # the same for all processes. The only argument that will vary is the case ID.
    func_for_pool = partial(process_case_id_wrapper,
                            p_anomaly_nolabel=anomaly_nolabel_zip,
                            p_filtered_actset=filtered_actset,
                            p_act_freq=act_freq,
                            p_actset=actset,
                            p_NBGs=NBGs,
                            p_num_epi=num_epi,
                            alpha=alpha)

    # Set the number of parallel processes to the number of available CPU cores.
    num_processes = cpu_count()
    print(f"Using {num_processes} processes for parallel execution.", file=sys.stderr)

    # --- Execute the parallel processing ---
    # Create a pool of worker processes.
    with Pool(processes=num_processes) as pool:
        # `pool.map` distributes the `casezip` list across the workers.
        # Each worker runs `func_for_pool` on its assigned case IDs.
        # The results (a list of repaired dataframes) are collected.
        list_of_repaired_dfs_for_cases = pool.map(func_for_pool, casezip)

    # =============================================================================
    # 5. RESULT AGGREGATION & OUTPUT
    # =============================================================================

    # Concatenate the list of dataframes from the workers into a single dataframe.
    # This contains the repaired results for the unique, "zipped" cases.
    final_repaired_df_zip = pd.concat([df for df in list_of_repaired_dfs_for_cases], ignore_index=True)

    # --- "Unzip" the results ---
    # Map the repaired unique traces back to all their original case IDs.
    final_repaired_df = pd.DataFrame()
    for cid in range(len(casezip)):
        # Get the repaired trace for the representative case.
        filtered_df = final_repaired_df_zip.loc[final_repaired_df_zip['case:concept:name'] == casezip[cid]]
        len1 = len(filtered_df)
        
        # Duplicate the repaired trace for all original cases belonging to this variant.
        num_duplicates = len(traces['caseids'][cid])
        filtered_df = pd.concat([filtered_df] * num_duplicates, ignore_index=True)
        
        # Assign the original case IDs back to the duplicated traces.
        filtered_df['case:concept:name'] = np.repeat(traces['caseids'][cid], len1)
        
        # Append to the final result dataframe.
        final_repaired_df = pd.concat([final_repaired_df, filtered_df], ignore_index=True)

    # --- Final formatting and saving ---
    # Remove temporary 'Start' and 'End' activities from the final output.
    final_repaired_df = final_repaired_df.loc[~final_repaired_df["concept:name"].isin(['Start', 'End'])].reset_index(drop=True)

    # Rename columns to a more user-friendly format.
    final_repaired_df = final_repaired_df.rename(columns={"case:concept:name": "Case ID",
                                                          "concept:name": "Activity",
                                                          "order": "Order",
                                                          "predict_patterns": "AnomalyPatterns"}, copy=False)
    
    # Recalculate the 'Order' column to ensure it's sequential within each case.
    final_repaired_df['Order'] = final_repaired_df.groupby('Case ID').cumcount() + 1
    
    try:
        final_repaired_df= final_repaired_df.rename(columns={
                                'case:concept:name': 'Case ID',
                                'concept:name': 'Activity'
                            }
                        )
        final_repaired_df.to_csv(sys.stdout, sep=',', encoding='utf-8', index=False)
    except Exception as e:

        print(f"Python script error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc(file=sys.stderr) 
        sys.exit(1) 
    
    
    print(f"Parallel processing complete.", file=sys.stderr)

    # =============================================================================
    # 6. PERFORMANCE METRICS
    # =============================================================================

    # Stop the master timer.
    end_time = time.time()

    # Calculate and print the execution times.
    total_execution_time = end_time - start_time
    parallel_processing_execution_time = end_time - processing_start_time

    print(f"\nTotal execution time (including NBG discovery): {total_execution_time:.4f} seconds", file=sys.stderr)
    print(f"Parallel P-BEAR-RL training time: {parallel_processing_execution_time:.4f} seconds", file=sys.stderr)