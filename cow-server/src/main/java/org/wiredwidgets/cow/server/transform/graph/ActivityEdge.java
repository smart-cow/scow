package org.wiredwidgets.cow.server.transform.graph;

import org.jgrapht.graph.DefaultEdge;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

public class ActivityEdge extends DefaultEdge {

	private static final long serialVersionUID = 1L;

	private String expression = null;
	
	private Activity varSource = null;
	
	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getExpression() {
		return this.expression;
	}

	/**
	 * Reference to an Activity that produces a variable that will be 
	 * evaluated in this edge.  Currently this is used only for Decision
	 * activities where the DecisionTask produces an output variable.  The
	 * link is needed the ID of the activity is used to create the variable
	 * name and the ID is not known at this stage.
	 * @return
	 */
	public Activity getVarSource() {
		return varSource;
	}

	public void setVarSource(Activity varSource) {
		this.varSource = varSource;
	}
	
	
	
}
