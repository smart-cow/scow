package org.wiredwidgets.cow.server.transform.graph.builder;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Exit;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.graph.ActivityEdge;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;

@Component
public class ExitGraphBuilder extends AbstractGraphBuilder<Exit> {
	
	private static Logger log = Logger.getLogger(ExitGraphBuilder.class);

	@Override
	protected void buildInternal(Exit exit, ActivityGraph graph, Process process) {
		
		// remove any outgoing edges
		// note we must create a collection here to avoid concurrentModificationException
		// Collection<ActivityEdge> edges = new HashSet<ActivityEdge>();
		// edges.addAll(graph.outgoingEdgesOf(exit));	
		// graph.removeAllEdges(edges);
		
		// XXX *** change this to leave outgoing edges in place 
		// otherwise we have a possibility of ending up with a converging gateway
		// with only one incoming path which is not valid in jbpm.
		// consider changing in the future so that we remove the outgoing edges and then
		// modify the graph as necessary to make it valid. (this could get tricky...)
		
	}

	@Override
	public Class<Exit> getType() {
		return Exit.class;
	}

}
