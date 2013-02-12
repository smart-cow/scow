package org.wiredwidgets.cow.server.repo;

import java.util.Date;
import java.util.List;

import org.jbpm.task.Task;
import org.jbpm.task.User;
import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends CrudRepository<Task, Long> {
	
	public List<Task> findByTaskDataProcessId(String id);
	
	public List<Task> findByTaskDataProcessInstanceId(Long id);
	
	public List<Task> findByTaskDataActualOwnerAndTaskDataCompletedOnBetween(User owner, Date start, Date end);
	
        public Task findById(Long id);
}
