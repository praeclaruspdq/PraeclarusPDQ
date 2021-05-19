package com.processdataquality.praeclarus.ui.component;

import com.processdataquality.praeclarus.plugin.PluginService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Michael Adams
 * @date 16/4/21
 */
public class TreeData {

    private final List<TreeItem> itemList = createItemList();

    public List<TreeItem> getItems() { return itemList; }

    public List<TreeItem> getRootItems() {
        return itemList.stream()
                .filter(item -> item.getParent() == null)
                .collect(Collectors.toList());
    }

    public List<TreeItem> getChildItems(TreeItem parent) {
        return itemList.stream().filter(
                item -> Objects.equals(item.getParent(), parent))
                .collect(Collectors.toList());
    }


    private List<TreeItem> createItemList() {
        List<TreeItem> list = new ArrayList<>();
        TreeItem readers = new TreeItem("Readers", null);
        TreeItem writers = new TreeItem("Writers", null);
        TreeItem patterns = new TreeItem("Patterns", null);
        TreeItem actions = new TreeItem("Actions", null);
        list.add(readers);
        list.add(writers);
        list.add(patterns);
        list.add(actions);

        for (String name : PluginService.readers().getPluginNames()) {
            list.add(new TreeItem(name, readers));
        }

        for (String name : PluginService.writers().getPluginNames()) {
            list.add(new TreeItem(name, writers));
        }

        for (String name : PluginService.patterns().getPluginNames()) {
            list.add(new TreeItem(name, patterns));
        }

        return list;
    }



}
