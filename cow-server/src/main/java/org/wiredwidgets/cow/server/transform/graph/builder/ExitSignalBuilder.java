package org.wiredwidgets.cow.server.transform.graph.builder;

import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Signal;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;
import org.wiredwidgets.cow.server.transform.graph.activity.EndActivity;
import org.wiredwidgets.cow.server.transform.graph.activity.ExclusiveGatewayActivity;
import org.wiredwidgets.cow.server.transform.graph.activity.GatewayActivity;

@Component
public class ExitSignalBuilder extends AbstractGraphBuilder<EndActivity> {


	@Override
	public Class<EndActivity> getType() {
		return null;
	}

	/**
	 * Create a converging gateway conneted to a Signal node that will
	 * catch events sent by the Exit script nodes.  This will cause the 
	 * process to exit.
	 */
	@Override
	public boolean buildGraph(EndActivity end, ActivityGraph graph) {
		ExclusiveGatewayActivity ega = new ExclusiveGatewayActivity();
		ega.setDirection(GatewayActivity.CONVERGING);
		graph.addVertex(ega);
		moveIncomingEdges(graph, end, ega);
		graph.addEdge(ega, end);
		
		Signal signal = new Signal();
		signal.setSignalId("_exit");
		graph.addVertex(signal);
		graph.addEdge(signal, ega);
		
		return false;
		
	}

}
