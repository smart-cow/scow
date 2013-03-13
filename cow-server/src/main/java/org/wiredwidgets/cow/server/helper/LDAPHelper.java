/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.service.Membership;
import org.wiredwidgets.cow.server.api.service.User;

import com.unboundid.ldap.sdk.AddRequest;
import com.unboundid.ldap.sdk.DeleteRequest;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldif.LDIFException;

/**
 *
 * @author FITZPATRICK
 */
@Component
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
            LDAPConnection lc = getConnection();

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
            LDAPConnection lc = getConnection();

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
            LDAPConnection lc = getAdminConnection();

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
            LDAPConnection lc = getAdminConnection();

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
            LDAPConnection lc = getAdminConnection();

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
            LDAPConnection lc = getAdminConnection();

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
            LDAPConnection lc = getAdminConnection();

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
            LDAPConnection lc = getAdminConnection();
            
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
            LDAPConnection lc = getAdminConnection();

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
            }
            usersGroups.remove(m.getGroup());
        }

        for (String deletedGroup : usersGroups.keySet()) {
            deleteUserFromGroup(user.getId(), deletedGroup);
        }
    }

    public Map<String, String> getUsersGroups(String userId) {
        Map<String, String> usersGroups = new HashMap<String, String>();

        try {
            LDAPConnection lc = getConnection();
            
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
    
    private LDAPConnection getConnection() throws LDAPException {
    	return getConnection(false);
    }
    
    private LDAPConnection getAdminConnection() throws LDAPException {
    	return getConnection(true);
    }
    
    private LDAPConnection getConnection(boolean admin) throws LDAPException {
    	LDAPConnectionOptions options = new LDAPConnectionOptions();
    	options.setConnectTimeoutMillis(1000);
    	
    	LDAPConnection lc = null;
    	
    	if (admin) {
    		lc = new LDAPConnection(options, LDAP_HOST, LDAP_PORT, LDAP_ADMIN, LDAP_ADMIN_PASSWORD);
    	}
    	else {	
    		lc = new LDAPConnection(options, LDAP_HOST, LDAP_PORT);
    	}
    	return lc;
    }
}
