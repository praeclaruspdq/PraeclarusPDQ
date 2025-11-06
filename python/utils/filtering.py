"""
FLAWD: a formal language for describing event log data quality issues
"""
__author__ = "Jonghyeon Ko"
__version__ = "0.1"
__date__ = "03/10/2025"


import numpy as np
import pandas as pd
import pm4py 
import re 
from Declare4Py.D4PyEventLog import D4PyEventLog
from Declare4Py.Utils.Declare.TraceStates import TraceState
from Declare4Py.Utils.Declare.Checkers import ConstraintChecker
from Declare4Py.ProcessModels.DeclareModel import DeclareModelTemplate
from Declare4Py.ProcessModels.DeclareModel import DeclareModel
import collections


def tranformer_declare(line):
    split = line.split("[", 1)
    template_search = re.search(r'(^.+?)(\d*$)', split[0])
    if template_search is not None:
        template_str, cardinality = template_search.groups()
        template = DeclareModelTemplate.get_template_from_string(template_str)
        if template is not None:
            activities = split[1].split("]")[0]
            activities = activities.split(", ")
            tmp = {"template": template, "activities": activities,
                    "condition": re.split(r'\s+\|', line)[1:]}
            if template.supports_cardinality:
                tmp['n'] = 1 if not cardinality else int(cardinality)
                cardinality = tmp['n']
        
    return tmp


def filter_declare(data, line: str, case_id_key: str):
    
    event_log = D4PyEventLog()
    data_copy = data.rename(columns={case_id_key: "case:concept:name"})
    event_log.log = pm4py.convert_to_event_log(data_copy)

    event_log.log_length = len(event_log.log)
    event_log.timestamp_key = "Timestamp"
    event_log.activity_key = "Activity"

    contraint = tranformer_declare(line)

    tmp_model = DeclareModel()
    tmp_model.constraints.append(contraint)
    tmp_model.set_constraints()
    sat_ctr = 0

    index = []
    list_trace = []
    for i, trace in enumerate(event_log.get_log()):
        trc_res = ConstraintChecker().check_trace_conformance(trace, tmp_model,  False, event_log.activity_key)
        if not trc_res:  # Occurring when constraint data conditions are formatted bad
            break
        # constraint_str, checker_res = next(iter(trc_res.items()))  # trc_res will always have only one element inside
        checker_res = trc_res[0]
        if checker_res.state == TraceState.SATISFIED:
            index.append(i)
            list_trace.append(trace.attributes['concept:name'])
            
    return list_trace


def filter_time(x, filter_time_start, filter_time_end, timestamp_key):
    if filter_time_start == None:
        filter_time_start = min(x[timestamp_key])
    if filter_time_end == None:
        filter_time_end = max(x[timestamp_key])
    
    if sum((filter_time_start <= x[timestamp_key]) & (x[timestamp_key] <= filter_time_end )) == len(x):
        return x
    else:
        pass



def generate_resource(data, ngroup, groupsize):

    params = {
        'Case': 'count'
    }

    activitylist = data.groupby('Activity').agg(params).reset_index()
    activitylist.columns = ['Activity', 'count']
    activitylist = activitylist.sort_values(by = 'count', ascending= False)

    rl=list(np.repeat("act",len(activitylist)))
    k = 0
    for i in activitylist['Activity']:
        k += 1
        rl[k-1] = list(["Department" + str(np.random.randint(0, int(ngroup) ))])
    
    activitylist['Department'] = pd.DataFrame(rl)
    activitylist = activitylist[['Activity' , 'Department']]

    data2 = pd.merge(data, activitylist, on="Activity")

    d = {
        'Department': ["Department" + str(i) for i in range(0, ngroup)],
        'Department_Size': [groupsize] * ngroup 
    }    
    
    group_size = pd.DataFrame(data=d)
    data2 = pd.merge(data2, group_size , on="Department")

    # generatio resource
    attach = ["Res" + str(np.random.randint(0,int(i)+1)) for i in data2["Department_Size"]]
    data2["attach"] = attach
    data2["Resource"] = data2[['attach', 'Department']].apply(lambda x: "_in_".join(x) ,axis=1)
    del data2['attach']
    del data2['Department_Size']
    data2 =  data2.sort_values(by = ['Case']).reset_index(drop=True)
    
    return data2

def generate_system(data, nsys, link = True):


    params = {
        'Case': 'count'
    }
    activitylist = data.groupby('Activity').agg(params).reset_index()
    activitylist.columns = ['Activity', 'count']
    activitylist = activitylist.sort_values(by = 'count', ascending= False)
    
    if link:
        k = 0
        sl=list(np.repeat("act",len(activitylist)))
        for i in activitylist['Activity']:
            k += 1
            sl[k-1] = list(["System" + str(np.random.randint(0, int(nsys) ))])
            
        activitylist['System'] = pd.DataFrame(sl)
        activitylist = activitylist[['Activity' , 'System']]
        
        
        data2 = pd.merge(data, activitylist, on="Activity")
    else:
        data2['System'] = ["System" + str(np.random.randint(0, int(nsys) )) for i in range(len(data))]
            
    return data2


# sub-seq miner (used for Form-based / Collateral Events / Scattered Case)

def miner_subseq(extracted_data, subseq_size):
    cases = extracted_data.Case.unique()
    sub_patterns = []
    c_index = []
    for c in cases:
        dat = extracted_data.loc[extracted_data.Case == c]
        c_seq = dat['Activity'].tolist()
        c_index = c_index + dat.index.tolist()[0:(len(c_seq) - subseq_size) ]
        for l in range(0, len(c_seq) - subseq_size):
            sub_patterns.append(c_seq[l:(l+subseq_size)])

    counter = collections.Counter([tuple(sub_pattern) for sub_pattern in sub_patterns])
    table_subseq = pd.DataFrame(data=counter.items())
    table_subseq.columns = ['sub_seq', 'count']
    table_subseq = table_subseq.sort_values(by = 'count', ascending= False).reset_index(drop=True)

    return table_subseq
