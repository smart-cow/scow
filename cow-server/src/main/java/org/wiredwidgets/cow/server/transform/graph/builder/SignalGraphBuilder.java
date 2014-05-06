package org.wiredwidgets.cow.server.transform.graph.builder;

import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.model.v2.Signal;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;
import org.wiredwidgets.cow.server.transform.graph.activity.GatewayActivity;
import org.wiredwidgets.cow.server.transform.graph.activity.ParallelGatewayActivity;

@Component
public class SignalGraphBuilder extends AbstractGraphBuilder<Signal> {

	@Override
	public Class<Signal> getType() {
		return Signal.class;
	}

	@Override
	protected void buildInternal(Signal signal, ActivityGraph graph,
			Process process) {
		
		// Create a new converging gateway and connect the signal's incoming edge(s) 
		// to the gateway, and then connect the signal to the gateway.
		
		GatewayActivity converging = new ParallelGatewayActivity();
		converging.setName("converging");
		converging.setDirection(GatewayActivity.CONVERGING);
		graph.addVertex(converging);
		
		moveIncomingEdges(graph, signal, converging);
		moveOutgoingEdges(graph, signal, converging);
		graph.addEdge(signal, converging);	
	}

}
