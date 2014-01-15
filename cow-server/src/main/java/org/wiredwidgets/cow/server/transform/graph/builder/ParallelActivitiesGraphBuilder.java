package org.wiredwidgets.cow.server.transform.graph.builder;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
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
			diverging.setName("diverging");
			GatewayActivity converging = new ParallelGatewayActivity();
			converging.setName("converging");
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

}
