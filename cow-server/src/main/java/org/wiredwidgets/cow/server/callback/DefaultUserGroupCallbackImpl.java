/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jbpm.task.identity.UserGroupCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author FITZPATRICK
 */
public class DefaultUserGroupCallbackImpl implements UserGroupCallback{
    //@Autowired
    //HashMap userGroups;
    
    @Override
    public boolean existsUser(String userId) {
        return true;
    }

    @Override
    public boolean existsGroup(String groupId) {
        return true;
    }

    @Override
    public List<String> getGroupsForUser(String userId, List<String> groupIds, List<String> allExistingGroupIds) {
        /*if (userGroups.containsKey(userId)){
            return (List<String>)userGroups.get(userId);
        } else{
            return new ArrayList<String>();
        }*/
        return null;
    }
    
}
