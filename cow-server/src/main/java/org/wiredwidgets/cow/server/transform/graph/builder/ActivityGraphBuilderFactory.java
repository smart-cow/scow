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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;

@Component
public class ActivityGraphBuilderFactory {
	
	// assumes all builders are marked @Component and scanned
	@Autowired
	Collection<ActivityGraphBuilder> builders;
	
	@Autowired
	BypassGraphBuilder bypassBuilder;
	
	private Map<Class<? extends Activity>, Set<ActivityGraphBuilder>> builderMap = 
			new HashMap<Class<? extends Activity>, Set<ActivityGraphBuilder>>();
	
	private static Logger log = Logger.getLogger(ActivityGraphBuilderFactory.class);
	
	private ActivityGraphBuilder getBuilder(Activity activity) {
		// bypass builder is a special case as we have to test the instance
		if (activity.isBypassable() && !activity.isWrapped() ) {
			return bypassBuilder;
		}
		
		Set<ActivityGraphBuilder> candidates = builderMap.get(activity.getClass());
		if (candidates != null) {
			if (candidates.size() == 1) {
				return candidates.iterator().next();
			}
			else {
				for (ActivityGraphBuilder builder : candidates) {
					if (builder.supports(activity)) {
						return builder;
					}
				}
			}
		}
		
		// no builder was found for this class
		// create a default builder for the class and add it to the map
		// so we go faster next time
		
		ActivityGraphBuilder defaultBuilder = defaultBuilder(activity.getClass());
		Set<ActivityGraphBuilder> s = new HashSet<ActivityGraphBuilder>();
		s.add(defaultBuilder);
		builderMap.put(activity.getClass(), s);
		return defaultBuilder;
	}
	
	public <T extends Activity> void buildGraph(T activity, ActivityGraph graph, Process process) {
		ActivityGraphBuilder builder = getBuilder(activity);
		builder.buildGraph(activity, graph, process);
	}
	
	private <T extends Activity> ActivityGraphBuilder defaultBuilder(Class<T> type) {
		return new DefaultGraphBuilder<T>(type);
	}
	
	@PostConstruct
	private void init() {
		for (ActivityGraphBuilder builder : builders) {
			// the Bypass builder returns null as it's a special case
			if (builder.getType() != null) {
				if (builderMap.get(builder.getType()) == null) {
					builderMap.put(builder.getType(), new HashSet<ActivityGraphBuilder>());
				}
				builderMap.get(builder.getType()).add(builder);
			}
		}
	}

}
