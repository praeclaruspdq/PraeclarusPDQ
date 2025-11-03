from __future__ import annotations

import logging
import typing
from abc import ABC

from Declare4Py.ProcessMiningTasks.AbstractPMTask import AbstractPMTask
from Declare4Py.ProcessModels.AbstractModel import ProcessModel


"""

An abstract class for log generators.


Parameters
-------
log_length object of type int
PMTask inheriting from PMTask
"""


class LogGenerator(AbstractPMTask, ABC):

    def __init__(self, num_traces: int, min_event: int, max_event: int, p_model: ProcessModel):
        super().__init__(None, p_model)
        if min_event > max_event :
            raise ValueError(f"min_events({min_event}) > max_events({max_event}) not valid! Min events are greater than max events")
        if min_event < 0:
            raise ValueError(f"min_events({min_event}) > max_events({max_event}) not valid!")
        if min_event < 0 or max_event < 0:
            raise ValueError(f"min and max events should be greater than 0!")
        if not isinstance(min_event, int) or not isinstance(max_event, int):
            raise ValueError(f"min_events or/and max_events are not valid!")
        self.__py_logger = logging.getLogger("Log generator")
        self.log_length: int = num_traces
        self.max_events: int = max_event
        self.min_events: int = min_event

        # Distributions Setting
        self.traces_length = {}
        self.distributor_type: typing.Literal["uniform", "gaussian", "custom"] = "uniform"
        self.custom_probabilities: None = None
        self.scale: float = None
        self.loc: float = None

        # Constraint violations
        """
        A trace is positive if it satisfies all three constraints that are defined in this model. Whereas it is
        negative if at least one of them is not satisfied. In the generated log you sent me, in all traces the 
        constraint " Response[Driving_Test, Resit] |A.Grade<=2 | " is not satisfied, i.e. it is violated!
        """
        self.violate_all_constraints: bool = False  # if false: clingo will decide itself the constraints to violate
        self.violatable_constraints: [str] = []  # constraint list which should be violated
        self.negative_traces = 0

        # constraint template conditions
        self.activation_conditions: dict = None

    def add_constraints_to_violate(self, constrains_to_violate: typing.Union[str, list[str]] = True):
        if isinstance(constrains_to_violate, str):
            self.violatable_constraints.append(constrains_to_violate)
        else:
            self.violatable_constraints = constrains_to_violate
        return self

