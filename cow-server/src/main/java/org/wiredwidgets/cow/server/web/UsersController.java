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
package org.wiredwidgets.cow.server.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
public class UsersController extends CowServerController {
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
    
    
    
    /**
     * Gets the user logged in for the current session
     * @return the logged in user
     */
    @RequestMapping(value = "/whoami")
    @ResponseBody
    public ResponseEntity<User> whoAmI() {
    	String loggedInUsername = ((LdapUserDetailsImpl) 
    			SecurityContextHolder
    			.getContext()
    			.getAuthentication()
    			.getPrincipal())
    			.getUsername();
    	
    	List<User> users = usersService.findAllUsers();
    	for (User user : users) {
    		if (user.getId().equals(loggedInUsername)) {
    			return ok(user);
    		}
    	}
    	
    	return notFound();
    }
}
