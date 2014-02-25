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

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

public class NodeGraph extends DefaultDirectedGraph<Bpmn20Node, Bpmn20Edge> {
	
	private static final long serialVersionUID = 1L;
	
	private static EdgeFactory<Bpmn20Node, Bpmn20Edge> ef = new ClassBasedEdgeFactory<Bpmn20Node, Bpmn20Edge>(Bpmn20Edge.class);
	
	public NodeGraph() {
		super(ef);		
	}

}
