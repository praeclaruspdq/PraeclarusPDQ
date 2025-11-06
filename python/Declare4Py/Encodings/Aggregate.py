from sklearn.base import TransformerMixin, BaseEstimator
import pandas as pd
import numpy as np
from time import time
from typing import Union, List
from pandas import DataFrame, Index


class Aggregate(BaseEstimator, TransformerMixin):
    
    def __init__(self, case_id_col: str, cat_cols: List[str], num_cols: List[str] = [], boolean: bool = False,
                 fillna: bool = True, aggregation_functions: List[str] = ('mean', 'max', 'min', 'sum')):
        """
        Parameters
        -------------------
        case_id_col
            a column indicating the case identifier in an event log
        cat_cols
            columns indicating the categorical attributes in an event log
        num_cols
            columns indicating the numerical attributes in an event log       
        boolean
            TRUE: Result the existence of a value as 1/0  / False: Count the frequency
        fillna        
            TRUE: replace NA to 0 value in dataframe / FALSE: keep NA           
        """
        
        self.case_id_col = case_id_col  
        self.cat_cols = cat_cols    
        self.num_cols = num_cols    
        self.boolean = boolean    
        self.fillna = fillna       
        self.columns = None
        self.fit_time = 0
        self.transform_time = 0
        self.aggregation_functions = aggregation_functions

    def fit(self, X: Union[np.array, DataFrame], y=None):
        return self
    
    def transform(self, X: Union[np.array, DataFrame], y=None) -> DataFrame:
        """
        Tranforms the event log X into an aggregated numeric matrix:

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
        
        # transform numeric cols
        if len(self.num_cols) > 0:
            dt_numeric = X.groupby(self.case_id_col)[self.num_cols].agg(self.aggregation_functions)
            dt_numeric.columns = ['_'.join(col).strip() for col in dt_numeric.columns.values]
            
        # transform cat cols
        dt_transformed = pd.get_dummies(X[self.cat_cols])
        dt_transformed[self.case_id_col] = X[self.case_id_col]
        del X
        if self.boolean:
            dt_transformed = dt_transformed.groupby(self.case_id_col).max()
        else:
            dt_transformed = dt_transformed.groupby(self.case_id_col).sum()

        # concatenate
        if len(self.num_cols) > 0:
            dt_transformed = pd.concat([dt_transformed, dt_numeric], axis=1)
            del dt_numeric
        
        # fill missing values with 0-s
        if self.fillna:
            dt_transformed = dt_transformed.fillna(0)
            
        # add missing columns if necessary
        if self.columns is None:
            self.columns = dt_transformed.columns
        else:
            missing_cols = [col for col in self.columns if col not in dt_transformed.columns]
            for col in missing_cols:
                dt_transformed[col] = 0
            dt_transformed = dt_transformed[self.columns]
        
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
