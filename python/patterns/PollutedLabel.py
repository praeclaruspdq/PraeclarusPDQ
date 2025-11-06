"""
FLAWD: a formal language for describing event log data quality issues
"""
__author__ = "Jonghyeon Ko"
__version__ = "0.1"
__date__ = "03/10/2025"


import re
import pandas as pd
from datetime import datetime
from utils.filtering import filter_time
from utils.filtering import filter_declare
import random
import string
import sys

def PollutedLabel(data: pd.DataFrame, target: str, action:str, DecConstraint:str, 
                  tstart: datetime= None, tend: datetime= None, ratio:float= None,\
    case_id_key = 'Case',timestamp_key:str = "Timestamp" ):
    
    data_save = data.copy()
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

    
    
    form_parts = [s for s in re.split(r"\[|\]", action) if s != '']

    for idx, x in enumerate(form_parts):
        condition_time = re.compile(r"[^\(]*\([^\)]*\)")
        if condition_time.match(x):
            loc_time = idx
            attr_time = re.split(r"\*", x)[0]
            form_time = re.split(r"\(|\)", x)[1]
            form_parts[loc_time] = attr_time

    form_var = [i for i in form_parts if action[action.find(i)-1] == "["] 
    form_attr = [i for i in form_var if i in data.columns]
    form_regular = [i for i in form_var if i not in data.columns]
    
    result = data.copy()
    result['label'] = ""
    
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
        
    for c_id in case_sampled:
        
        if condition is None:
            loc = result.index[(result[case_id_key]==c_id)]
        else:
            if sum(result[attr_polluted].isin(condition)) == 0 :
                raise ValueError('Error: Non-relavant activity with activity condition exists')
            else:
                loc = result.index[(result[case_id_key]==c_id) & (result[attr_polluted].isin(condition))]
        
        for index in loc:
            row_str = ''
            for part in form_parts:
                if part in form_attr:
                    if (part == attr_time) and (form_time is not None):
                        row_str = row_str+  str( (result.iloc[index][attr_time]).strftime(str(form_time)) )
                    else:
                        row_str = row_str+str(result.iloc[index][part])
                elif part in form_regular:
                    condition_count = re.compile(r"[^\(]*\:[^\)]*")
                    if 'a-zA-Z' in part:
                        if condition_count.match(part):
                            repeat= int(  re.split(r"\{|\}", re.split(r"\:", part)[1] )[1] )
                        else:
                            repeat=1
                        random_text = ''.join(random.choices(string.ascii_letters, k=repeat))
                    elif 'a-z' in part:
                        if condition_count.match(part):
                            repeat= int(  re.split(r"\{|\}", re.split(r"\:", part)[1] )[1] )
                        else:
                            repeat=1
                        random_text = ''.join(random.choices(string.ascii_lowercase, k=repeat))
                    elif 'A-Z' in part:
                        if condition_count.match(part):
                            repeat= int(  re.split(r"\{|\}", re.split(r"\:", part)[1] )[1] )
                        else:
                            repeat=1
                        random_text = ''.join(random.choices(string.ascii_uppercase, k=repeat))
                    elif '0-9' in part:
                        if condition_count.match(part):
                            repeat= int(  re.split(r"\{|\}", re.split(r"\:", part)[1] )[1] )
                        else:
                            repeat=1
                        random_text = ''.join([str(random.randint(0, 9)) for x in range(repeat)])
                    else:
                        print("Error: wrong or unsupportable regular expression:" + part, file=sys.stderr)
                    
                    row_str = row_str+random_text
                else:
                    row_str = row_str+part

            org = result.loc[index, attr_polluted]
            result.loc[index, attr_polluted] = row_str
            result.loc[index, 'label'] = "polluted Label(" + str(attr_polluted) + ":'" + str(org) +"')"
        
    cases = result.Case.unique()
    data_save = data_save[~data_save[case_id_key].isin(cases)].reset_index(drop=True)
    data_save['label'] = ""
    data = pd.concat([data_save, result]).reset_index(drop=True)
    data = data.sort_values([case_id_key, timestamp_key],ascending=[True, True])
    data = data.reset_index(drop=True)
    return data
 