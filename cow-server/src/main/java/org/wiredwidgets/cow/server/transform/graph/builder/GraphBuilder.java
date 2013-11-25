package org.wiredwidgets.cow.server.transform.graph.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Exit;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;
import org.wiredwidgets.cow.server.transform.graph.activity.EndActivity;
import org.wiredwidgets.cow.server.transform.graph.activity.StartActivity;

@Component
public class GraphBuilder {
	
	@Autowired
	ActivityGraphBuilderFactory factory;
	
	@Autowired
	ExitSignalBuilder exitSignalBuilder;
	
	private static Logger log = Logger.getLogger(GraphBuilder.class);
	
	public ActivityGraph buildGraph(Process process) {
	
		// create the starting graph, consisting of start, Activities, and end
		StartActivity start = new StartActivity();
		start.setName("Start");
		EndActivity end = new EndActivity();
		end.setName("End");
		
		Activity main = process.getActivity().getValue();
		
		// create the graph
		ActivityGraph graph = new ActivityGraph();
		graph.addVertex(start);
		graph.addVertex(main);
		graph.addVertex(end);
		
		graph.addEdge(start, main);
		graph.addEdge(main, end);
		
		// build the Activities (this will build everything else)
		factory.buildGraph(main, graph, process);
				
		// if there are any Exit nodes in the graph, we need to modify
		// the End node of the graph
		if (hasExit(graph)) {
			// add signal and converging gateway
			exitSignalBuilder.buildGraph(end, graph, process);
		}
		
		return graph;
	}
	
	/**
	 * Determine whether the graph contains an Exit
	 * @param graph
	 * @return
	 */
	private boolean hasExit(ActivityGraph graph) {
		for (Activity activity : graph.vertexSet() ) {
			if (activity instanceof Exit) {
				return true;
			}
		}
		return false;
	}

}
