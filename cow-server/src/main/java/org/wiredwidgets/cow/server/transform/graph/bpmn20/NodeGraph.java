package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

public class NodeGraph extends DefaultDirectedGraph<Bpmn20Node, Bpmn20Edge> {
	
	private static final long serialVersionUID = 1L;
	
	private static EdgeFactory<Bpmn20Node, Bpmn20Edge> ef = new ClassBasedEdgeFactory<Bpmn20Node, Bpmn20Edge>(Bpmn20Edge.class);
	
	public NodeGraph() {
		super(ef);		
	}

}
