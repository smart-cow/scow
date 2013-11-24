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

@Component
public class ActivityGraphBuilderFactory {
	
	@Autowired
	Collection<ActivityGraphBuilder<?>> builders;
	
	@Autowired
	BypassGraphBuilder bypassBuilder;
	
	private Map<Class<? extends Activity>, Set<ActivityGraphBuilder<?>>> builderMap = 
			new HashMap<Class<? extends Activity>, Set<ActivityGraphBuilder<?>>>();
	
	private static Logger log = Logger.getLogger(ActivityGraphBuilderFactory.class);
	
	public  ActivityGraphBuilder<? extends Activity> getBuilder(Activity activity) {
		// bypass builder is a special case as we have to test the instance
		if (activity.isBypassable()) {
			return bypassBuilder;
		}
		
		Set<ActivityGraphBuilder<?>> candidates = builderMap.get(activity.getClass());
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
		
		ActivityGraphBuilder<?> defaultBuilder = defaultBuilder(activity.getClass());
		Set<ActivityGraphBuilder<?>> s = new HashSet<ActivityGraphBuilder<?>>();
		s.add(defaultBuilder);
		builderMap.put(activity.getClass(), s);
		return defaultBuilder;

	}
	
	private <T extends Activity> ActivityGraphBuilder<T> defaultBuilder(Class<T> type) {
		return new DefaultGraphBuilder<T>(type);
	}
	
	@PostConstruct
	private void init() {
		for (ActivityGraphBuilder<?> builder : builders) {
			// the Bypass builder returns null as it's a special case
			if (builder.getType() != null) {
				if (builderMap.get(builder.getType()) == null) {
					builderMap.put(builder.getType(), new HashSet<ActivityGraphBuilder<?>>());
				}
				builderMap.get(builder.getType()).add(builder);
			}
		}
	}

}
