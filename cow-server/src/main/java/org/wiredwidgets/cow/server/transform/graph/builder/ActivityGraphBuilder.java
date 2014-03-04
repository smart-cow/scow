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

import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;

public interface ActivityGraphBuilder {
	
	/**
	 * Inserts the specified activity into the graph using whatever
	 * structure is appropriate.  Each Activity subclass will implement this
	 * to handle its particular graph structure.
	 * @param activity
	 * @param graph
	 * @param process
	 * @return true if another pass at the graph will be needed after this is done.
	 * If the inserted graph fragment includes an Activity of an unspecified
	 * type, then the method should return TRUE as this Activity will also need
	 * a graph built for it.  If the inserted graph fragment is complete then
	 * the method should return false.  The graph builder process will iterate
	 * though the vertices of the graph
	 * until all graph nodes have been fully built, i.e. until all graph builders
	 * return FALSE.
	 */
	public void buildGraph(Activity activity, ActivityGraph graph, Process process);
	
	/**
	 * Determines if this graph builder supports the specified activity instance
	 * @param activity
	 * @return
	 */
	public boolean supports(Activity activity);
	
	/**
	 * Indicates the class of Activity that this builder supports.
	 * This should be a concrete type.  If more than one builder returns the same
	 * value for getType then supports() will be called to select one.
	 * @return
	 */
	public Class<? extends Activity> getType();

}
