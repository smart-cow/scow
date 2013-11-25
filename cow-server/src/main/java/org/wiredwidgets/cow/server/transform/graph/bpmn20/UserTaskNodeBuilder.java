package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Task;

@Component
public class UserTaskNodeBuilder extends AbstractUserTaskNodeBuilder<Task> {
	
	@Override
	public Class<Task> getType() {
		return Task.class;
	}

}
