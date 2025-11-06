# Generic Utils
# static methods
import re


class Utils:
    @staticmethod
    def parse_activity(act: str) -> str:
        """
        This function takes a string, containing numbers, as parameter and returns a copy of it.
        The copy contains characters instead of numbers and whitespaces between words in the string are removed.

        Args:
            act: string containing decimals numbers

        Returns:
            modified string with numbers changed to lower case characters
        """
        int_char_map = {0: 'a', 1: 'b', 2: 'c', 3: 'd', 4: 'e', 5: 'f', 6: 'g', 7: 'h', 8: 'i', 9: 'l', "->": "-> ",
                        "&&": " && ", "||": "|| ", ":": "_"}
        for int_key in int_char_map.keys():
            act = act.replace(str(int_key), int_char_map[int_key])
        act = re.sub(r"\s\b", "", act)
        return act

    @staticmethod
    def parse_parenthesis(act: str) -> str:
        """
        This function takes a string, containing numbers, as parameter and returns a copy of it.
        The copy contains characters instead of numbers and whitespaces between words in the string are removed.

        Args:
            act: string containing decimals numbers

        Returns:
            modified string with numbers changed to lower case characters
        """
        int_char_map = {"(": "_", ")": "_", "[": "_", "]": "_", "{": "_", "}": "_"}
        for int_key in int_char_map.keys():
            act = act.replace(str(int_key), int_char_map[int_key])
        act = re.sub(r"\s\b", "", act)
        return act

    @staticmethod
    def normalize_formula(input_formula: str) -> str:

        unary_operators = {"g(": "G(", "x(": "X(", "f(": "F(", "x[!](": "X[!]("}

        binary_operators = {" u ": " U ", "u(": "U(", " r ": " R ", "r(": "R(", " w ": " W ",
                            "w(": "W(", " m ": " M ", "m(": "M(", " v ": " V ", "v(": "V("}

        formula = Utils.parse_activity(input_formula)
        formula = formula.lower()

        for key, value in unary_operators.items():
            formula = formula.replace(key, value)

        for key, value in binary_operators.items():
            formula = formula.replace(key, value)

        return formula

    @staticmethod
    def encode_attribute_type(attr_type: str) -> str:
        encoding = ""
        for i in range(3):
            encoding += attr_type[i]
        return encoding
