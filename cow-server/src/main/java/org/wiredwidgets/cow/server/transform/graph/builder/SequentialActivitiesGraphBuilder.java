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

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;

@Component
public class SequentialActivitiesGraphBuilder extends AbstractGraphBuilder<Activities> {
	
	private static Logger log = Logger.getLogger(SequentialActivitiesGraphBuilder.class);

	@Override
	protected void buildInternal(Activities activity, ActivityGraph graph, Process process) {
		
		
		Activity previous = null;
		Activity current = null;
		for (JAXBElement<? extends Activity> element : activity.getActivities()) {
			current = element.getValue();
			graph.addVertex(current);
			if (previous == null) {
				// first node
				// connect all incoming edges
				moveIncomingEdges(graph, activity, current);
			}
			else {
				log.debug("Adding edge from " + previous.getClass().getSimpleName() + " to " + current.getClass().getSimpleName());
				graph.addEdge(previous, current);
			}			
			previous = current;	
		}
		
		// outgoing edges
		moveOutgoingEdges(graph, activity, current);
		
		// build the activities
		for (JAXBElement<? extends Activity> element : activity.getActivities()) {
			factory.buildGraph(element.getValue(), graph, process);
		}
		
		graph.removeVertex(activity);

	}

	@Override
	protected boolean supportsInternal(Activities activity) {
		return (activity.isSequential());
	}

	@Override
	public Class<Activities> getType() {
		return Activities.class;
	}
	
}
