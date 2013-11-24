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
	private Set<NodeBuilder<?>> builders;
	
	private Map<Class<?>, NodeBuilder<?>> builderMap = new HashMap<Class<?>, NodeBuilder<?>>();
	
	@SuppressWarnings("unchecked")
	private <T extends Activity> NodeBuilder<T> getNodeBuilder(Class<T> activityClass) {
		if (builderMap.get(activityClass) != null) {
			return (NodeBuilder<T>)builderMap.get(activityClass);
		}
		
		for (NodeBuilder<? extends Activity> nodeBuilder : builders) {
			if (nodeBuilder.getType().equals(activityClass)) {
				builderMap.put(activityClass, nodeBuilder);
				return (NodeBuilder<T>)nodeBuilder;
			}
		}
		log.error("No builder found for type " + activityClass.getSimpleName());
		return null;
	}
	
	public <T extends Activity> Bpmn20Node buildNode(T activity, Bpmn20ProcessContext context) {
		@SuppressWarnings("unchecked")
		NodeBuilder<T> builder = (NodeBuilder<T>) getNodeBuilder(activity.getClass());
		return builder.build(activity, context);
	}

}
