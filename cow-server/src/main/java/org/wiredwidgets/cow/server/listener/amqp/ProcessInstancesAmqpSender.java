package org.wiredwidgets.cow.server.listener.amqp;

import org.springframework.beans.factory.annotation.Autowired;
import org.wiredwidgets.cow.server.api.service.ProcessInstance;
import org.wiredwidgets.cow.server.listener.ProcessInstancesListener;

public class ProcessInstancesAmqpSender implements ProcessInstancesListener {

	private static final String PROC_INSTANCE_CATEGORY = "processInstances";
	
	@Autowired
	AmqpSender sender;
	
	
	@Override
	public void onProcessStart(ProcessInstance processInstance) {
		send("start", processInstance);
	}

	@Override
	public void onProcessCompleted(ProcessInstance processInstance) {
		send("complete", processInstance);
		
	}
	
	
	private void send(String action, ProcessInstance pi) {
        String pid = pi.getId();
        if (!pid.contains(".")) {
        	pid = pi.getName() + '.' + pid;
        }
   
		sender.send(pid, PROC_INSTANCE_CATEGORY, action, pi, null);
	}

}
