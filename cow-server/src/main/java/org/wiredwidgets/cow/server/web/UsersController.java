/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.wiredwidgets.cow.server.api.service.Group;
import org.wiredwidgets.cow.server.api.service.Groups;
import org.wiredwidgets.cow.server.api.service.User;
import org.wiredwidgets.cow.server.api.service.Users;
import org.wiredwidgets.cow.server.service.UsersService;

/**
 *
 * @author FITZPATRICK
 */
@Controller
public class UsersController {
    @Autowired
    UsersService usersService;

    /*
     * Responds with a representation of all users
     */
    @RequestMapping("/users")
    @ResponseBody
    public Users getUsers() {
        Users users = new Users();
        users.getUsers().addAll(usersService.findAllUsers());
        return users;
    }

    /*
     * Responds with a representation of a single user as specified in the path
     */
    @RequestMapping("/users/{id}")
    @ResponseBody
    public User getUser(@PathVariable("id") String id, HttpServletResponse response) {
        User user = usersService.findUser(id);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        return user;
    }

    /*
     * Create or update a user.  Expects an XML or JSON representation of the user as the
     * request body. Responds with a location header indicating the URL for the user
     * that was posted.
     */
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public void createOrUpdateUser(@RequestBody User user, HttpServletRequest request, HttpServletResponse response) {
        usersService.createOrUpdateUser(user);
        response.setHeader("Location", request.getRequestURL() + "/" + user.getId());
        response.setStatus(HttpServletResponse.SC_CREATED); // 201
    }

    /*
     * Delete the user specified in the request URL.
     * does nothing if the user does not exist.
     */
    @RequestMapping(value = "/users/{id}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable("id") String id, HttpServletResponse response) {
        if (usersService.deleteUser(id)) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /*
     * Retrieve a representation of all known groups.
     */
    @RequestMapping("/groups")
    @ResponseBody
    public Groups getGroups() {
        Groups groups = new Groups();
        groups.getGroups().addAll(usersService.findAllGroups());
        return groups;
    }

    /*
     * Retrieve a representation of the group specified in the path.
     */
    @RequestMapping("/groups/{id}")
    @ResponseBody
    public Group getGroup(@PathVariable("id") String id, HttpServletResponse response) {
        Group group = usersService.findGroup(id);
        if (group == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        return group;
    }

    /*
     * Delete the group specified in the path.  This also removes this group
     * from the memberships of any users who are members of the group.
     * Does nothing if the group does not exist.
     */
    @RequestMapping(value = "/groups/{id}", method = RequestMethod.DELETE)
    public void deleteGroup(@PathVariable("id") String id, HttpServletResponse response) {
        if (usersService.deleteGroup(id)) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /*
     * Create a new group, as specified by the 'name' parameter.
     * the response location header contains the URL of the creted group.
     */
    @RequestMapping(value = "/groups", method = RequestMethod.POST)
    public void createGroup(@RequestParam("name") String name, HttpServletRequest request,
            HttpServletResponse response) {
        String id = usersService.createGroup(name);
        response.setHeader("Location", request.getRequestURL() + "/" + id);
        response.setStatus(HttpServletResponse.SC_CREATED); // 201
    }
}
