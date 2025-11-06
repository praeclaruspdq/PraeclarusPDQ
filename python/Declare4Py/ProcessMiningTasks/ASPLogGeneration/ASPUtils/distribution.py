from __future__ import annotations

import collections
import fractions
import logging
import typing
from typing import *

import numpy as np


class Distributor:
    """
    A class for generating trace lengths according to different distributions.
    """

    def __init__(self):
        self.__logger = logging.getLogger("Distributor")

    def custom_distribution(self, min_num: int, max_num: int, traces_num: int, probabilities: [float]):
        """
        Generates trace lengths according to a custom distribution specified by the `probabilities` list.

        Args:
        - min_num: The minimum trace length.
        - max_num: The maximum trace length.
        - traces_num: The number of traces to generate.
        - probabilities: A list of probabilities for each trace length from `min_num` to `max_num`.
         The list must have a length equal to `max_num - min_num + 1`, and the sum of the probabilities must be 1.

        Returns:
        A `collections.Counter` object containing the count of each trace length generated.

        Raises:
        - ValueError: If `probabilities` is not provided or if the sum of the probabilities is not 1.
        - ValueError: If the number of probabilities provided is not equal to the difference between `max_num` and `min_num`.
        """

        self.__logger.debug(f"Custom_dist() min_mu:{min_num} max_sigma:{max_num} num_traces:{traces_num}")
        if probabilities is None:
            raise ValueError(" custom probabilities must be provided")
        s = sum(probabilities)
        self.__logger.debug(f"Probabilities sum {s}")
        if s != 1:
            raise ValueError(f"Sum of provided list must be 1 but found {s}")
        prob_len = len(probabilities)
        prefixes = (max_num + 1) - min_num
        if prob_len != prefixes:
            raise ValueError(
                f"Number of probabilities provided are {prob_len} but min and max difference is {prefixes}")
        return self.__distribute_random_choices(min_num, max_num, traces_num, probabilities)

    def uniform_distribution(self, min_num, max_num, traces_num: int):
        """
        Generates trace lengths according to a uniform distribution.

        Args:
        - min_num: The minimum trace length.
        - max_num: The maximum trace length.
        - traces_num: The number of traces to generate.

        Returns:
        A `collections.Counter` object containing the count of each trace length generated.
        """
        probabilities = self.__get_uniform_probabilities((max_num + 1) - min_num)
        self.__logger.debug(f"Uniform() probabilities: {probabilities}")
        return self.custom_distribution(min_num, max_num, traces_num, probabilities)

    def normal_distribution(self, mu, sigma, num_traces: int):
        """
        Generates trace lengths according to a normal (Gaussian) distribution.

        Args:
        - mu: The mean of the distribution.
        - sigma: The standard deviation of the distribution.
        - num_traces: The number of traces to generate.

        Returns:
        A `collections.Counter` object containing the count of each trace length generated.

        Notes:
        - Trace lengths less than 1 are not included in the output.
        """
        trace_lens = np.random.normal(mu, sigma, num_traces)
        trace_lens = np.round(trace_lens)
        trace_lens = trace_lens[trace_lens > 1]
        c = collections.Counter(trace_lens)
        return c

    def __get_uniform_probabilities(self, num_probabilities: int):
        """
        Generates a list of uniform probabilities for the given number of probabilities.

        Args:
        - num_probabilities: The number of probabilities to generate.

        Returns:
        A list of uniform probabilities.
        """
        # return [1 / num_probabilities for p in range(0, num_probabilities)]
        return [fractions.Fraction(1, num_probabilities) for i in range(0, num_probabilities)]

    def __distribute_random_choices(self, min_num, max_num, traces_num, probabilities: [float]):
        """
        Generates trace lengths according to a custom distribution specified by the `probabilities` list, using the `numpy.random.choice()` function.

        Args:
        - min_num: The minimum trace length.
        - max_num: The maximum trace length.
        - traces_num: The number of traces to generate.
        - probabilities: A list of probabilities for each trace length from `min_num` to `max_num`. The list must have a length equal to `max_num - min_num + 1`, and the sum of the probabilities must be 1.

        Returns:
        A `collections.Counter` object containing the count of each trace length generated.
        """
        prefixes = range(min_num, max_num + 1)
        trace_lens = np.random.choice(prefixes, traces_num, p=probabilities)
        self.__logger.debug(f"Distribution result: {trace_lens}")
        c = collections.Counter(trace_lens)
        return c

    def distribution(
            self,
            min_num_events_or_mu: typing.Union[int, float],
            max_num_events_or_sigma: typing.Union[int, float],
            num_traces: typing.Union[int, float],
            dist_type: Literal["uniform", "gaussian", "custom"] = "uniform",
            custom_probabilities: Optional[List[float]] = None):
        """
        Generates trace lengths according to the specified distribution.

        Args:
        - min_num_events_or_mu: The minimum trace length for uniform distributions, or the mean of the distribution for normal distributions.
        - max_num_events_or_sigma: The maximum trace length for uniform distributions, or the standard deviation of the distribution for normal distributions.
        - num_traces: The number of traces to generate.
        - dist_type: The type of distribution to use. Can be "uniform", "gaussian", or "custom". Default is "uniform".
        - custom_probabilities: A list of custom probabilities to use for the "custom" distribution type.

        Returns:
        - For "uniform" and "custom" distribution types, a `collections.Counter` object containing the count of each trace length generated.
        - For "gaussian" distribution type, an integer representing the total number of traces generated.

        Raises:
        - AttributeError: If `dist_type` is not one of the supported distribution types.
        """
        self.__logger.debug(f"Distribution() {dist_type} min_mu: {min_num_events_or_mu}"
                            f" max_sigma: {max_num_events_or_sigma} num_traces: {num_traces}"
                            f" custom_prob: {custom_probabilities}")
        if dist_type == "gaussian":
            return self.normal_distribution(min_num_events_or_mu, max_num_events_or_sigma, num_traces)
        elif dist_type == "uniform":
            return self.uniform_distribution(min_num_events_or_mu, max_num_events_or_sigma, num_traces)
        elif dist_type == "custom":
            return self.custom_distribution(min_num_events_or_mu, max_num_events_or_sigma, num_traces,
                                            custom_probabilities)
        else:
            raise AttributeError(f"Specified type of distribution {dist_type} not supported yet.")


# if __name__ == "__main__":
#     # print(distribution(2, 4, "uniform", num_traces=100))
#     # print(normal_distribution(1.5, 0.15, 1000))
#     # print(distribution(2, 10, "normal"))
#     d = Distributor()
#     print(d.uniform_distribution(2, 4, 10))
#     print(d.custom_distribution(2, 4, 10, [0.3333333333333333, 0.3333333333333333, 0.3333333333333333]))
#     print(d.normal_distribution(3, 4, 10))
