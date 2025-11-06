from __future__ import annotations

from abc import ABC

from Declare4Py.ProcessMiningTasks.AbstractPMTask import AbstractPMTask
from Declare4Py.D4PyEventLog import D4PyEventLog
from Declare4Py.ProcessModels.AbstractModel import ProcessModel

"""
Initializes class Discovery, inheriting from class PMTask

Parameters
-------
    PMTask
        inheriting from PMTask
Attributes
-------
    consider_vacuity : bool
        True means that vacuously satisfied traces are considered as satisfied, violated otherwise.
        
    support : float
        the support that a discovered constraint needs to have to be included in the filtered result.

    max_declare_cardinality : int, optional
        the maximum cardinality that the algorithm checks for DECLARE templates supporting it (default 3).
"""


class AbstractDiscovery(AbstractPMTask, ABC):

    def __init__(self, log: D4PyEventLog, process_model: ProcessModel, min_support: float = 0.1):
        super().__init__(log, process_model)
        self.min_support: float = min_support
