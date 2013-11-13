/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.convert;

import org.jbpm.task.query.TaskSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.service.Task;


@Component
public class JbpmTaskToSc2Task extends AbstractConverter<org.jbpm.task.Task, Task> {

	@Autowired
	private ConversionService converter;
	
    @Override
    public Task convert(org.jbpm.task.Task task) {
    	// convert to TaskSummary first
    	// allows us to consolidate on a single converter to avoid code duplication
        return converter.convert(converter.convert(task, TaskSummary.class), Task.class);
    }
}
