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

import java.util.List;

import org.jbpm.eventmessaging.EventKey;
import org.jbpm.eventmessaging.EventResponseHandler;
import org.jbpm.task.Attachment;
import org.jbpm.task.Comment;
import org.jbpm.task.Content;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.FaultData;
import org.springframework.beans.factory.annotation.Autowired;

public class ThreadLocalLocalTaskService implements TaskService {
	
	@Autowired
	TaskServiceFactory factory;

	@Override
	public void activate(long taskId, String userId) {
		factory.getTaskService().activate(taskId, userId);
	}

	@Override
	public void addAttachment(long taskId, Attachment attachment,
			Content content) {
		factory.getTaskService().addAttachment(taskId, attachment, content);
	}

	@Override
	public void addComment(long taskId, Comment comment) {
		factory.getTaskService().addComment(taskId, comment);
	}

	@Override
	public void addTask(Task task, ContentData content) {
		factory.getTaskService().addTask(task, content);
	}

	@Override
	public void claim(long taskId, String userId) {
		factory.getTaskService().claim(taskId, userId);
	}

	@Override
	@Deprecated
	public void claim(long taskId, String userId, List<String> groupIds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void claimNextAvailable(String userId, String language) {
		factory.getTaskService().claimNextAvailable(userId, language);	
	}

	@Override
	@Deprecated
	public void claimNextAvailable(String userId, List<String> groupIds,
			String language) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void complete(long taskId, String userId, ContentData outputData) {
		factory.getTaskService().complete(taskId, userId, outputData);
	}

	@Override
	public void completeWithResults(long taskId, String userId, Object results) {
		factory.getTaskService().completeWithResults(taskId, userId, results);	
	}

	@Override
	public boolean connect() {
		return factory.getTaskService().connect();
	}

	@Override
	public boolean connect(String address, int port) {
		return factory.getTaskService().connect(address, port);
	}

	@Override
	public void delegate(long taskId, String userId, String targetUserId) {
		factory.getTaskService().delegate(taskId, userId, targetUserId);
	}

	@Override
	public void deleteAttachment(long taskId, long attachmentId, long contentId) {
		factory.getTaskService().deleteAttachment(taskId, attachmentId, contentId);
	}

	@Override
	public void deleteComment(long taskId, long commentId) {
		factory.getTaskService().deleteComment(taskId, commentId);
	}

	@Override
	public void deleteFault(long taskId, String userId) {
		factory.getTaskService().deleteFault(taskId, userId);
	}

	@Override
	public void deleteOutput(long taskId, String userId) {
		factory.getTaskService().deleteOutput(taskId, userId);
	}

	@Override
	public void disconnect() throws Exception {
		factory.getTaskService().disconnect();	
	}

	@Override
	public void exit(long taskId, String userId) {
		factory.getTaskService().exit(taskId, userId);
	}

	@Override
	public void fail(long taskId, String userId, FaultData faultData) {
		factory.getTaskService().fail(taskId, userId, faultData);
	}

	@Override
	public void forward(long taskId, String userId, String targetEntityId) {
		factory.getTaskService().forward(taskId, userId, targetEntityId);	
	}

	@Override
	public Content getContent(long contentId) {
		return factory.getTaskService().getContent(contentId);
	}

	@Override
	public List<TaskSummary> getSubTasksAssignedAsPotentialOwner(long parentId,
			String userId, String language) {
		return factory.getTaskService().getSubTasksAssignedAsPotentialOwner(parentId, userId, language);
	}

	@Override
	public List<TaskSummary> getSubTasksByParent(long parentId) {
		return factory.getTaskService().getSubTasksByParent(parentId);
	}

	@Override
	public Task getTask(long taskId) {
		return factory.getTaskService().getTask(taskId);
	}

	@Override
	public Task getTaskByWorkItemId(long workItemId) {
		return factory.getTaskService().getTaskByWorkItemId(workItemId);
	}

	@Override
	public List<TaskSummary> getTasksAssignedAsBusinessAdministrator(String userId, String language) {
		return factory.getTaskService().getTasksAssignedAsBusinessAdministrator(userId, language);
	}

	@Override
	public List<TaskSummary> getTasksAssignedAsExcludedOwner(String userId,
			String language) {
		return factory.getTaskService().getTasksAssignedAsExcludedOwner(userId, language);
	}

	@Override
	public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId,
			String language) {
		return factory.getTaskService().getTasksAssignedAsPotentialOwner(userId, language);
	}

