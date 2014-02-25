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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;

@Component
public class NodeBuilderService {
	
	private static Logger log = Logger.getLogger(NodeBuilderService.class);
	
	@Autowired
	private Set<NodeBuilder> builders;
	
	private Map<Class<?>, NodeBuilder> builderMap = new HashMap<Class<?>, NodeBuilder>();
	
	private <T extends Activity> NodeBuilder getNodeBuilder(Class<T> activityClass) {
		if (builderMap.get(activityClass) != null) {
			return builderMap.get(activityClass);
		}
		
		for (NodeBuilder nodeBuilder : builders) {
			if (nodeBuilder.getType().equals(activityClass)) {
				builderMap.put(activityClass, nodeBuilder);
				return nodeBuilder;
			}
		}
		log.error("No builder found for type " + activityClass.getSimpleName());
		return null;
	}
	
	public Bpmn20Node buildNode(Activity activity, Bpmn20ProcessContext context) {
		NodeBuilder builder = getNodeBuilder(activity.getClass());
		return builder.build(activity, context);
	}

}
