package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import org.jgrapht.graph.DefaultEdge;
import org.wiredwidgets.cow.server.transform.graph.ActivityEdge;

public class Bpmn20Edge extends DefaultEdge {

	private static final long serialVersionUID = 1L;
	
	private ActivityEdge activityEdge;
	

	/**
	 * Reference to the ActivityEdge corresponding to this 
	 * BPMN20 Edge.  In some cases we may need access to properties
	 * of this edge while building the process.
	 * @return
	 */
	public ActivityEdge getActivityEdge() {
		return activityEdge;
	}

	public void setActivityEdge(ActivityEdge activityEdge) {
		this.activityEdge = activityEdge;
	} 
	

}