	@Override
	@Deprecated
	public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId,
			List<String> groupIds, String language) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId,
			List<String> groupIds, String language, int firstResult,
			int maxResult) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TaskSummary> getTasksAssignedAsRecipient(String userId,
			String language) {
		return factory.getTaskService().getTasksAssignedAsRecipient(userId, language);
	}

	@Override
	public List<TaskSummary> getTasksAssignedAsTaskInitiator(String userId,
			String language) {
		return factory.getTaskService().getTasksAssignedAsTaskInitiator(userId, language);
	}

	@Override
	public List<TaskSummary> getTasksAssignedAsTaskStakeholder(String userId,
			String language) {
		return factory.getTaskService().getTasksAssignedAsTaskStakeholder(userId, language);
	}

	@Override
	public List<TaskSummary> getTasksOwned(String userId, String language) {
		return factory.getTaskService().getTasksOwned(userId, language);
	}

	@Override
	public List<TaskSummary> getTasksOwned(String userId, List<Status> status,
			String language) {
		return factory.getTaskService().getTasksOwned(userId, status, language);
	}

	@Override
	public void nominate(long taskId, String userId,
			List<OrganizationalEntity> potentialOwners) {
		factory.getTaskService().nominate(taskId, userId, potentialOwners);
	}

	@Override
	@Deprecated
	public List<?> query(String qlString, Integer size, Integer offset) {
		return factory.getTaskService().query(qlString, size, offset);
	}

	@Override
	public void register(long taskId, String userId) {
		factory.getTaskService().register(taskId, userId);
	}

	@Override
	public void registerForEvent(EventKey key, boolean remove,
			EventResponseHandler responseHandler) {
		factory.getTaskService().registerForEvent(key, remove, responseHandler);
	}

	@Override
	public void unregisterForEvent(EventKey key) {
		factory.getTaskService().unregisterForEvent(key);
	}

	@Override
	public void release(long taskId, String userId) {
		factory.getTaskService().release(taskId, userId);
	}

	@Override
	public void remove(long taskId, String userId) {
		factory.getTaskService().remove(taskId, userId);
	}

	@Override
	public void resume(long taskId, String userId) {
		factory.getTaskService().resume(taskId, userId);
	}

	@Override
	public void setDocumentContent(long taskId, Content content) {
		factory.getTaskService().setDocumentContent(taskId, content);
	}

	@Override
	public void setFault(long taskId, String userId, FaultData fault) {
		factory.getTaskService().setFault(taskId, userId, fault);	
	}

	@Override
	public void setOutput(long taskId, String userId,
			ContentData outputContentData) {
		factory.getTaskService().setOutput(taskId, userId, outputContentData);
	}

	@Override
	public void setPriority(long taskId, String userId, int priority) {
		factory.getTaskService().setPriority(taskId, userId, priority);
	}

	@Override
	public void skip(long taskId, String userId) {
		factory.getTaskService().skip(taskId, userId);
	}

	@Override
	public void start(long taskId, String userId) {
		factory.getTaskService().start(taskId, userId);
	}

	@Override
	public void stop(long taskId, String userId) {
		factory.getTaskService().stop(taskId, userId);
	}

	@Override
	public void suspend(long taskId, String userId) {
		factory.getTaskService().suspend(taskId, userId);
	}

	@Override
	public List<TaskSummary> getTasksAssignedAsPotentialOwnerByStatus(
			String userId, List<Status> status, String language) {
		return factory.getTaskService().getTasksAssignedAsPotentialOwnerByStatus(userId, status, language);
	}

	@Override
	@Deprecated
	public List<TaskSummary> getTasksAssignedAsPotentialOwnerByStatusByGroup(
			String userId, List<String> groupIds, List<Status> status,
			String language) {
		return factory.getTaskService().getTasksAssignedAsPotentialOwnerByStatusByGroup(userId, groupIds, status, language);
	}

	@Override
	public List<TaskSummary> getTasksByStatusByProcessId(
			long processInstanceId, List<Status> status, String language) {
		return factory.getTaskService().getTasksByStatusByProcessId(processInstanceId, status, language);
	}

	@Override
	public List<TaskSummary> getTasksByStatusByProcessIdByTaskName(
			long processInstanceId, List<Status> status, String taskName,
			String language) {
		return factory.getTaskService().getTasksByStatusByProcessId(processInstanceId, status, language);
	}

}
