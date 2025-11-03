from sklearn.base import BaseEstimator, TransformerMixin
import pandas as pd
import numpy as np
from time import time
from typing import Union, List
from pandas import DataFrame, Index


class IndexBased(BaseEstimator, TransformerMixin):
    
    def __init__(self, case_id_col: str, cat_cols: List[str], num_cols: List[str] = [], max_events: int = None,
                 fillna: bool = True, create_dummies: bool = True):
        """
        Parameters
        -------------------
        case_id_col
            a column indicating the case identifier in an event log
        cat_cols
            columns indicating the categorical attributes in an event log
        num_cols
            columns indicating the numerical attributes in an event log       
        max_events
            maximum prefix length to be transformed  / Default: maximum prefix length in traces
        fillna
            TRUE: replace NA to 0 value in dataframe / FALSE: keep NA
        create_dummies        
            TRUE: transform categorical attributes as dummy attributes         
        """
        
        self.case_id_col = case_id_col
        self.cat_cols = cat_cols      
        self.num_cols = num_cols       
        self.max_events = max_events   
        self.fillna = fillna            
        self.create_dummies = create_dummies
        self.columns = None
        self.fit_time = 0
        self.transform_time = 0

    def fit(self, X: Union[np.array, DataFrame], y=None):
        return self
    
    def transform(self, X: Union[np.array, DataFrame], y=None) -> DataFrame:
        """
        Tranforms the event log X into an index-based encoded matrix:

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
        
        grouped = X.groupby(self.case_id_col, as_index=False)
        
        if self.max_events is None:
            self.max_events = max(grouped.size()['size'])
        
        dt_transformed = pd.DataFrame(grouped.apply(lambda x: x.name), columns=[self.case_id_col])

        for i in range(self.max_events):
            dt_index = grouped.nth(i)[[self.case_id_col] + self.cat_cols + self.num_cols]
            dt_index.columns = [self.case_id_col] + [f"{col}_{i}" for col in self.cat_cols] + \
                               [f"{col}_{i}" for col in self.num_cols]
            dt_transformed = pd.merge(dt_transformed, dt_index, on=self.case_id_col, how="left")
        dt_transformed.index = dt_transformed[self.case_id_col]

        # one-hot-encode cat cols
        if self.create_dummies:
            
            all_cat_cols = [f"{col}_{i}" for col in self.cat_cols for i in range(self.max_events)]
            dt_transformed = pd.get_dummies(dt_transformed, columns=all_cat_cols).drop(self.case_id_col, axis=1)
        
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
