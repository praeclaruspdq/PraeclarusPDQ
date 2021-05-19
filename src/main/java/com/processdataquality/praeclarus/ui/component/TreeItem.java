package com.processdataquality.praeclarus.ui.component;

import com.processdataquality.praeclarus.plugin.PluginGroup;

public class TreeItem {

    private String label;
    private TreeItem parent;
    private PluginGroup group;

    
    public TreeItem(String label, TreeItem parent) {
        this.label = label;
        this.parent = parent;
    }

    public TreeItem getParent() {
        return parent;
    }

    public TreeItem getTopAncestor() {
        TreeItem parent = getParent();
        while (parent != null) {
            parent = parent.getParent();
        }
        return parent;
    }

    public String getName() { return label; }
}
