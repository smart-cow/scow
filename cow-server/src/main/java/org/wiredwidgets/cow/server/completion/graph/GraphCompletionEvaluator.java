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

package org.wiredwidgets.cow.server.completion.graph;

import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.COMPLETED;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.CONTINGENT;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.OPEN;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.PLANNED;
import static org.wiredwidgets.cow.server.api.model.v2.CompletionState.PRECLUDED;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jbpm.process.audit.NodeInstanceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.transform.graph.ActivityEdge;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;
import org.wiredwidgets.cow.server.transform.graph.activity.ExclusiveGatewayActivity;
import org.wiredwidgets.cow.server.transform.graph.builder.GraphBuilder;

@Component
public class GraphCompletionEvaluator {
	
	private final Logger log = LoggerFactory.getLogger(GraphCompletionEvaluator.class);
	
	@Autowired
	GraphBuilder graphBuilder;
	
	@Autowired
	ConversionService converter;
	
	public ActivityGraph evaluate(Process process, List<NodeInstanceLog> nodes) {
		
		ActivityGraph graph = graphBuilder.buildGraph(process);
		Map<String, Set<NodeInstanceLog>> nodeMap = getNodeMap(nodes);
		// mostly for testing, clear any previous completion state 
		clearCompletion(graph);
		evaluate(graph, graph.getStart(), null, nodeMap);
		return graph;	
	}
	
	private void clearCompletion(ActivityGraph graph) {
		for (Activity activity : graph.getActivities()) {
			activity.setCompletionState(null);
			activity.setWrapped(false);
		}
		for (Activity activity : graph.vertexSet()) {
			activity.setCompletionState(null);
			activity.setWrapped(false);
		}
	}
	
    private Map<String, Set<NodeInstanceLog>> getNodeMap(List<NodeInstanceLog> nodes) {
    	// get all node instances for the process instance Id and put them into a map
    	// where the map key is the unique node name and the value is a sorted set in descending date/time order
    	Map<String, Set<NodeInstanceLog>> nodeMap = new HashMap<String, Set<NodeInstanceLog>>();
    	
    	for (NodeInstanceLog nil : nodes) {
    		String nodeName = nil.getNodeName();
    		if (nodeMap.get(nodeName) == null) {
    			// create a sorted set using Date and then Entry/Exit as our comparator
    			Set<NodeInstanceLog> nodeSet = new TreeSet<NodeInstanceLog>(
    					new Comparator<NodeInstanceLog>() {
    						@Override
    						public int compare(NodeInstanceLog o1, NodeInstanceLog o2) {
    							int result = o1.getDate().compareTo(o2.getDate());
    							if (result == 0) {
    								// The event timestamp is the same 
    								// Mysql only has full second resolution so this will happen frequently
    								// To break the tie we next next sort on Entry (0) vs. Exit (1)
    								result = new Integer(o1.getType()).compareTo(new Integer(o2.getType()));
    							}
    							// transform to descending order: most recent activity first
    							return (0 - result);
    						}
    					});
    			nodeMap.put(nodeName, nodeSet);
    		}
    		nodeMap.get(nodeName).add(nil);	
    	}
    	return nodeMap;
    }
	
	private void evaluate(ActivityGraph graph, Activity current, Activity previous, Map<String, Set<NodeInstanceLog>> nodeMap) {
			
		if (current.getCompletionState() != null) {
			// this node has already been visited
			// This can occur in case of a loop.  We need to avoid an infinite loop.
			return;
		}
		
		if (nodeMap.containsKey(current.getName())) {
			NodeInstanceLog lastEvent = nodeMap.get(current.getName()).iterator().next();
			if (lastEvent.getType() == NodeInstanceLog.TYPE_ENTER) {
				// have not exited, so node is currently active
				current.setCompletionState(OPEN);
				
				if (current instanceof Task) {
					((Task)current).setCreateTime(converter.convert(lastEvent.getDate(), XMLGregorianCalendar.class));
				}
				
			}
			else {
				// this node has been exited
				current.setCompletionState(COMPLETED);
				
				if (current instanceof Task) {
					
					((Task)current).setCompletionTime(converter.convert(lastEvent.getDate(), XMLGregorianCalendar.class));
					
					// the previous event should always be the node entry event
					NodeInstanceLog previousEvent = nodeMap.get(current.getName()).iterator().next();
					((Task)current).setCreateTime(converter.convert(previousEvent.getDate(), XMLGregorianCalendar.class));
				}
			}
		}
		else {
			// this node has not been reached yet
			if (previous == null) {
				// this is the start node
				// this should not happen -- upon starting the process we should always
				// enter and exit the start node
				log.error("Have not exited start node??? Node={}", current.getClass().getSimpleName());
				return;
			}
			if (previous.getCompletionState() == COMPLETED) {
				if (!(previous instanceof ExclusiveGatewayActivity)) {
					// we exited the previous node but did not arrive here. that
					// should only happen in case of a branching gateway
					log.error("Unexpected condition! node is not a Gateway: {}", previous.getClass().getSimpleName() );
					return;
				}
				// this is a "path not taken"
				current.setCompletionState(PRECLUDED);
			}
			else if (previous.getCompletionState() == OPEN) {
				// we will get here once the previous node is complete.
				current.setCompletionState(PLANNED);
			}
			else if (previous instanceof ExclusiveGatewayActivity) {
				// only one path will be selected and we don't know which one
				current.setCompletionState(CONTINGENT);
			}
			else {
				current.setCompletionState(previous.getCompletionState());
			}
		}
	
		for (ActivityEdge out : graph.outgoingEdgesOf(current)) {
			evaluate(graph, graph.getEdgeTarget(out), current, nodeMap);
		}

	}

}
