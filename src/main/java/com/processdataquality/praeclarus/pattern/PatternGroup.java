package com.processdataquality.praeclarus.pattern;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public enum PatternGroup {

    FORM_BASED("Form Based"),
    TIME_TRAVEL("Time Travel"),
    UNANCHORED_EVENT("Unanchored Event"),
    SCATTERED_EVENT("Scattered Event"),
    ELUSIVE_CASE("Elusive Case"),
    SCATTERED_CASE("Scattered Case"),
    COLLATERAL_EVENTS("Collateral Events"),
    POLLUTED_LABEL("Polluted Label"),
    DISTORTED_LABEL("Distorted Label"),
    SYNONYMOUS_LABELS("Synonymous Labels"),
    HOMONYMOUS_LABELS("Homonymous Labels"),
    NONE("Ungrouped");

    private final String _name;

    PatternGroup(String name) { _name = name; }

    public String getName() { return _name; }
}
