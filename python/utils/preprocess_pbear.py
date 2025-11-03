"""
P-BEAR-RL: A tool to repair anomalous traces in an event log.
"""
__author__ = "Jonghyeon Ko"
__version__ = "0.1"
__date__ = "03/10/2025"

# =============================================================================
# IMPORTS & SETUP
# =============================================================================

# Ignore warnings to keep the output clean.
import warnings
warnings.filterwarnings(action='ignore')

# Standard library imports for data manipulation.
import os
import pandas as pd
import numpy as np


# =============================================================================
# DATA PREPROCESSING FUNCTIONS
# =============================================================================

# def add_startend(data):
#     data['order'] = data.groupby('case:concept:name', observed=True).cumcount() + 1

#     start_df = data[data['order'] == 1].copy()
#     start_df.loc[:, 'order'] = 0

#     start_df.loc[:, 'concept:name'] = "Start"

#     end_indices = data.groupby('case:concept:name', observed=True)['order'].idxmax()
#     end_df = data.loc[end_indices].copy()
#     end_df.loc[:, 'order'] = end_df['order'] + 1
#     # Rename the activity to 'End'.
#     end_df.loc[:, 'concept:name'] = "End"

#     data2 = pd.concat([data, start_df, end_df], ignore_index=True) \
#         .sort_values(by=["case:concept:name", 'order'], kind='stable') \
#         .reset_index(drop=True)
    
#     return data2

def add_startend(data):

    data['order'] = data.groupby('case:concept:name', observed=True).cumcount() + 1

    start_df_orig = data[data['order'] == 1].copy() 
    start_df_orig.loc[:, 'order'] = 0
    start_df_orig.loc[:, 'concept:name'] = "Start"

    end_indices = data.groupby('case:concept:name', observed=True)['order'].idxmax()
    end_df_orig = data.loc[end_indices].copy()
    end_df_orig.loc[:, 'order'] = end_df_orig['order'] + 1
    end_df_orig.loc[:, 'concept:name'] = "End"

    data2 = pd.concat([data, start_df_orig, end_df_orig], ignore_index=True).sort_values(by=["case:concept:name", 'order'], kind='stable').reset_index(drop=True)
    
    return data2


def freq_act(clean, filtered_actset):
    """
    Calculates the frequency and probability of each activity in a clean event log.

    This function is used to understand the distribution of activities, which
    can be used, for example, to guide the action selection in an RL agent.

    Args:
        clean (pd.DataFrame): The clean (normal) event log DataFrame.
        filtered_actset (list): A list of core activities to calculate frequencies for.

    Returns:
        pd.DataFrame: A DataFrame with columns ['concept:name', 'count', 'prob']
                      for each activity in filtered_actset.
    """
    # Calculate the frequency of each activity.
    act_freq = clean['concept:name'].value_counts()

    # Before processing, check if the frequency Series is not empty.
    if not act_freq.empty:
        # Exclude 'Start' and 'End' activities as they are not core process steps.
        act_freq = act_freq.drop(index=['Start', 'End'], errors='ignore')

    # After dropping, check again if the Series has any content.
    if not act_freq.empty:
        act_freq = act_freq.reset_index()
        # Explicitly name the columns for clarity.
        act_freq.columns = ['concept:name', 'count']
        
        # Calculate the probability of each activity.
        act_freq['prob'] = act_freq['count'] / sum(act_freq['count'])
        
        # Reindex the DataFrame to match the order of 'filtered_actset' and ensure
        # all required activities are present (with NaN for any missing ones).
        act_freq = act_freq.set_index('concept:name').reindex(filtered_actset).reset_index()
        act_freq = act_freq.fillna(0) # Replace any NaN values with 0.

    else:
        # Handle the edge case where the event log has no core activities.
        # Create an empty but correctly structured DataFrame to avoid downstream errors.
        act_freq = pd.DataFrame(columns=['concept:name', 'count', 'prob'])
        if filtered_actset:
            act_freq['concept:name'] = filtered_actset
            act_freq = act_freq.fillna(0) # Ensure count and prob are 0, not NaN.
    
    return act_freq