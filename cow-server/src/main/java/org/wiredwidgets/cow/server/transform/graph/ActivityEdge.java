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
