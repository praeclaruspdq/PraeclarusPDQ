"""
FLAWD: a formal language for describing event log data quality issues
"""
__author__ = "Jonghyeon Ko"
__version__ = "0.1"
__date__ = "03/10/2025"


import pandas as pd
from datetime import datetime
import re
import sys
import os
dir_home = os.getcwd()
dir_output = dir_home +"/output"

def ScatteredCase(data: pd.DataFrame,  syslist: str, tstart: datetime= None, tend: datetime= None, 
                  case_id_key:str = "Case",activity_key:str = "Activity", timestamp_key:str = "Timestamp"):


    attr = (re.split(r"\:", syslist)[0])[1:]
    condition = (re.split(r"\:", syslist)[1])[:-1] 
    
    if type(eval(condition)) == str:
        condition = [eval(condition)]
    else:
        condition = list(eval(condition))
        
    if (tstart == None) & (tend == None):
        log_split1 = data[data[attr] not in condition].reset_index(drop=True)
        log_split2 = data[data[attr] in condition].reset_index(drop=True)
        
    else:
        
        if tstart == None:
            tstart = min(data[timestamp_key])
        if tend == None:
            tend = max(data[timestamp_key])
        
        if sum( ((tstart > data[timestamp_key]) | (data[timestamp_key] > tend ))) ==0:
            raise ValueError( print("Error: no matched events in the time interval", file=sys.stderr))
    
        cond = (data[attr].isin(condition)) & ((tstart <= data[timestamp_key]) & (data[timestamp_key] <= tend ))
        log_split1 = data[~cond].reset_index(drop=True)
        log_split2 = data[cond ].reset_index(drop=True)
        
        error_case = log_split2[[case_id_key, activity_key]].groupby([case_id_key])[activity_key].apply(lambda x: list(x.unique()))
        log_split1['label'] = ""
        for index, value in enumerate(error_case):
            log_split1.loc[log_split1[case_id_key] == str(index), 'label'] =  "Scattered cases(" + str(value) + ")"
        
        # log_split2.to_csv(dir_output + '/' + log_name + '.csv', index= False)

    return log_split1, log_split2