package org.wiredwidgets.cow.server.jbpm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.runtime.manager.impl.DefaultRegisterableItemsFactory;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CowRegisterableItemsFactory extends DefaultRegisterableItemsFactory {
	
	@Autowired
	EventDrivenTaskHandler edTaskHandler;
	
	@Autowired
	AuditTaskHandler auditHandler;
	
	@Autowired
	EdaProcessEventListener edaListener;
	
	@Override
	public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
		Map<String, WorkItemHandler> handlers = super.getWorkItemHandlers(runtime);
		handlers.put("Task", edTaskHandler);
		handlers.put("Audit", auditHandler);
		return handlers;
	}
	
	@Override
	public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
		// don't register JPA logger
		List<ProcessEventListener> listeners = new ArrayList<ProcessEventListener>();
		listeners.add(edaListener);
		return listeners;
	}
	
	@Override
	public List<TaskEventListener> getTaskEventListeners(RuntimeEngine runtime) {
		
	}

}
