package org.wiredwidgets.cow.server.transform.graph.builder;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activities;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;

@Component
public class SequentialActivitiesGraphBuilder extends AbstractGraphBuilder<Activities> {
	
	private static Logger log = Logger.getLogger(SequentialActivitiesGraphBuilder.class);

	@Override
	public boolean buildGraph(Activities activity, ActivityGraph graph) {
		
		
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
		graph.removeVertex(activity);

		return true;
	}

	@Override
	public boolean supports(Activities activity) {
		return (activity.isSequential());
	}

	@Override
	public Class<Activities> getType() {
		return Activities.class;
	}

}
