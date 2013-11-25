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
