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
