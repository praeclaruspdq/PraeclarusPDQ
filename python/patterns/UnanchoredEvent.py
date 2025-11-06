"""
FLAWD: a formal language for describing event log data quality issues
"""
__author__ = "Jonghyeon Ko"
__version__ = "0.1"
__date__ = "03/10/2025"


import re
from datetime import datetime
from utils.filtering import filter_time
from utils.filtering import filter_declare
import random
import sys

def UnanchoredEvent(data, syslist, TimeFormat, DecConstraint:str, 
                     tstart: datetime= None, tend: datetime= None, ratio:float= None, 
                     case_id_key:str = 'Case', timestamp_key:str = "Timestamp"):

    result = data.copy()
    result['label'] ="" 
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

    
    condition = None
    if ':' in syslist:
        attr_sys = (re.split(r"\:", syslist)[0])[1:]
        condition = (re.split(r"\:", syslist)[1])[:-1]
        
        if type(eval(condition)) == str:
            condition = [eval(condition)]
        else:
            condition = list(eval(condition))
    else:
        if '[' in syslist:
            attr_sys = re.split(r"\(|\)", syslist)[1]
        else:
            attr_sys = syslist    

    org = result.loc[(result[attr_sys].isin(condition))&(result[case_id_key].isin(case_sampled)), timestamp_key]
    org = org.apply(lambda x:  str("unanchored event(") + str(x) + ")")

    time = result.apply(lambda row: str(row[timestamp_key].strftime(TimeFormat)) if (row[attr_sys] in condition)&(row[case_id_key] in case_sampled)  else str(row[timestamp_key]),  axis=1)
    result[timestamp_key] = time
    result.loc[(result[attr_sys].isin(condition))&(result[case_id_key].isin(case_sampled)), 'label'] = org
    
    return result

