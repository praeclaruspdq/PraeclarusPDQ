"""
FLAWD: a formal language for describing event log data quality issues
"""
__author__ = "Jonghyeon Ko"
__version__ = "0.1"
__date__ = "03/10/2025"


import os
import sys

org_path = os.path.abspath(__file__)
parent_path = os.path.dirname(org_path)

print("python_path:", org_path, file=sys.stderr)


pattern = sys.argv[1]
print(f"Imperfection Pattern: {pattern}", file=sys.stderr) 


import warnings
warnings.filterwarnings(action='ignore')
import pandas as pd
from datetime import datetime

org_path = os.getcwd()
input_path = os.sep.join([str(org_path), "input"])

from utils.filtering import generate_system
from patterns.FormBased import FormBased
from patterns.CollateralEvent import CollateralEvent
from patterns.ScatteredCase import ScatteredCase
from patterns.PollutedLabel import PollutedLabel
from patterns.ScatteredEvent import ScatteredEvent
from patterns.SynonymousLabel import SynonymousLabel
from patterns.HomonymousLabel import HomonymousLabel
from patterns.ElusiveCase import ElusiveCase
from patterns.UnanchoredEvent import UnanchoredEvent
from patterns.DistortedLabel import DistortedLabel
from patterns.InadvertentTimeTravel import InadvertentTimeTravel


seed =1234

try:
    event_log = pd.read_csv(sys.stdin)
except Exception as e:
    print(f"Error reading from stdin: {e}", file=sys.stderr)
    sys.exit(1)


# extracted_data= event_log.rename(columns={
#                                     'case:concept:name': 'Case',
#                                     'concept:name': 'Activity',
#                                     'time:timestamp': 'Timestamp'
#                                 }
#                             )

extracted_data= event_log.rename(columns={
                                    'Case ID': 'Case',
                                    'Complete Timestamp': 'Timestamp'
                                }
                            )

if extracted_data['Timestamp'].dtype == 'object':
    extracted_data['Timestamp'] = pd.to_datetime(extracted_data['Timestamp'])
    
if "Event" not in extracted_data.columns:
    extracted_data["Event"] = list(range(0,len(event_log.index)))
    
extracted_data = extracted_data.sort_values(["Case", "Timestamp", "Activity"],ascending=[True, True, True]) # Reorder rows
extracted_data.Case = extracted_data.Case.astype(str) 

EL = extracted_data.copy()
EL = EL.dropna(subset=['Case'])
del extracted_data['Event']
del EL['Event']

if pattern == 'Form-based Event Capture':
    print("Injecting Form-based Event Capture pattern...", file=sys.stderr)

    if sys.argv[3]=='':
        para_ts = None
    else:
        para_ts = sys.argv[3]
    if sys.argv[4]=='':
        para_te = None
    else:
        para_te = sys.argv[4]
    if sys.argv[5]=='':
        para_r = None
    else:
        para_r = float(sys.argv[5])
    if sys.argv[6]=='':
        para_dec = None
    else:
        para_dec = sys.argv[6]

    EL_polluted = FormBased(EL, 
                        which = eval(sys.argv[2]), 
                        ratio= para_r , 
                        tstart = para_ts,
                        tend = para_te,
                        DecConstraint = para_dec, 
                        case_id_key = "Case",
                        timestamp_key = "Timestamp")
    

elif pattern == 'Collateral Events':
    print("Injecting Collateral Events pattern...", file=sys.stderr)

    if sys.argv[3]=='':
        para_tb = 1
    else:
        para_tb = float(sys.argv[3])
    if sys.argv[4]=='':
        para_ts = None
    else:
        para_ts = sys.argv[4]
    if sys.argv[5]=='':
        para_te = None
    else:
        para_te = sys.argv[5]
    if sys.argv[6]=='':
        para_r = None
    else:
        para_r = float(sys.argv[6])
    if sys.argv[7]=='':
        para_dec = None
    else:
        para_dec = sys.argv[7]
        
    EL_polluted = CollateralEvent(EL, 
                        target_collats = sys.argv[2],
                        ratio= para_r , 
                        timep = para_tb,
                        unit = 'sec',
                        tstart = para_ts,
                        tend = para_te,
                        DecConstraint = para_dec, 
                        case_id_key = "Case",
                        timestamp_key = "Timestamp",
                        activity_key = "Activity")
    
