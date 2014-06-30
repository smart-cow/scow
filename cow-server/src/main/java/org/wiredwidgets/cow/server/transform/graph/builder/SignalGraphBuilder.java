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
