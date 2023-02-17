/*
 * Copyright (c) 2021-2023 Queensland University of Technology
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

import static com.eclipsesource.json.WriterConfig.PRETTY_PRINT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.processdataquality.praeclarus.exception.NodeRunnerException;
import com.processdataquality.praeclarus.graph.GraphRunner;
import com.processdataquality.praeclarus.graph.GraphRunnerEvent;
import com.processdataquality.praeclarus.graph.GraphRunnerEventListener;
import com.processdataquality.praeclarus.graph.GraphRunnerStateChangeListener;
import com.processdataquality.praeclarus.logging.EventLogger;
import com.processdataquality.praeclarus.logging.EventType;
import com.processdataquality.praeclarus.node.Node;
import com.processdataquality.praeclarus.node.NodeFactory;
import com.processdataquality.praeclarus.node.PatternNode;
import com.processdataquality.praeclarus.option.ColumnNameListAndStringOption;
import com.processdataquality.praeclarus.option.ColumnNameListOption;
import com.processdataquality.praeclarus.option.Option;
import com.processdataquality.praeclarus.option.Options;
import com.processdataquality.praeclarus.pattern.AbstractDataPattern;
import com.processdataquality.praeclarus.plugin.AbstractPlugin;
import com.processdataquality.praeclarus.plugin.PluginService;
import com.processdataquality.praeclarus.plugin.uitemplate.ButtonAction;
import com.processdataquality.praeclarus.plugin.uitemplate.PluginUI;
import com.processdataquality.praeclarus.support.math.Pair;
import com.processdataquality.praeclarus.ui.MainView;
import com.processdataquality.praeclarus.ui.canvas.Canvas;
import com.processdataquality.praeclarus.ui.canvas.CanvasPrimitive;
import com.processdataquality.praeclarus.ui.canvas.CanvasSelectionListener;
import com.processdataquality.praeclarus.ui.canvas.Workflow;
import com.processdataquality.praeclarus.ui.component.announce.Announcement;
import com.processdataquality.praeclarus.ui.component.dialog.MessageDialog;
import com.processdataquality.praeclarus.ui.component.dialog.StoredWorkflowsDialog;
import com.processdataquality.praeclarus.ui.component.dialog.UploadDialog;
import com.processdataquality.praeclarus.ui.component.layout.VerticalScrollLayout;
import com.processdataquality.praeclarus.ui.component.plugin.PluginUIDialog;
import com.processdataquality.praeclarus.ui.component.plugin.PluginUIListener;
import com.processdataquality.praeclarus.ui.util.UiUtil;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;

import tech.tablesaw.api.Table;

/**
 * @author Michael Adams
 * @date 30/4/21
 */
