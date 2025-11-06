from __future__ import annotations
from typing import List, Union, Optional
from Declare4Py.Utils.Declare.Checkers import CheckerResult
import pandas as pd

"""
Initializes class ConformanceCheckingResults

Attributes
-------
    dict_results : dict
        dictionary of conformance checking results
"""


class MPDeclareResultsBrowser:

    def __init__(self, matrix_results: List[List[CheckerResult]], serialized_constraints: List[str]):
        self.serialized_constraints = serialized_constraints
        self.model_check_res: List[List[CheckerResult]] = matrix_results

    def get_metric(self, metric: str, trace_id: int = None, constr_id: int = None) -> Union[pd.DataFrame, List, int]:
        if type(metric) is not str:
            raise RuntimeError("You must specify a metric among num_activations, num_violations, num_fulfillments, "
                               "num_pendings, state.")
        if metric not in ["num_activations", "num_violations", "num_fulfillments", "num_pendings", "state"]:
            raise RuntimeError("You must specify a metric among num_activations, num_violations, num_fulfillments, "
                               "num_pendings, state.")
        results = []
        if trace_id is None and constr_id is None:
            for trace_res in self.model_check_res:
                tmp_list = []
                for result_checker in trace_res:
                    tmp_list.append(self.retrieve_metric(result_checker, metric))
                results.append(tmp_list)
            results = pd.DataFrame(results, columns=self.serialized_constraints)
        elif trace_id is not None and constr_id is None:
            for result_checker in self.model_check_res[trace_id]:
                results.append(self.retrieve_metric(result_checker, metric))
        elif trace_id is None and constr_id is not None:
            for trace_res in self.model_check_res:
                result_checker = trace_res[constr_id]
                results.append(self.retrieve_metric(result_checker, metric))
        else:
            try:
                if metric == "state":
                    results = 0 if getattr(self.model_check_res[trace_id][constr_id], metric).value == 'Violated' else 1
                else:
                    results = getattr(self.model_check_res[trace_id][constr_id], metric)
            except IndexError:
                print("The index of the trace must be lower than the log size. The index of the constraint must be "
                      "lower than the total number of constraints in the Declare model.")
            except TypeError as e:
                print(f"The index of the trace/constraint must be integers or slices, not {e}.")
            except AttributeError:
                print("You must specify a metric among num_activations, num_violations, num_fulfillments, "
                      "num_pendings, state.")
        return results

    @staticmethod
    def retrieve_metric(result_checker: CheckerResult, metric: str) -> Optional[int]:
        try:
            if metric == "state":
                return 0 if getattr(result_checker, metric).value == 'Violated' else 1
            else:
                return getattr(result_checker, metric)
        except AttributeError:
            print("You must specify a metric among num_activations, num_violations, num_fulfillments, "
                  "num_pendings, state.")
