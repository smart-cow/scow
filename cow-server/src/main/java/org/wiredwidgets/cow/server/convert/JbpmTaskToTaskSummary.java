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

package org.wiredwidgets.cow.server.convert;

import org.jbpm.task.I18NText;
import org.jbpm.task.Task;
import org.jbpm.task.query.TaskSummary;
import org.springframework.stereotype.Component;

@Component
public class JbpmTaskToTaskSummary extends AbstractConverter<Task, TaskSummary> {
	
	private static final String LANGUAGE = "en-UK";

	@Override
	public TaskSummary convert(Task task) {
		TaskSummary ts = new TaskSummary();
		ts.setId(task.getId());
		ts.setProcessInstanceId(task.getTaskData().getProcessInstanceId());
		if (task.getNames() != null && task.getNames().size() > 0) {
			ts.setName(I18NText.getLocalText(task.getNames(), LANGUAGE, LANGUAGE));
		}
		if (task.getSubjects() != null && task.getSubjects().size() > 0) {
			ts.setSubject(I18NText.getLocalText(task.getSubjects(), LANGUAGE, LANGUAGE));
		}
		if (task.getDescriptions() != null && task.getDescriptions().size() > 0) {
			ts.setDescription(I18NText.getLocalText(task.getDescriptions(), LANGUAGE, LANGUAGE));
		}
		ts.setStatus(task.getTaskData().getStatus());
		ts.setPriority(task.getPriority());
		ts.setSkipable(task.getTaskData().isSkipable());
		ts.setActualOwner(task.getTaskData().getActualOwner());
		ts.setCreatedBy(task.getTaskData().getCreatedBy());
		ts.setCreatedOn(task.getTaskData().getCreatedOn());
		
		// for some reason, no getter for this in TaskData ?????
		//ts.setActivationTime()
	
		ts.setExpirationTime(task.getTaskData().getExpirationTime());
		ts.setProcessId(task.getTaskData().getProcessId());
		ts.setProcessSessionId(task.getTaskData().getProcessSessionId());
		
		return ts;
	}

}
