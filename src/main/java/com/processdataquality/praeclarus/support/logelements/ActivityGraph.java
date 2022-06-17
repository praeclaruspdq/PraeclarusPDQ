//This code is taken from https://gist.github.com/ShobhinGoyal/15edd0967cc0f7704c36b65f431b707c.

package com.processdataquality.praeclarus.support.logelements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.LinkedList;

public class ActivityGraph {
	private int V;
	private LinkedList<Integer> adj[];

	public ActivityGraph(int v) {
		V = v;
		adj = new LinkedList[v];
		for (int i = 0; i < v; ++i)
			adj[i] = new LinkedList();
	}

	public void addEdge(int v, int w) {
		adj[v].add(w);
		adj[w].add(v);
	}

	public void removeEdge(Integer v, Integer w) {
		adj[v].remove(w);
		adj[w].remove(v);
	}

	public Boolean isCyclicUtil(int v, Boolean visited[], int parent) {
		visited[v] = true;
		Integer i;
		Iterator<Integer> it = adj[v].iterator();
		while (it.hasNext()) {
			i = it.next();
			if (!visited[i]) {
				if (isCyclicUtil(i, visited, v))
					return true;
			} else if (i != parent)
				return true;
		}
		return false;
	}

	public Boolean isCyclic() {
		Boolean visited[] = new Boolean[V];
		for (int i = 0; i < V; i++)
			visited[i] = false;

		for (int u = 0; u < V; u++) {

			if (!visited[u])
				if (isCyclicUtil(u, visited, -1))
					return true;
		}

		return false;
	}
}
