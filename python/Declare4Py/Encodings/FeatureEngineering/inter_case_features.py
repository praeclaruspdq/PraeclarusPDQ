import statistics
from collections import defaultdict, OrderedDict
import pm4py
from pandas import DataFrame
from pm4py.objects.log.obj import EventLog
import pdb


# TODO capire se event_executions è utile
# TODO capire come codificare questi dizionari
class InterCaseFeatures():

    @staticmethod
    def events_by_date(log: EventLog, timestamp_attr: str) -> OrderedDict:
        """
        Creates dict of events by date ordered by date

        Args:
            log: the event log
            timestamp_attr: the name of the timestamp attribute

        Returns:
            A dictionary with dates as keys and number of events as values, e.g., {'2010-12-30': 7, '2011-01-06': 8}
        """
        stamp_dict = defaultdict(lambda: 0)
        for trace in log:
            for event in trace:
                timestamp = event.get(timestamp_attr)
                if timestamp is None:
                    raise Exception(f"{timestamp_attr} attribute does not exist.")
                stamp_dict[str(timestamp.date())] += 1
        return OrderedDict(sorted(stamp_dict.items()))

    @staticmethod
    def resources_by_date(log: EventLog, timestamp_attr: str, payload_attr: str) -> OrderedDict:
        """
        Creates dict of used unique resources ordered by date

        Args:
            log: the event log
            timestamp_attr: the name of the timestamp attribute
            payload_attr: the name of the payload attribute

        Returns:
            A dictionary with a date and the number of unique resources used on that day, e.g., {'2010-12-30': 7, '2011-01-06': 8}
        """
        stamp_dict = defaultdict(lambda: [])
        for trace in log:
            for event in trace:
                payload = event.get(payload_attr)
                if payload is None:
                    raise Exception(f"{payload_attr} attribute does not exist.")
                timestamp = event.get(timestamp_attr)
                if timestamp is None:
                    raise Exception(f"{timestamp_attr} attribute does not exist.")
                stamp_dict[str(timestamp.date())].append(payload)

        for key, value in stamp_dict.items():
            stamp_dict[key] = len(set(value))

        return OrderedDict(sorted(stamp_dict.items()))

    @staticmethod
    def event_executions(log: EventLog, activity_attr: str) -> OrderedDict:
        """
        Creates dict of event execution count

        Args:
            log: the event log
            activity_attr: the name of the activity attribute

        Returns:
            A dictionary with event names as keys and the number of their executions in the whole log as values, e.g., {'Event A': 7, 'Event B': 8}
        """
        executions = defaultdict(lambda: 0)
        for trace in log:
            for event in trace:
                activity = event.get(activity_attr)
                if activity is None:
                    raise Exception(f"{activity_attr} attribute does not exist.")
                executions[activity] += 1
        return OrderedDict(sorted(executions.items()))

    @staticmethod
    def new_trace_start(log: EventLog, timestamp_attr: str) -> OrderedDict:
        """
        Creates dict of new traces by date

        Args:
            log: the event log
            timestamp_attr: the name of the timestamp attribute

        Returns:
            A dictionary with dates as keys and the number of traces started in that date as values, e.g., {'2010-12-30': 1, '2011-01-06': 2}
        """
        executions = defaultdict(lambda: 0)
        for trace in log:
            event = trace[0]
            timestamp = event.get(timestamp_attr)
            if timestamp is None:
                raise Exception(f"{timestamp_attr} attribute does not exist.")
            executions[str(timestamp.date())] += 1
        return OrderedDict(sorted(executions.items()))


################## COSE CHE NON SERVONO #########################
def trace_attributes(log: EventLog) -> list:
    """Creates an array of dicts that describe trace attributes.
    Only looks at first trace. Filters out `concept:name`.

    :return [{name: 'name', type: 'string', example: 34}]
    """
    values = []
    trace = log[0]  # TODO: this might be a bug if first trace has different events then others
    for attribute in trace.attributes:
        if attribute != "concept:name":
            atr_type = _is_number(trace.attributes[attribute])
            atr = {'name': attribute, 'type': atr_type, 'example': str(trace.attributes[attribute])}
            values.append(atr)
    values = sorted(values, key=lambda k: k['name'])
    return values


def _is_number(s) -> str:
    if (isinstance(s, (float, int)) or (s.isdigit() if hasattr(s, 'isdigit') else False)) and not isinstance(s, bool):
        return 'number'
    return 'string'


def events_in_trace(log: EventLog) -> OrderedDict:
    """Creates dict of number of events in trace

    :return {'4': 11, '3': 8}
    """
    stamp_dict = defaultdict(lambda: 0)
    for trace in log:
        stamp_dict[trace.attributes[NAME_CLASSIFIER]] = len(trace)
    return OrderedDict(sorted(stamp_dict.items()))


def max_events_in_log(log: EventLog) -> int:
    """Returns the maximum number of events in any trace

    :return 3
    """
    return max([len(trace) for trace in log])


def avg_events_in_log(log: EventLog) -> int:
    """Returns the average number of events in any trace

    :return 3
    """
    return statistics.mean([len(trace) for trace in log])


def std_var_events_in_log(log: EventLog) -> int:
    """Returns the standard variation of the average number of events in any trace

    :return 3
    """
    return statistics.stdev([len(trace) for trace in log])


def trace_ids_in_log(log: EventLog) -> list:
    return [trace.attributes[NAME_CLASSIFIER] for trace in log]


def traces_in_log(log: EventLog) -> list:
    return [{'attributes': trace.attributes, 'events': [event for event in trace]} for trace in log]


################################################################################

def count_on_event_day(trace, date_dict: dict, event_id):
    """Finds the date of event and returns the value from date_dict
    :param date_dict one of the dicts from log_metrics.py
    :param event_id Event id
    :param trace Log trace
    """
    try:
        event = trace[event_id]
        date = str(event['time:timestamp'].date())
        return date_dict.get(date, 0)
    except IndexError:
        return 0


def main():
    print('esempio di uso')

    log_path = 'SepsisCasesEventLog.xes'
    log = pm4py.read_xes(log_path)
    log = pm4py.convert_to_event_log(log)
    prefix_length = 2

    executed_events = InterCaseFeatures.events_by_date(log, "time:timestamp")
    resources_used = InterCaseFeatures.resources_by_date(log, "time:timestamp", "org:group")
    new_traces = InterCaseFeatures.new_trace_start(log, "time:timestamp")
    event_executed = InterCaseFeatures.event_executions(log, "concept:name")

    encoded_dataset = []
    for trace in log:
        encoded_trace = [event['concept:name'] for event in
                         trace[:prefix_length]]  # esempio di traccia encodata con il simple
        encoded_trace += [
            count_on_event_day(trace, executed_events, prefix_length - 1),
            count_on_event_day(trace, resources_used, prefix_length - 1),
            count_on_event_day(trace, new_traces, prefix_length - 1)
        ]
        encoded_dataset += [encoded_trace]

    dataset_df = DataFrame(
        columns=['prefix_' + str(i) for i in range(prefix_length)] + [
            'executed_events_per_day',
            'resources_used_per_day',
            'new_traces_per_day'
        ],
        data=encoded_dataset
    )
    pdb.set_trace()


if __name__ == '__main__':
    main()