elif pattern == 'Scattered Case':
    print("Injecting Scattered Case pattern...", file=sys.stderr)
        
    if sys.argv[3]=='':
        para_ts = None
    else:
        para_ts = sys.argv[3]
    if sys.argv[4]=='':
        para_te = None
    else:
        para_te = sys.argv[4]
        
    EL_polluted, EL_polluted2 = ScatteredCase(EL,
                                syslist = sys.argv[2],
                                tstart = para_ts,
                                tend = para_te,
                                case_id_key = "Case",
                                timestamp_key = "Timestamp")
    
    # EL_polluted2.to_csv(temp_path + "\\" + "temp_scatteredcase.csv", sep=',', encoding='utf-8')

elif pattern == 'Scattered Event':
    print("Injecting Scattered Event pattern...", file=sys.stderr)

    if sys.argv[5]=='':
        para_del = False
    else:
        para_del = eval(sys.argv[5])
    if sys.argv[6]=='':
        para_ts = None
    else:
        para_ts = sys.argv[6]
    if sys.argv[7]=='':
        para_te = None
    else:
        para_te = sys.argv[7]
    if sys.argv[8]=='':
        para_r = None
    else:
        para_r = float(sys.argv[8])
    if sys.argv[9]=='':
        para_dec = None
    else:
        para_dec = sys.argv[9]
        
    EL_polluted = ScatteredEvent(EL, 
                                target = sys.argv[2],
                                action = sys.argv[3],
                                loc = sys.argv[4],
                                Del = para_del, 
                                ratio = para_r, 
                                tstart = para_ts,
                                tend = para_te,
                                DecConstraint = para_dec, 
                                case_id_key = "Case",
                                activity_key = "Activity",
                                timestamp_key = "Timestamp")

elif pattern == 'Polluted Label':
    print("Injecting Polluted Label pattern...", file=sys.stderr)
    
    if sys.argv[4]=='':
        para_ts = None
    else:
        para_ts = sys.argv[4]
    if sys.argv[5]=='':
        para_te = None
    else:
        para_te = sys.argv[5]
    if sys.argv[6]=='':
        para_r = None
    else:
        para_r = float(sys.argv[6])
    if sys.argv[7]=='':
        para_dec = None
    else:
        para_dec = sys.argv[7]
        
    EL_polluted = PollutedLabel(EL, 
                                target = sys.argv[2],
                                action = sys.argv[3],
                                ratio = para_r,
                                tstart = para_ts,
                                tend = para_te,
                                DecConstraint = para_dec, 
                                case_id_key = "Case",
                                timestamp_key = "Timestamp")

elif pattern == 'Synonymous Labels':
    print("Injecting Synonymous Labels pattern...", file=sys.stderr)
    
    if sys.argv[5]=='':
        para_ts = None
    else:
        para_ts = sys.argv[5]
    if sys.argv[6]=='':
        para_te = None
    else:
        para_te = sys.argv[6]
    if sys.argv[7]=='':
        para_r = None
    else:
        para_r = float(sys.argv[7])
    if sys.argv[8]=='':
        para_dec = None
    else:
        para_dec = sys.argv[8]
        
    EL_polluted = SynonymousLabel(EL, 
                                target = sys.argv[2],
                                syns = eval(sys.argv[3]),
                                prob = eval(sys.argv[4]),
                                ratio = para_r,
                                tstart = para_ts,
                                tend = para_te,
                                DecConstraint = para_dec, 
                                case_id_key = "Case",
                                timestamp_key = "Timestamp")

elif pattern == 'Homonymous Label':
    print("Injecting Homonymous Label pattern...", file=sys.stderr)

    if sys.argv[4]=='':
        para_ts = None
    else:
        para_ts = sys.argv[4]
    if sys.argv[5]=='':
        para_te = None
    else:
        para_te = sys.argv[5]
    if sys.argv[6]=='':
        para_r = None
    else:
        para_r = float(sys.argv[6])
    if sys.argv[7]=='':
        para_dec = None
    else:
        para_dec = sys.argv[7]
        
    EL_polluted = HomonymousLabel(EL,
                                target = sys.argv[2],
                                hlabel = sys.argv[3],
                                ratio = para_r,
                                tstart = para_ts,
                                tend = para_te,
                                DecConstraint = para_dec, 
                                case_id_key = "Case",
                                timestamp_key = "Timestamp")

elif pattern == 'Elusive Case':
    print("Injecting Elusive Case pattern...", file=sys.stderr)
    
    if sys.argv[4]=='':
        para_ts = None
    else:
        para_ts = sys.argv[4]
    if sys.argv[5]=='':
        para_te = None
    else:
        para_te = sys.argv[5]
    if sys.argv[6]=='':
        para_r = None
    else:
        para_r = float(sys.argv[6])
    if sys.argv[7]=='':
        para_dec = None
    else:
        para_dec = sys.argv[7]
        
    EL_polluted = ElusiveCase(EL,
                            method = sys.argv[2],
                            gnum = int(sys.argv[3]),
                            ratio = para_r,
                            tstart = para_ts,
                            tend = para_te,
                            DecConstraint = para_dec, 
                            case_id_key = "Case",
                            activity_key = "Activity",
                            timestamp_key = "Timestamp")

