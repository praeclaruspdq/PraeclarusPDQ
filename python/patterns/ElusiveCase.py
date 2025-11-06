"""
FLAWD: a formal language for describing event log data quality issues
"""
__author__ = "Jonghyeon Ko"
__version__ = "0.1"
__date__ = "03/10/2025"


import pandas as pd
from datetime import datetime
from utils.filtering import filter_time
from utils.filtering import filter_declare
import random
from pm4py.objects.conversion.log import converter as log_converter
from pm4py.utils import get_properties
from pm4py.statistics.variants.log.get import get_variants
from tqdm import tqdm
from sklearn.cluster import KMeans
import sys

def ElusiveCase(data, method:str, case_id_key:str, activity_key:str, timestamp_key:str, 
                DecConstraint:str, tstart:datetime, tend:datetime, ratio:float, gnum:int = 4):
    result = data.copy()
    
    step = 1
    if (tstart == None) & (tend == None):
        pass
    else:
        data = data.groupby([case_id_key]).apply(lambda x: filter_time(x, tstart, tend, timestamp_key))
        data = data.reset_index(drop=True)
        if data.empty:
            raise ValueError("Error: no matched cases in the time interval")
        else:
            print("Filtering step", step, ". The number of cases in the time interval (", tstart, ",", tend ,"): ", len(data.Case.unique()), file=sys.stderr)
        step += 1

    if DecConstraint:
        declared_cases = filter_declare(data, DecConstraint, case_id_key)
        print("Filtering step", step, ". The number of cases by declare rule: ", len(declared_cases) , file=sys.stderr)
        data = data[data[case_id_key].isin(declared_cases)].reset_index(drop=True)
        step += 1
    
    if ratio == None:
        case_sampled = data[case_id_key].unique().tolist()
    else:
        case_sampled = random.sample(data[case_id_key].unique().tolist(), round(len(data[case_id_key].unique().tolist())* ratio))
        print("Filtering step", step, ". The number of cases to be filtered by defined random portion: ", len(case_sampled), file=sys.stderr)

    
    if method == 'KMeans':
        if len(case_sampled) <= gnum:
            raise ValueError('Error: "gnum" should be less than: ', len(case_sampled))
    
    result_selected = result.loc[result[case_id_key].isin(case_sampled)].reset_index(drop= True)
    result_others = result.loc[~result[case_id_key].isin(case_sampled)].reset_index(drop= True)
    result_others['draft_ID'] = result_others[case_id_key]
    result2 = result_selected.sort_values(timestamp_key, ascending=True, kind='mergesort').reset_index(drop=True)

        
    # variant way
    if method == 'Variant':
        parameters = get_properties(result2)
        parameters['pm4py:param:case_id_key'] =case_id_key
        parameters['pm4py:param:activity_key'] =activity_key
        parameters['pm4py:param:timestamp_key'] = timestamp_key

        log = log_converter.apply(result2, variant=log_converter.Variants.TO_EVENT_LOG, parameters=parameters)
        variants = get_variants(log, parameters=parameters)
        
        print("Generating draft IDs... (the below count is based on variant-level)", file=sys.stderr)
        d_id = 0
        rows = []  
        for variant in tqdm(variants):
            for trace in variants[variant]:
                row = [trace.attributes['concept:name'], "draft_" + str(round(d_id/gnum))]
                rows.append(row)  
                d_id += 1

            d_id = (d_id // gnum + 1) * gnum

        df_var = pd.DataFrame(rows, columns=[case_id_key, 'draft_ID'])

        result_selected = result_selected.merge(df_var, how='left', on=case_id_key)

        result = pd.concat([result_selected, result_others])
    
    else:  
        temp_dat = result_selected.copy()
        del temp_dat[timestamp_key]
        cols = list(temp_dat.columns.values)
        cols = [i for i in cols if i != case_id_key]
        grouped = temp_dat.groupby(temp_dat[case_id_key], as_index=False)
        max_events = max(grouped.size()['size'] ) 
        
        dt_transformed = pd.DataFrame(grouped.apply(lambda x: x.name), columns=[case_id_key])

        for i in range(max_events):
            dt_index = grouped.nth(i)[[case_id_key] + cols]
            dt_index.columns = [case_id_key] + ["%s_%s"%(col, i) for col in cols] 
            dt_transformed = pd.merge(dt_transformed, dt_index, on=case_id_key, how="left")
        dt_transformed.index = dt_transformed[case_id_key]
        del dt_transformed[case_id_key]

        # one-hot-encode cat cols

        dt_transformed = pd.get_dummies(dt_transformed)
        
        km = KMeans(n_clusters=gnum)
        km.fit(dt_transformed)
        dt_transformed['draft_ID'] = km.labels_
        dt_transformed['draft_ID'] = dt_transformed['draft_ID'].apply(lambda x: "draft_" + str(x))
        dt_transformed[case_id_key] = dt_transformed.index
        dt_transformed = dt_transformed[[case_id_key, 'draft_ID']].reset_index(drop=True)
        print("The size of each cluster is:", file=sys.stderr)
        print(dt_transformed['draft_ID'].value_counts(), file=sys.stderr)
        
        result_selected = result_selected.merge(dt_transformed, how='left', on=case_id_key)
        
        result = pd.concat([result_selected, result_others])
        
    result['label'] = result[case_id_key]
    del result[case_id_key]
    result = result.reset_index(drop=True)
    
    return result
