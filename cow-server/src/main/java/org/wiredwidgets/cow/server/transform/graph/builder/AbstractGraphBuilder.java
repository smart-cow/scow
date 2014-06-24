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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.graph.ActivityEdge;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;

public abstract class AbstractGraphBuilder<T extends Activity> implements ActivityGraphBuilder {
	
	@Autowired
	ActivityGraphBuilderFactory factory;
	
	private static Logger log = Logger.getLogger(AbstractGraphBuilder.class);
	
	/**
	 * Return false by default.  This is called only in case there is more
	 * than one builder for a given Activity type.  In that case the builders
	 * should implement Supports() to determine which one should be chosen
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean supports(Activity activity) {
		return supportsInternal((T)activity);
	}
	
	protected boolean supportsInternal(T activity) {
		return false;
	}
	
	@Override
	public abstract Class<T> getType();	
	
	@SuppressWarnings("unchecked")
	@Override
	public void buildGraph(Activity activity, ActivityGraph graph, Process process) {
		
		// add the activity to the list
		graph.getActivities().add(activity);
		
		// replace any non GUID keys
		if (activity.getKey() != null && !activity.getKey().contains("-")) {
			activity.setKey(null);
		}
		

		if (activity.getKey() == null || activity.getKey().isEmpty() ) {
			activity.setKey("_" + UUID.randomUUID().toString().toUpperCase());
		}
		buildInternal((T)activity, graph, process);
	}
	
	protected abstract void buildInternal(T activity, ActivityGraph graph, Process process);	
	
	/**
	 * Move incoming edges from one vertex to another
	 * @param graph
	 * @param sourceVertex
	 * @param targetVertex
	 */
	protected void moveIncomingEdges(ActivityGraph graph, Activity sourceVertex, Activity targetVertex) {
		// build a new collection to iterate so we can remove edges
		List<ActivityEdge> edges = new ArrayList<ActivityEdge>();
		edges.addAll(graph.incomingEdgesOf(sourceVertex));
		
		for (ActivityEdge edge : edges) {
			Activity source = graph.getEdgeSource(edge);
			log.debug("adding edge from " + source.getClass().getSimpleName() + " to " + targetVertex.getClass().getSimpleName());
			
			ActivityEdge newEdge = graph.addEdge(source, targetVertex);
			newEdge.setExpression(edge.getExpression());
			newEdge.setVarSource(edge.getVarSource());
			
			graph.removeEdge(source, sourceVertex);
		}	
	}
	
	/**
	 * Move outgoing edges from one vertex to another
	 * @param graph
	 * @param sourceVertex
	 * @param targetVertex
	 */
	protected void moveOutgoingEdges(ActivityGraph graph, Activity sourceVertex, Activity targetVertex) {
		
		// build a new collection to iterate so we can remove edges
		List<ActivityEdge> edges = new ArrayList<ActivityEdge>();
		edges.addAll(graph.outgoingEdgesOf(sourceVertex));		
		
		for (ActivityEdge edge : edges) {
			Activity target = graph.getEdgeTarget(edge);
			log.debug("adding edge from " + targetVertex.getClass().getSimpleName() + " to " + target.getClass().getSimpleName());
			
			ActivityEdge newEdge = graph.addEdge(targetVertex, target);
			newEdge.setExpression(edge.getExpression());
			newEdge.setVarSource(edge.getVarSource());
			
			graph.removeEdge(sourceVertex, target);
		}		
	}	

}
