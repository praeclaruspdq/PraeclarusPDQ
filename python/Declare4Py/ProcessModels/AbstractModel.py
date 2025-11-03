from __future__ import annotations
from abc import abstractmethod
from typing import TypeVar, Generic, List

T = TypeVar("T")


class ProcessModel(Generic[T]):

    def __init__(self):
        self.activities: List[str] = []

    @abstractmethod
    def parse_from_file(self, model_path: str, **kwargs) -> T:
        pass

    @abstractmethod
    def parse_from_string(self, content: str, **kwargs) -> T:
        pass

    @abstractmethod
    def to_file(self, model_path: str, **kwargs):
        pass

    def get_model_activities(self):
        return self.activities

    #
    # @abstractmethod
    # def parse_model(self) -> T:
    #     pass

