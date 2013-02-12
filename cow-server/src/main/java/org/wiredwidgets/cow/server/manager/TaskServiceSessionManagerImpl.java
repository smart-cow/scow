/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.manager;

import java.util.*;
import org.apache.log4j.Logger;
import org.jbpm.task.Group;
import org.jbpm.task.User;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.wiredwidgets.cow.server.helper.LDAPHelper;

/**
 *
 * @author FITZPATRICK
 */
@Transactional
public class TaskServiceSessionManagerImpl implements TaskServiceSessionManager {

    private static Logger log = Logger.getLogger(TaskServiceSessionManagerImpl.class);
    
    TaskService jbpmTaskService;
    TaskServiceSession jbpmTaskServiceSession;
    LDAPHelper ldapHelper;

    @Override
    public void init() {
        jbpmTaskServiceSession = jbpmTaskService.createSession();
        addUserGroupToSession();
    }

    @Override
    public TaskService getjbpmTaskService() {
        return jbpmTaskService;
    }

    @Override
    public void setjbpmTaskService(TaskService jbpmTaskService) {
        this.jbpmTaskService = jbpmTaskService;
    }
    
    @Override
    public LDAPHelper getldapHelper() {
        return ldapHelper;
    }

    @Override
    public void setldapHelper(LDAPHelper ldapHelper) {
        this.ldapHelper = ldapHelper;
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
