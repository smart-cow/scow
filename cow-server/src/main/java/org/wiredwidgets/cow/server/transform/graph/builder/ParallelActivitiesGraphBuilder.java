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
import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;
import org.wiredwidgets.cow.server.transform.graph.activity.ExclusiveGatewayActivity;
import org.wiredwidgets.cow.server.transform.graph.activity.GatewayActivity;
import org.wiredwidgets.cow.server.transform.graph.activity.ParallelGatewayActivity;

@Component
public class ParallelActivitiesGraphBuilder extends AbstractGraphBuilder<Activities> {
	
	private static Logger log = Logger.getLogger(ParallelActivitiesGraphBuilder.class);

	@Override
	protected void buildInternal(Activities activity, ActivityGraph graph, Process process) {
		
		// special case if there is only one activity.  JBPM does not allow gateways with only one path.
		if (activity.getActivities().size() == 1) {
			Activity single = activity.getActivities().get(0).getValue();
			graph.addVertex(single);
			moveIncomingEdges(graph, activity, single);
			moveOutgoingEdges(graph, activity, single);
			factory.buildGraph(single, graph, process);
		}
		else { 
			// two or more activities.  Use gateways
			GatewayActivity diverging = new ParallelGatewayActivity();
			diverging.setDirection(GatewayActivity.DIVERGING);
			diverging.setName(getDivergingGatewayName(activity));
			
			GatewayActivity converging = null;
			
			// special case for a "race condition" parallel set
			// in this case the first path to complete triggers the gateway
			if (activity.getMergeCondition() != null && activity.getMergeCondition().equals("1")) {
				converging = new ExclusiveGatewayActivity();
			}
			else {
				converging = new ParallelGatewayActivity();
			}
			
			converging.setName(getConvergingGatewayName(activity));
			converging.setDirection(GatewayActivity.CONVERGING);
			graph.addVertex(diverging);
			graph.addVertex(converging);
			moveIncomingEdges(graph, activity, diverging);
			moveOutgoingEdges(graph, activity, converging);
			
			for (JAXBElement<? extends Activity> element : activity.getActivities()) {
				Activity current = element.getValue();
				graph.addVertex(current);
				graph.addEdge(diverging, current);
				graph.addEdge(current, converging);
				factory.buildGraph(current, graph, process);
			}
		}
		
		graph.removeVertex(activity);
	}

	@Override
	protected boolean supportsInternal(Activities activity) {
		return (!activity.isSequential());
	}

	@Override
	public Class<Activities> getType() {
		return Activities.class;
	}
	
	public static String getDivergingGatewayName(Activities activities) {
		return activities.getName() + ":diverging";
	}
	
	public static String getConvergingGatewayName(Activities activities) {
		return activities.getName() + ":converging";
	}

}
