from __future__ import annotations

from abc import ABC

from Declare4Py.ProcessMiningTasks.AbstractPMTask import AbstractPMTask
from Declare4Py.D4PyEventLog import D4PyEventLog
from Declare4Py.ProcessModels.AbstractModel import ProcessModel

"""
Initializes class Monitoring, inheriting from class PMTask

Parameters
-------
    PMTask
        inheriting from PMTask
"""


class AbstractMonitoring(AbstractPMTask, ABC):

    def __init__(self, log: D4PyEventLog, process_model: ProcessModel):
        super().__init__(log, process_model)
