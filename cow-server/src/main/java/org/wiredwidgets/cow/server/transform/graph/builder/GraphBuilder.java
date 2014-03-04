/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2014 The MITRE Corporation,
 * Licensed under the Apache License,
 * Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wiredwidgets.cow.server.transform.graph.builder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Exit;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;
import org.wiredwidgets.cow.server.transform.graph.activity.EndActivity;
import org.wiredwidgets.cow.server.transform.graph.activity.GatewayActivity;
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
		
		// "build" the start and end nodes.  Really this just generates the IDs
		factory.buildGraph(start, graph, process);
		factory.buildGraph(end, graph, process);
		
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
	
/*	private void fixGraph(ActivityGraph graph) {
		for (Activity activity : graph.vertexSet()) {
			
			// Converging gateway must have at least two incoming paths
			
			if (activity instanceof GatewayActivity) {
				GatewayActivity gateway = (GatewayActivity)activity;
				if (gateway.getDirection().equals(GatewayActivity.CONVERGING)) {
					if (graph.incomingEdgesOf(gateway).size() < 2) {
						
						// bypass the gateway and connect directly to the outgoing node
						// use a loop here but really there should be only one
						for (ActivityEdge edge : graph.incomingEdgesOf(gateway)) {
							
						}
						
					}
				}
			}
			
		}
	}*/

}