elif pattern == 'Unanchored Event':
    print("Injecting Unanchored Event pattern...", file=sys.stderr)
    
    if sys.argv[3]=='':
        para_ts = None
    else:
        para_ts = sys.argv[3]
    if sys.argv[4]=='':
        para_te = None
    else:
        para_te = sys.argv[4]
    if sys.argv[5]=='':
        para_tf = None
    else:
        para_tf = sys.argv[5]  
    if sys.argv[6]=='':
        para_r = None
    else:
        para_r = float(sys.argv[6])
    if sys.argv[7]=='':
        para_dec = None
    else:
        para_dec = sys.argv[7]
        
        
    EL_polluted = UnanchoredEvent(EL, 
                                syslist = sys.argv[2],
                                TimeFormat = para_tf, 
                                tstart = para_ts,
                                tend = para_te,
                                DecConstraint = para_dec,
                                ratio= para_r,
                                case_id_key = "Case",
                                timestamp_key = "Timestamp")


elif pattern == 'Distorted Label':
    print("Injecting Distorted Label pattern...", file=sys.stderr)

    if sys.argv[2]=='':
        para_who = None
    else:
        para_who = sys.argv[2] 
    if sys.argv[4]=='':
        para_ts = None
    else:
        para_ts = sys.argv[4]
    if sys.argv[5]=='':
        para_te = None
    else:
        para_te = sys.argv[5]
    if sys.argv[6]=='':
        para_r = None
    else:
        para_r = float(sys.argv[6])
    if sys.argv[7]=='':
        para_dec = None
    else:
        para_dec = sys.argv[7]
        
    EL_polluted = DistortedLabel(EL, 
                            who = para_who,
                            distortion =  sys.argv[3],
                            ratio = para_r,
                            tstart = para_ts,
                            tend = para_te,
                            DecConstraint = para_dec, 
                            case_id_key = "Case",
                            timestamp_key = "Timestamp")


elif pattern == 'Inadvertent Time Travel':
    print("Injecting Inadvertent Time Travel pattern...", file=sys.stderr)
    

    if sys.argv[5]=='':
        para_ts = None
    else:
        para_ts = sys.argv[5]
    if sys.argv[6]=='':
        para_te = None
    else:
        para_te = sys.argv[6]
    if sys.argv[7]=='':
        para_r = None
    else:
        para_r = float(sys.argv[7])
    if sys.argv[8]=='':
        para_dec = None
    else:
        para_dec = sys.argv[8]
        
    EL_polluted = InadvertentTimeTravel(EL, 
                          target = sys.argv[2],
                          tunit = sys.argv[3],    #day, month, year
                          prob_func = sys.argv[4],  # poisson, exponential
                          tstart = para_ts,
                          tend = para_te,
                          DecConstraint = para_dec, 
                          ratio = para_r,
                          case_id_key = "Case",
                          timestamp_key = "Timestamp")



if pattern == 'Scattered Case':
    EL_polluted = EL_polluted.rename(columns={
                            'Case': 'case:concept:name',
                            'Activity': 'concept:name'
                        }
                    )
    EL_polluted2 = EL_polluted2.rename(columns={
                            'Case': 'case:concept:name',
                            'Activity': 'concept:name'
                        }
                    )

    EL_polluted.to_csv(sys.stdout, sep=',', encoding='utf-8', index=False, line_terminator='\n')

    print("\n---AUXILIARY_DATA---", file=sys.stdout, flush=True, end='\n')

    EL_polluted2.to_csv(sys.stdout, sep=',', encoding='utf-8', index=False, line_terminator='\n')

    # EL_polluted.to_csv(sys.stdout, sep=',', encoding='utf-8', index=False)
    # print("\n---AUXILIARY_DATA---\n", file=sys.stdout, flush=True)
    # EL_polluted2.to_csv(sys.stdout, sep=',', encoding='utf-8', index=False)

else:
    try:
        EL_polluted= EL_polluted.rename(columns={
                                'Case': 'case:concept:name',
                                'Activity': 'concept:name'
                            }
                        )
        EL_polluted.to_csv(sys.stdout, sep=',', encoding='utf-8', index=False)
    except Exception as e:

        print(f"Python script error: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc(file=sys.stderr) 
        sys.exit(1) 