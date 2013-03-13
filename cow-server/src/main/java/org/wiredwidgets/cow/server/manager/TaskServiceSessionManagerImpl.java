/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jbpm.task.Group;
import org.jbpm.task.User;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.wiredwidgets.cow.server.helper.LDAPHelper;

/**
 *
 * @author FITZPATRICK
 */
@Component
@Transactional
public class TaskServiceSessionManagerImpl implements TaskServiceSessionManager {

    private static Logger log = Logger.getLogger(TaskServiceSessionManagerImpl.class);
    
    @Autowired
    TaskService jbpmTaskService;
    
    TaskServiceSession jbpmTaskServiceSession;
    
    @Autowired
    LDAPHelper ldapHelper;

    public void initLdap() {
        jbpmTaskServiceSession = jbpmTaskService.createSession();
        addUserGroupToSession();
    }

    private void addUserGroupToSession() {
        for (String group : ldapHelper.getLDAPGroups()/*getAllGroups()*/) {
            jbpmTaskServiceSession.addGroup(new Group(group));
        }

        List <String> users = new ArrayList<String>();
        for (String username : ldapHelper.getLDAPUsers()/*getDefaultUsers()*/) {
            jbpmTaskServiceSession.addUser(new User(username));
            users.add(username);
        }
    }

    /*private List<String> getAllGroups() {
        List<String> allGroups = new ArrayList<String>();
        allGroups.add("group1");
        allGroups.add("SIDO");
        allGroups.add("DT");
        allGroups.add("DOC");
        allGroups.add("DAC");
        allGroups.add("COA");
        allGroups.add("AM");

        return allGroups;
    }

    private List<String> getDefaultUsers() {
        List<String> allUsers = new ArrayList<String>();
        allUsers.add("Administrator");
        allUsers.add("shawn");
        allUsers.add("lew");
        allUsers.add("jon");
        allUsers.add("matt");
        allUsers.add("prema");
        return allUsers;
    }*/

}
