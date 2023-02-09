/*
 * Copyright (c) 2021-2022 Queensland University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.processdataquality.praeclarus.ui.component;

import com.processdataquality.praeclarus.annotation.Pattern;
import com.processdataquality.praeclarus.annotation.Plugin;
import com.processdataquality.praeclarus.pattern.PatternGroup;
import com.processdataquality.praeclarus.plugin.PluginFactory;
import com.processdataquality.praeclarus.plugin.PluginService;

import java.util.*;
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


    public boolean isLeaf(TreeItem item) {
        return getChildItems(item).isEmpty();
    }


    private List<TreeItem> createItemList() {
        List<TreeItem> list = new ArrayList<>();

        // create headers
        TreeItem readers = new TreeItem("Readers");
        TreeItem patterns = new TreeItem("Patterns");
        TreeItem actions = new TreeItem("Actions");
        TreeItem writers = new TreeItem("Writers");
        list.add(readers);
        list.add(patterns);
        list.add(actions);
        list.add(writers);

        // create menu items
        list.addAll(createItems(PluginService.readers(), readers));
        list.addAll(createItems(PluginService.actions(), actions));
        list.addAll(createPatternItems(PluginService.patterns(), patterns));
        list.addAll(createItems(PluginService.writers(), writers));

        return list;
    }

    
    private List<TreeItem> createItems(PluginFactory<?> factory, TreeItem parent) {
        List<TreeItem> items = new ArrayList<>();
        for (String name : factory.getPluginClassNames()) {
            Plugin metaData = factory.getPluginAnnotation(name);
            String label = metaData != null ? metaData.name() :
                    name.substring(name.lastIndexOf('.'));
            items.add(new TreeItem(label, parent, name));
        }
        return sort(items);
    }


    private List<TreeItem> createPatternItems(PluginFactory<?> factory, TreeItem patterns) {
        List<TreeItem> items = new ArrayList<>();
        Map<PatternGroup, TreeItem> patternMap = new HashMap<>();
        for (String name : factory.getPluginClassNames()) {
            Plugin pluginMetaData = factory.getPluginAnnotation(name);
            List<Pattern> patternList = factory.getPatternAnnotations(name);
            if (patternList.isEmpty()) {
                TreeItem groupItem = getGroupItem(PatternGroup.UNGROUPED, patterns, items, patternMap);
                items.add(newPatternItem(pluginMetaData, name, groupItem));
            }
            else {
                for (Pattern pattern : patternList) {
                    TreeItem groupItem = getGroupItem(pattern.group(), patterns, items, patternMap);
                    items.add(newPatternItem(pluginMetaData, name, groupItem));
                }
            }
        }

        return sort(items);
    }


    private List<TreeItem> sort(List<TreeItem> items) {
        items.sort(Comparator.comparing(TreeItem::getLabel));
        return items;
    }


    // get or add sub-header for pattern group
    private TreeItem getGroupItem(PatternGroup group, TreeItem patterns, List<TreeItem> items,
                   Map<PatternGroup, TreeItem> patternMap) {
        TreeItem groupItem = patternMap.get(group);
        if (groupItem == null) {
            groupItem = new TreeItem(group.getName(), patterns, null);
            patternMap.put(group, groupItem);
            items.add(groupItem);
        }
        return groupItem;
    }


    private TreeItem newPatternItem(Plugin metaData, String name, TreeItem groupItem) {
        String label = metaData != null ? metaData.name() :
                name.substring(name.lastIndexOf('.'));
       return new TreeItem(label, groupItem, name);
    }

}
