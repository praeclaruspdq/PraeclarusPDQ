from Declare4Py.D4PyEventLog import D4PyEventLog

import pm4py
from pm4py.objects.log.obj import EventLog, Trace
from typing import Union, Set, List, Tuple, Dict
import packaging
from packaging import version


class BasicFilters:

    def __init__(self, event_log: D4PyEventLog):
        self.event_log: D4PyEventLog = event_log

    def filter_time_range_contained(self, start_date: str, end_date: str, mode: str = "events", ) -> EventLog:
        """
        This function uses the get_log() of the Declare4Py package.

        Args:
            start_date: str in form year-month-day hours:minutes:seconds. E.g.: 2013-01-01 00:00:00
            end_date: str in form year-month-day hours:minutes:seconds. E.g.: 2013-01-01 00:00:00
            mode: define which of the three modes wil be set: events, traces_intersecting and traces_contained

        Returns:
            the filtered log in the timeframe.

        """
        if packaging.version.parse(pm4py.__version__) > packaging.version.Version("2.3.1"):
            return pm4py.filter_time_range(self.event_log.log, start_date, end_date, mode,
                                           self.event_log.timestamp_key, self.event_log.case_id_key)
        else:
            filtered_time_range = pm4py.filter_time_range(self.event_log.log, start_date, end_date, mode)
            return filtered_time_range

    def filter_case_performance(self, min_performance: float, max_performance: float) -> EventLog:
        """
        Filters the log using two integers values, which are the performance of the event

        Args:
            min_performance: minimum allowed case duration
            max_performance: maximum allowed case duration

        Returns:
            Returns the filtered log containing cases in the range of the specified performance interval.

        """
        if packaging.version.parse(pm4py.__version__) > packaging.version.Version("2.3.1"):
            return pm4py.filter_case_performance(self.event_log.log, min_performance, max_performance,
                                                 self.event_log.timestamp_key, self.event_log.case_id_key)
        else:
            filtered_case_performance = pm4py.filter_case_performance(self.event_log.log, min_performance,
                                                                      max_performance)
            return filtered_case_performance

    def filter_start_activities(self, activities: Union[Set[str], List[str]], retain: bool = True, activity_key: str = "concept:name") -> EventLog:
        """
        Filters all activities that start with the specified start activities

        Args:
            activities: Union[set[str], list[str]] Collection of start activities
            retain: if True, we retain the traces containing the given start activities, if false, we drop the traces
            activity_key: attribute to be used for the activity

        Returns:
            Returns filtered log that contains cases having a start activities in the specified list.

        """
        if packaging.version.parse(pm4py.__version__) > packaging.version.Version("2.3.1"):
            return pm4py.filter_start_activities(self.event_log.log, activities, retain, activity_key,
                                                 self.event_log.timestamp_key, self.event_log.case_id_key)
        else:
            filtered_start_activities = pm4py.filter_start_activities(self.event_log.log, activities)
            return filtered_start_activities

    def filter_end_activities(self, activities: [Set[str], List[str]], retain: bool = True, activity_key: str = "concept:name") -> EventLog:
        """
        Filter cases having an end activity in the provided list

        Args:
            activities: collection of end activities
            retain: if True, we retain the traces containing the given end activities, if false, we drop the traces
            activity_key: attribute to be used for the activity

        Returns:
            Returns filtered log containing cases having an end activity in the provided list.

        """
        if packaging.version.parse(pm4py.__version__) > packaging.version.Version("2.3.1"):
            return pm4py.filter_end_activities(self.event_log.log, activities, retain, activity_key,
                                               self.event_log.timestamp_key, self.event_log.case_id_key)
        else:
            filter_activities = pm4py.filter_end_activities(self.event_log.log, activities)
            return filter_activities

    def filter_variants_top_k(self, k: int, activity_key: str = "concept:name") -> EventLog:
        """
        Keeps the top-k variants of the log.

        Args:
            k: number of variants that should be kept
            activity_key: attribute to be used for the activity

        Returns:
            Returns log containing top-k variants.

        """
        if packaging.version.parse(pm4py.__version__) > packaging.version.Version("2.3.1"):
            return pm4py.filter_variants_top_k(self.event_log.log, k, activity_key, self.event_log.timestamp_key,
                                               self.event_log.case_id_key)
        else:
            variants_top_k = pm4py.filter_variants_top_k(self.event_log.log, k)
            return variants_top_k

    def filter_variants(self, variants: [Set[str], List[str]], retain: bool = True, activity_key: str = "concept:name") -> EventLog:
        """
        Filter a log by a specified set of variants.

        Args:
            variants: collection of variants to filter;
                A variant should be specified as a list of tuples of activity names, e.g., [('a', 'b', 'c')]
            retain: if True all traces conforming to the specified variants are retained; if False, all those traces are removed
            activity_key: attribute to be used for the activity

        Returns:
            Returns filtered log on specified variants.
        """
        if packaging.version.parse(pm4py.__version__) > packaging.version.Version("2.3.1"):
            return pm4py.filter_variants(self.event_log.log, variants, retain, activity_key,
                                         self.event_log.timestamp_key, self.event_log.case_id_key)
        else:
            filtered_variants = pm4py.filter_variants(self.event_log.log, variants)
            return filtered_variants

    def filter_event_attribute_values(self, attribute_key: str, values: Union[Set[str], List[str]], level: str = "case",
                                      retain: bool = True, ) -> EventLog:
        """
        Filter an event log on the values of some event attribute.

        Args:
            attribute_key: attribute to filter
            values: admitted (or forbidden) values
            level: specifies how the filter should be applied.
            ('case' filters the cases where at least one occurrence happens, 'event' filter the events eventually
            trimming the cases)
            retain: specifies if the values should be kept or removed

        Returns:
            Returns filtered eventlog object on the values of some event attribute.

        """
        if packaging.version.parse(pm4py.__version__) > packaging.version.Version("2.3.1"):
            return pm4py.filter_event_attribute_values(self.event_log.log, attribute_key, values, level, retain,
                                                       self.event_log.case_id_key)
        else:
            filtered_event_attribute_val = pm4py.filter_event_attribute_values(self.event_log.log, attribute_key,
                                                                               values, level, retain)
            return filtered_event_attribute_val

    def is_activity_of_i_state_A(self, activity: str, index: int):
        log = self.event_log.get_log()
        log_act = self.event_log.activity_key
        traces = list(filter(lambda trace: (trace[index][log_act] == activity), log))
        return traces
