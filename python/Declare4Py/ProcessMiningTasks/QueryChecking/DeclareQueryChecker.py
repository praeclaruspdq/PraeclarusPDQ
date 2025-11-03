from __future__ import annotations

import pdb
import re
from abc import ABC
from typing import Optional
from Declare4Py.D4PyEventLog import D4PyEventLog
from Declare4Py.ProcessMiningTasks.AbstractQueryChecking import AbstractQueryChecking
from Declare4Py.ProcessMiningTasks.QueryChecking.DeclareResultsBrowser import DeclareResultsBrowser
from Declare4Py.ProcessModels.DeclareModel import DeclareModel, DeclareModelTemplate
from Declare4Py.Utils.Declare.Checkers import ConstraintChecker

"""
Initializes class QueryCheckingResults

Attributes
--------
    dict_results : dict
        dictionary of conformance checking results

"""





"""
Class used to provide basic query checking functionalities

Parameters
--------
    QueryChecking
        inherits attributes from the QueryChecking class

Attributes
--------
    QueryChecking init
        inherits init attributes form the QueryChecking class
        
    basic_query_checking_results : dict
        output type for this class
        
        
"""


class DeclareQueryChecker(AbstractQueryChecking, ABC):

    def __init__(self, log: D4PyEventLog, template: Optional[str] = None,
                 activation: Optional[str] = None, target: Optional[str] = None,
                 activation_condition: Optional[str] = None, target_condition: Optional[str] = None,
                 time_condition: Optional[str] = None, min_support: float = 0.1, consider_vacuity: bool = False,
                 max_declare_cardinality: int = 1, return_first: bool = False):
        super().__init__(log, DeclareModel(), min_support, return_first)
        self.consider_vacuity = consider_vacuity
        self.template: Optional[str] = template
        self.activation: Optional[str] = activation
        self.target: Optional[str] = target
        self.activation_condition: Optional[str] = activation_condition if activation_condition is not None else ""
        self.target_condition: Optional[str] = target_condition if target_condition is not None else ""
        self.time_condition: Optional[str] = time_condition if time_condition is not None else ""
        self.max_declare_cardinality: int = max_declare_cardinality

    def run(self) -> DeclareResultsBrowser:
        """
        Performs query checking for a (list of) template, activation activity and target activity. Optional
        activation, target and time conditions can be specified.

        Parameters
        ----------
        consider_vacuity : bool
            True means that vacuously satisfied traces are considered as satisfied, violated otherwise.

        template_str : str, optional
            if specified, the query checking is restricted on this DECLARE template. If not, the query checking is
            performed over the whole set of supported templates.

        max_declare_cardinality : int, optional
            the maximum cardinality that the algorithm checks for DECLARE templates supporting it (default 1).

        activation : str, optional
            if specified, the query checking is restricted on this activation activity. If not, the query checking
            considers in turn each activity of the log as activation.

        target : str, optional
            if specified, the query checking is restricted on this target activity. If not, the query checking
            considers in turn each activity of the log as target.

        act_cond : str, optional
            activation condition to evaluate. It has to be written by following the DECLARE standard format.

        trg_cond : str, optional
            target condition to evaluate. It has to be written by following the DECLARE standard format.

        time_cond : str, optional
            time condition to evaluate. It has to be written by following the DECLARE standard format.

        min_support : float, optional
            the minimum support that a constraint needs to have to be included in the result (default 1).

        Returns
        -------
        basic_query_checking_results
            dictionary with keys the DECLARE constraints satisfying the assignments. The values are a structured
            representations of these constraints.
        """

        is_template_given = bool(self.template)
        is_activation_given = bool(self.activation)
        is_target_given = bool(self.target)

        if not is_template_given and not is_activation_given and not is_target_given:
            raise RuntimeError("You must set at least one parameter among (template, activation, target).")
        if is_template_given:
            template = DeclareModelTemplate.get_template_from_string(self.template)
            if template is None:
                raise RuntimeError("You must insert a supported DECLARE template.")
            if not template.is_binary and is_target_given:
                raise RuntimeError("You cannot specify a target activity for unary templates.")
        if not 0 <= self.min_support <= 1:
            raise RuntimeError("Min. support must be in range [0, 1].")
        if self.max_declare_cardinality <= 0:
            raise RuntimeError("Cardinality must be greater than 0.")
        if self.event_log is None:
            raise RuntimeError("You must load a log before.")

        templates_to_check = list()
        if is_template_given:
            templates_to_check.append(self.template)
        else:
            # templates_to_check = DeclareModelTemplate.get_binary_not_shortcut_templates()
            templates_to_check += list(map(lambda t: t.templ_str, DeclareModelTemplate.get_binary_not_shortcut_templates()))
            # templates_to_check += list(map(lambda t: t.templ_str, DeclareModelTemplate.get_binary_templates()))
            if not is_target_given:
                for template in DeclareModelTemplate.get_unary_templates():
                    if template.supports_cardinality:
                        for card in range(self.max_declare_cardinality):
                            templates_to_check.append(template.templ_str + str(card + 1))
                    else:
                        templates_to_check.append(template.templ_str)

        activations_to_check = self.event_log.get_event_attribute_values(self.event_log.activity_key) \
            if self.activation is None else [self.activation]
        if not isinstance(activations_to_check, list):
            activations_to_check = activations_to_check.keys()

        targets_to_check = self.event_log.get_event_attribute_values(self.event_log.activity_key) \
            if self.target is None else [self.target]
        if not isinstance(targets_to_check, list):
            activations_to_check = targets_to_check.keys()

        activity_combos = []
        for activation in activations_to_check:
            for target in targets_to_check:
                if activation != target:
                    activity_combos.append((activation, target))

        # activity_combos = tuple(filter(lambda c: c[0] != c[1], product(activations_to_check, targets_to_check)))
        query_checker_results = []
        for template_str in templates_to_check:
            template_str, cardinality = re.search(r'(^.+?)(\d*$)', template_str).groups()
            template = DeclareModelTemplate.get_template_from_string(template_str)

            constraint = {"template": template}
            if cardinality:
                constraint['n'] = int(cardinality)

            if template.is_binary:
                constraint['condition'] = (self.activation_condition, self.target_condition, self.time_condition)
                for couple in activity_combos:
                    # constraint['activities'] = ', '.join(couple)
                    constraint['activities'] = couple

                    # constraint_str = self.constraint_checking_with_support(constraint)
                    constraint_satisfaction = ConstraintChecker().constraint_checking_with_support(constraint,
                                                                                                   self.event_log,
                                                                                                   self.consider_vacuity,
                                                                                                   self.min_support)
                    if constraint_satisfaction:
                        # res_value = {
                        #    "template": template_str, "activation": couple[0], "target": couple[1],
                        #    "activation_condition": self.activation_condition, "target_condition": self.target_condition,
                        #    "time_condition": self.time_condition
                        # }
                        # self.basic_query_checking_results[constraint_str] = res_value
                        query_checker_results.append([template_str, couple[0], couple[1], self.activation_condition,
                                                         self.target_condition, self.time_condition])
                        if self.return_first:
                            return DeclareResultsBrowser(query_checker_results)

            else:  # unary template
                constraint['condition'] = (self.activation_condition, self.time_condition)
                for activity in activations_to_check:
                    constraint['activities'] = activity

                    # constraint_str = self.constraint_checking_with_support(constraint)
                    constraint_satisfaction = ConstraintChecker().constraint_checking_with_support(constraint,
                                                                                                   self.event_log,
                                                                                                   self.consider_vacuity,
                                                                                                   self. min_support)

                    if constraint_satisfaction:
                        query_checker_results.append([template_str, activity, None, self.activation_condition, None,
                                                      self.time_condition])
                        if self.return_first:
                            return DeclareResultsBrowser(query_checker_results)
                        # res_value = {
                        #    "template": template_str, "activation": activity,
                        #    "activation_condition": self.activation_condition, "time_condition": self.time_condition
                        # }
                        # self.basic_query_checking_results[constraint_str] = res_value

        return DeclareResultsBrowser(query_checker_results)




