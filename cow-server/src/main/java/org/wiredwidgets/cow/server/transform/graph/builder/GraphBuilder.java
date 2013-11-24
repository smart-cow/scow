package org.wiredwidgets.cow.server.transform.graph.builder;

import java.util.ArrayList;
import java.util.List;

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
	
		ActivityGraph graph = new ActivityGraph();
		
		// create the starting graph, consisting of start, Activities, and end
		StartActivity start = new StartActivity();
		start.setName("Start");
		EndActivity end = new EndActivity();
		end.setName("End");
		graph.addVertex(start);
		Activity main = process.getActivity().getValue();
		graph.addVertex(main);
		graph.addVertex(end);
		
		graph.addEdge(start, main);
		graph.addEdge(main, end);
			
		// Iterate through the graph vertices, expanding where needed
		// this may require multiple passes through the vertex set, as any
		// given pass may produce more vertices.  When no more vertices have
		// been added during a complete pass, we can stop.
		
		boolean done = false;
		while(!done) {
			done = true; // initialize true for this pass
			
			// graph.vertexSet() returns an unmodifiable collection
			// so we need to copy the elements into a new collection
			// that we can iterate over and modify the underlying graph
		
			List<Activity> vertexSet = new ArrayList<Activity>();
			vertexSet.addAll(graph.vertexSet());
			
			for (Activity activity : vertexSet) {		
				@SuppressWarnings("rawtypes")
				ActivityGraphBuilder builder = factory.getBuilder(activity);
				@SuppressWarnings("unchecked")
				boolean graphModified = builder.buildGraph(activity, graph, process);
				done = (!done ? done : !graphModified);
			}
		}
		
		// special handling for Exit nodes, if there are any
		if (hasExit(graph)) {
			// add signal and converging gateway
			exitSignalBuilder.buildGraph(end, graph);
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
