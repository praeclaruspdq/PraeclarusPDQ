from __future__ import annotations

from typing import List
import pandas as pd

"""
Initializes class ConformanceCheckingResults

Attributes
-------
    dict_results : dict
        dictionary of conformance checking results
"""


class DeclareMinerResultsBrowser:

    def __init__(self, query_checker_results: List[List[str]]):
        self.df_results: pd.DataFrame = pd.DataFrame(
            query_checker_results,
            columns=["template", "activation", "target",
                     "activation_condition",
                     "target_condition",
                     "time_condition"
                     ])