@CssImport("./styles/pdq-styles.css")
@JsModule("./src/fs.js")
public class WorkflowPanel extends VerticalLayout
		implements GraphRunnerEventListener, GraphRunnerStateChangeListener, PluginUIListener {

	private static final Logger LOG = LoggerFactory.getLogger(WorkflowPanel.class);

	private final Workflow _workflow; // frontend
	private final MainView _parent;
	private final RunnerButtons _runnerButtons;
	private final Canvas _canvas;
	private final GraphRunner _runner;

	private final Button _removeButton = createRemoveButton();
	private final Button _storeButton = createStoreButton();
	private final Button _downloadButton = createDownloadButton();
	private final Button _resetButton = createResetButton();
	private final Button _uploadButton = createUploadButton();
	private final Button _searchButton = createSearchButton();
	private final Button _clearButton = createClearButton();

	public WorkflowPanel(MainView parent) {
		_parent = parent;
		_canvas = new Canvas(1600, 800);
		_runner = new GraphRunner();
		_runner.addNodeRunnerEventListener(this);
		_runner.addNodeRunnerStateChangeListener(this);
		_workflow = new Workflow(this, _canvas.getContext());
		_canvas.addListener(_workflow);
		_runnerButtons = initRunnerButtons();
		addVertexSelectionListener(_runnerButtons);
		add(_runnerButtons, createCanvasContainer());
		changedSelected(null); // init with default properties panel
	}

	private String _fileName = null;

	@Override
	public void pluginUICloseEvent(ButtonAction buttonAction, Node node) {
		try {
			_runner.action(GraphRunner.RunnerAction.RESUME, node); // button action doesn't matter
		} catch (NodeRunnerException e) {
			Announcement.error(e.getMessage());
			LOG.error("Error attempting to continue after closing plugin UI", e);
		}
	}

	@Override
	public void runnerStateChanged(GraphRunner.RunnerState state) {
		_runnerButtons.setState(state);
		enableButtons(state, _workflow.getSelectedVertex());
	}

	@Override
	public void runnerEvent(GraphRunnerEvent event) {
		if (event.getEventType() == EventType.NODE_PAUSED) {
			Node node = event.getNode();

			// pattern detected but not yet repaired
			if (node instanceof PatternNode && !node.hasCompleted()) {
				PluginUI ui = ((AbstractDataPattern) node.getPlugin()).getUI();
				if (ui != null) {
					new PluginUIDialog(ui, node, this).open();
				}
			}
		}
	}

	public boolean hasChanges() {
		return _workflow.hasChanges();
	}

	public void setWorkflowChanged(boolean changed) {
		_parent.setUnsavedChanges(changed);
	}

	public void changedSelected(Node selected) {
		showPluginProperties(selected);
		_runnerButtons.enable();
	}

	public void canvasSelectionChanged(CanvasPrimitive selected) {
		enableButtons(_runner.getState(), selected);
	}

	public void showPluginProperties(Node selected) {
		AbstractPlugin plugin = selected != null ? selected.getPlugin() : null;
		if (plugin != null) {
			addColumnListIfRequired(selected, plugin.getOptions());
			addAdditionalOptions(selected, plugin);
			_parent.getPropertiesPanel().set(plugin);
		} else {
			_parent.getPropertiesPanel().set(_workflow.getGraph());
		}
	}

	private void addAdditionalOptions(Node node, AbstractPlugin plugin) {
		if (plugin.getName().equals("Aggregate")) {
			List<Table> inputs = node.getInputs();
			if (!inputs.isEmpty()) {		
				plugin.getAuxiliaryDatasets().put("inputTable", inputs.get(0));
			}
		}
	}

	private RunnerButtons initRunnerButtons() {
		RunnerButtons buttons = new RunnerButtons(_runner);
		buttons.addButton(_resetButton);
		buttons.addSeparator();
		buttons.addButton(_removeButton);
		buttons.addSeparator();
		buttons.addButton(_clearButton);
		buttons.addSeparator();
		buttons.addButton(_searchButton);
		buttons.addButton(_storeButton);
		buttons.addSeparator();
		buttons.addButton(_uploadButton);
		buttons.addButton(_downloadButton);
		buttons.addSeparator();
		buttons.addLabel();
		return buttons;
	}

	private VerticalLayout createCanvasContainer() {
		DropTarget<Canvas> dropTarget = DropTarget.create(_canvas);
		dropTarget.setDropEffect(DropEffect.COPY);
		dropTarget.addDropListener(event -> {
			if (event.getDropEffect() == DropEffect.COPY) {
				if (event.getDragData().isPresent()) {

					@SuppressWarnings("unchecked")
					List<TreeItem> droppedItems = (List<TreeItem>) event.getDragData().get();
					TreeItem item = droppedItems.get(0); // only one is dropped
					try {
						Node node = addPluginInstance(item);
						_workflow.addVertex(node);
						_runnerButtons.enable();
					} catch (InvocationTargetException | NoSuchMethodException | InstantiationException
							| IllegalAccessException e) {
						Announcement.error("Failed to create plugin: " + e.getMessage());
						LOG.error("Failed to create plugin: ", e);
					}

				}
			}
		});
		VerticalScrollLayout container = new VerticalScrollLayout();
		container.add(_canvas);
		UiUtil.removeTopPadding(container);
		return container;

	}

	private Node addPluginInstance(TreeItem item)
			throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		String pTypeName = item.getRoot().getLabel();
		AbstractPlugin instance = null;
		if (pTypeName.equals("Readers")) {
			instance = PluginService.readers().newInstance(item.getName());
		}
		if (pTypeName.equals("Writers")) {
			instance = PluginService.writers().newInstance(item.getName());
		}
		if (pTypeName.equals("Patterns")) {
			instance = PluginService.patterns().newInstance(item.getName());
		}
		if (pTypeName.equals("Actions")) {
			instance = PluginService.actions().newInstance(item.getName());
		}

		Node node = NodeFactory.create(instance);
		showPluginProperties(node);
		return node;
	}

	/**
	 * @return the runner for this panel
	 */
	public GraphRunner getRunner() {
		return _runner;
	}

	private Button createRemoveButton() {
		return UiUtil.createToolButton(VaadinIcon.TRASH, "Remove vertex", false, e -> {
			_workflow.removeSelected();
			changedSelected(null);
			_downloadButton.setEnabled(_workflow.hasContent());
		});
	}

	private Button createUploadButton() {
		return UiUtil.createToolButton(VaadinIcon.UPLOAD, "Upload workflow", true, e -> {
			if (_workflow.hasChanges()) {
				handleExistingWorkflow(s -> {
					storeWorkflow();
					loadFromFile();
				}, d -> loadFromFile());
			} else {
				loadFromFile();
			}
		});
	}

	private Button createDownloadButton() {
		return UiUtil.createToolButton(VaadinIcon.DOWNLOAD, "Download workflow", false, e -> downloadWorkflow());
	}

	private Button createResetButton() {
		return UiUtil.createToolButton(VaadinIcon.FAST_BACKWARD, "Reset all", false, e -> _workflow.resetAll());
	}

	private void saveThenLoadNewWorkflow() {
		String jsonStr = _workflow.asJson().toString(PRETTY_PRINT);
		_canvas.saveThenLoadFile(jsonStr);
	}

	// if there's inputs to this node, then get the list of column names
	// and create a list of items for each column name option
	@SuppressWarnings("unchecked")
	private void addColumnListIfRequired(Node node, Options options) {
		List<Table> inputs = node.getInputs();
		if (!inputs.isEmpty()) {
			for (Option option : options.values()) {
				if (option instanceof ColumnNameListOption) {
					List<String> colNames = new ArrayList<>();
					for (Table table : inputs) {
						colNames.addAll(table.columnNames());
					}
					if (!colNames.isEmpty()) {
						String value = option.asString(); // get any current value
						option.setValue(colNames);
						if (!StringUtils.isEmpty(value) && colNames.contains(value)) {
							((ColumnNameListOption) option).setSelected(value);
						}
					}
				} else if (option instanceof ColumnNameListAndStringOption) {
					List<String> colNames = new ArrayList<>();
					for (Table table : inputs) {
						colNames.addAll(table.columnNames());
					}
					if (!colNames.isEmpty()) {
						Pair<List<String>, String> val = (Pair<List<String>, String>) option.value();
						String colValue = String.valueOf(val.getKey()); // get any current value
						String nameValue = String.valueOf(val.getValue());
						option.setValue(new Pair<List<String>, String>(colNames, ""));
						if (!StringUtils.isEmpty(colValue) && colNames.contains(colValue)) {
							if (!StringUtils.isEmpty(nameValue)) {
								((ColumnNameListAndStringOption) option)
										.setSelected(new Pair<String, String>(colValue, nameValue));
							} else {
								((ColumnNameListAndStringOption) option)
										.setSelected(new Pair<String, String>(colValue, ""));
							}
						}
					}
				}
			}
		}
	}

	public Workflow getWorkflow() {
		return _workflow;
	}

	private Button createStoreButton() {
		return UiUtil.createToolButton(VaadinIcon.PLUS, "Store workflow", false, e -> storeWorkflow());
	}

	private Button createSearchButton() {
		return UiUtil.createToolButton(VaadinIcon.DATABASE, "Search stored workflows", true,
				e -> showStoredWorkflows());
	}

	private Button createClearButton() {
		return UiUtil.createToolButton(VaadinIcon.SUN_O, "Reset workflow canvas", false, e -> {
			if (_workflow.hasChanges()) {
				handleExistingWorkflow(s -> {
					storeWorkflow();
					_workflow.clear();
				}, d -> _workflow.clear());
			} else {
				_workflow.clear();
			}
			changedSelected(null);
			_runnerButtons.clearLabel();
		});
	}

	private void enableButtons(GraphRunner.RunnerState state, CanvasPrimitive selected) {
		boolean isIdle = state == GraphRunner.RunnerState.IDLE;
		boolean hasContent = isIdle && _workflow != null && _workflow.hasContent();
		_removeButton.setEnabled(isIdle && selected != null);
		_downloadButton.setEnabled(hasContent);
		_searchButton.setEnabled(isIdle);
		_storeButton.setEnabled(hasContent);
		_resetButton.setEnabled(hasContent);
		_uploadButton.setEnabled(isIdle);
		_clearButton.setEnabled(hasContent);
	}

	private void handleExistingWorkflow(ComponentEventListener<ClickEvent<Button>> confirmListener,
			ComponentEventListener<ClickEvent<Button>> rejectListener) {
		MessageDialog dialog = new MessageDialog("Save changes to existing workflow?");
		dialog.setText("Click 'Store' to save changes, " + "'Discard' to discard changes, 'Cancel' to keep working.");
		dialog.addConfirmButton(new Button("Store", confirmListener));
		dialog.addRejectButton(new Button("Discard", rejectListener));
		dialog.addCancelButton();
		dialog.open();
	}

	private void loadFromFile() {
		UploadDialog dialog = new UploadDialog(e -> {
			if (e.successful) {
				try {
					String content = new String(e.inputStream.readAllBytes(), StandardCharsets.UTF_8);
					_workflow.fileLoaded(content);
					_fileName = e.fileName;
					_runnerButtons.setLabel(e.fileName);
				} catch (IOException ex) {
					Announcement.error("Failed to load file: " + ex.getMessage());
				}
			}
		}, new String[] { ".pwf", ".json" });
		dialog.open();

		// _canvas.loadFromFile(); *requires https for direct fs access
	}

	private boolean downloadWorkflow() {
		_workflow.getGraph().updateLastSavedTime();
		String jsonStr = _workflow.asJson().toString(PRETTY_PRINT);

		// _canvas.saveToFile(jsonStr); *requires https as above

		if (_fileName == null) {
			_fileName = _workflow.getGraph().getName() + ".pwf";
		}
		downloadFile(_fileName, jsonStr.getBytes(StandardCharsets.UTF_8));

		_workflow.setChanged(false);
		EventLogger.graphDownloadEvent(_workflow.getGraph());
		return true;
	}

	private void storeWorkflow() {
		_workflow.store();
	}

	private void showStoredWorkflows() {
		new StoredWorkflowsDialog(_workflow).open();
	}

	public void onResize() {
		_workflow.render();
	}

	public void addVertexSelectionListener(CanvasSelectionListener listener) {
		_workflow.addVertexSelectionListener(listener);
	}

	protected void downloadFile(String fileName, byte[] content) {
		InputStreamFactory isFactory = () -> new ByteArrayInputStream(content);
		StreamResource resource = new StreamResource(fileName, isFactory);
		resource.setContentType("multipart/form-data");
		resource.setCacheTime(0);
		resource.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

		Anchor downloadAnchor = new Anchor(resource, "");
		Element element = downloadAnchor.getElement();
		element.setAttribute("download", true);
		element.getStyle().set("display", "none");
		add(downloadAnchor);

		// simulate a click & remove anchor after file downloaded
		element.executeJs("return new Promise(resolve =>{this.click(); " + "setTimeout(() => resolve(true), 150)})",
				element).then(jsonValue -> remove(downloadAnchor));
	}

}
