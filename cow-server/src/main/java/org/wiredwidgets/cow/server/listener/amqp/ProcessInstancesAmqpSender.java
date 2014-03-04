/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2014 The MITRE Corporation,
 * Licensed under the Apache License,
 * Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

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
   
		sender.send(pid, PROC_INSTANCE_CATEGORY, action, pi);
	}

}
