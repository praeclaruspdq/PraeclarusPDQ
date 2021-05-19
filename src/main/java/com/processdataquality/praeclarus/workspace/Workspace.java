package com.processdataquality.praeclarus.workspace;

import com.processdataquality.praeclarus.plugin.PDQPlugin;
import com.processdataquality.praeclarus.workspace.node.Node;
import com.processdataquality.praeclarus.workspace.node.NodeFactory;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Michael Adams
 * @date 12/5/21
 */
public class Workspace {

    private final List<Node> _workflow = new ArrayList<>();
    private final List<Table> _outputs = new ArrayList<>();
    private int _index = 0;

    public void appendPlugin(PDQPlugin plugin) {
        appendNode(NodeFactory.create(plugin));
    }

    public void insertPlugin(PDQPlugin plugin, int index) {
        insertNode(index, NodeFactory.create(plugin));
    }

    public void appendNode(Node node) { _workflow.add(node); }

    public void insertNode(int index, Node node) { _workflow.add(index, node); }

    public boolean dropNode(Node node) { return _workflow.remove(node); }

    public boolean dropNode(String name) {
        Node node = getNode(name);
        return node != null && dropNode(node);
    }

    public Node getNode(int index) { return _workflow.get(index); }

    public Node getNode(String name) {
        for (Node node : _workflow) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    public int getNodeCount() { return _workflow.size(); }

    public boolean hasNodes() { return getNodeCount() > 0; }

    public boolean canRun() { return hasNodes() && _index < getNodeCount(); }

    public boolean canStep() { return canRun(); }

    public boolean canStepBack() { return hasNodes() && _index > 0; }

    
    public Table step() {
        if (_index < _workflow.size()) {
            Node next = _workflow.get(_index++);
            Table output = next.run();
            if (_index < _workflow.size()) {
                _workflow.get(_index).addInput(output);
            }
            return output;
        }
        else throw new IllegalArgumentException("Attempt to step past end of process");
    }


    public void back() {
        if (_index > 0) {
            Node next = _workflow.get(_index);
            Node prev = _workflow.get(--_index);
            Table table = prev.clearOutput();
            next.clearInput(table);
        }
    }


    public void restart() {
        reset();
        run();
    }

    
    public List<Table> run() {
        for (int i = _index; i < _workflow.size(); i++) {
            _outputs.add(step());
            if (_workflow.get(i).shouldPause()) {
                break;
            }
        }
        return _outputs;
    }


    public void reset() {
        _outputs.clear();
        _workflow.forEach(Node::clearInputs);
        _index = 0;
    }

    public Node getLastNode() {
        return _workflow.get(_index -1);
    }

    public List<String> getNodeNames() {
        return _workflow.stream().map(Node::getName).collect(Collectors.toList());
    }
}
