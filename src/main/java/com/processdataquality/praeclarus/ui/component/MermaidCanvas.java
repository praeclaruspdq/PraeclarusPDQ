package com.processdataquality.praeclarus.ui.component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;

/**
 * @author Michael Adams
 * @date 23/4/21
 */
@JavaScript("https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js")
public class MermaidCanvas extends Div {

    public MermaidCanvas() {
 //       this.setId("canvas");
        setClassName("mermaid");
        UI.getCurrent().getPage().executeJs("mermaid.initialize({startOnLoad:true});");

        getElement().setText(" graph LR\n" +
                "%%{config: { 'fontFamily': 'Menlo', 'fontSize': 48, 'fontWeight': 400} }%%\n" +
                "    A[Square Rect] -- Link text --> B((Circle))\n" +
                "    A --> C(Round Rect)\n" +
                "    B --> D{Rhombus}\n" +
                "    C --> D");
    }
}
