package com.processdataquality.praeclarus.ui.component;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.plugin.PluginService;
import com.processdataquality.praeclarus.ui.MainView;
import com.processdataquality.praeclarus.workspace.Workspace;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 30/4/21
 */
@CssImport("./styles/pdq-styles.css")
@JsModule("./src/test.js")
public class PipelinePanel extends VerticalLayout {

    private final Workspace _workspace;
    private final List<String> _pipeLabels = new ArrayList<>();
    private final List<PDQPlugin> _pipeItems = new ArrayList<>();
    private final MainView _parent;
    private final RunnerButtons _runnerButtons;
    private final ListBox<String> _pipelineList;

    private int _selectedIndex;

    public PipelinePanel(MainView parent) {
        _parent = parent;
        _workspace = new Workspace();
        _pipelineList = new ListBox<>();
        _runnerButtons = new RunnerButtons(_workspace, _parent.getResultsPanel());
        _runnerButtons.addButton(createRemoveButton());
        add(new H3("Pipeline"));
        add(createCanvas());
        add(_runnerButtons);
//        add(new Html("<input type='file' id='file-input' onchange='inputselect()'/>"));


 //       UI.getCurrent().getPage().executeJs("window.posn()");
    }

 //   private ListBox<String> createCanvas() {

    private HorizontalLayout createCanvas() {
        HorizontalLayout hl = new HorizontalLayout();
  //      _pipelineList.setSizeFull();

        _pipelineList.addValueChangeListener(e -> {
            String selected = e.getValue();
            _selectedIndex = 0;
            for (int i = _selectedIndex; i < _pipeLabels.size(); i++) {
                if (_pipeLabels.get(i).equals(selected)) break;
            }

            // get props for selected
            showPluginProperties(_selectedIndex);
        });

        Html canvas = new Html("<canvas id='thecanvas' width='1000' height='500'> </canvas>");
        DropTarget<Html> dropTarget = DropTarget.create(canvas);
//        DropTarget<ListBox<String>> dropTarget = DropTarget.create(_pipelineList);
        dropTarget.setDropEffect(DropEffect.COPY);
        dropTarget.addDropListener(event -> {
            if (event.getDropEffect() == DropEffect.COPY) {
                if (event.getDragData().isPresent()) {
                    List<TreeItem> droppedItems = (List<TreeItem>) event.getDragData().get();
                    TreeItem item = droppedItems.get(0);     // only one is dropped
                    _pipeLabels.add(item.getName());
                    _pipelineList.setItems(_pipeLabels);
                    addPluginInstance(item);
                    UI.getCurrent().getPage().executeJs("window.drawStep($0)", item.getName());
                }
            }
        });

 //       return _pipelineList;

        hl.add(_pipelineList, canvas);
        hl.setSizeFull();
        return hl;
    }


    private void showPluginProperties(int selected) {
        PDQPlugin plugin = _workspace.getNode(selected).getPlugin();
        _parent.getPropertiesPanel().setPlugin(plugin);
    }


    private void addPluginInstance(TreeItem item) {
        String pTypeName = item.getParent().getName();
        PDQPlugin instance = null;
        if (pTypeName.equals("Readers")) {
            instance = PluginService.readers().newInstance(item.getName());
        }
        if (pTypeName.equals("Writers")) {
            instance = PluginService.writers().newInstance(item.getName());
        }
        if (pTypeName.equals("Patterns")) {
            instance = PluginService.patterns().newInstance(item.getName());
        }
        _workspace.appendPlugin(instance);
        _runnerButtons.enable();
        _pipeItems.add(instance);
        showPluginProperties(_pipeItems.size() -1);
    }


    private Button createRemoveButton() {
        Icon icon = VaadinIcon.TRASH.create();
        icon.setSize("24px");
        return new Button(icon, e -> {
            String removed = _pipeLabels.remove(_selectedIndex);
            _pipelineList.setItems(_pipeLabels);
            _workspace.dropNode(removed);
        });
    }

}
