/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.manager;

import com.unboundid.ldap.sdk.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jbpm.task.identity.LDAPUserGroupCallbackImpl;
import org.jbpm.task.identity.LDAPUserInfoImpl;
import org.jbpm.task.identity.UserGroupCallback;
import org.jbpm.task.identity.UserGroupCallbackManager;
import org.jbpm.task.service.mina.MinaTaskServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author FITZPATRICK
 */
public class MinaTaskServerManager {

    MinaTaskServer minaTaskServer;
    //DefaultUserGroupCallbackImpl userGroupCallback;
    LDAPUserGroupCallbackImpl ldapUserGroupCallback;
    

    public void init() {
        Properties properties = new Properties();
        properties.setProperty(LDAPUserGroupCallbackImpl.BIND_USER, "ou=People,dc=smart-cow,dc=org");
        properties.setProperty(LDAPUserGroupCallbackImpl.USER_CTX, "ou=People,dc=smart-cow,dc=org");
        properties.setProperty(LDAPUserGroupCallbackImpl.ROLE_CTX, "ou=Roles,dc=smart-cow,dc=org");
        properties.setProperty(LDAPUserGroupCallbackImpl.USER_ROLES_CTX, "ou=Roles,dc=smart-cow,dc=org");
        properties.setProperty(LDAPUserGroupCallbackImpl.USER_FILTER, "(uid={0})");
        properties.setProperty(LDAPUserGroupCallbackImpl.ROLE_FILTER, "(cn={0})");
        properties.setProperty(LDAPUserGroupCallbackImpl.USER_ROLES_FILTER, "(roleOccupant={0})");
        //properties.setProperty("ldap.user.id.dn", "true");
        properties.setProperty("java.naming.provider.url", "ldap://scout3:389/");

        ldapUserGroupCallback = new LDAPUserGroupCallbackImpl(properties);
        
        UserGroupCallbackManager.getInstance().setCallback(ldapUserGroupCallback);
        Thread thread = new Thread(minaTaskServer);
        thread.start();
    }

    public void getMinaTaskServer() {
    }

    public void testUser() {
        List<String> groups = ldapUserGroupCallback.getGroupsForUser("fitzpatrick", null, null);
        for (String g: groups){
            System.out.println(g);
        }
    }

    public void setMinaTaskServer(MinaTaskServer minaTaskServer) {
        this.minaTaskServer = minaTaskServer;
    }
    /*
     * public LDAPUserGroupCallbackImpl getUserGroupCallback(){ return
     * userGroupCallback; }
     *
     * public void setUserGroupCallback(LDAPUserGroupCallbackImpl
     * userGroupCallback){ this.userGroupCallback = userGroupCallback; }
     */
}
