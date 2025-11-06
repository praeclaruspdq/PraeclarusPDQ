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
import string
import sys

def ScatteredEvent(data: pd.DataFrame, target: list, action:str, loc:str = None, Del:bool = False, ratio:float= None, 
                    tstart: datetime= None, tend: datetime= None,   DecConstraint:str = None,
                    case_id_key:str = "Case",  activity_key:str = "Activity" , timestamp_key:str = "Timestamp"):
    
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
            print("Filtering step", step, ". The number of cases in the time interval (", tstart, ",", tend ,"): ", len(data[case_id_key].unique()), file=sys.stderr)
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

    result = data.copy()

    if '>>' in target:  
        attr = (re.split(r"\:", target)[0])[1:]
        remain = (re.split(r"\:", target)[1])[:-1]
        act_init = (re.split(r"\>\>", remain)[0])[1:-1]
        act_added =  (re.split(r"\>\>", remain)[1])
        data_attr = list(result.columns.values)
        data_attr = [i for i in data_attr if i not in [case_id_key, activity_key, timestamp_key]]
        if type(eval(act_added)) == str:
            act_added = [eval(act_added)]
        else:
            act_added = list(eval(act_added))
        
        idx_add_all = result.index[(result[attr]==str(act_init))]
        
        for idx_add in idx_add_all:
            row = result.iloc[[idx_add]]        
            form_time = result[timestamp_key].iloc[idx_add]
            form_time_next = result[timestamp_key].iloc[idx_add+1]
            duration = form_time_next - form_time
            div=1
            for act in act_added:                
                row[attr] = act
                row[timestamp_key] = form_time+duration*div/(len(act_added)+1)
                
                for da in data_attr:
                    if type(row[da].iat[0]) == str:
                        row[da] = result[da].sample(1).iat[0]
                    elif type(row[da].iat[0]) in [float, int] :
                        row[da] = np.random.choice(np.arange(min(result[da]), max(result[da])))
                    else:
                        pass
                result = pd.concat( [result, row] )
                div = div+1
                
        result = result.sort_values([case_id_key,timestamp_key, activity_key],ascending=[True, True, True])
        result = result.reset_index(drop= True)
        temp = result.copy()
            
    form_parts = [s for s in re.split(r"\[|\]", action) if s != '']


    for idx, x in enumerate(form_parts):
        condition_time = re.compile(r"[^\(]*\([^\)]*\)")
        if condition_time.match(x):
            loc_time = idx
            attr_time = re.split(r"\*", x)[0]
            form_time = re.split(r"\(|\)", x)[1]
            form_parts[loc_time] = attr_time

    form_var = [i for i in form_parts if action[action.find(i)-1] == "["] 
    form_attr = [i for i in form_var if i in result.columns]
    form_regular = [i for i in form_var if i not in result.columns]
    
    if '>>' in target:
        condition = act_added
    
    else:
        condition = None
        if ':' in target:
            attr = (re.split(r"\:", target)[0])[1:]
            condition = (re.split(r"\:", target)[1])[:-1]
            if type(eval(condition)) == str:
                condition = [eval(condition)]
            else:
                condition = list(eval(condition))
        else:
            if '[' in target:
                attr = re.split(r"\(|\)", target)[1]
            else:
                attr = target
            

    idx_move = None
    if ':' in loc:
        name = (re.split(r"\:", loc)[0])[1:]
        idx_move =  int(re.split(r"\(|\)", re.split(r"\:", loc)[1])[1])  # int((re.split(r"\:", loc)[1])[:-1])
        if idx_move >= 0:
            raise ValueError('Error: idx value to be moved should be less than 0')
    else:
        if '[' in loc:
            name = re.split(r"\[|\]", loc)[1]
        else:
            name = loc
        
        if Del == True:
            idx_move = -1


    result[name] = ""
    result['label'] = ""
        
    for c_id in case_sampled:
        
        if condition is None:
            loc = result.index[(result[case_id_key]==c_id)]
        else:
            if sum(result[attr].isin(condition)) == 0 :
                raise ValueError('Error: Non-relavant activity with activity condition exists')
            else:
                loc = result.index[(result[case_id_key]==c_id) & (result[attr].isin(condition))]
        start_idx = min(result.index[(result[case_id_key]==c_id)])
        
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

            for f in form_attr:
                result.loc[index, f] = result.loc[index-1, f]

            if idx_move is None:
                result.loc[index, name] = row_str
            else:
                if min(loc) + idx_move >= start_idx:
                    if result.loc[min(loc) + idx_move, name] == "":
                        result.loc[min(loc) + idx_move, name] = [row_str]
                    else:
                        result.at[min(loc) + idx_move, name] = [result.loc[min(loc) + idx_move, name]] + [row_str]
                        
                else:
                    result.loc[index, name] = row_str
                
            result.loc[index, 'label'] = "Scattered Events(Scattered attr = " + str(form_attr) + ")"

        if (Del == True) and (idx_move is not None):
            if min(loc) + idx_move >= start_idx:
                result.loc[min(loc) + idx_move, 'label'] = "Scattered Events(Scattered attr = " +  str(form_attr) +  ", Activity:"+  str(condition) +  ")"
                result.loc[loc, 'label'] = "DELETE"
    
    
    result = result.loc[result.label != "DELETE"]
    result = result.sort_values([case_id_key,timestamp_key, activity_key],ascending=[True, True, True])

    result = result.reset_index(drop=True)
    
    cases = result.Case.unique()
    data_save = data_save[~data_save[case_id_key].isin(cases)].reset_index(drop=True)
    data_save['label'] = ""
    data = pd.concat([data_save, result]).reset_index(drop=True)
    data = data.sort_values([case_id_key, timestamp_key],ascending=[True, True])
    data = data.reset_index(drop=True)
    return data
 