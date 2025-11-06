"""
FLAWD: a formal language for describing event log data quality issues
"""
__author__ = "Jonghyeon Ko"
__version__ = "0.1"
__date__ = "03/10/2025"


import re
import pandas as pd
import numpy as np
from datetime import datetime
from utils.filtering import filter_time
from utils.filtering import filter_declare
import random
import sys


def CollateralEvent(data: pd.DataFrame, target_collats: list, DecConstraint: str, tstart: datetime= None, 
                     tend: datetime= None, ratio:float= None,timep:float = 1, unit = 'sec',
                     case_id_key:str = "Case", timestamp_key:str = "Timestamp",  activity_key:str = "Activity"):
    data_save = data.copy()
    step = 1
    if (tstart == None) & (tend == None):
        pass
    else:
        data = data.groupby(['Case']).apply(lambda x: filter_time(x, tstart, tend, timestamp_key))
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


    result = data.copy()
    result['label'] = ""

    # generating way
    if '>>' in target_collats:  # "[Activity:Make decision>>('Make revision1', 'Make revision2')]"
        attr = (re.split(r"\:", target_collats)[0])[1:]
        remain = (re.split(r"\:", target_collats)[1])[:-1]
        act_init = (re.split(r"\>\>", remain)[0])[1:-1]
        act_added =  (re.split(r"\>\>", remain)[1])
        data_attr = list(result.columns.values)
        data_attr = [i for i in data_attr if i not in [case_id_key, activity_key, timestamp_key]]
        if type(eval(act_added)) == str:
            act_added = [eval(act_added)]
        else:
            act_added = list(eval(act_added))

    for c_id in case_sampled:
        
        loc = result.index[(result[case_id_key]==c_id) & (result[activity_key]== act_init )]
        
        for index in loc:
            row = result.iloc[[index]]        
            form_time = result[timestamp_key].iloc[index]
            temp_time = pd.to_timedelta(0, unit=unit)
            for act in act_added:                
                row[activity_key] = act
                time_add = pd.to_timedelta(round(np.random.uniform(0,timep), 1), unit=unit) + temp_time
                row[timestamp_key] = form_time+time_add
                temp_time = time_add 
                row['label'] = "collateral events(" + str(act_init) + ")"
                result = pd.concat( [result, row] )
        
        result = result.reset_index(drop=True)

    result = result.sort_values([case_id_key, timestamp_key],ascending=[True, True])
    result = result.reset_index(drop=True)
    
    cases = result.Case.unique()
    data_save = data_save[~data_save[case_id_key].isin(cases)].reset_index(drop=True)
    data_save['label'] = ""
    data = pd.concat([data_save, result]).reset_index(drop=True)
    data = data.sort_values([case_id_key, timestamp_key],ascending=[True, True])
    data = data.reset_index(drop=True)
    return data
 