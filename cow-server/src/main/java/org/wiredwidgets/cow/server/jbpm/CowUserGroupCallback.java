package org.wiredwidgets.cow.server.jbpm;

import java.util.List;

import org.kie.api.task.UserGroupCallback;

public class CowUserGroupCallback implements UserGroupCallback {

	@Override
	public boolean existsUser(String userId) {
		return true;
	}

	@Override
	public boolean existsGroup(String groupId) {
		return true;
	}

	@Override
	public List<String> getGroupsForUser(String userId, List<String> groupIds,
			List<String> allExistingGroupIds) {
		return null;
	}

}
