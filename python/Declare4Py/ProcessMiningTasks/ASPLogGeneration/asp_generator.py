from __future__ import annotations

import collections
import logging
import math
import re
import typing
import warnings
from datetime import datetime, timedelta
import random
from random import randrange

import clingo
import pm4py
from clingo import Symbol

from Declare4Py.ProcessMiningTasks.ASPLogGeneration.ASPTranslator.asp_translator import ASPModel
from Declare4Py.ProcessMiningTasks.ASPLogGeneration.ASPUtils.asp_encoding import ASPEncoding
from Declare4Py.ProcessMiningTasks.ASPLogGeneration.ASPUtils.asp_result_parser import ASPResultTraceModel
from Declare4Py.ProcessMiningTasks.ASPLogGeneration.ASPUtils.asp_template import ASPTemplate
from Declare4Py.ProcessMiningTasks.ASPLogGeneration.ASPUtils.distribution import Distributor
from Declare4Py.ProcessMiningTasks.log_generator import LogGenerator
from Declare4Py.ProcessModels.AbstractModel import ProcessModel
from Declare4Py.ProcessModels.DeclareModel import DeclareModel, DeclareParsedDataModel, \
    DeclareModelConstraintTemplate, DeclareModelAttributeType, DeclareModelAttr, DeclareModelTemplate
import concurrent.futures
import pandas as pd

class LogTracesType(typing.TypedDict):
    positive: typing.List
    negative: typing.List


def custom_sort_trace_key(x):
    # Extract the numeric parts of the string
    parts = re.findall(r'\d+', x.name)
    # Convert the numeric parts to integers
    parts = [int(p) for p in parts]
    return parts


