package org.wiredwidgets.cow.server.manager;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.workitem.wsht.GenericHTWorkItemHandler;
import org.jbpm.task.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiredwidgets.cow.server.service.KnowledgeSessionService;

public class WorkItemHandlerFactory {
	
	@Autowired
	private KnowledgeSessionService kss;
		
	public GenericHTWorkItemHandler createInstance(StatefulKnowledgeSession session, RestServiceTaskHandler handle, TaskService taskClient ) {
		// call via service so it can be wrapped in a proxy transaction
		return kss.createWorkItemHandler(session, handle, taskClient);
	}

}
