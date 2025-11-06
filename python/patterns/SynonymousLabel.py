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

def SynonymousLabel(data: pd.DataFrame, target: str, syns: list, prob: list, DecConstraint:str, 
                     tstart: datetime= None, tend: datetime= None, ratio:float= None, case_id_key:str = 'Case', timestamp_key:str = "Timestamp"):

    
    result = data.copy()
    result['label'] = ""
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
        print("Filtering step", step, ". The number of cases by declare rule: ", len(declared_cases), file=sys.stderr )
        data = data[data[case_id_key].isin(declared_cases)].reset_index(drop=True)
        step += 1
    
    if ratio == None:
        case_sampled = data[case_id_key].unique().tolist()
    else:
        case_sampled = random.sample(data[case_id_key].unique().tolist(), round(len(data[case_id_key].unique().tolist())* ratio))
        print("Filtering step", step, ". The number of cases to be filtered by defined random portion: ", len(case_sampled), file=sys.stderr)


    condition = None
    if ':' in target:
        attr_polluted = (re.split(r"\:", target)[0])[1:]
        condition = (re.split(r"\:", target)[1])[:-1]
        
        if type(eval(condition)) == str:
            condition = [eval(condition)]
        else:
            condition = list(eval(condition))

    else:
        if '[' in target:
            attr_polluted = re.split(r"\(|\)", target)[1]
        else:
            attr_polluted = target

    target = re.split(r"\(|\)", target)[1]

    if sum(result[attr_polluted].isin(condition)) == 0 :
        raise ValueError('Error: Non-relavant activity with activity condition exists')
    else:
        value_size = sum((result[case_id_key].isin(case_sampled))  &  (result[attr_polluted].isin(condition)))
    
        synonymous_label = np.random.choice(syns, size =value_size,  p = prob) 

        result.loc[(result[case_id_key].isin(case_sampled))  & (result[attr_polluted].isin(condition)), 'label'] = str("synonymous label(" + str(target) + ")")
        result.loc[(result[case_id_key].isin(case_sampled))  & (result[attr_polluted].isin(condition)), attr_polluted] = synonymous_label
    
    
    return result
