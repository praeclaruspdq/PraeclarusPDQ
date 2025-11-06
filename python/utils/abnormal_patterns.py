"""
AIR-BAGEL: An interactive root cause-based anomaly generator for event logs
"""
__author__ = "Jonghyeon Ko"
__version__ = "0.1"
__date__ = "03/10/2025"


import warnings
warnings.filterwarnings(action='ignore')

#For Data preprocess
import numpy as np
import pandas as pd
import random
import datetime
from datetime import timedelta
import os
import sys 


#Pages
org_path = os.getcwd()
temp_path =  os.sep.join([str(org_path), "temp"])

class Abnorm_p():
    def __init__(self, input_file):

        self.input_file = input_file
        
    # Preprocess: to make new attribute "type_res_event" for parameter value in dataframe
    def setting1(self, df, types, mag):   # types= anomaly patterns, mag= weights
        if sum(mag) != 1:
            total = sum(mag)
            mag = [i/total for i in mag]
            
        df = df.sort_values(by=["Case", "Timestamp"], ascending=[True, True])
        df["type_res_event"] = np.nan
        
        num_fail = df["Resource_Anomaly/Normal"].sum()
        applied_patterns = np.random.choice(types, num_fail, p = mag)
        fail_df = df[df["Resource_Anomaly/Normal"] == 1]
        clean_df = df[df["Resource_Anomaly/Normal"] == 0]
        applied_patterns[applied_patterns == "switch"] = "switch_from" 
        fail_df["type_res_event"] = applied_patterns
        df = pd.concat([clean_df, fail_df], ignore_index=True)
        df = df.sort_values(by=["Case", "Timestamp"], ascending=[True, True])
        return df

    # Preprocess: To add 'parameter' attribute in df
    def setting2(self, df, m_skip, m_form, s_moved, m_switch, m_insert, m_rework, m_replace):
        df = df.sort_values(by=["Case", "Timestamp"], ascending=[True, True])
        df_new = pd.DataFrame.copy(df)
        df["check"] = 1
        df_new["order"] = df.groupby(["Case"])["check"].cumsum()            #length = event position in a case
        # to prevent imcomplete pattern starting at first event of a case.
        fix1_i = list(df_new.index[(df_new["type_res_event"] == 'incomplete') & (df_new["order"] == 1) 
                                   & (df["Resource_Anomaly/Normal"] == 1)])
        fix1_c = list(df_new.loc[fix1_i, "Case"])
        fix2_i = list(df_new.index[ df_new["Case"].isin(fix1_c) ])
        df_new.loc[fix1_i, "Resource_Anomaly/Normal"] = 0
        df_new.loc[fix2_i, "type_res_event"] = 'normal'
        df_new["max"] = df_new.groupby(["Case"])["order"].transform("max")  #max = trace length
        df_new["parameter_l"] = np.nan # just for a mean of preprocessing
        df_new["parameter"] = np.nan # applied size of each anomaly pattern
        df_c = df_new.shift(-1, axis=0)
        df_new["duration"] = df_c["Timestamp"] - df_new["Timestamp"]  #duration = time interval between events
        df_new.loc[(df_new["order"] == df_new["max"]), "duration"] = datetime.timedelta()
        df_new["duration"] = df_new.apply(lambda x: x["duration"].seconds, axis=1)
        
        # df_ct = df_new.loc[(df_new["Resource_Anomaly/Normal"] == 1) & 
        #                    (df_new["cusum"] == 1) & (df_new["type_res_event"] is not np.nan)]
        
        df_ct = df_new.loc[(df_new["Resource_Anomaly/Normal"] == 1) &
                            (df_new["type_res_event"] is not np.nan)]
        df_ct["type_res_case"] = df_ct["type_res_event"]
        df_ct = df_ct.groupby("Case").apply(lambda x: list(x["type_res_case"])).to_frame(name = "type_res_case").reset_index()
        df_ct["type_res_len"] = df_ct["type_res_case"].apply(len)
        df_new = pd.merge(df_new, df_ct, on="Case", how="outer").copy()
        df_new.loc[df_new["type_res_case"].isna(), "type_res_case"] = "normal"
        df_new = df_new.sort_values(by=["Case", "Timestamp"], ascending=[True, True])
        
        #"type_res_trace": For PBAR (to notify whether each pattern changed trace)
        df_new["type_res_trace"] = df_new["type_res_event"].copy()
        
        
        if "incomplete" in df_new["type_res_event"].unique():
            df_new.loc[ (df_new["type_res_event"] == "incomplete"), "parameter"] = df_new["max"] - df_new["order"] + 1
            m_case = df_new[df_new["type_res_case"].apply(lambda x: "incomplete" in x) ]  # case with incomplete
            m_case1 = m_case.loc[m_case["type_res_len"] > 1]
            m_case2 = m_case.loc[m_case["type_res_len"] == 1]
            o_case = df_new[df_new["type_res_case"].apply(lambda x: "incomplete" not in x) ]  # o.c.
            m_case1["loc"] = (m_case1["type_res_event"] == "incomplete") 
            m_case1["loc"] = m_case1.groupby(["Case"])["loc"].cumsum() 
            m_case1["loc"] = m_case1.groupby(["Case"])["loc"].cumsum() # to make only the first "imcomplete" to have "1"
            # do not make any posterior pattern after "imcomplete" happened
            m_case1.loc[ (m_case1["loc"] > 1) , "type_res_event"] = np.nan
            m_case1 = m_case1.drop(columns=["loc", "type_res_case", "type_res_len"])
            
            # re-assign "type_res_case", "type_res_len"
            df_ct = m_case1.loc[(m_case1["Resource_Anomaly/Normal"] == 1) &
                            (m_case1["type_res_event"] is not np.nan)]
            df_ct["type_res_case"] = df_ct["type_res_event"]
            df_ct = df_ct.groupby("Case").apply(lambda x: [i for i in x["type_res_case"] if i is not np.nan] ).to_frame(name = "type_res_case").reset_index()
            df_ct["type_res_len"] = df_ct["type_res_case"].apply(len)
            m_case1 = pd.merge(m_case1, df_ct, on="Case", how="outer")
            df_new = pd.concat([m_case1, m_case2, o_case])
            df_new = df_new.sort_values(by=["Case", "Timestamp"], ascending=[True, True])
            df_new.reset_index(drop=True, inplace=True)


        if "skip" in df_new["type_res_event"].unique():
            # df_new.loc[(df_new["Resource_Anomaly/Normal"] == 1) & (df_new["cusum"] == 1) & (df_new["type_res_event"] == "skip"), "parameter_l"] = 1
            # df_new.loc[(df_new["Resource_Anomaly/Normal"] == 1) & (df_new["cusum"] == 1) & (df_new["type_res_event"] == "skip"), "parameter_r"] = 1
            df_new.loc[ (df_new["type_res_event"] == "skip"), "parameter_l"] = 1
            df_new.loc[ (df_new["type_res_event"] == "skip"), "parameter"] = 1

        if "form based" in df_new["type_res_event"].unique():
            if m_form < 2:
                m_form = 2
            df_new.loc[(df_new["type_res_event"] == "form based"), "parameter_l"] = df_new["max"] - df_new["order"] + 1
            df_new.loc[(df_new["type_res_event"] == "form based"), "parameter"] = df_new["parameter_l"].apply(lambda x: max(x, random.randint(2, m_form)))

        if "rework" in df_new["type_res_event"].unique():
            df_new.loc[(df_new["type_res_event"] == "rework"), "parameter_l"] = 1
            df_new.loc[(df_new["type_res_event"] == "rework"), "parameter"] = df_new["parameter"].apply(lambda x:random.randint(1, m_rework))

        if "replace" in df_new["type_res_event"].unique():
            df_new.loc[ (df_new["type_res_event"] == "replace"), "parameter_l"] = 1
            df_new.loc[ (df_new["type_res_event"] == "replace"), "parameter"] = df_new["parameter"].apply(lambda x:random.randint(1, m_replace))

        # if df_new["parameter_r"].all() != np.nan:
        #     df_new.loc[df_new["parameter_l"] >= df_new["parameter_r"], "parameter"] = df_new["parameter_r"]
        #     df_new.loc[df_new["parameter_l"] < df_new["parameter_r"], "parameter"] = df_new["parameter_l"]
        # else:
        #     pass
        
        if "moved" in df_new["type_res_event"].unique():
            df_new.loc[(df_new["type_res_event"] == "moved"),"parameter"] = df_new[(df_new["type_res_event"] == "moved")].apply(lambda x: random.randint(-s_moved, s_moved) if x["type_res_event"] =="moved" else np.nan, axis=1  )
            
            # For PBAR
            df_new2 = pd.DataFrame()
            df_new2 = df_new.copy()
            df_c = pd.DataFrame()
            df_c = df_new2.shift(1, axis=0).copy()
            df_new2["duration_prior"] = df_c["duration"].copy()
            df_new2.loc[(df_new2["order"] == 1), "duration_prior"] = 0
            
            df_part1 = df_new2[(df_new2["type_res_event"] == "moved" )]
            df_part2 = df_new2[(df_new2["type_res_event"] != "moved" )]
            ind_part1 = df_part1.apply(lambda x: x["parameter"] > -1*x["duration_prior"] and x["parameter"] < x["duration"], axis =1)
            if sum(ind_part1) >0 :
                df_part1.loc[ind_part1 , "type_res_trace"] = "no_effect"
            df_new =  pd.concat([df_part1, df_part2], ignore_index=True)
            df_new.drop(columns= ["duration_prior"])

            def asd(x):
                if "no_effect" in x.tolist() :
                    return sum(x == "no_effect")
                else:
                    return 0
            noeffect_len = df_new.groupby(["Case"]).apply(lambda x: 
                asd(x["type_res_trace"])).to_frame(name = "noeffect_len").reset_index()
            df_new = pd.merge(df_new, noeffect_len, on="Case", how="outer")
            
            def remove_rep(x, y):
                xy = []
                k = y
                if x == "normal":
                    x = ["normal"]
                    
                for i in x:
                    if i == "moved" and k>0:
                        k = k-1
                    else:
                        xy.append(i)

                return xy
            df_new["type_res_trace"] = df_new.apply(lambda x: remove_rep(x["type_res_case"], x["noeffect_len"] ), axis=1) 
            df_new.reset_index(drop=True, inplace=True)
            df_new.loc[df_new["type_res_trace"].apply(lambda x: "normal" in x or len(x) ==0 ) , "type_res_trace"] = np.nan
            df_new = df_new.sort_values(by=["Case", "Timestamp"], ascending=[True, True])
         
            
        if "insert" in df_new["type_res_event"].unique():
            df_new.loc[(df_new["type_res_event"] == "insert"), "parameter"] = df_new["parameter"].apply(lambda x: random.randint(1, m_insert))
            
        if "switch_from" in list(df_new["type_res_event"]):
            df_new.loc[(df_new["type_res_event"] == "switch_from"), "parameter_l"] = 1
            df_new.loc[(df_new["type_res_event"] == "switch_from"), "parameter"] = df_new["parameter"].apply(lambda x:random.randint(1, m_switch))

            c_list = df_new[(df_new["type_res_event"] == "switch_from")]
            # c_list = c_list.groupby(["cusum"])["Case"].unique()
            c_list = c_list["Event"].unique()
            
            m_case = df_new[df_new["type_res_case"].apply(lambda x: "switch_from" in x)] # case with switch
            n_case = df_new[df_new["type_res_case"].apply(lambda x: "normal" in x)] # case with normal 
            a_case = df_new[df_new["type_res_case"].apply(lambda x: ("normal" not in x or "switch_from" not in x) ) ]    # case with other types
            n_case_list = n_case["Case"].unique()
            n_case_list = list(np.random.choice(n_case_list, len(c_list), replace=False))
            
            n_case_f = n_case.loc[n_case["Case"].isin(n_case_list), ["Case", "Event"]]
            n_case_f = n_case_f.groupby("Case").apply(lambda x: int(np.random.choice(x["Event"], 1)) )
            n_case.loc[n_case['Event'].isin(n_case_f.values.tolist()), "parameter"] = c_list
            df_new = pd.concat([m_case, n_case, a_case])
            df_new.reset_index(drop=True, inplace=True)
        
        # df_p = df_new.loc[(df_new["Resource_Anomaly/Normal"] == 1) & (df_new["type_res_event"] is not np.nan)]
        df_p = df_new.loc[(df_new["Resource_Anomaly/Normal"] == 1) ]
        # df_p.reset_index(drop=True, inplace=True)
        # df_p["resource_parameter"] = df_p.apply(lambda x: "loc = {0}, len = {1}".format(x["order"], x["parameter"]) 
        #                                         if x["type_res_case"] != "incomplete" 
        #                                         else "loc = {0}, len = NaN".format(x["order"]), axis=1)
        
        df_p["resource_parameter_event"] = df_p.apply(lambda x: dict(type= x["type_res_event"], 
                                                               attributes= dict({'loc': x["order"],
                                                                                  'len': x["parameter"] }) ), axis=1 )   
        
        df_p_event = df_p[["Event", "resource_parameter_event"]]
        df_p_case = df_p[["Case", "resource_parameter_event"]]
        df_p_case = df_p_case.groupby("Case").apply(lambda x: 
            list(x["resource_parameter_event"])).to_frame(name = "resource_parameter_case").reset_index()
        
        df_new = pd.merge(df_new, df_p_event, on="Event", how="outer")
        df_new = pd.merge(df_new, df_p_case, on="Case", how="outer")
        
        # df_new["Resource_Anomaly/Normal"] = df_new.apply(lambda x: 0 if (x["Resource_Anomaly/Normal"] == 1) else x["Resource_Anomaly/Normal"], axis=1)
        df_new.reset_index(drop=True, inplace=True)
        
        global data_with_parameter_res
        data_with_parameter_res = df_new
        # PageThree.data_with_parameter_res = data_with_parameter_res
        
        df_new = df_new.sort_values(by=["Case", "Timestamp"], ascending=[True, True])
        return df_new

    # Events are not recorded
    def skip(self, df):

        df_new = pd.DataFrame.copy(df)
        list_skip_i = list(df_new.index[(df_new["type_res_event"] == "skip")])
        list_skip_p = list(df_new.loc[list_skip_i, "parameter"])
        dic_skip = {"index": list_skip_i, "parameter": list_skip_p}
        list_skip = pd.DataFrame(dic_skip)
        list_skip = list_skip.apply(lambda row: np.arange(row["index"], int(row["index"] + row["parameter"]), 1), axis=1)
        list_skip = np.concatenate(list_skip)
        df_new = df.drop(list_skip)
        df_new = df_new.sort_values(by=["Case", "order"], ascending=[True, True])
        df_new.reset_index(drop=True, inplace=True)
        return df_new

    # Events have same timestamp
    def form_based(self, df):

        df_new = pd.DataFrame.copy(df)
        df_new["resource_check2"] = np.nan
        df_ct = df_new.loc[(df_new["type_res_event"] == "form based")]
        df_ct["resource_check1"] = df_ct["Resource"]
        df_ct = df_ct[["Case", "resource_check1"]]
        df_new = pd.merge(df_new, df_ct, on="Case", how="outer")
        df_new["resource_check2"] = df_new.apply(lambda x: 1 if x["Resource"] == x["resource_check1"] else 0, axis=1)
        
        list_form_i = list(df_new.index[(df_new["type_res_event"] == "form based") ])
        list_form_p = list(df_new.loc[list_form_i, "parameter"])
        list_form_ts = list(df_new.loc[list_form_i, "Timestamp"])
        dic_form = {"index": list_form_i, "parameter": list_form_p, "Timestamp": list_form_ts}
        list_form = pd.DataFrame(dic_form)
        list_form_1 = list_form.apply(lambda row: np.arange(row["index"], int(row["index"] + row["parameter"]), 1), axis=1)
        list_form_1 = np.concatenate(list_form_1)
        list_form_2 = list_form.apply(lambda row: np.repeat(a=row["Timestamp"], repeats=row["parameter"]), axis=1)
        list_form_2 = np.concatenate(list_form_2)
        df_new["Timestamp_temp"] = np.nan
        df_new["Timestamp_chg"] = np.nan
        df_new.loc[list_form_1, "Timestamp_temp"] = list_form_2
        df_new.loc[list_form_1, "Timestamp_chg"] = 1
        df_new["Timestamp"] = df_new.apply(lambda x: x["Timestamp_temp"] if (x["resource_check2"] == 1) & (x["Timestamp_chg"] == 1) else x["Timestamp"], axis=1)
        df_new.drop(columns=["resource_check1", "resource_check2", "Timestamp_temp", "Timestamp_chg"])
        df_new = df_new.sort_values(by=["Case", "order"], ascending=[True, True])
        df_new2 = df_new2.drop(["resource_check2", "resource_check1", "Timestamp_temp", 
                          "Timestamp_chg"])
        df_new.reset_index(drop=True, inplace=True)
        return df_new

    # Last events in case are not recorded
    def incomplete(self, df):
        df_new = pd.DataFrame.copy(df)
        list_incom_i = list(df_new.index[(df_new["type_res_event"] == "incomplete") & (df_new["order"] != 1)])
        list_incom_p = list(df_new.loc[list_incom_i, "parameter"])
        dic_incom = {"index": list_incom_i, "parameter": list_incom_p}
        list_incom = pd.DataFrame(dic_incom)
        list_incom = list_incom.apply(lambda row: np.arange(row["index"], int(row["index"] + row["parameter"]), 1), axis=1)
        list_incom = np.concatenate(list_incom)
        df_new = df.drop(list_incom)
        df_new = df_new.sort_values(by=["Case", "order"], ascending=[True, True])
        df_new.reset_index(inplace=True, drop=True)
        return df_new

    # Wrong timestamp are recorded
    def moved(self, df):
        df_new = pd.DataFrame.copy(df)
        df_new["resource_parameter_event"] = np.where(df_new["type_res_event"] == "moved", df_new.apply(lambda x: "eventID = {0}, duration = {1}".format(x["Event"], x["parameter"]), axis=1), df_new["resource_parameter_event"])
        # df_new["resource_parameter"] = np.where((df_new["type_res_event"] != "moved") & (df_new["type_res_case"] == "moved"), np.nan, df_new["resource_parameter"])
        df_new["resource_parameter_event"] = np.where((df_new["type_res_event"] != "moved") , np.nan, df_new["resource_parameter_event"])

        # moved_c = df_new[df_new["Resource_Anomaly/Normal"] == 0]
        # moved_f = df_new[(df_new["Resource_Anomaly/Normal"] == 1) & (df_new["type_res_event"] == "moved")]
        # moved_a = df_new[(df_new["Resource_Anomaly/Normal"] == 1) & (df_new["type_res_event"] != "moved")]
        moved_f = df_new[ (df_new["type_res_event"] == "moved")]
        moved_a = df_new[ (df_new["type_res_event"] != "moved")]

        moved_f.reset_index(drop=True, inplace=True)
        moved_a.reset_index(drop=True, inplace=True)
        unixtime = df["unixtime"]
        list_moved_i = list(df_new.index[(df_new["type_res_event"] == "moved")])
        list_moved_p = list(df_new.loc[list_moved_i, "parameter"])
        unixtime = list(unixtime.loc[list_moved_i])
        dic_moved = {"index": list_moved_i, "parameter": list_moved_p, "Timestamp": unixtime}
        list_moved = pd.DataFrame(dic_moved)
        list_moved_2 = list_moved.apply(lambda row: row["Timestamp"] + row["parameter"], axis=1)
        list_moved_2 = list_moved_2.apply(lambda row: datetime.datetime.utcfromtimestamp(row))
        moved_f["Timestamp"] = list_moved_2
        
        df_new = pd.concat([moved_f, moved_a])
        df_new = df_new.sort_values(by=["Case", "Timestamp"], ascending=[True, True])
        df_new.reset_index(drop=True, inplace=True)
        return df_new

    # Same events are recorded more than one time
    def rework(self, df):

        df_new = pd.DataFrame.copy(df)
        col = list(df_new.columns)
        list_rework_i = list(df_new.index[(df_new["type_res_event"] == "rework")])
        list_rework_c = df_new.loc[list_rework_i]
        list_rework = np.repeat(a=list_rework_c.values, repeats=list_rework_c["parameter"].astype("int"), axis=0)
        list_rework = pd.DataFrame(list_rework, columns=col)
        df_new = pd.concat([df_new, list_rework])
        df_new = df_new.sort_values(by=["Case", "order"], ascending=[True, True])
        df_new.reset_index(inplace=True, drop=True)
        return df_new

    # Unrelated event is recorded in a case
    def insert(self, df):

        df_new = pd.DataFrame.copy(df)
        col = list(df_new.columns)
        act_a = list(df["Activity"].unique())
        act_c = df_new.groupby(["Case"])["Activity"].unique()
        act_r = df_new.groupby(["Activity"])["Resource"].unique()
        # act_c = act_c.apply(lambda x: list(set(act_a) - set(x)) if len(list(set(act_a) - set(x))) > 3 else list(set(act_a + ["random_XYZ", "random_YXZ", "random_ZXY"]) - set(x)) )
        act_c = act_c.apply(lambda x: list(set(act_a) - set(x))  )
        
        list_insert_i = list(df_new.index[(df_new["type_res_event"] == "insert")])
        list_insert_c = df_new.loc[list_insert_i]
        list_insert = np.repeat(a=list_insert_c.values, repeats=list_insert_c["parameter"].astype("int"), axis=0)
        list_insert = pd.DataFrame(list_insert, columns=col)
        list_insert["duration"] = list_insert.apply(lambda x: random.randrange(0, x["duration"] + 1), axis=1)
        list_insert["Activity"] = list_insert.apply(lambda x: 
            random.choice(act_c[x["Case"]] if len(act_c[x["Case"]]) > 3 
                          else act_c[x["Case"]] + ["random_XYZ", "random_YXZ", "random_ZXY"]) , axis=1)
        list_insert["Resource"] = list_insert.apply(lambda x: 
            random.choice(act_r[x["Activity"]] if x["Activity"] not in ["random_XYZ", "random_YXZ", "random_ZXY"]
                          else ["Resource_XYX"]), axis=1)
        list_insert["Timestamp"] = list_insert.apply(lambda x: x["Timestamp"] + timedelta(seconds=x["duration"]), axis=1)
        df_new = pd.concat([df_new, list_insert])
        df_new = df_new.sort_values(by=["Case", "order"], ascending=[True, True])
        df_new.reset_index(inplace=True, drop=True)
        return df_new

    # Events are recorded in wrong case
    def switch(self, df):

        df_new = pd.DataFrame.copy(df)
        c_list = df_new[(df_new["type_res_event"] == "switch_from")]
        c_list = c_list["Event"].unique()
        c_list = list(c_list)
        list_n_i = list(df_new.index[(df_new["Resource_Anomaly/Normal"] == 0) & (df_new["parameter"].isin(c_list))])
        list_n_c = list(df_new.loc[list_n_i, "Case"])
        list_n_ts = list(df_new.loc[list_n_i, "Timestamp"])
        list_n_d = list(df_new.loc[list_n_i, "duration"])
        list_switch_i = list(df_new.index[(df_new["type_res_event"] == "switch_from") ])
        list_switch_c = list(df_new.loc[list_switch_i, "Case"])
        list_switch_p = list(df_new.loc[list_switch_i, "parameter"])
        list_switch_ts = list(df_new.loc[list_switch_i, "Timestamp"])

        dic_switch = {"index": list_switch_i, "Case": list_switch_c, "parameter": list_switch_p,
                     "parameter_c": list_n_c,
                     "Timestamp": list_switch_ts, "Timestamp_c": list_n_ts, "duration": list_n_d}
        
        list_switch = pd.DataFrame(dic_switch)
        list_switch_1 = list_switch.apply(lambda row: np.arange(row["index"], int(row["index"] + row["parameter"]), 1),
                                        axis=1)
        list_switch_1 = np.concatenate(list_switch_1)
        list_switch_2 = list_switch.apply(lambda row: np.repeat(a=row["parameter_c"], repeats=row["parameter"]),
                                        axis=1)
        list_switch_2 = np.concatenate(list_switch_2)
        list_switch_3 = list_switch.apply(lambda row: np.repeat(a=row["Timestamp_c"], repeats=row["parameter"]), axis=1)
        list_switch_3 = np.concatenate(list_switch_3)
        list_switch_4 = list_switch.apply(lambda row: np.repeat(a=row["duration"] / (row["parameter"] + 1), repeats=row["parameter"]), axis=1)
        list_switch_4 = list(map(lambda x: x.cumsum(), list_switch_4))
        list_switch_4 = np.concatenate(list_switch_4)
        list_switch_5 = pd.DataFrame(list(map(lambda x, y: x + timedelta(seconds=y), list_switch_3, list_switch_4)), columns=["Timestamp"])
        list_switch_i2 = set(list_switch_1)
        list_clean = [x for x in list(df_new.index) if x not in list_switch_i2]
        switch_c = df_new.loc[list_clean]
        switch_f = df_new.loc[list_switch_1]
        switch_c.reset_index(inplace=True, drop=True)
        switch_f.reset_index(inplace=True, drop=True)
        switch_f["resource_parameter_event"] = switch_f.apply(lambda x: "caseID = {0}".format(x["Case"]), axis=1)
        switch_f["Case"] = list_switch_2
        switch_f["Timestamp"] = list_switch_5
        df_new = pd.concat([switch_c, switch_f])
        df_new.loc[df_new["Case"].isin(list_n_c), "type_res_case"] = "switch_to"
        df_new.loc[df_new["Event"].isin(c_list), "type_res_event"] = "switch_to"
        df_new = df_new.sort_values(by=["Case", "order"], ascending=[True, True])
        df_new.reset_index(inplace=True, drop=True)
        return df_new

    def choice_act(self, act, x):

        act_new = []
        for i in range(len(act)):
            act_new.append(act[i])
        act_new.remove(x)
        activity = random.choice(act_new)
        return activity
    
    def replace(self, df):

        df_new = pd.DataFrame.copy(df)
        act = list(df["Activity"].unique())
        list_replace_i = list(df_new.index[(df_new["type_res_event"] == "replace")])
        list_replace_p = list(df_new.loc[list_replace_i, "parameter"])
        dic_replace = {"index": list_replace_i, "parameter": list_replace_p}
        list_replace = pd.DataFrame(dic_replace)
        list_replace_1 = list_replace.apply(lambda row: np.arange(row["index"], int(row["index"] + row["parameter"]), 1), axis=1)
        list_replace_1 = np.concatenate(list_replace_1)
        list_replace_act = list(df_new.loc[list_replace_1, "Activity"])
        list_replace_act = list(map(lambda x: self.choice_act(act, x), list_replace_act))
        df_new.loc[list_replace_1, "Activity"] = list_replace_act
        df_new = df_new.sort_values(by=["Case", "order"], ascending=[True, True])
        df_new.reset_index(inplace=True, drop=True)
        return df_new

    # Implement functions and save result
    def implement(self, types, mag=[], m_skip=1, m_form=2, s_moved=1, m_switch=1, m_insert=1, m_rework=1, m_replace=1, df_res=None):

        if df_res is None:
            df_res = self.input_file    #event logs with "Pass/Fail"
        else:
            pass
        
        print("Started preprocessing to set the input of anomaly patterns", file=sys.stderr)
        start_parameter = datetime.datetime.now()
        df_1 = self.setting1(df_res, types, mag)
        
        df_1["unixtime"] = (df_1["Timestamp"].astype('int64') // 10**9)
        # df_1.to_csv(temp_path + "\\data_with_parameter0.csv", mode='w', index=False)
        df_new = self.setting2(df_1, m_skip, m_form, s_moved, m_switch, m_insert, m_rework, m_replace)
        # df_new.to_csv(temp_path + "\\data_with_parameter1.csv", mode='w', index=False)
        end_parameter = datetime.datetime.now()
         
        print("Finished preprocessing (running time={0})".format(end_parameter-start_parameter), file=sys.stderr)
        types2 = df_new["type_res_event"].unique()
        types2 = [x for x in types2 if str(x) != 'nan']
        
        # first application = moved 
        if "moved" in types2:
            types2.remove("moved")
            types2 = ["moved"] +types2
        else:
            pass
         
        print("Started to inject anomaly patterns", file=sys.stderr)
        start_inject = datetime.datetime.now()
        for i in range(len(types2)):
            if types2[i] == "skip":
                df_new = self.skip(df_new)
                 
            elif types2[i] == "incomplete":
                df_new = self.incomplete(df_new)
                 
            elif types2[i] == "switch_from":
                df_new = self.switch(df_new)
                 
            elif types2[i] == "form based":
                df_new = self.form_based(df_new)
                 
            elif types2[i] == "rework":
                df_new = self.rework(df_new)
                 
            elif types2[i] == "moved":
                df_new = self.moved(df_new)
                 
            elif types2[i] == "insert":
                df_new = self.insert(df_new)
                 
            elif types2[i] == "replace":
                df_new = self.replace(df_new)
                 
            else:
                pass

        df_new["order_b"] = df_new["order"]
        df_new["trace_temp"] = np.nan
        df_new["is_trace_anomalous(resource)"] = np.nan
        df_new = df_new.sort_values(by=["Case", "Timestamp"], ascending=[True, True])
        df_new["Resource_Anomaly/Normal"] = pd.to_numeric(df_new["Resource_Anomaly/Normal"])
        df_2 = pd.DataFrame.copy(df_new)
        df_2["check"] = 1
        df_new["order"] = df_2.groupby(["Case"])["check"].cumsum()
        df_new["max"] = df_new.groupby(["Case"])["order"].transform("max")
        df_3 = df_new.shift(-1, axis=0)
        df_new["duration"] = df_3["Timestamp"] - df_new["Timestamp"]
        df_new.loc[(df_new["order"] == df_new["max"]), "duration"] = datetime.timedelta()
        df_new["duration"] = df_new.apply(lambda x: x["duration"].seconds, axis=1)
        df_new["trace_temp"] = np.where(df_new["order"] != df_new["order_b"], 1, 0)
        df_new["is_trace_anomalous(resource)"] = df_new.groupby(["Case"])["trace_temp"].transform("max")
        df_new["is_trace_anomalous(resource)"] = np.where((df_new["type_res_case"] == "skip") | (df_new["type_res_case"] == "switch_from") | (df_new["type_res_case"] == "switch_to") | (df_new["type_res_case"] == "incomplete") | (df_new["type_res_case"] == "replace"), 1, df_new["is_trace_anomalous(resource)"])
        end_inject = datetime.datetime.now()
         
        np.random.seed(0)
        df_cal = df_new.sort_values(by=["Case", "Timestamp"], ascending=[True, True])
        df_cal_a = list(df_cal.groupby(["Case"])["Activity"])
        df_cal_b = []
        for i in range(len(df_cal_a)):
            df_cal_b.append(tuple(df_cal_a[i][1].reset_index(drop=True)))
        df_cal_c = list(set(df_cal_b))
        df_cal_d = {}
        for i in range(len(df_cal_c)):
            df_cal_d[df_cal_c[i]] = "var_{0}".format(i)
        df_cal_e = {}
        for i in range(len(df_cal_a)):
            df_cal_e[df_cal_a[i][0]] = df_cal_d[tuple(df_cal_a[i][1].reset_index(drop=True))]
        df_new2 = pd.DataFrame.copy(df_new)
        df_new2["variant_num"] = np.nan
        df_new2["variant_num"] = df_new2.apply(lambda x: df_cal_e[x["Case"]], axis=1)
        df_new2 = df_new2.drop(["order_b", "trace_temp"], axis=1)
        print("Finished to inject anomaly patterns (running time={0})".format(end_inject-start_inject), file=sys.stderr)

        return df_new2

    def implement_resource(self, types, mag=[], m_skip=1, m_form=2, s_moved=1, m_switch=1, m_insert=1, m_rework=1, m_replace=1, df_res=None):
        df_r = self.implement(types, mag, m_skip, m_form, s_moved, m_switch, m_insert, m_rework, m_replace, df_res)
        df_p = df_r
        # df_p.to_csv(temp_path + "\\final_raw.csv", mode='w', index=False)
        # df_p = df_p.drop(["unixtime", "max", "parameter_l", "parameter", "duration"], axis=1)
        # df_p.to_csv(temp_path + "\\final.csv", mode='w', index=False)

        return df_p
