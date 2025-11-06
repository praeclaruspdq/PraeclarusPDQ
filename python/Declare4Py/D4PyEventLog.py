from __future__ import annotations

import pdb

import packaging
from packaging import version
import warnings
from mlxtend.frequent_patterns import fpgrowth, apriori

import pm4py
from pm4py.objects.log.obj import EventLog, Trace

from typing import List, Optional, Tuple, Dict

from pandas import DataFrame
from Declare4Py.Encodings.Aggregate import Aggregate


class D4PyEventLog:
    """
    Wrapper that collects the input log, the computed binary encoding and frequent item set for the input log.

    Args:
        log: the input event log parsed from a XES file
        log_length: the trace number of the input log
        frequent_item_sets: list of the most frequent item sets found along the log traces, together with their support and length
    """

    def __init__(self, case_name: str = "case:concept:name", log: Optional[EventLog] = None):
        """The class constructor

        Example::

            d4py_log = D4PyEventLog()
        """

        if log is not None:
            self.log: Optional[EventLog] = log
            self.log_length = len(log)
            self.timestamp_key = log._properties['pm4py:param:timestamp_key']
            self.activity_key = log._properties['pm4py:param:activity_key']
        else:
            self.log: Optional[EventLog] = None
            self.log_length: Optional[int] = None
            self.activity_key: Optional[str] = None
            self.timestamp_key: Optional[str] = None
        self.case_id_key: str = case_name

    def parse_xes_log(self, log_path: str) -> None:
        """
        Set the 'log' EventLog object and the 'log_length' integer by reading and parsing the log corresponding to
        given log file path.

        Note:
            the current version of Declare4Py supports only (zipped) XES format of the event logs.

        Args:
            log_path: File path where the log is stored.

        Example::

            log_path = path/to/my/xes
            d4py_log = D4PyEventLog()
            d4py_log.parse_xes_log(log_path)
        """
        with warnings.catch_warnings():
            warnings.filterwarnings("ignore", category=DeprecationWarning)
            log = pm4py.read_xes(log_path)

            if packaging.version.parse(pm4py.__version__) > packaging.version.Version("2.3.1"):
                self.log = pm4py.convert_to_event_log(log)
            else:
                self.log = log

        self.log_length = len(self.log)
        self.timestamp_key = self.log._properties['pm4py:param:timestamp_key']
        self.activity_key = self.log._properties['pm4py:param:activity_key']

    def get_log(self) -> EventLog:
        """
        Returns the log previously fed in input.

        Returns:
            the input log.
        """
        if self.log is None:
            raise RuntimeError("You must load a log before.")
        return self.log

    def get_length(self) -> int:
        """
        Return the length of the log, which was previously fed in input.

        Returns:
            the length of the log.
        """
        if self.log_length is None:
            raise RuntimeError("You must load a log before.")
        return self.log_length

    def get_concept_name(self) -> str:
        if self.log_length is None:
            raise RuntimeError("You must load a log before.")
        return self.activity_key

    def get_case_name(self) -> str:
        if self.log_length is None:
            raise RuntimeError("You must load a log before.")
        return self.case_id_key

    def get_timestamp_name(self) -> str:
        if self.log_length is None:
            raise RuntimeError("You must load a log before.")
        return self.timestamp_key

    def get_event_attribute_values(self, attribute: str, count_once_per_case: bool = False) -> Dict[str, int]:
        """
        Returns the values for a specified (event) attribute.

        Args:
            attribute: attribute
            count_once_per_case: If True, consider only an occurrence of the given attribute value inside a case
        (if there are multiple events sharing the same attribute value, count only 1 occurrence)
            case_id_key: attribute to be used as case identifier

        Returns:
            Returns filtered log on specified variants.

        """
        if self.log is None:
            raise RuntimeError("You must load a log before.")
        if packaging.version.parse(pm4py.__version__) > packaging.version.Version("2.3.1"):
            return pm4py.get_event_attribute_values(self.log, attribute, count_once_per_case, self.case_id_key)
        else:
            event_attribute_val = pm4py.get_event_attribute_values(self.log, attribute)
            return event_attribute_val

    def get_start_activities(self) -> Dict[str, int]:
        """
        Retrieves all starting activities of the log

        Returns:
            Returns a dictionary containing all start activities.

        """
        if self.log is None:
            raise RuntimeError("You must load a log before.")
        if packaging.version.parse(pm4py.__version__) > packaging.version.Version("2.3.1"):
            return pm4py.get_start_activities(self.log, self.activity_key, self.timestamp_key, self.case_id_key)
        else:
            start_activities = pm4py.get_start_activities(self.log)
            return start_activities

    def get_end_activities(self) -> Dict[str, int]:
        """
        Returns the end activities of a log


        Returns:
            Returns a dictionary containing all end activities.

        """
        if self.log is None:
            raise RuntimeError("You must load a log before.")
        if packaging.version.parse(pm4py.__version__) > packaging.version.Version("2.3.1"):
            return pm4py.get_end_activities(self.log, self.activity_key, self.timestamp_key, self.case_id_key)
        else:
            end_activities = pm4py.get_end_activities(self.log)
            return end_activities

    def get_variants(self) -> Dict[Tuple[str], List[Trace]]:
        """
        Retrieves all variants from the log.

        Returns:
            Returns a dictionary containing all variants in the log.
        """
        if self.log is None:
            raise RuntimeError("You must load a log before.")
        if packaging.version.parse(pm4py.__version__) > packaging.version.Version("2.3.1"):
            return pm4py.get_variants(self.log, self.activity_key, self.timestamp_key, self.case_id_key)
        else:
            return pm4py.get_variants(self.log)
    """
    def get_log_alphabet_attribute(self, attribute_name: str = None) -> List[str]:
        if self.log is None:
            raise RuntimeError("You must load a log before.")
        attribute_values = set()
        try:
            for trace in self.log:
                for event in trace:
                    attribute_values.add(event[attribute_name])
        except KeyError as e:
            print(f"{e} attribute does not exist. Check the log.")
        return list(attribute_values)
    """
    def get_trace(self, id_trace: int = None) -> Trace:
        if self.log is None:
            raise RuntimeError("You must load a log before.")
        try:
            return self.log[id_trace]
        except IndexError:
            print("The index of the trace must be lower than the log size.")
        except TypeError as e:
            print(f"The index of the trace must be integers or slices, not {e}.")

    def attribute_log_projection(self, attribute_name: str = None) -> List[List[str]]:
        """
        Returns for each trace a time-ordered list of the values of the input attribute for each event.

        Returns:
            nested lists, the outer one addresses traces while the inner one contains event activity names.
        """
        projection = []
        if self.log is None:
            raise RuntimeError("You must load a log before.")
        try:
            for trace in self.log:
                tmp_trace = []
                for event in trace:
                    tmp_trace.append(event[attribute_name])
                projection.append(tmp_trace)
        except KeyError as e:
            print(f"{e} attribute does not exist. Check the log.")
        return projection

    def to_dataframe(self):
        if self.log is None:
            raise RuntimeError("You must load a log before.")
        if isinstance(self.log, DataFrame):
            raise RuntimeError("Your log is already in a DataFrame format.")
        with warnings.catch_warnings():
            warnings.filterwarnings("ignore", category=DeprecationWarning)
            self.log = pm4py.convert_to_dataframe(self.log)

    def to_eventlog(self):
        if self.log is None:
            raise RuntimeError("You must load a log before.")
        if isinstance(self.log, EventLog):
            raise RuntimeError("Your log is already in a EventLog format.")
        with warnings.catch_warnings():
            warnings.filterwarnings("ignore", category=DeprecationWarning)
            self.log = pm4py.convert_to_event_log(self.log)

    def compute_frequent_itemsets(self, min_support: float, case_id_col: str, categorical_attributes: List[str] = None,
                                  algorithm: str = 'fpgrowth', len_itemset: int = 2,
                                  remove_column_prefix: bool = False) -> DataFrame:
        """
        Compute the most frequent item sets with a support greater or equal than 'min_support' with the given algorithm
        and over the given dimension.

        Args:
            min_support: the minimum support of the returned item sets.
            case_id_col: the name of the log attribute containing the ids of the cases
            categorical_attributes: a list of strings containing the names of the attributes to be encoded. For example, 'concept:name' for the activity names and 'org:group' for the resources.
            algorithm: the algorithm for extracting frequent itemsets, choose between 'fpgrowth' (default) and 'apriori'.
            len_itemset: the maximum length of the extracted itemsets.
        """
        if self.log is None:
            raise RuntimeError("You must load a log before.")
        if not 0 <= min_support <= 1:
            raise RuntimeError("Min. support must be in range [0, 1].")

        with warnings.catch_warnings():
            warnings.filterwarnings("ignore", category=DeprecationWarning)
            log_df = pm4py.convert_to_dataframe(self.log)

        for attr_name in categorical_attributes:
            if attr_name not in log_df.columns:
                raise RuntimeError(f"{attr_name} attribute does not exist. Check the log.")

        encoder: Aggregate = Aggregate(case_id_col=case_id_col, cat_cols=categorical_attributes,
                                       num_cols=[], boolean=True)
        binary_encoded_log = encoder.fit_transform(log_df)
        if remove_column_prefix:
            new_col_names = {}
            for col_name in binary_encoded_log.columns:
                column_tokens = col_name.split('_')
                new_col_names[col_name] = '_'.join(column_tokens[1:])

            binary_encoded_log.rename(columns=new_col_names, inplace=True)

        with warnings.catch_warnings():
            warnings.filterwarnings("ignore", category=DeprecationWarning)
            if algorithm == 'fpgrowth':
                frequent_itemsets = fpgrowth(binary_encoded_log, min_support=min_support, use_colnames=True)
            elif algorithm == 'apriori':
                frequent_itemsets = apriori(binary_encoded_log, min_support=min_support, use_colnames=True)
            else:
                raise RuntimeError(f"{algorithm} algorithm not supported. Choose between fpgrowth and apriori")

        frequent_itemsets['length'] = frequent_itemsets['itemsets'].apply(lambda x: len(x))
        if len_itemset is None:
            return frequent_itemsets
        elif len_itemset < 1:
            raise RuntimeError(f"The parameter len_itemset must be greater than 0.")
        else:
            return frequent_itemsets[(frequent_itemsets['length'] <= len_itemset)]

    def save_xes(self, path: str):
        if self.log is None:
            raise RuntimeError("You must load a log before.")
        if type(path) is not str:
            raise RuntimeError("The path must be  a string.")
        try:
            if packaging.version.parse(pm4py.__version__) > packaging.version.Version("2.3.1"):
                pm4py.write_xes(self.log, path, case_id_key=self.case_id_key)
            else:
                pm4py.write_xes(self.log, path)
        except FileNotFoundError as e:
            print(f"{e} is no a valid path")
