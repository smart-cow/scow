/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2011 The MITRE Corporation,
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

package org.wiredwidgets.cow.webapp.client;

import org.wiredwidgets.cow.webapp.client.page.Admin;
import org.wiredwidgets.cow.webapp.client.page.CreateNewTask;
import org.wiredwidgets.cow.webapp.client.page.EditWorkflow;
import org.wiredwidgets.cow.webapp.client.page.Login;
import org.wiredwidgets.cow.webapp.client.page.ManageWorkflows;
import org.wiredwidgets.cow.webapp.client.page.ManageWorkflows2;
import org.wiredwidgets.cow.webapp.client.page.PageWidget;
import org.wiredwidgets.cow.webapp.client.page.Tasks;
import org.wiredwidgets.cow.webapp.client.page.ViewActiveWorkflows;
import org.wiredwidgets.cow.webapp.client.page.ViewWorkflow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.BaseWidget;

/**
 * Singleton
 * Manages which page is currently being displayed
 * @author JSTASIK
 *
 */
public class PageManager {
	// The instance of the PageManager
	private static PageManager instance;
	
	private BaseWidget currentPage;
	
	/** Constructor */
	private PageManager() {
		currentPage = null;
	}
	
	
	/**
	 * Gets the instance of PageManager, or creates it if it doesn't exist yet
	 * @return
	 */
	public static PageManager getInstance() {
		if(instance == null)
			instance = new PageManager();
		return instance;
	}
	
	/**
	 * Sets the current page, and removes the old one
	 * All pages should inherit from the PageWidget class, which is a Composite (which is a Widget of Widgets)
	 * @param w The page to set
	 */
	public void setPage(BaseWidget b) {
			if(currentPage != null)
				currentPage.destroy();
			currentPage = b;
			currentPage.draw();
		
	}
	
	public enum Pages {
	    TEMPLATE,TASK,LOGIN,WORKFLOW,EDITWORKFLOWSTRING,VIEWACTIVEWORKFLOWS,ADMIN,CREATENEWTASK,EDITWORKFLOWBOOLEAN,
	    MANAGEWORKFLOWS2,MANAGEWORKFLOWS,VIEWWORKFLOW
	}
	
	/**
	 * Sets the current page, and removes the old one using History
	 * All pages should inherit from the PageWidget class, which is a Composite (which is a Widget of Widgets)
	 * @param w The page to set
	 */
	public void setPageHistory(Pages p, Object[] args) {
		if (args == null){
			String s = p.toString();
			//Ensures initial load happens properly
			History.newItem(p.toString());
			if (s == "LOGIN"){
				BaseWidget b = new Login();
				PageManager.getInstance().setPage(b);
			}
			
			
		}
		else{
			String s = p.toString();
			for (Object o: args){
				s = s + "-ARG-" + o.toString();
			}
			History.newItem(s);
			
		}
				
	}
	
	public PageWidget getPage() {
		return (PageWidget)currentPage;
	}
}
