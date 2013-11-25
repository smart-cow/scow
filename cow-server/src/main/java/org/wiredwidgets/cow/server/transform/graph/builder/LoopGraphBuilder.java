package org.wiredwidgets.cow.server.transform.graph.builder;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.model.v2.Loop;
import org.wiredwidgets.cow.server.transform.graph.ActivityEdge;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;
import org.wiredwidgets.cow.server.transform.graph.activity.DecisionTask;
import org.wiredwidgets.cow.server.transform.graph.activity.ExclusiveGatewayActivity;
import org.wiredwidgets.cow.server.transform.graph.activity.GatewayActivity;

@Component
public class LoopGraphBuilder extends AbstractGraphBuilder<Loop> {
	
	private static Logger log = Logger.getLogger(LoopGraphBuilder.class);

	@Override
	protected void buildInternal(Loop loop, ActivityGraph graph, Process process) {
		
		// a loop consists of a sequence of:
		// (1) converging gateway
		// (2) Activity
		// (3) Decision Task (choose repeat or done)
		// (4) Diverging gateway
		
		GatewayActivity converging = new ExclusiveGatewayActivity();
		converging.setDirection(GatewayActivity.CONVERGING);
		converging.setName("converging");
		graph.addVertex(converging);
		moveIncomingEdges(graph, loop, converging);
		
		Activity loopActivity = loop.getActivity().getValue();
		graph.addVertex(loopActivity);
		graph.addEdge(converging, loopActivity);
		
		DecisionTask dt = new DecisionTask(loop.getLoopTask());
		dt.addOption(loop.getDoneName());
		dt.addOption(loop.getRepeatName());
		
		graph.addVertex(dt);
		graph.addEdge(loopActivity, dt);
		
		GatewayActivity diverging = new ExclusiveGatewayActivity();
		diverging.setDirection(GatewayActivity.DIVERGING);
		diverging.setName("diverging");
		graph.addVertex(diverging);
		graph.addEdge(dt, diverging);
		
		// the "done" path
		moveOutgoingEdges(graph, loop, diverging);
		// assume we can have only one outgoing edge
		ActivityEdge doneEdge = graph.outgoingEdgesOf(diverging).iterator().next();
		doneEdge.setExpression(loop.getDoneName());
		doneEdge.setVarSource(dt);
		
		// the "repeat" path
		ActivityEdge repeatEdge = graph.addEdge(diverging, converging);
		repeatEdge.setExpression(loop.getRepeatName());
		repeatEdge.setVarSource(dt);
		
		// build the activity
		factory.buildGraph(loopActivity, graph, process);
		
		graph.removeVertex(loop);
	}

	@Override
	public Class<Loop> getType() {
		return Loop.class;
	}

}
