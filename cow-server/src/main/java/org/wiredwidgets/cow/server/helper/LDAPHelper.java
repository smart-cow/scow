/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.helper;

import com.unboundid.ldap.sdk.*;
import com.unboundid.ldif.LDIFException;
import java.util.*;
import org.apache.log4j.Logger;
import org.jbpm.task.identity.LDAPUserGroupCallbackImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.service.Membership;
import org.wiredwidgets.cow.server.api.service.User;

/**
 *
 * @author FITZPATRICK
 */
public class LDAPHelper {

    private static Logger log = Logger.getLogger(LDAPHelper.class);
    @Value("${ldap.host}")
    String LDAP_HOST;
    @Value("${ldap.port}")
    int LDAP_PORT;
    @Value("${ldap.role.context}")
    String LDAP_ROLE_CONTEXT;
    @Value("${ldap.user.context}")
    String LDAP_USER_CONTEXT;
    @Value("${ldap.admin}")
    String LDAP_ADMIN;
    @Value("${ldap.admin.password}")
    String LDAP_ADMIN_PASSWORD;

    public List<String> getLDAPGroups() {
        List<String> ldapGroups = new ArrayList<String>();

        try {
            LDAPConnection lc = new LDAPConnection(LDAP_HOST, LDAP_PORT);

            String baseDN = LDAP_ROLE_CONTEXT;
            String filter = "(&(objectClass=organizationalRole))";

            SearchResult searchResult = null;

            if (lc.isConnected()) {
                searchResult = lc.search(baseDN, SearchScope.ONE, filter);
            }

            List<SearchResultEntry> results = searchResult.getSearchEntries();

            for (SearchResultEntry e : results) {
                ldapGroups.add(e.getAttributeValue("cn"));
            }

            lc.close();
        } catch (LDAPException e) {
            log.error(e);
        }

        return ldapGroups;
    }

    public List<String> getLDAPUsers() {
        List<String> ldapUsers = new ArrayList<String>();

        try {
            LDAPConnection lc = new LDAPConnection(LDAP_HOST, LDAP_PORT);

            String baseDN = LDAP_USER_CONTEXT;
            String filter = "(&(objectClass=inetOrgPerson))";

            SearchResult searchResult = null;

            if (lc.isConnected()) {
                searchResult = lc.search(baseDN, SearchScope.ONE, filter);
            }

            List<SearchResultEntry> results = searchResult.getSearchEntries();

            for (SearchResultEntry e : results) {
                ldapUsers.add(e.getAttributeValue("uid"));
            }

            lc.close();
        } catch (LDAPException e) {
            log.error(e);
        }

        return ldapUsers;
    }

    public String createGroup(String groupName) {
        String[] ldifLines = {
            "dn: cn=" + groupName + "," + LDAP_ROLE_CONTEXT,
            "objectclass: organizationalRole",
            "cn: " + groupName
        };

        try {
            LDAPConnection lc = new LDAPConnection(LDAP_HOST, LDAP_PORT, LDAP_ADMIN, LDAP_ADMIN_PASSWORD);

            if (lc.isConnected()) {
                lc.add(new AddRequest(ldifLines));
            }

            lc.close();
            return groupName + " successfully created.";
        } catch (LDAPException e) {
            log.error(e);
        } catch (LDIFException e) {
            log.error(e);
        }

        return groupName + " creation unsuccessful.";
    }

    public void createUser(User user) {
        final String[] ldifLines = {
            "dn: uid=" + user.getId() + "," + LDAP_USER_CONTEXT,
            "objectclass: inetOrgPerson",
            "cn: " + user.getFirstName() + " " + user.getLastName(),
            "sn: " + user.getId(),
            "uid: " + user.getId(),
            "mail: " + user.getEmail()
        };

        try {
            LDAPConnection lc = new LDAPConnection(LDAP_HOST, LDAP_PORT, LDAP_ADMIN, LDAP_ADMIN_PASSWORD);

            if (lc.isConnected()) {
                lc.add(new AddRequest(ldifLines));
            }

            lc.close();
        } catch (LDAPException e) {
            log.error(e);
        } catch (LDIFException e) {
            log.error(e);
        }

        for (Membership membership : user.getMemberships()) {
            addUserToGroup(user.getId(), membership.getGroup());
        }
    }

