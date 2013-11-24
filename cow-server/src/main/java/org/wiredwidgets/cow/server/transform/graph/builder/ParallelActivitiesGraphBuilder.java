package org.wiredwidgets.cow.server.transform.graph.builder;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;
import org.wiredwidgets.cow.server.transform.graph.activity.ComplexGatewayActivity;
import org.wiredwidgets.cow.server.transform.graph.activity.GatewayActivity;

@Component
public class ParallelActivitiesGraphBuilder extends AbstractGraphBuilder<Activities> {
	
	private static Logger log = Logger.getLogger(ParallelActivitiesGraphBuilder.class);

	@Override
	public boolean buildGraph(Activities activity, ActivityGraph graph) {
		
		GatewayActivity diverging = new ComplexGatewayActivity();
		diverging.setDirection(GatewayActivity.DIVERGING);
		diverging.setName("diverging");
		GatewayActivity converging = new ComplexGatewayActivity();
		converging.setName("converging");
		converging.setDirection(GatewayActivity.CONVERGING);
		graph.addVertex(diverging);
		graph.addVertex(converging);
		moveIncomingEdges(graph, activity, diverging);
		
		for (JAXBElement<? extends Activity> element : activity.getActivities()) {
			Activity current = element.getValue();
			graph.addVertex(current);
			graph.addEdge(diverging, current);
			graph.addEdge(current, converging);
		}
		
		// outgoing edges
		moveOutgoingEdges(graph, activity, converging);
		graph.removeVertex(activity);

		return true;
	}

	@Override
	public boolean supports(Activities activity) {
		return (!activity.isSequential());
	}

	@Override
	public Class<Activities> getType() {
		return Activities.class;
	}

}
