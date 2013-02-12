package org.wiredwidgets.cow.server.manager;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.workitem.wsht.GenericHTWorkItemHandler;
import org.jbpm.process.workitem.wsht.MinaHTWorkItemHandler;

public class WorkItemHandlerFactory {
		
	public static GenericHTWorkItemHandler createInstance(StatefulKnowledgeSession session, RestServiceTaskHandler handle ) {
		GenericHTWorkItemHandler handler = new MinaHTWorkItemHandler(session);
		session.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
		
		//RestServiceTaskHandler restHandler = new RestServiceTaskHandler();
		session.getWorkItemManager().registerWorkItemHandler("RestService", handle);

		return handler;
	}

}
