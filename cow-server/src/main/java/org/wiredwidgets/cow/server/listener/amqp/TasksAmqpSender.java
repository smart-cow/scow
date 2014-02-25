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
import org.wiredwidgets.cow.server.listener.TasksEventListener;

public class TasksAmqpSender implements TasksEventListener {

	private static final String TASK_CATEGORY = "tasks";
	
	@Autowired
	AmqpSender sender;
	

	@Override
	public void onCreateTask(EventParameters evtParams) {
		send("create", evtParams);
	}


	
	@Override
	public void onCompleteTask(EventParameters evtParams) {
		send("complete", evtParams);
		
	}


	@Override
	public void onTakeTask(EventParameters evtParams) {
		send("take", evtParams);
	}
	
	
	
	private void send(String action, EventParameters evtParams) {
		sender.send(
				evtParams.getTask().getProcessInstanceId(), 
				TASK_CATEGORY, 
				action, 
				evtParams.getTask(), 
				evtParams.getGroups(), 
				evtParams.getUsers());
	}
}
