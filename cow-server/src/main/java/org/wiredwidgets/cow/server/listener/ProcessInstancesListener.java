package org.wiredwidgets.cow.server.listener;

import org.wiredwidgets.cow.server.api.service.ProcessInstance;

public interface ProcessInstancesListener {

	public void onProcessStart(ProcessInstance processInstance);
	
	public void onProcessCompleted(ProcessInstance processInstance);
}
