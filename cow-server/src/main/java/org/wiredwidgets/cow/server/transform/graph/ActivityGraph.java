package org.wiredwidgets.cow.server.transform.graph;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

public class ActivityGraph extends DefaultDirectedGraph<Activity, ActivityEdge> {
	
	private static final long serialVersionUID = 1L;
	
	private static EdgeFactory<Activity, ActivityEdge> ef = new ClassBasedEdgeFactory<Activity, ActivityEdge>(ActivityEdge.class);
	
	public ActivityGraph() {
		super(ef);		
	}

}
