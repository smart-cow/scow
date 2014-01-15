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
		diverging.setName("diverging");
		GatewayActivity converging = new ExclusiveGatewayActivity();
		converging.setName("converging");
		converging.setDirection(GatewayActivity.CONVERGING);
		graph.addVertex(diverging);
		graph.addVertex(converging);
		moveIncomingEdges(graph, activity, diverging);
		moveOutgoingEdges(graph, activity, converging);
	
		Task bypassTask = new Task();
		bypassTask.setName("Bypass Task");
		bypassTask.setAssignee(process.getBypassAssignee());
		bypassTask.setCandidateGroups(process.getBypassCandidateGroups());
		bypassTask.setCandidateUsers(process.getBypassCandidateUsers());
		
		graph.addVertex(bypassTask);
		graph.addEdge(diverging, bypassTask);
		graph.addEdge(diverging, activity);
		graph.addEdge(bypassTask, converging);
		graph.addEdge(activity, converging);
		
		// set to false so on the next pass we won't do this again.
		activity.setBypassable(false);
		
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
}