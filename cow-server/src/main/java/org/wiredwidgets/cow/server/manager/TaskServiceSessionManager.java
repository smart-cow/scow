/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.manager;

import java.util.HashMap;
import java.util.List;
import org.jbpm.task.service.TaskService;
import org.wiredwidgets.cow.server.helper.LDAPHelper;

/**
 *
 * @author FITZPATRICK
 */
public interface TaskServiceSessionManager {
	public void initLdap();
}