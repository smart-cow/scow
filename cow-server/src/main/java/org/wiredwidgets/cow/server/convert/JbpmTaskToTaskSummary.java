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
