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

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Exit;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.graph.ActivityEdge;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;

@Component
public class ExitGraphBuilder extends AbstractGraphBuilder<Exit> {
	
	private static Logger log = Logger.getLogger(ExitGraphBuilder.class);

	@Override
	protected void buildInternal(Exit exit, ActivityGraph graph, Process process) {
		
		// remove any outgoing edges
		// note we must create a collection here to avoid concurrentModificationException
		// Collection<ActivityEdge> edges = new HashSet<ActivityEdge>();
		// edges.addAll(graph.outgoingEdgesOf(exit));	
		// graph.removeAllEdges(edges);
		
		// XXX *** change this to leave outgoing edges in place 
		// otherwise we have a possibility of ending up with a converging gateway
		// with only one incoming path which is not valid in jbpm.
		// consider changing in the future so that we remove the outgoing edges and then
		// modify the graph as necessary to make it valid. (this could get tricky...)
		
	}

	@Override
	public Class<Exit> getType() {
		return Exit.class;
	}

}
