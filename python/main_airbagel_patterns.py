"""
AIR-BAGEL: An interactive root cause-based anomaly generator for event logs
"""
__author__ = "Jonghyeon Ko"
__version__ = "0.1"
__date__ = "03/10/2025"

import os
import sys
org_path = os.path.abspath(__file__)

print("python_path:", org_path, file=sys.stderr)

import warnings
warnings.filterwarnings(action='ignore')
import pandas as pd
import numpy as np
from datetime import datetime

org_path = os.getcwd()
input_path = os.sep.join([str(org_path), "input"])

from utils.filtering import generate_system, generate_resource
from utils.abnormal_patterns import Abnorm_p


seed =1234

try:
    event_log = pd.read_csv(sys.stdin)
except Exception as e:
    print(f"Error reading from stdin: {e}", file=sys.stderr)
    sys.exit(1)


# extracted_data= event_log.rename(columns={
#                                     'Case ID': 'Case',
#                                     'Activity': 'Activity',
#                                     'Start Timestamp': 'Timestamp'
#                                 }
#                             )

extracted_data= event_log.rename(columns={
                                    'Case ID': 'Case',
                                    'Complete Timestamp': 'Timestamp'
                                }
                            )

extracted_data['Timestamp'] = extracted_data['Timestamp'].apply(lambda x: datetime.fromisoformat(x))

if "Event" not in extracted_data.columns:
    extracted_data["Event"] = list(range(0,len(event_log.index)))
    
extracted_data = extracted_data.sort_values(["Case", "Timestamp", "Activity"],ascending=[True, True, True]) # Reorder rows
extracted_data.Case = extracted_data.Case.astype(str) 

params = {
    'Case': 'count',
    'Activity': lambda x: ','.join(sorted(pd.Series.unique(x)))
}

resourcelist2 = extracted_data.groupby('Resource').agg(params).reset_index()
resourcelist2.columns = ["Resource", "Frequency", "Activities"]
resourcelist2 = resourcelist2.sort_values(["Frequency"], ascending=False)
cols = ["Resource", "Frequency",  "Activities"]
resourcelist2 = resourcelist2[cols]
resourcelist = resourcelist2

# set probability of resource failure
resourcelist2["Resource_failure_rate"] = float(sys.argv[10])  # parameter
resourcelist3 = resourcelist2[["Resource", "Resource_failure_rate"]]

EL = pd.merge(extracted_data, resourcelist3, on="Resource")

# simulate resource failure
np.random.seed(seed)
PF = np.random.binomial(np.repeat(1, len(EL)), EL["Resource_failure_rate"])
EL['Resource_Anomaly/Normal'] = PF


if sys.argv[6]=='':
    p1insert = 1
else:
    p1insert = float(sys.argv[6])
if sys.argv[7]=='':
    p1rework = 1
else:
    p1rework = float(sys.argv[7])
if sys.argv[8]=='':
    p1move = 0
else:
    p1move = float(sys.argv[8])
if sys.argv[9]=='Year':
    p2move = 31556952
elif sys.argv[9]=='Month':
    p2move = 2629746
elif sys.argv[9]=='Day':
    p2move = 86400
elif sys.argv[9]=='Hour':
    p2move = 3600
elif sys.argv[9]=='Minute':
    p2move = 60
else:
    p2move = 1
    
EL2 = Abnorm_p(EL).implement_resource(
    types=["skip",  "insert", "rework", "moved", "replace"],
    mag=[float(sys.argv[1]),
         float(sys.argv[2]),
         float(sys.argv[3]),
         float(sys.argv[4]),
         float(sys.argv[5])],
    m_insert=p1insert,
    m_rework=p1rework,
    s_moved= p1move*p2move)  


try:
    EL2= EL2.rename(columns={
                            'Case': 'case:concept:name',
                            'Activity': 'concept:name'
                        }
                    )
    EL2.to_csv(sys.stdout, sep=',', encoding='utf-8', index=False)
except Exception as e:

    print(f"Python script error: {e}", file=sys.stderr)
    import traceback
    traceback.print_exc(file=sys.stderr) 
    sys.exit(1) 