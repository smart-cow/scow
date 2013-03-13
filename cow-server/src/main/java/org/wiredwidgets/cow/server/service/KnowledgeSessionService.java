package org.wiredwidgets.cow.server.service;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.workitem.wsht.GenericHTWorkItemHandler;
import org.wiredwidgets.cow.server.manager.RestServiceTaskHandler;

public interface KnowledgeSessionService {

	public abstract StatefulKnowledgeSession createInstance();

	public abstract GenericHTWorkItemHandler createWorkItemHandler(StatefulKnowledgeSession session, RestServiceTaskHandler handle,
			org.jbpm.task.TaskService taskClient);

}
