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

package org.wiredwidgets.cow.server.repo;

import java.util.Date;
import java.util.List;

import org.jbpm.services.task.impl.model.TaskImpl;
import org.kie.api.task.model.User;
import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends CrudRepository<TaskImpl, Long> {
	
	public List<TaskImpl> findByTaskDataProcessId(String id);
	
	public List<TaskImpl> findByTaskDataProcessInstanceId(Long id);
	
	// public List<TaskImpl> findByTaskDataActualOwnerAndTaskDataCompletedOnBetween(User owner, Date start, Date end);
	
    public TaskImpl findById(Long id);
}
