import warnings

import clingo
from clingo import SymbolType


class ASPResultEventModel:
    """
    Represents a single event in a trace from the ASP results.

    Attributes:
        name (str): The name of the event.
        pos (int): The position of the event in the trace.
        resource (Dict[str, str]): A dictionary containing the resource or value associated with the event.
        fact_symbol (List[clingo.symbol.Symbol]): The clingo symbols representing the event.
    """

    def __init__(self, fact_symbol: [clingo.symbol.Symbol]):
        self.name: str
        self.pos: int
        self.resource: {str, str} = {}
        self.fact_symbol: [clingo.symbol.Symbol] = fact_symbol
        self.parse_clingo_event()

    def parse_clingo_event(self):
        for symbols in self.fact_symbol:
            # syb: clingo.symbol.Symbol = symbols
            if symbols.type == SymbolType.Function:
                # print(syb.positive)
                self.name = str(symbols.name)
            if symbols.type == SymbolType.Number:
                self.pos = symbols.number

    def __str__(self) -> str:
        st = f"""{{ "event_name":"{self.name}", "position": "{self.pos}", "resource_or_value": {self.resource} }}"""
        return st.replace("'", '"')

    def __repr__(self) -> str:
        return self.__str__()


class ASPResultTraceModel:
    """
    Represents a single trace from the ASP results.

    Attributes:
        model (List[clingo.solving.Model]): The clingo model for the trace.
        name (str): The name of the trace.
        events (List[ASPResultEventModel]): A list of events in the trace.
        parsed_result (List): The parsed result of the trace containing events and their resources.
    """
    def __init__(self, trace_name: str, model: [clingo.solving.Model]):
        self.model = model
        self.name: str = trace_name
        self.events: [ASPResultEventModel] = []
        # ASP/clingo doesn't handle floats, thus we're scaling up the number values and now, we have to scale down back
        # after result
        self.parsed_result = self.parse_clingo_result(model)

    def parse_clingo_result(self, result: [clingo.solving.Model]) -> list:
        """
        Parses the clingo result to extract traces and resources.

        Args:
            result (List[clingo.solving.Model]): The clingo result containing traces and resources.

        Returns:
            list: A list containing the traces and their associated resources.
        """
        traces = {}
        resources = []
        obj = {}
        # We collect traces( which are events) and resources (attributes) along with positions
        # later, we merge both according position.
        for item in result:
            if item.name == 'trace':  # fact: -> "trace(event_name, position)"
                event_name, position = item.arguments
                event_name = str(event_name)
                position = (position.number)
                traces[position] = event_name
            elif item.name == 'assigned_value':  # fact: -> "assigned_value(res_name, res_value, position)"
                var_name, var_value, position = item.arguments
                var_name = str(var_name)
                var_value = str(var_value)
                resources.append({"res_name": var_name, "res_val": var_value, "pos": position.number})
            else:
                warnings.warn(f"What is happening here {str(item)}")
        result, obj = self.map_traces_and_resources(traces, resources)
        return obj

    def map_traces_and_resources(self, traces: dict, resources: list):
        """
        Maps the traces and resources based on their positions.

        Args:
            traces (list): A list containing the traces.
            resources (list): A list containing the resources.

        Returns:
            list: A list containing the combined traces and resources.
        """
        result = []
        nResult = {}
        for trace_pos in traces:
            event = {}
            event["name"] = traces[trace_pos]
            event["ev_position"] = trace_pos
            event["resources"] = {}
            nResult[trace_pos] = {"name": event["name"], "resources": []}
            for resource in resources:
                if resource["pos"] == trace_pos:
                    event["resources"][resource["res_name"]] = resource["res_val"]
                    nResult[trace_pos]["resources"].append({resource["res_name"]: resource["res_val"]})
                    event["resources"]["__position"] = resource["pos"]
                    event["__position"] = resource["pos"]
            result.append(event)
        result = sorted(result, key=lambda x: x['ev_position'])
        return result, nResult

    def __str__(self):
        st = f"""{{ "name": "{self.name}", "events": {self.events} }}"""
        return st.replace("'", '"')

    def __repr__(self):
        return self.__str__()

