/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.service;

import java.util.List;
import org.wiredwidgets.cow.server.api.service.Group;
import org.wiredwidgets.cow.server.api.service.User;
/**
 *
 * @author FITZPATRICK
 */
public interface UsersService {
    /**
     * Create a group with the specified group name.
     * @param groupName the name of the group
     * @return the system assigned Id of the group.  In practice, same as the group name.
     */
    String createGroup(String groupName);

    /**
     * Create a new user or update an existing user. The implementation may optionally
     * delete the user and create a new record with the same attributes.
     * @param user the user
     */
    void createOrUpdateUser(User user);

    /**
     * Delete the specified user. If the user does not exist, do nothing.
     * @param id the user Id.
     * @return true if the user existed and was deleted, false if no such user
     */
    boolean deleteUser(String id);

    /**
     * Find all groups 
     * @return the list of groups
     */
    List<Group> findAllGroups();

    /**
     * Finds all users in the system
     * @return a list of all users
     */
    List<User> findAllUsers();

    /**
     * Find a single group by Id
     * @param id the group Id
     * @return the group
     */
    Group findGroup(String id);

    /**
     * Find a single user by Id
     * @param id the user Id
     * @return the user or null if not found
     */
    User findUser(String id);

    /**
     * Delete the specified group, along with any user memberships in that group
     * @param id the group id
     * @return true if the group existed and was deleted, false if no such group exists
     */
    boolean deleteGroup(String id);
}
