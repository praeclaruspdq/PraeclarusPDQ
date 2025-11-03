"""
AIR-BAGEL: An interactive root cause-based anomaly generator for event logs
"""
__author__ = "Jonghyeon Ko"
__version__ = "0.1"
__date__ = "03/10/2025"


import os

org_path = os.path.abspath(__file__)
import sys


print("python_path:", org_path, file=sys.stderr)


root = sys.argv[1]
print(f"Generated Attribute: {root}", file=sys.stderr) 


import warnings
warnings.filterwarnings(action='ignore')
import pandas as pd
from datetime import datetime
from utils.filtering import generate_system, generate_resource


seed =1234

try:
    event_log = pd.read_csv(sys.stdin)
except Exception as e:
    print(f"Error reading from stdin: {e}", file=sys.stderr)
    sys.exit(1)



# extracted_data= event_log.rename(columns={
#                                     'case:concept:name': 'Case',
#                                     'concept:name': 'Activity'
#                                 }
#                             )


extracted_data= event_log.rename(columns={
                                    'Case ID': 'Case',
                                    'Complete Timestamp': 'Timestamp'
                                }
                            )


time = extracted_data['Timestamp'].apply(lambda x: datetime.fromisoformat(x))

if "Event" not in extracted_data.columns:
    extracted_data["Event"] = list(range(0,len(event_log.index)))
    
extracted_data = extracted_data.sort_values(["Case", "Timestamp", "Activity"],ascending=[True, True, True]) # Reorder rows
extracted_data.Case = extracted_data.Case.astype(str) 

EL = extracted_data.copy()
EL = EL.dropna(subset=['Case'])
del extracted_data['Event']
del EL['Event']

if root == 'System':
    EL = EL.drop(columns=['System'], errors='ignore')
    EL2 = generate_system(EL, nsys=int(sys.argv[2]))
else:
    EL = EL.drop(columns=['Resource'], errors='ignore')
    EL2 = generate_resource(EL, ngroup = int(sys.argv[2]), groupsize= int(sys.argv[3]))



try:
    EL2.to_csv(sys.stdout, sep=',', encoding='utf-8', index=False)
except Exception as e:

    print(f"Python script error: {e}", file=sys.stderr)
    import traceback
    traceback.print_exc(file=sys.stderr) 
    sys.exit(1) 