class AspGenerator(LogGenerator):

    def __init__(self, decl_model: DeclareModel, num_traces: int, min_event: int, max_event: int,
                 encode_decl_model: bool = True):
        """
        ASPGenerator generates the log from declare model which translate declare model
        into ASP, and then it passes to the clingo, which generates the traces

        Parameters
        ----------
        decl_model: DeclModel
            Declare model object
        num_traces: int
            an integer representing the number of traces to generate
        min_event: int
            an integer representing the minimum number of events that a trace can have
        max_event: int
            an integer representing the maximum number of events that a trace can have
        encode_decl_model: boolean
            indicating whether the declare model should be encoded or not.

        Because, clingo doesn't accept some names such as a name starting with capital letter.
        """
        super().__init__(num_traces, min_event, max_event, decl_model)
        self.py_logger = logging.getLogger("ASP generator")
        self.clingo_output = []
        self.clingo_current_output: typing.Sequence[Symbol]
        self.clingo_output_traces_variation = []
        # self.asp_generated_traces: typing.List[ASPResultTraceModel] | None = None
        self.asp_generated_traces: typing.Union[LogTracesType, None] = None
        self.asp_encoding = ASPEncoding().get_ASP_encoding()
        self.asp_template = ASPTemplate().value
        self.num_repetition_per_trace = 0
        self.trace_counter = 0
        self.trace_variations_key_id = 0  #
        self.parallel_workers = 10
        self.trace_counter_id = 0
        self.run_parallel: bool = False
        self.parallel_futures: [] = []
        # instead of using distribution
        self._custom_counter: dict[
            str, typing.Union[collections.Counter, None]] = None  # {"positive": None, "negative": None}
        # self._custom_counter: { "positive": collections.Counter | None, "negative": collections.Counter | None} | None = None

        self.lp_model: ASPModel = None
        self.traces_generated_events = None
        self.encode_decl_model = encode_decl_model
        self.py_logger.debug(f"Distribution for traces {self.distributor_type}")
        self.py_logger.debug(
            f"traces: {num_traces}, events can have a trace min({self.min_events}) max({self.max_events})")
        self.compute_distribution()

    def generate_asp_from_decl_model(self, encode: bool = True, save_file: str = None,
                                     process_model: ProcessModel = None, violation: dict = None) -> str:
        """
        Generates an ASP translation of the Declare model.
        Parameters
        ----------
        encode: bool
            whether to use the encoded values to generate ASP
        save_file: str
            specify filepath with name, in which the generated ASP will be saved
        process_model: DeclareModel
            DeclareModel which will be converted or translated into the ASP
        violation: dict
            A dictionary containing information about the constraint templates which should be violated or in order
            to generate the negative traces.

        Returns
        -------
        str
            ASP program
        """
        """
             It takes an optional encode parameter, which is a boolean
             indicating whether to encode the model or not. The default value is True.
        """
        if process_model is None:
            process_model = self.process_model
        self.py_logger.debug("Translate declare model to ASP")
        self.lp_model = ASPModel(encode).from_decl_model(process_model, violation)
        self.__handle_activations_condition_asp_generation()
        lp = self.lp_model.to_str()
        if save_file:
            with open(save_file, 'w+') as f:
                f.write(lp)
        self.py_logger.debug(f"Declare model translated to ASP. Total Facts {len(self.lp_model.fact_names)}")

        if self.negative_traces > 0:
            self.asp_encoding = ASPEncoding(True).get_ASP_encoding(self.lp_model.fact_names)
        else:
            self.asp_encoding = ASPEncoding(False).get_ASP_encoding(self.lp_model.fact_names)
        self.py_logger.debug("ASP encoding generated")
        return lp

    def __handle_activations_condition_asp_generation(self) -> None:
        """ Handles the logic for the activations condition """
        if self.activation_conditions is None:
            return
        decl_model: DeclareParsedDataModel = self.process_model.parsed_model
        # decl_model.templates[0].template_line
        for template_def, cond_num_list in self.activation_conditions.items():
            template_def = template_def.strip()
            decl_template_parsed: [DeclareModelConstraintTemplate] = [val for key, val in decl_model.templates.items()
                                                                      if val.line == template_def]
            decl_template_parsed: DeclareModelConstraintTemplate = decl_template_parsed[0]
            asp_template_idx = decl_template_parsed.template_index
            if decl_template_parsed is None:
                warnings.warn("Unexpected found. Same constraint templates are defined multiple times.")
            if len(cond_num_list) == 2:
                decoder = {v: k for k, v in decl_template_parsed.events_activities[0].event_name.encoder.encoded_values.items()}

                if (decl_template_parsed.template == DeclareModelTemplate.ALTERNATE_PRECEDENCE) or (decl_template_parsed.template == DeclareModelTemplate.PRECEDENCE) or (decl_template_parsed.template == DeclareModelTemplate.CHAIN_PRECEDENCE):
                    B = decoder[decl_template_parsed.events_activities[0].event_name.value]
                    A = decoder[decl_template_parsed.events_activities[1].event_name.value]
                else:
                    A = decoder[decl_template_parsed.events_activities[0].event_name.value]
                    B = decoder[decl_template_parsed.events_activities[1].event_name.value]

                if cond_num_list[0] <= 0:
                    # left side tends to -inf or 0 starting from cond_num_list[1]. cond_num_list = [0, 2]
                    # means it can have only at most 2 activations
                    self.lp_model.add_asp_line(
                        f":- #count{{T:trace({A},T), activation_condition({asp_template_idx},T)}} < {cond_num_list[1]}.")
                    if decl_template_parsed.template.both_activation_condition:  # some templates's both conditions are activation conditions
                        self.lp_model.add_asp_line(
                            f":- #count{{T:trace({B},T), correlation_condition({asp_template_idx},T)}} < {cond_num_list[1]}.")
                elif cond_num_list[1] == math.inf:
                    # right side tends to inf from cond_num_list[0] to +inf. cond_num_list = [2, math.inf]
                    # means it can have it should at least 2 activations and can go to infinite
                    self.lp_model.add_asp_line(
                        f":- #count{{T:trace({A},T), activation_condition({asp_template_idx},T)}} > {cond_num_list[0]}.")
                    if decl_template_parsed.template.both_activation_condition:
                        self.lp_model.add_asp_line(
                            f":- #count{{T:trace({B},T), correlation_condition({asp_template_idx},T)}} > {cond_num_list[0]}.")
                else:
                    # ie cond_num_list = [2, 4]
                    # self.lp_model.add_asp_line(
                    #     f":- #count{{T:trace(A,T), activation_condition({asp_template_idx},T)}} > {cond_num_list[0]}.")
                    # self.lp_model.add_asp_line(
                    #     f":- #count{{T:trace(A,T), activation_condition({asp_template_idx},T)}} < {cond_num_list[1]}.")
                    # if decl_template_parsed.template.both_activation_condition:
                    #     self.lp_model.add_asp_line(
                    #         f":- #count{{T:trace(A,T), correlation_condition({asp_template_idx},T)}} > {cond_num_list[0]}.")
                    #     self.lp_model.add_asp_line(
                    #         f":- #count{{T:trace(A,T), correlation_condition({asp_template_idx},T)}} < {cond_num_list[1]}.")
                    self.lp_model.add_asp_line(
                        f":- #count{{T:trace({A},T), activation_condition({asp_template_idx},T)}} < {cond_num_list[0]}.")
                    self.lp_model.add_asp_line(
                        f":- #count{{T:trace({A},T), activation_condition({asp_template_idx},T)}} > {cond_num_list[1]}."
                    )
                    if decl_template_parsed.template.both_activation_condition:
                        self.lp_model.add_asp_line(
                            f":- #count{{T:trace({B},T), correlation_condition({asp_template_idx},T)}} < {cond_num_list[0]}.")
                        self.lp_model.add_asp_line(
                            f":- #count{{T:trace({B},T), correlation_condition({asp_template_idx},T)}} > {cond_num_list[1]}.")

            else:
                raise ValueError(
                    "Interval values are wrong. It must have only 2 values, represents, left and right interval")

    def run(self, generated_asp_file_path: typing.Union[str, None] = None):
        """
            Runs Clingo on the ASP translated, encoding and templates of the Declare model to generate the traces.
        Parameters
        ----------
        generated_asp_file_path: str, optional
            Specify the file name if you want to write the ASP generated program
        """
        if self.negative_traces > self.log_length:
            warnings.warn("Negative traces can not be greater than total traces asked to generate. Nothing Generating")
            return
        self.trace_counter = 0
        pos_traces = self.log_length - self.negative_traces
        neg_traces = self.negative_traces
        self.parallel_futures = []
        if self._custom_counter is not None:
            self.py_logger.debug("******* Using custom traces length *******")
            if ("positive" in self._custom_counter) or ("negative" in self._custom_counter):
                pos_traces_dist = self._custom_counter["positive"]
                neg_traces_dist = self._custom_counter["negative"]
        else:
            self.py_logger.debug("Using custom traces length")
            pos_traces_dist = self.compute_distribution(pos_traces)
            neg_traces_dist = self.compute_distribution(neg_traces)

        result: LogTracesType = LogTracesType(negative=[], positive=[])
        result_variation: LogTracesType = LogTracesType(negative=[], positive=[])
        if self.negative_traces > 0:
            self.py_logger.debug("Generating negative traces")
            violation = {'constraint_violation': True, 'violate_all_constraints': self.violate_all_constraints}
            dupl_decl_model = self.__get_decl_model_with_violate_constraint()
            if generated_asp_file_path is not None:
                lp = self.generate_asp_from_decl_model(self.encode_decl_model, generated_asp_file_path + '.neg.lp',
                                                       dupl_decl_model, violation)
            else:
                lp = self.generate_asp_from_decl_model(self.encode_decl_model, None, dupl_decl_model, violation)
                self.__generate_traces(lp, neg_traces_dist, "negative")

            result['negative'] = self.clingo_output
            result_variation['negative'] = self.clingo_output_traces_variation

        self.py_logger.debug("Generating traces")
        lp = self.generate_asp_from_decl_model(self.encode_decl_model, generated_asp_file_path)
        # print(lp) 
        self.__generate_traces(lp, pos_traces_dist, "positive")
        if self.run_parallel:
            concurrent.futures.wait(self.parallel_futures)
        result['positive'] = self.clingo_output
        result_variation['positive'] = self.clingo_output_traces_variation

        self.py_logger.debug(f"Traces generated. Positive: {len(result['positive'])}"
                             f" Neg: {len(result['negative'])}. Parsing Trace results.")
        self.__resolve_clingo_results(result)
        self.__resolve_clingo_results_variation(result_variation)
        self.py_logger.debug(f"Trace results parsed")
        self.__pm4py_log()

    def __generate_traces(self, lp_model: str, traces_to_generate: collections.Counter, trace_type: str):
        """
        Runs Clingo on the ASP translated, encoding and templates of the Declare model to generate the traces.
        Parameters
        ----------
        lp_model: str
            ASP model
        traces_to_generate: collections.Counter
            a counter ({ 4:2, 1: 3}), means 2 traces of 4 events, 3 traces with just 1 event.
        trace_type: str
            trace type: negative or positive
        Returns
        -------
        """
        self.clingo_output = []
        self.clingo_output_traces_variation = {}
        self.py_logger.debug(f"Start generating traces: {traces_to_generate}")
        if self.run_parallel:
            with concurrent.futures.ThreadPoolExecutor(max_workers=self.parallel_workers) as executor:
                for events, traces in traces_to_generate.items():
                    future = executor.submit(self.__generate_asp_trace, lp_model, events, traces, trace_type)
                    self.parallel_futures.append(future)
        else:
            for events, traces in traces_to_generate.items():
                self.py_logger.debug(
                    f" Total trace to generate and events: Traces:{traces}, Events: {events}, RandFrequency: 0.9")
                self.__generate_asp_trace(lp_model, events, traces, trace_type)

    def __generate_asp_trace(self, asp: str, num_events: int, num_traces: int, trace_type: str, freq: float = 0.9):
        #import pdb
        #pdb.set_trace()
        """
        Generate ASP trace using Clingo based on the given parameters and then generate also the variation
        Parameters
        ----------
        asp: str
            ASP model
        num_events: int
            number of events to be present in the trace
        num_traces: int
            total number of traces
        trace_type: str
            type of trace: postive or negative
        freq: float
            a random float value between 0 and 0.9
        Returns
        -------

        """
        # "--project --sign-def=3 --rand-freq=0.9 --restart-on-model --seed=" + seed
        if self.run_parallel:
            with concurrent.futures.ThreadPoolExecutor(max_workers=self.parallel_workers) as executor:
                for i in range(num_traces):
                    self.py_logger.debug(f" Setting thread for trace:{i + 1}/{num_traces} with events:{num_events})")
                    future = executor.submit(self.__run_clingo, i, num_traces, num_events, freq, asp, trace_type)
                    self.parallel_futures.append(future)
        else:
            for i in range(num_traces):
                self.py_logger.debug(f" Generating trace:{i + 1}/{num_traces} with events:{num_events})")
                self.__run_clingo(i, num_traces, num_events, freq, asp, trace_type)

    def __run_clingo(self, i, num_traces, num_events, freq, asp, trace_type):
        self.clingo_current_output = None
        seed = randrange(0, 2 ** 32 - 1)
        self.py_logger.debug(f" Initializing clingo trace({i + 1}/{num_traces}) with length:{num_events}), Seed {seed}")
        ctl = clingo.Control([
            "-c",
            f"t={int(num_events)}",
            f"1",
            "--project",
            "--sign-def=rnd",
            f"--rand-freq={freq}",
            f"--restart-on-model",
            # f"--seed=8794",
            f"--seed={seed}",
        ])
        ctl.add(asp)
        ctl.add(self.asp_encoding)
        ctl.add(self.asp_template)
        ctl.ground([("base", [])], context=self)  # ctl.ground()
        result = ctl.solve(on_model=self.__handle_clingo_result)
        self.py_logger.debug(f" Clingo Result: {str(result)}")

        # ctl.ground([("base", [])], context=self)
        # # ctl.ground()
        # # result = ctl.solve(on_model=self.__handle_clingo_result, yield_=True)
        # result = ctl.solve(on_model=self.__handle_clingo_result)

        if result.unsatisfiable:
            """
                Clingo was not able to generate trace events with exactly num_events, thus it returns
                unsatisfiable.
            """
            warnings.warn(
                f'WARNING: Cannot generate {num_traces} {trace_type} trace/s exactly with {num_events} events with this Declare model.')
            return  # we exit because we cannot generate more traces with same params.
        elif self.num_repetition_per_trace > 0:
            self.trace_counter = self.trace_counter + 1
            self.clingo_output_traces_variation[
                len(self.clingo_output_traces_variation)] = []  # to generate the name of variation trace
            num = self.num_repetition_per_trace - 1
            if num > 0 and self.clingo_current_output is not None:
                c = ASPResultTraceModel(f"variation_{i}_trace_{self.trace_counter}", self.clingo_current_output)
                asp_variation = asp + "\n"
                for ev in c.events:
                    asp_variation = asp_variation + f"trace({ev.name}, {ev.pos}).\n"
                for nm in range(0, num):
                    self.__generate_asp_trace_variation(asp_variation, num_events, 1, freq)

    def __generate_asp_trace_variation(self, asp: str, num_events: int, num_traces: int, freq: float = 0.9):
        """
        Generate variation traces based on the parameters
        Parameters
        ----------
        asp: str
            asp model program
        num_events: int
            number of events in a trace
        num_traces: int
            number of traces
        freq: float
            any float number between 0 to 1

        Returns
        -------

        """
        """ """
        if self.run_parallel:
            with concurrent.futures.ThreadPoolExecutor(max_workers=self.parallel_workers) as executor:
                for i in range(num_traces):
                    future = executor.submit(self.__run_clingo_trace_variation, asp, num_events, num_traces, freq)
                    self.parallel_futures.append(future)
        else:
            for i in range(num_traces):
                self.__run_clingo_trace_variation(asp, num_events, num_traces, freq)

    def __run_clingo_trace_variation(self, asp: str, num_events: int, num_traces: int, freq: float = 0.9):
        """
        Generate variation traces based on the parameters
        Parameters
        ----------
        asp: str
            asp model program
        num_events: int
            number of events in a trace
        num_traces: int
            number of traces
        freq: float
            any float number between 0 to 1

        Returns
        -------

        """
        """ """
        # "--project --sign-def=3 --rand-freq=0.9 --restart-on-model --seed=" + seed
        seed = randrange(0, 2 ** 30 - 1)
        self.py_logger.debug(f" Generating variation trace: {num_traces}, events{num_events}, seed:{seed}")
        ctl = clingo.Control([f"-c t={int(num_events)}", "--project", f"1",  # f"{int(num_traces)}",
                              f"--seed={seed}", f"--sign-def=rnd", f"--restart-on-model", f"--rand-freq={freq}"])
        ctl.add(asp)
        ctl.add(self.asp_encoding)
        ctl.add(self.asp_template)
        ctl.ground([("base", [])], context=self)
        result = ctl.solve(on_model=self.__handle_clingo_variation_result)
        self.py_logger.debug(f" Clingo variation Result :{str(result)}")
        if result.unsatisfiable:
            warnings.warn(f'WARNING: Failed to generate trace variation/case.')

    def __handle_clingo_result(self, output: clingo.solving.Model):
        """A callback method which is given to the clingo """
        symbols = output.symbols(shown=True)
        self.clingo_current_output = symbols
        self.py_logger.debug(f" Traces generated :{symbols}")
        self.clingo_output.append(symbols)

    def __resolve_clingo_results(self, results: LogTracesType):
        """Resolve clingo produced result in customized structured
        Parameters
        ----------
        results: LogTracesType
            An object containing information about the generated traces/ solution model but to be parsed
        Returns
        -------

        """
        self.asp_generated_traces = LogTracesType(positive=[], negative=[])
        i = 0
        for result in results:  # result value can be 'negative' or 'positive'
            asp_model = []
            for clingo_trace in results[result]:
                trace_model = ASPResultTraceModel(f"case_{i}", clingo_trace)
                asp_model.append(trace_model)
                i = i + 1
            self.asp_generated_traces[result] = asp_model

    def __resolve_clingo_results_variation(self, variations_result: LogTracesType):
        """Resolve clingo produced variations result in particular structured """
        if self.asp_generated_traces is None:
            self.asp_generated_traces = LogTracesType(positive=[], negative=[])
        for result in variations_result:  # result value can be 'negative' or 'positive'
            asp_model = []
            for traces_key_id in variations_result[result]:
                i = 0
                for clingo_trace in variations_result[result][traces_key_id]:
                    trace_model = ASPResultTraceModel(f"case_{traces_key_id}_variation_{i}", clingo_trace)
                    asp_model.append(trace_model)
                    i = i + 1
            self.asp_generated_traces[result] = self.asp_generated_traces[result] + asp_model

    def __handle_clingo_variation_result(self, output: clingo.solving.Model):
        """A callback method which is given to the clingo """
        symbols = output.symbols(shown=True)
        self.py_logger.debug(f" Variation traces generated :{symbols}")
        self.clingo_output_traces_variation[len(self.clingo_output_traces_variation) - 1].append(symbols)

    def __pm4py_log(self):
        """
        Generate event logs in pm4py Format
        Returns
        -------

        """
        self.py_logger.debug(f"Generating Pm4py log")
        decl_model: DeclareParsedDataModel = self.process_model.parsed_model
        attr_list: dict[str, DeclareModelAttr] = decl_model.attributes_list
        tot_traces_generated = 0
        flattened = {}
        for result in self.asp_generated_traces:
            tot_traces_generated = tot_traces_generated + len(self.asp_generated_traces[result])
            traces_generated = self.asp_generated_traces[result]
            # traces_generated.sort(key=lambda x: x.name)
            traces_generated = sorted(traces_generated, key=custom_sort_trace_key)
            instance = []
            for trace in traces_generated:  # Positive, Negative...
                _instance = {"trace_name": trace.name, "posNeg": result, "events": []}
                for trace_position in trace.parsed_result:
                    asp_event = trace.parsed_result[trace_position]
                    _event = {}
                    _event = {
                        "ev": decl_model.decode_value(asp_event['name'], self.encode_decl_model),
                        "lifecycle:transition": "complete",
                        "resources": [],
                        "time:timestamp": datetime.now() + timedelta(hours=trace_position, seconds=random.randint(0, 3599))
                    }
                    _instance["events"].append(_event)
                    for res in asp_event['resources']:
                        res_name, res_value = list(res.items())[0]
                        #res_name_decoded = decl_model.decode_value(res_name, self.encode_decl_model)
                        #res_value_decoded = decl_model.decode_value(res_value, self.encode_decl_model)
                        res_name_decoded, res_value_decoded = self.__decode_and_scale_value(decl_model, attr_list, res_name, res_value)
                        _event["resources"].append({res_name_decoded: res_value_decoded})
                instance.append(_instance)
            flattened[result] = instance
        self.traces_generated_events = flattened
        if tot_traces_generated != self.log_length:
            num = self.num_repetition_per_trace
            if num <= 0:
                num = 1
            self.py_logger.warning(f'PM4PY log generated: {tot_traces_generated}/{self.log_length * num} only.')
        self.py_logger.debug(f"Pm4py generated but not saved yet")

    def __decode_and_scale_value(self, decl_model, attr_list, res_name, res_value):
        res_name_decoded = decl_model.decode_value(res_name, self.encode_decl_model)
        res_value_decoded = decl_model.decode_value(res_value, self.encode_decl_model)
        res_value_decoded = str(res_value_decoded)
        is_number = re.match(r"[+-]?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?", res_value_decoded)
        if is_number:
            if res_name_decoded in attr_list:
                attr = attr_list[res_name_decoded]
                if attr.value_type != DeclareModelAttributeType.ENUMERATION:
                    num = res_value_decoded
                    if attr.value_type == DeclareModelAttributeType.FLOAT_RANGE:
                        num = int(res_value_decoded) / attr.attr_value.precision
                    elif attr.value_type == DeclareModelAttributeType.INTEGER_RANGE:
                        num = int(res_value_decoded)
                    res_value_decoded = num
        if isinstance(res_value_decoded, str):
            return res_name_decoded, res_value_decoded.strip()
        else:
            return res_name_decoded, res_value_decoded

    def toPD(self, data) -> pd.DataFrame:
        activities = []
        for trace_type in data:
            for trace in data[trace_type]:
                trace_id = trace["trace_name"]
                for id_ev, event in enumerate(trace["events"]):
                    traceEvent = {
                        "case:concept:name": f'{trace_id}',
                        "time:timestamp": datetime.now() + timedelta(hours=id_ev, seconds=random.randint(0, 3599)), #.isoformat(),
                        # "date": datetime.now(),
                        "lifecycle:transition": event["lifecycle:transition"],
                        "concept:name": event["ev"],
                        "case:label": trace_type,  # "complete",
                    }
                    for res in event["resources"]:
                        for k, v in res.items():
                            traceEvent[k] = v
                    activities.append(traceEvent)
        df = pd.DataFrame(activities)
        return df

    def to_xes(self, output_fn: str):
        """
        Save log in xes file
        Parameters
        ----------
        output_fn: str
            filename
        Returns
        -------

        """
        if self.traces_generated_events is None or len(self.traces_generated_events) == 0:
            self.__pm4py_log()
        if len(self.traces_generated_events['positive']) == 0 and len(self.traces_generated_events['negative']) == 0:
            warnings.warn("Unable to produce the logs with given model and parameters set for it.")
            return
        pd_dataframe = self.toPD(self.traces_generated_events)
        pd_dataframe.dropna(axis='columns', how='all')
        pm4py.write_xes(pd_dataframe, output_fn)
        # ## Following code is to clean the NaN values not yet tested if it removes all event or just an attribute ##
        # xes = pm4py.read_xes(output_fn)
        # float_or_int_cols = pd_dataframe.select_dtypes(include=['float64', 'int64']).columns.tolist()
        # cols_with_nan = pd_dataframe[float_or_int_cols].columns[pd_dataframe[float_or_int_cols].isna().any()].tolist()
        # rows_to_remove = xes[cols_with_nan].isna().any(axis=1)
        # cleaned_xes = xes[~rows_to_remove]
        # pm4py.write_xes(cleaned_xes, output_fn)


    def set_constraints_to_violate(self, tot_negative_trace: int, violate_all: bool, constraints_list: list[str]):
        """
        Add constraints to violate

        Parameters
        ----------
        tot_negative_trace: int
            total negative traces to generate
        violate_all: bool
            whether to violate all the given constraint templates or let decide clingo
        constraints_list: list
            the list of the constraint templates which have to be violated.
        Returns
        -------
            declare_model_violate_constraints
        """
        assert tot_negative_trace >= 0
        self.negative_traces = tot_negative_trace
        self.violate_all_constraints = violate_all
        self.add_constraints_to_violate(constraints_list)

    def set_constraints_to_violate_by_template_index(self, tot_negative_trace: int, violate_all: bool,
                                                     constraints_idx_list: list[int]):
        """
        Add constraints to violate

        Parameters
        ----------
        tot_negative_trace: int
            the number of total negative traces to generate. Cannot be greater than the Total traces len
        violate_all: bool
            whether all constraints should be violated or some of them (decided by clingo using && op)
        constraints_idx_list: list
            an integer list indicating the indexing of constraint templates

        Returns
        -------
        """
        templates: dict[int, DeclareModelConstraintTemplate] = self.process_model.parsed_model.templates
        constraints_list = []
        for idx in constraints_idx_list:
            constraints_list.append(templates[idx].line)
        self.set_constraints_to_violate(tot_negative_trace, violate_all, constraints_list)

    def set_number_of_repetition_per_trace(self, repetition: int):
        """
        Example: 4(number of traces) Traces with 8(repetition) repetition. Suppose we have generated 4 traces as following:
        - A B E D
        - C D A F
        - E D C A
        - B A C E
        and then for each of these trace we generate other 7 traces.
        We want clusters of traces where each cluster contains traces with the same order of events but different payload (resources or time)
        -------
        Parameters
        repetition: int
            number of repetition for each trace.
        """
        self.num_repetition_per_trace = repetition

    def __get_decl_model_with_violate_constraint(self) -> DeclareModel:
        """
        Creates a duplicate process model with change in template list, assigning a boolean value to `violate` property

        Returns
        -------
        DeclModel
        """
        parsed_tmpl: dict[int, DeclareModelConstraintTemplate] = self.process_model.parsed_model.templates
        for cv in self.violatable_constraints:
            for tmpl_idx, tmpl in parsed_tmpl.items():
                if tmpl.line == cv:
                    tmpl.violate = True
        return self.process_model

    def set_activation_conditions(self, activations_list: dict[str, list[int]]):
        """
        the activation conditions are used TODO: add more info about it.
        TODO: this method should be in the ASPLogGeneration generator rather than abstract class and also self.activation_conditions.

        Parameters
        ----------
        : param activations_list dict: accepts a dictionary with key as a string which represent a declare model
            constraint template, and value as an list with number values.
            i.e 'Response[A,B] | A.attribute is value1 | |': [3, 5].
            Here key represents a constraint template and the number list represents how many times activation key of
            that constraint template should be occurred. In this example we are saying, that it should at least 3 times
            and at most 5 times.
            the value must be a list of 2 integer which represents the bounding limits of activation. You can add math.inf
            as the 2 second element. First element should be greater or equal than 0.

        Returns
        -------

        """
        self.activation_conditions = activations_list
        return self

    def set_activation_conditions_by_template_index(self, activations_list: dict[int, list[int]]):
        """
        we want to specify rules for the activations, that is a number for the activation events.

        Parameters
        ----------
        : param activations_list dict: accepts a dictionary with key as a string which represent a declare model
            constraint template, and value as an list with number values.
            i.e 'Response[A,B] | A.attribute is value1 | |': [3, 5].
            Here key represents a constraint template and the number list represents how many times activation key of
            that constraint template should be occurred. In this example we are saying, that it should at least 3 times
            and at most 5 times.

        Returns
        -------

        """
        # indexes = activations_list.keys()  # indexes of constraint templates
        templates = self.process_model.parsed_model.templates
        n_dict = {}
        for m, n in activations_list.items():
            n_dict[templates[m].line] = n
        self.activation_conditions = n_dict
        return self

    def compute_distribution(self, total_traces: typing.Union[int, None] = None):
        """
         The compute_distribution method computes the distribution of the number of events in a trace based on
         the distributor_type parameter. If the distributor_type is "gaussian", it uses the loc and scale parameters
         to compute a Gaussian distribution. Otherwise, it uses a uniform or custom distribution.+

         Parameters
         total_traces: int, optional
            the number of traces
        """
        self.py_logger.info("Computing distribution")
        d = Distributor()
        if total_traces is None:
            total_traces = self.log_length
        traces_len = {}
        if total_traces == 0:
            return collections.Counter()
        if self.distributor_type == "gaussian":
            self.py_logger.info(f"Computing gaussian distribution with mu={self.loc} and sigma={self.scale}")
            assert self.loc > 1  # Mu atleast should be 2
            assert self.scale >= 0  # standard deviation must be a positive value
            result: typing.Union[collections.Counter, None] = d.distribution(
                self.loc, self.scale, total_traces, self.distributor_type, self.custom_probabilities)
            self.py_logger.info(f"Gaussian distribution result {result}")
            if result is None or len(result) == 0:
                raise ValueError("Unable to found the number of traces with events to produce in log.")
            for k, v in result.items():
                if self.min_events <= k <= self.max_events:  # TODO: ask whether the boundaries should be included
                    traces_len[k] = v
            self.py_logger.info(f"Gaussian distribution after refinement {traces_len}")
        else:
            traces_len: typing.Union[collections.Counter, None] = d.distribution(self.min_events, self.max_events,
                                                                                 total_traces,
                                                                                 self.distributor_type,
                                                                                 self.custom_probabilities)
        self.py_logger.info(f"Distribution result {traces_len}")
        self.traces_length = traces_len
        return traces_len

    def set_custom_trace_lengths(self, custom_lengths: dict[int, int],
                                 negative_custom_lengths: dict[int, int] | None = None):
        """
        Set custom traces lengths in order to generate positive and negative traces instead of
        using the distributions
        
        the key in the dict represents the number of events in a trace and value in the dict represents the number of traces with event
        i.e {68: 2} means that you want 2 traces with 68 events in each.
        
        Parameters
        ----------
        custom_lengths: dict[int, int]
            traces length for positive traces
        negative_custom_lengths: dict[int, int], Optional
            traces length for negative traces
        Returns
        -------
        """

        if custom_lengths and len(custom_lengths) > 0:
            # self.traces_length = len(custom_lengths)
            self.traces_length = sum(custom_lengths.values())
            events = custom_lengths.keys()
            self.min_events = min(events)
            self.max_events = max(events)

        if negative_custom_lengths:
            if len(negative_custom_lengths) > 0:
                events = negative_custom_lengths.keys()
                self.min_events = min(events, self.min_events)
                self.max_events = max(events, self.max_events)
            self.negative_traces = sum(negative_custom_lengths.values())
            self.traces_length = self.traces_length + self.negative_traces
        self.py_logger.info(f"****----**** Trace lengths, min_events, max_events are updated ****----****")
        self.py_logger.info(
            f"**--** Pos: {self.traces_length}, Neg: {self.negative_traces}, min {self.min_events}, max: {self.max_events} **--**")
        self._custom_counter = {"positive": custom_lengths, "negative": negative_custom_lengths}

    def set_distribution(self, distributor_type: typing.Literal["uniform", "gaussian", "custom"] = "uniform",
                         custom_probabilities: typing.Optional[typing.List[float]] = None,
                         loc: float = None, scale: float = None):
        """
        We specify rules regarding the length of a trace that spans between a minimum and a maximum.
         This span is set according to a uniform, gaussian or custom distribution.

        Parameters
        ----------
        distributor_type: str
            "uniform", "gaussian", "custom"
        custom_probabilities: list, optional
            it must be used when custom distribution is chosen
        loc: float
            used for gaussian/normal distribution
        scale: float
            used for gaussian/normal distribution

        Returns
        -------

        """
        self.distributor_type = distributor_type
        self.custom_probabilities = custom_probabilities
        self.scale = scale
        self.loc = loc
