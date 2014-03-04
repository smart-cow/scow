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
import org.wiredwidgets.cow.server.api.model.v2.Signal;
import org.wiredwidgets.cow.server.api.model.v2.Process;
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
	 * Create a converging gateway connected to a Signal node that will
	 * catch events sent by the Exit script nodes.  This will cause the 
	 * process to exit.
	 */
	@Override
	protected void buildInternal(EndActivity end, ActivityGraph graph, Process process) {
		ExclusiveGatewayActivity ega = new ExclusiveGatewayActivity();
		ega.setDirection(GatewayActivity.CONVERGING);
		graph.addVertex(ega);
		moveIncomingEdges(graph, end, ega);
		graph.addEdge(ega, end);
		
		Signal signal = new Signal();
		signal.setSignalId("_exit");
		graph.addVertex(signal);
		graph.addEdge(signal, ega);	
	}

}
