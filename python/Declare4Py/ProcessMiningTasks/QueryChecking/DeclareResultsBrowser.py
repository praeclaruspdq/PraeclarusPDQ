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


class DeclareResultsBrowser:

    def __init__(self, query_checker_results: List[List[str]]):
        self.df_results: pd.DataFrame = pd.DataFrame(query_checker_results, columns=["template", "activation", "target",
                                                                                     "activation_condition",
                                                                                     "target_condition",
                                                                                     "time_condition"])

    def filter_query_checking(self, queries: List[str]) -> pd.DataFrame:
        """
        The function outputs, for each constraint of the query checking result, only the elements of the constraint
        specified in the 'queries' list.

        Parameters
        ----------
        queries : list[str]
            elements of the constraint that the user want to retain from query checking result. Choose one (or more)
            elements among: 'template', 'activation', 'target'.

        Returns
        -------
        assignments
            list containing an entry for each constraint of query checking result. Each entry of the list is a list
            itself, containing the queried constraint elements.
        """
        if self.df_results is None:
            raise RuntimeError("You must run a query checking task before.")
        if len(queries) == 0 or len(queries) > 6:
            raise RuntimeError("The list of queries has to contain at least one query and six queries as maximum")

        try:
            return self.df_results[queries]
        except KeyError:
            print(f"{queries} does not contain an allowed query. Allowed queries are: template, activation, target, "
                  f"activation_condition, target_condition, time_condition.")
