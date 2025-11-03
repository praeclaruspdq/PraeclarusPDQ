import pdb

from Declare4Py.D4PyEventLog import D4PyEventLog
from sklearn.base import TransformerMixin, BaseEstimator
import pandas as pd
import numpy as np
from time import time
from typing import Union, List
from pandas import DataFrame, Index
from Declare4Py.ProcessModels.DeclareModel import DeclareModel
from Declare4Py.ProcessModels.DeclareModel import DeclareModelTemplate
from Declare4Py.ProcessMiningTasks.ConformanceChecking.MPDeclareAnalyzer import MPDeclareAnalyzer
from Declare4Py.ProcessMiningTasks.ConformanceChecking.MPDeclareResultsBrowser import MPDeclareResultsBrowser


class Declare(BaseEstimator, TransformerMixin):
    
    def __init__(self, event_log: D4PyEventLog, act_col: str = 'concept:name', itemset_support: float = 0.1,
                 boolean: bool = True, max_declare_cardinality: int = 1):
        """
        Parameters
        -------------------
        act_col
            columns indicating the categorical attributes in an event log
        itemset_support
            columns indicating the numerical attributes in an event log       
        boolean
            TRUE: Result the existence of a value as 1/0  / False: Count the frequency
        max_declare_cardinality
            TRUE: Result the existence of a value as 1/0  / False: Count the frequency
        """

        self.act_col = act_col
        self.itemset_support = itemset_support
        self.boolean = boolean
        self.columns = None
        self.transform_time = 0
        self.event_log = event_log
        self.max_declare_cardinality = max_declare_cardinality

    def fit(self, X: Union[np.array, DataFrame], y=None):
        return self
    
    def transform(self, X: Union[np.array, DataFrame], y=None) -> DataFrame:
        start = time()
        frequent_itemsets = self.event_log.compute_frequent_itemsets(self.itemset_support,
                                                                     case_id_col=self.event_log.case_id_key,
                                                                     categorical_attributes=[self.act_col],
                                                                     algorithm='fpgrowth', len_itemset=2,
                                                                     remove_column_prefix=True)

        declare_model = DeclareModel()

        frequent_events = [list(item)[0] for item in frequent_itemsets["itemsets"] if len(item) == 1]
        frequent_pairs = [list(item) for item in frequent_itemsets["itemsets"] if len(item) == 2]

        for unary_template in DeclareModelTemplate.get_unary_templates():
            for event in frequent_events:
                if unary_template.supports_cardinality:
                    for i in range(self.max_declare_cardinality):
                        constraint = {"template": unary_template, "activities": [event], "condition": ("", ""),
                                      "n": i + 1}
                        declare_model.constraints.append(constraint)
                else:
                    constraint = {"template": unary_template, "activities": [event], "condition": ("", "")}
                    declare_model.constraints.append(constraint)

        for binary_template in DeclareModelTemplate.get_binary_not_shortcut_templates():
            for event_pair in frequent_pairs:
                constraint = {"template": binary_template, "activities": event_pair, "condition": ("", "")}
                declare_model.constraints.append(constraint)

        declare_model.set_constraints()

        basic_checker = MPDeclareAnalyzer(log=self.event_log, declare_model=declare_model, consider_vacuity=False)
        conf_check_res: MPDeclareResultsBrowser = basic_checker.run()

        df_activations = conf_check_res.get_metric(metric="num_activations")
        df_state = conf_check_res.get_metric(metric="state")
        df_transformed = df_state.combine(df_activations, self.combine_fulfillments_and_state)
        self.columns = df_state.columns
        self.transform_time = time() - start
        if self.boolean:
            return df_state
        else:
            return pd.DataFrame(df_transformed, columns=self.columns)

    def get_feature_names(self) -> Index:
        """
        Print all attribute names in a Pandas DataFrame:

        Returns
        ------------------
        :rtype: Index
            column names of a Pandas DataFrame
        """
        return self.columns

    @staticmethod
    def combine_fulfillments_and_state(col1: pd.Series, col2: pd.Series) -> pd.Series:
        # col1 da state, col2 da activations
        if col2.isna().any():
            return col1.replace(0, -1)
        else:
            tmp_col = col1 * col2.replace(0, -2)  # create boolean mask: 0 means unsat, pos or neg values mean sat
            tmp_col = tmp_col.replace(0, -1)  # -1 means unsat, pos values mean sat, neg values mean vacuous sat
            return tmp_col.replace(-2, 0)  # encode 0 as vacuous sat
