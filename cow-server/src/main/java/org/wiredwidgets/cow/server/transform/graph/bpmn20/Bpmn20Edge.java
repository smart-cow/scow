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
