class ASPEncoding(str):
    """
    A class which create the encoding for the ASP.
    """

    def __init__(self, is_unsat: bool = False):

        self.value: str = "time(1..t). %t = lunghezza traccia\n" \
            "cur_state(I,S,0) :- initial(Name,S),template(I,Name).\n"

        # {trace(A,T) : activity(A)} = 1 :- time(T).
        self.val2 = "{assigned_value(K,V,T) : value(K,V)} = 1 :- trace(A,T), has_attribute(A,K).\n" \
            "cur_state(I,S2,T) :- cur_state(I,S1,T-1), template(I,Name), automaton(Name,S1,c,S2), trace(A,T), not activation(I,A), not target(I,A).\n" \
            "cur_state(I,S2,T) :- cur_state(I,S1,T-1), template(I,Name), automaton(Name,S1,c,S2), trace(A,T), activation(I,A), not activation_condition(I,T).\n" \
            "cur_state(I,S2,T) :- cur_state(I,S1,T-1), template(I,Name), automaton(Name,S1,a,S2), trace(A,T), activation(I,A), activation_condition(I,T).\n" \
            "cur_state(I,S2,T) :- cur_state(I,S1,T-1), template(I,Name), automaton(Name,S1,c,S2), trace(A,T), target(I,A), not correlation_condition(I,T).\n" \
            "cur_state(I,S2,T) :- cur_state(I,S1,T-1), template(I,Name), automaton(Name,S1,b,S2), trace(A,T), target(I,A), correlation_condition(I,T).\n" \
            "sat(I,T) :- cur_state(I,S,T), template(I,Name), accepting(Name,S).\n" \
            # "%:- template(I,_), not sat(I,t).\n"

        self.val3 = ":- sat(I), not sat(I,t)." + "\n" + ":- unsat(I), sat(I,t).\n"""
        if not is_unsat:
            # self.val3 = """ :- template(I,_), not sat(I,t).\n"""
            self.val3 = """ :- sat(I), not sat(I,t). \n"""

        self.val4 = """#show trace/2.\n#show assigned_value/3.\n%#show sat/2.\n"""

    def get_ASP_encoding(self, facts_name: [str] = ["activity"]):
        """
        We need add the facts. The facts name can be anything described in the decl model.
        Parameters
        ----------
        facts_name

        Returns
        -------

        """
        # {trace(A,T) : activity(A)} = 1 :- time(T).
        ls = []
        fact_contains = []
        for n in facts_name:
            if n.lower() not in fact_contains:
                ls.append(f"{{trace(A,T) : {n}(A)}} = 1 :- time(T).")
                fact_contains.append(n.lower())
        return self.value + "\n".join(ls) + "\n" + self.val2 + "\n" + self.val3 + "\n" + self.val4
