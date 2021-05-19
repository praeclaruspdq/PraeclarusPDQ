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

public class TreeItem {

    private String label;
    private TreeItem parent;

    
    public TreeItem(String label, TreeItem parent) {
        this.label = label;
        this.parent = parent;
    }

    public TreeItem getParent() {
        return parent;
    }

    public TreeItem getRoot() {
        TreeItem parent = getParent();
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }
        return parent;
    }

    public String getName() { return label; }
}
