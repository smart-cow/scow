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

package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Task;

@Component
public class UserTaskNodeBuilder extends AbstractUserTaskNodeBuilder<Task> {
	
	public static String TASK_INPUT_VARIABLES_NAME = "Variables";
	public static String TASK_OUTPUT_VARIABLES_NAME = "Variables";	
	
	@Override
	public Class<Task> getType() {
		return Task.class;
	}
	
	/**
	 * Variables used by the system that should not be displayed to users.
	 * @return
	 */
    public static Set<String> getSystemVariableNames() {
    	Set<String> varNames = new HashSet<String>();
    	varNames.add(TASK_INPUT_VARIABLES_NAME);
    	varNames.add(TASK_VARIABLES_INFO);
    	varNames.add("ProcessInstanceName");
    	varNames.add("Options");
    	varNames.add("Comment");
    	varNames.add("TaskName");    	
    	varNames.add("ActorId");   
    	varNames.add("GroupId"); 
    	return varNames;
    }	

}
