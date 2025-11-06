import json
from abc import ABC, abstractmethod

"""

An utility or helper abstract class with some default methods
to print nested dictionaries in Models/Objects. Sometimes the nested data models are not printed and can not be produce
json. 

"""


class CustomUtilityDict(dict, ABC):
    """
    Custom DICT helper class: printable and serializable object

    CustomUtilityDict is a subclass of the built-in dict class that adds several additional features:
    It is printable and serializable using the json module.

    It has an update_props() method, which is called automatically by the class whenever one of the dictionary methods
     is called. This method is marked as abstract, so it must be implemented by any subclass of CustomUtilityDict.
    Example
    Here is an example of how to use CustomUtilityDict:

    ```class MyCustomDict(CustomUtilityDict):
        def __init__(self):
            super().__init__()
            self.name: str = ""

        def update_props(self):
            self.key_value["name"] = self.name

        my_dict = MyCustomDict()
        my_dict["key1"] = "value1"
        my_dict["key2"] = "value2"
    ```
    """

    def __init__(self, *args, **kw):
        super().__init__()
        self.key_value = dict(*args, **kw)

    def __getitem__(self, key):
        self.update_props()
        return self.key_value[key]

    def __setitem__(self, key, value):
        self.key_value[key] = value

    def __iter__(self):
        self.update_props()
        return iter(self.key_value)

    def __len__(self):
        self.update_props()
        return len(self.key_value)

    def __delitem__(self, key):
        self.update_props()
        del self.key_value[key]

    def __str__(self):
        self.update_props()
        # return json.dumps(self, default=lambda o: o.__dict__,)
        # return json.dumps(self.key_value, default=lambda o: self.default_json(o))
        # return json.dumps(self)
        return str(self.key_value)

    def to_json(self, pure=False) -> str:
        if pure:
            return json.dumps(self.key_value)
        st = str(self.key_value).replace("'", '"')
        return str(st)
        # return json.dumps(json.loads(st))
        # return o.__dict__
        # return "33"

    def __repr__(self):
        return self.__str__()

    @abstractmethod
    def update_props(self):
        """
        Assign all the variables/properties in "self" object as key value pair.
        So when you will print it, it will print the dictionary with values up to date.

        I.e. if your model has some variables/properties:

        ```
        self.name: str = ""
        self.event_type: str = ""
        self.attributes: dict[str, dict] = {}
        ```

        These properties should be added in the update_props method as follows in order to print all values
        ```
        self.key_value["name"] = self.name
        self.key_value["event_type"] = self.event_type
        self.key_value["attributes"] = self.attributes
        ```
        Returns
        -------

        """
        pass
