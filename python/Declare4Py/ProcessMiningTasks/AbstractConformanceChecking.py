from __future__ import annotations

from abc import ABC

from Declare4Py.ProcessMiningTasks.AbstractPMTask import AbstractPMTask
from Declare4Py.D4PyEventLog import D4PyEventLog
from Declare4Py.ProcessModels.AbstractModel import ProcessModel

"""

An abstract class for verifying whether the behavior of a given process model, as recorded in a log,
 is in line with some expected behaviors provided in the form of a process model ()

Parameters
-------
    consider_vacuity : bool
        True means that vacuously satisfied traces are considered as satisfied, violated otherwise.

"""


class AbstractConformanceChecking(AbstractPMTask, ABC):

    def __init__(self, log: D4PyEventLog, process_model: ProcessModel):
        super().__init__(log, process_model)

