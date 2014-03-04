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
