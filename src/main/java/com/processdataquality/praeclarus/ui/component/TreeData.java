/*
 * Copyright (c) 2021 Queensland University of Technology
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

import com.processdataquality.praeclarus.annotations.PluginMetaData;
import com.processdataquality.praeclarus.pattern.PatternGroup;
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

        for (String name : PluginService.actions().getPluginNames()) {
             list.add(new TreeItem(name, actions));
         }


        Map<PatternGroup, TreeItem> patternMap = new HashMap<>();
        for (String name : PluginService.patterns().getPluginNames()) {
            PluginMetaData metadata = PluginService.patterns().getMetaData(name);
            PatternGroup group = metadata != null ? metadata.group() : PatternGroup.UNGROUPED;
            TreeItem groupItem = patternMap.get(group);
            if (groupItem == null) {
                groupItem = new TreeItem(group.getName(), patterns);
                patternMap.put(group, groupItem);
                list.add(groupItem);
            }
            list.add(new TreeItem(name, groupItem));
        }

        return list;
    }
    
}
