from sklearn.base import BaseEstimator, TransformerMixin
import pandas as pd
from time import time
from typing import Union, List
from pandas import DataFrame, Index
import numpy as np


class PreviousState(BaseEstimator, TransformerMixin):
    
    def __init__(self, case_id_col: str, cat_cols: List[str], num_cols: List[str], fillna: bool = True):
        """
        Parameters
        -------------------
        case_id_col
            a column indicating the case identifier in an event log
        cat_cols
            columns indicating the categorical attributes in an event log
        num_cols
            columns indicating the numerical attributes in an event log       
        fillna
            TRUE: replace NA to 0 value in dataframe / FALSE: keep NA        
        """
        
        self.case_id_col = case_id_col
        self.cat_cols = cat_cols
        self.num_cols = num_cols
        self.fillna = fillna 
        self.columns = None
        self.fit_time = 0
        self.transform_time = 0

    def fit(self, X: Union[DataFrame, np.ndarray], y=None):
        return self

    def transform(self, X: Union[DataFrame, np.ndarray], y=None) -> DataFrame:
        """
        Tranforms the event log X into a previous-state encoded matrix (i.e., the previous state of the last state):

        Parameters
        -------------------
        X: DataFrame
            Event log / Pandas DataFrame to be transformed
            
        Returns
        ------------------
        :rtype: DataFrame
            Transformed event log
        """ 

        start = time()
        
        dt_last = X.groupby(self.case_id_col).nth(-2)
        
        # transform numeric cols
        dt_transformed = dt_last[self.num_cols]
        
        # transform cat cols
        if len(self.cat_cols) > 0:
            dt_cat = pd.get_dummies(dt_last[self.cat_cols])
            dt_transformed = pd.concat([dt_transformed, dt_cat], axis=1)

        # add 0 rows where previous value did not exist
        dt_transformed = dt_transformed.reindex(X.groupby(self.case_id_col).first().index, fill_value=0)
            
        # fill NA with 0 if requested
        if self.fillna:
            dt_transformed = dt_transformed.fillna(0)
            
        # add missing columns if necessary
        if self.columns is not None:
            missing_cols = [col for col in self.columns if col not in dt_transformed.columns]
            for col in missing_cols:
                dt_transformed[col] = 0
            dt_transformed = dt_transformed[self.columns]
        else:
            self.columns = dt_transformed.columns
        
        self.transform_time = time() - start
        return dt_transformed

    def get_feature_names(self) -> Index:
        """
        Print all attribute names in a Pandas DataFrame:

        Returns
        ------------------
        :rtype: Index
            column names of a Pandas DataFrame
        """
        return self.columns
