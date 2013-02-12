/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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
public class UsersServiceImpl extends AbstractCowServiceImpl implements UsersService {

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
        
        for (String ldapUser: users){
            if (ldapUser.equals(user.getId())){
                ldapHelper.updateUser(user);
                userExists = true;
                break;
            }
        }
        if (!userExists){
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
            return retUser;
        }
        return null;
    }

    @Override
    public boolean deleteGroup(String id) {
        return ldapHelper.deleteGroup(id);
    }
    
}
