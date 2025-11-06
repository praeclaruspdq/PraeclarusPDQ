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
from scipy.stats import poisson, expon
import sys 


def InadvertentTimeTravel(data, target, tunit, DecConstraint:str, prob_para:float = 0.2,
                     tstart: datetime= None, prob_func:str = "poisson", tend: datetime= None, ratio:float= None, 
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
    attr = (re.split(r"\:", target)[0])[1:]
    condition = (re.split(r"\:(?!\')", target)[1])[:-1]
    
    if type(eval(condition)) == str:
        condition = [eval(condition)]
    else:
        condition = list(eval(condition))
    
    loc_condition = result[attr].isin(condition)
    
    for c_id in case_sampled:
        
        loc = result.index[(result[case_id_key]==c_id) & (loc_condition)]

        for index in loc:
            org_time = result.loc[index, timestamp_key]

            if tunit == "day":
                dist_previous = (org_time- org_time.replace(hour = 0, minute= 0, second= 0, microsecond= 0)).total_seconds()/(60*60)
                dist_next = (org_time.replace(hour = 23, minute= 59, second= 59, microsecond= 9999) - org_time).total_seconds()/(60*60)
                dist = min(dist_previous, dist_next)
                if prob_func == "poisson":
                    if prob_func == "poisson":
                        p = 1- expon.cdf(dist, prob_para) 
                    else:
                        p = poisson.pmf(mu = dist, k= 1)
                
                if (np.random.uniform(0,1) < p):
                    result.loc[index, timestamp_key]  = org_time + pow(-1, 1+ (dist_next < dist_previous) )*pd.to_timedelta(1, unit=tunit)
                    result.loc[index, 'label'] = str("inadvertent time(") + str(tunit)+ ":"+str(org_time) + ")"
            
            elif tunit == "month":   
                
                if org_time.month in [1,3,5,7,8,10,12]:
                    dist = min(org_time.day - 1, 31- org_time.day)
                elif org_time.month in [4,6,9,11]:
                    dist = min(org_time.day - 1, 30- org_time.day)
                else:
                    dist = min(org_time.day - 1, 28- org_time.day)
                                 
                if prob_func == "poisson":
                    p = 1- expon.cdf(dist, prob_para) 
                else:
                    p = poisson.pmf(mu = dist, k= 1)
                
                if (np.random.uniform(0,1) < p):
                    if dist == org_time.day - 1:
                        result.loc[index, timestamp_key]  = org_time -  pd.DateOffset(months=1) 
                    else:
                        result.loc[index, timestamp_key]  = org_time +  pd.DateOffset(months=1) 
                    result.loc[index, 'label'] = str("inadvertent time(") + str(tunit)+ ":"+str(org_time) + ")"  

            elif tunit == "year":
                if org_time.month ==1:
                    dist = org_time.day - 1
                    
                    if prob_func == "poisson":
                        p = 1- expon.cdf(dist, prob_para) 
                    else:
                        p = poisson.pmf(mu = dist, k= 1)
                    
                    
                    if (np.random.uniform(0,1) < p):
                        result.loc[index, timestamp_key]  = org_time -  pd.DateOffset(years=1)
                        result.loc[index, 'label'] = str("inadvertent time(") + str(tunit)+ ":"+ str(org_time) + ")"    
                        
                elif org_time.month ==12:
                    dist = 31 - org_time.day
                    
                    if prob_func == "poisson":
                        p = 1- expon.cdf(dist, prob_para) 
                    else:
                        p = poisson.pmf(mu = dist, k= 1)
                    
                    
                    if (np.random.uniform(0,1) < p):
                        result.loc[index, timestamp_key]  = org_time +  pd.DateOffset(years=1)
                        result.loc[index, 'label'] = str("inadvertent time(") + str(tunit)+ ":"+ str(org_time) + ")"  
                        

            else:
                raise ValueError("Error: tunit is only available in (date, month, year)")
    
    return result


