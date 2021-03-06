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
package org.wiredwidgets.cow.server.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.wiredwidgets.cow.server.api.service.Group;
import org.wiredwidgets.cow.server.api.service.Membership;
import org.wiredwidgets.cow.server.api.service.User;
import org.wiredwidgets.cow.server.helper.LDAPHelper;

/**
 *
 * @author FITZPATRICK
 */
@Transactional
@Component
public class UsersServiceImpl extends AbstractCowServiceImpl implements UsersService, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    LDAPHelper ldapHelper;
    
    private static TypeDescriptor JBPM_USER_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(User.class));
    private static TypeDescriptor JBPM_GROUP_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Group.class));
    private static TypeDescriptor COW_USER_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.wiredwidgets.cow.server.api.service.User.class));
    private static TypeDescriptor COW_GROUP_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.wiredwidgets.cow.server.api.service.Group.class));
    
   

    @Override
    public String createGroup(String groupName) {
        return ldapHelper.createGroup(groupName);
    }

    @Override
    public void createOrUpdateUser(User user) {
        List<String> groups = ldapHelper.getLDAPGroups();
        List<String> users = ldapHelper.getLDAPUsers();
        boolean userExists = false;
        
        Map <String, String> groupMap = new HashMap<String, String>();
        
        // Create group map to quickly search to see if group exists
        for (String group: groups){
            groupMap.put(group, "1");
        }
        
        for (Membership membership: user.getMemberships()){
            if (!groupMap.containsKey(membership.getGroup())){
                ldapHelper.createGroup(membership.getGroup());
            }
        }
        
        if (users.contains(user.getId())) {
        	ldapHelper.updateUser(user);
        }
        else {
            ldapHelper.createUser(user);
        }
    }

    @Override
    public boolean deleteUser(String id) {
        return ldapHelper.deleteUser(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Group> findAllGroups() {
        List<String> groups = ldapHelper.getLDAPGroups();
        
        List<Group> retGroups = new ArrayList<Group>();
        for (String group: groups){
            Group temp = new Group();
            temp.setId(group);
            temp.setName(group);
            
            retGroups.add(temp);
        }
        
        return retGroups;
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> findAllUsers() {
        List<String> users = ldapHelper.getLDAPUsers();
        List<User> retUsers = new ArrayList<User>();
        for (String user: users){
            User temp = new User();
            temp.setId(user);
            
            addGroups(temp);
            
            retUsers.add(temp);
        }
        return retUsers;
    }

    @Transactional(readOnly = true)
    @Override
    public Group findGroup(String id) {
        List<String> groups = ldapHelper.getLDAPGroups();
        
        if (groups.contains(id)){
            Group retGroup = new Group();
            retGroup.setId(id);
            retGroup.setName(id);
            return retGroup;
        } 
        
        return null;
    }

    @Transactional(readOnly = true)
    @Override
    public User findUser(String id) {
        List<String> users = ldapHelper.getLDAPUsers();
        
        if (users.contains(id)){
            User retUser = new User();
            retUser.setId(id);
            addGroups(retUser);  
                     
            return retUser;
        }
        return null;
    }

    @Override
    public boolean deleteGroup(String id) {
        return ldapHelper.deleteGroup(id);
    }
    
    private void addGroups(User user) {
        Map<String, String> groups = ldapHelper.getUsersGroups(user.getId());
        for (String key : groups.keySet()) {
        	Membership m = new Membership();
        	m.setGroup(key);
        	user.getMemberships().add(m);
        }        	
    }

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// Verify minimal required setup
		
		List<String> groups = ldapHelper.getLDAPGroups();
		if (!groups.contains("user")) {
			createGroup("user");
			groups.add("user");
		}
		
		User admin = findUser("Administrator");
		if (admin == null) {
			admin = new User();
			admin.setId("Administrator");
		}
		
		// get groups as a Set
		Set<String> userGroups = getGroupSet(admin);
		
		for (String group : groups) {
			if (!userGroups.contains(group)) {
				Membership m = new Membership();
				m.setGroup(group);
				admin.getMemberships().add(m);
			}
		}
		createOrUpdateUser(admin);
		
	}
	
	private Set<String> getGroupSet(User user) {
		Set<String> groups = new HashSet<String>();
		for (Membership m : user.getMemberships()) {
			groups.add(m.getGroup());
		}
		return groups;
	}
	
    
}
