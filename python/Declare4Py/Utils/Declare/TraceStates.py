from enum import Enum


class TraceState(str, Enum):
    VIOLATED = "Violated"
    SATISFIED = "Satisfied"
    POSSIBLY_VIOLATED = "Possibly Violated"
    POSSIBLY_SATISFIED = "Possibly Satisfied"

