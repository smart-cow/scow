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
import org.wiredwidgets.cow.server.api.model.v2.Decision;
import org.wiredwidgets.cow.server.api.model.v2.Option;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.graph.ActivityEdge;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;
import org.wiredwidgets.cow.server.transform.graph.activity.DecisionTask;
import org.wiredwidgets.cow.server.transform.graph.activity.ExclusiveGatewayActivity;
import org.wiredwidgets.cow.server.transform.graph.activity.GatewayActivity;

@Component
public class DecisionGraphBuilder extends AbstractGraphBuilder<Decision> {
	
	// private static Logger log = LoggerFactory.getLogger(DecisionGraphBuilder.class);

	@Override
	protected void buildInternal(Decision decision, ActivityGraph graph, Process process) {
		
		DecisionTask dt = new DecisionTask(decision.getTask());
		
		// replace the Decision's existing task with the new DecisionTask
		// this is needed when we do completion evaluation
		decision.setTask(dt);
		
		// set the question
		dt.setQuestion(decision.getQuestion());

		graph.addVertex(dt);
		moveIncomingEdges(graph, decision, dt);
		
		if (decision.getOptions().size() == 1) {
			// A decision with only one option really should not be allowed from the UI.  However we will 
			// handle it anyway so that we don't build an invalid graph.
			// in this case we don't need to worry about expressions for the edge, as
			// there is only one path
		
			Activity option = decision.getOptions().get(0).getActivity().getValue();
			graph.addVertex(option);
			graph.addEdge(dt, option);
			moveOutgoingEdges(graph, decision, option);
			
			// the user has to choose something.  though it won't really matte in this case
			dt.addOption(option.getName());

			factory.buildGraph(option, graph, process);			
			
		}
		else {
		
			GatewayActivity diverging = new ExclusiveGatewayActivity();
			diverging.setDirection(GatewayActivity.DIVERGING);
			diverging.setName(getDivergingGatewayName(decision));
			graph.addVertex(diverging);
			graph.addEdge(dt, diverging);
			
			GatewayActivity converging = new ExclusiveGatewayActivity();
			converging.setDirection(GatewayActivity.CONVERGING);
			converging.setName(getConvergingGatewayName(decision));
			graph.addVertex(converging);
			
			moveOutgoingEdges(graph, decision, converging);
			
			for (Option option : decision.getOptions()) {
				
				ActivityEdge optionEdge = null;
				if (option.getActivity() != null) {
					Activity optionActivity = option.getActivity().getValue();
					graph.addVertex(optionActivity);
					optionEdge = graph.addEdge(diverging, optionActivity);
					
					// tie edge back to the DecisionTask
					// this is used for variable name handling
					optionEdge.setVarSource(dt);
					optionEdge.setExpression(option.getName());
					graph.addEdge(optionActivity, converging);
					factory.buildGraph(optionActivity, graph, process);
				}
				else {
					// Provide support for a "do nothing" path directly from diverging to converging
					optionEdge = graph.addEdge(diverging, converging);
					optionEdge.setVarSource(dt);
					optionEdge.setExpression(option.getName());
				}
						
				// we will need the options as inputs to the task
				dt.addOption(option.getName());
			}
		}

		graph.removeVertex(decision);
	}

	@Override
	public Class<Decision> getType() {
		return Decision.class;
	}
	
	public static String getDivergingGatewayName(Decision decision) {
		return decision.getName() + ":diverging";
	}
	
	public static String getConvergingGatewayName(Decision decision) {
		return decision.getName() + ":converging";
	}

}
