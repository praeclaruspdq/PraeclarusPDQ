from __future__ import annotations

import pdb

from Declare4Py.D4PyEventLog import D4PyEventLog
from Declare4Py.ProcessMiningTasks.AbstractConformanceChecking import AbstractConformanceChecking
from Declare4Py.ProcessMiningTasks.ConformanceChecking.MPDeclareResultsBrowser import MPDeclareResultsBrowser
from Declare4Py.ProcessModels.DeclareModel import DeclareModel
from Declare4Py.Utils.Declare.Checkers import ConstraintChecker

"""
Provides basic conformance checking functionalities
"""


class MPDeclareAnalyzer(AbstractConformanceChecking):

    def __init__(self, log: D4PyEventLog, declare_model: DeclareModel, consider_vacuity: bool):
        super().__init__(log, declare_model)
        self.consider_vacuity = consider_vacuity

    def run(self) -> MPDeclareResultsBrowser:
        """
        Performs conformance checking for the provided event log and DECLARE model.

        Parameters
        ----------
        consider_vacuity : bool
            True means that vacuously satisfied traces are considered as satisfied, violated otherwise.

        Returns
        -------
        conformance_checking_results
            dictionary where the key is a list containing trace position inside the log and the trace name, the value is
            a dictionary with keys the names of the constraints and values a CheckerResult object containing
            the number of pendings, activations, violations, fulfillments and the truth value of the trace for that
            constraint.
        """
        if self.event_log is None:
            raise RuntimeError("You must load the log before checking the model.")
        if self.process_model is None:
            raise RuntimeError("You must load the DECLARE model before checking the model.")

        log_checkers_results = []
        for trace in self.event_log.get_log():
            log_checkers_results.append(ConstraintChecker().check_trace_conformance(trace, self.process_model,
                                                                                    self.consider_vacuity,
                                                                                    self.event_log.activity_key))
        return MPDeclareResultsBrowser(log_checkers_results, self.process_model.serialized_constraints)