    public void updateUser(User user) {
        final String[] ldifLines = {
            "dn: uid=" + user.getId() + "," + LDAP_USER_CONTEXT,
            "changetype: modify",
            "replace: cn",
            "cn: " + user.getFirstName() + " " + user.getLastName()
        };

        final String[] ldifLines2 = {
            "dn: uid=" + user.getId() + "," + LDAP_USER_CONTEXT,
            "changetype: modify",
            "replace: mail",
            "mail: " + user.getEmail()
        };

        try {
            LDAPConnection lc = new LDAPConnection(LDAP_HOST, LDAP_PORT, LDAP_ADMIN, LDAP_ADMIN_PASSWORD);

            if (lc.isConnected()) {
                lc.modify(new ModifyRequest(ldifLines));
                lc.modify(new ModifyRequest(ldifLines2));
            }

            lc.close();
        } catch (LDAPException e) {
            log.error(e);
        } catch (LDIFException e) {
            log.error(e);
        }

        updateUsersGroups(user);
    }

    public boolean deleteGroup(String groupName) {

        try {
            LDAPConnection lc = new LDAPConnection(LDAP_HOST, LDAP_PORT, LDAP_ADMIN, LDAP_ADMIN_PASSWORD);

            if (lc.isConnected()) {
                lc.delete(new DeleteRequest("cn=" + groupName + "," + LDAP_ROLE_CONTEXT));
            }

            lc.close();
            return true;
        } catch (LDAPException e) {
            log.error(e);
        }

        return false;
    }

    public boolean deleteUser(String userId) {
        try {
            LDAPConnection lc = new LDAPConnection(LDAP_HOST, LDAP_PORT, LDAP_ADMIN, LDAP_ADMIN_PASSWORD);

            if (lc.isConnected()) {
                lc.delete(new DeleteRequest("uid=" + userId + "," + LDAP_USER_CONTEXT));
            }

            lc.close();


            Map<String, String> usersGroups = getUsersGroups(userId);
            for (String usersGroup : usersGroups.keySet()) {
                deleteUserFromGroup(userId, usersGroup);
            }

            return true;
        } catch (LDAPException e) {
            log.error(e);
        }

        return false;
    }

    public void addUserToGroup(String userId, String groupId) {
        final String[] ldifLines = {
            "dn: cn=" + groupId + "," + LDAP_ROLE_CONTEXT,
            "changetype: modify",
            "add: roleOccupant",
            "roleOccupant: " + "uid=" + userId + "," + LDAP_USER_CONTEXT
        };

        try {
            LDAPConnection lc = new LDAPConnection(LDAP_HOST, LDAP_PORT, LDAP_ADMIN, LDAP_ADMIN_PASSWORD);

            if (lc.isConnected()) {
                lc.modify(new ModifyRequest(ldifLines));
            }

            lc.close();
        } catch (LDAPException e) {
            log.error(e);
        } catch (LDIFException e) {
            log.error(e);
        }
    }

    public void deleteUserFromGroup(String userId, String groupId) {
        final String[] ldifLines = {
            "dn: cn=" + groupId + "," + LDAP_ROLE_CONTEXT,
            "changetype: modify",
            "delete: roleOccupant",
            "roleOccupant: " + "uid=" + userId + "," + LDAP_USER_CONTEXT
        };

        try {
            LDAPConnection lc = new LDAPConnection(LDAP_HOST, LDAP_PORT, LDAP_ADMIN, LDAP_ADMIN_PASSWORD);

            if (lc.isConnected()) {
                lc.modify(new ModifyRequest(ldifLines));
            }

            lc.close();
        } catch (LDAPException e) {
            log.error(e);
        } catch (LDIFException e) {
            log.error(e);
        }
    }

    public void updateUsersGroups(User user) {
        Map<String, String> usersGroups = getUsersGroups(user.getId());

        for (Membership m : user.getMemberships()) {
            if (!usersGroups.containsKey(m.getGroup())) {
                addUserToGroup(user.getId(), m.getGroup());
                usersGroups.remove(m.getGroup());
            }
        }

        for (String deletedGroup : usersGroups.keySet()) {
            deleteUserFromGroup(user.getId(), deletedGroup);
        }
    }

    public Map<String, String> getUsersGroups(String userId) {
        Map<String, String> usersGroups = new HashMap<String, String>();

        try {
            LDAPConnection lc = new LDAPConnection(LDAP_HOST, LDAP_PORT);

            String baseDN = LDAP_ROLE_CONTEXT;
            String filter = "(&(roleOccupant=uid=" + userId + "," + LDAP_USER_CONTEXT + "))";

            SearchResult searchResult = null;

            if (lc.isConnected()) {
                searchResult = lc.search(baseDN, SearchScope.SUB, filter);
            }

            List<SearchResultEntry> results = searchResult.getSearchEntries();

            for (SearchResultEntry e : results) {
                //if (e.getAttributeValue("cn").equals(groupId)){
                usersGroups.put(e.getAttributeValue("cn"), "1");
            }

            lc.close();

        } catch (LDAPException e) {
            log.error(e);
        }

        return usersGroups;
    }
}
