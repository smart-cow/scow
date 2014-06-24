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

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.transform.graph.activity.StartActivity;

public class ActivityGraph extends DefaultDirectedGraph<Activity, ActivityEdge> {
	
	private static final long serialVersionUID = 1L;
	
	private StartActivity start;
	
	private static EdgeFactory<Activity, ActivityEdge> ef = new ClassBasedEdgeFactory<Activity, ActivityEdge>(ActivityEdge.class);
	
	// keep a list of all activities, including those that don't end up
	// as vertices in the graph.  this gives us a way to iterate through all
	// the activities
	private List<Activity> activities = new ArrayList<Activity>();
	
	public ActivityGraph() {
		super(ef);		
	}
	
	public void setStart(StartActivity start) {
		this.start = start;
	}
	
	public StartActivity getStart() {
		return this.start;
	}

	public List<Activity> getActivities() {
		return activities;
	}


}
