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

import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;
import org.wiredwidgets.cow.server.transform.graph.activity.ExclusiveGatewayActivity;
import org.wiredwidgets.cow.server.transform.graph.activity.GatewayActivity;
import org.wiredwidgets.cow.server.transform.graph.activity.ParallelGatewayActivity;

/**
 * Special case builder to handle bypassable activities
 * 
 *
 */
@Component
public class BypassGraphBuilder extends AbstractGraphBuilder<Activity> {

	/**
	 * The "bypass" feature allows an activity to be bypassed by 
	 * completing a special "bypass" task. The final converging
	 * gateway is an XOR gateway so the flow will continue as soon
	 * as EITHER of the two paths are completed.
	 * 
	 * the graph structure consists of
	 * (1) diverging parallel gateway
	 * (2) in parallel, the "bypass" task and the original activity,
	 * (3) converging exclusive gateway.
	 */
	@Override
	protected void buildInternal(Activity activity, ActivityGraph graph, Process process) {
	
		GatewayActivity diverging = new ParallelGatewayActivity();
		diverging.setDirection(GatewayActivity.DIVERGING);
		diverging.setName(getBypassDivergingGatewayName(activity));
		GatewayActivity converging = new ExclusiveGatewayActivity();
		converging.setName(getBypassConvergingGatewayName(activity));
		converging.setDirection(GatewayActivity.CONVERGING);
		graph.addVertex(diverging);
		graph.addVertex(converging);
		moveIncomingEdges(graph, activity, diverging);
		moveOutgoingEdges(graph, activity, converging);
	
		Task bypassTask = new Task();
		bypassTask.setName(getBypassTaskName(activity));
		bypassTask.setAssignee(process.getBypassAssignee());
		bypassTask.setCandidateGroups(process.getBypassCandidateGroups());
		bypassTask.setCandidateUsers(process.getBypassCandidateUsers());
		
		graph.addVertex(bypassTask);
		graph.addEdge(diverging, bypassTask);
		graph.addEdge(diverging, activity);
		graph.addEdge(bypassTask, converging);
		graph.addEdge(activity, converging);
		
		// set to false so on the next pass we won't do this again.
		// activity.setBypassable(false);
		activity.setWrapped(true);
		
		factory.buildGraph(activity, graph, process);

	}

	/**
	 * Returns null.  Should not be called as this builder is 
	 * considered a special case and is tested for separately
	 */
	@Override
	public Class<Activity> getType() {
		return null;
	}
	
	public static String getBypassTaskName(Activity activity) {
		return "bypass:" + activity.getName();
	}
	
	public static String getBypassDivergingGatewayName(Activity activity) {
		return "diverging:bypass:" + activity.getName();
	}
	
	public static String getBypassConvergingGatewayName(Activity activity) {
		return "converging:bypass:" + activity.getName();
	}
}
