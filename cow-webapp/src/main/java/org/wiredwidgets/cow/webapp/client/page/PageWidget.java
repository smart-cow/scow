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

package org.wiredwidgets.cow.webapp.client.page;

import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.components.Header;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.BaseWidget;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * Base class for all pages. Pages must implement the refresh() method,
 * which is called when the user selects Refresh from the Page menu.
 * 
 * When adding a new PageWidget, a new enum value must be added so it 
 * can be passed to the Header on creation.
 * 
 * @author JSTASIK
 *
 */
public abstract class PageWidget extends VLayout {
	public static String PAGE_CREATENEWTASK = "create_new_task";
	public static String PAGE_TASKS = "tasks";
	public static String PAGE_LOGIN = "login";
	public static String PAGE_MANAGEWORKFLOWS = "manage_workflows";
	public static String PAGE_EDITWORKFLOW = "edit_workflow";
	public static String PAGE_VIEWWORKFLOW = "view_workflow";
	public static String PAGE_ADMIN = "admin";
	public static String PAGE_VIEWACTIVEWORKFLOWS = "view_active_workflows";
	
	protected Header header;
	protected boolean headerAdded;
	
	public PageWidget() {
		setWidth100();
		setHeight100();
		headerAdded = false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createPage(BaseWidget w, String name) {
		if(!name.equals(PAGE_LOGIN) && !headerAdded) {
			header = new Header(name, this);
			addMember(header);
			headerAdded = true;
		}
		if(w != null)
			addMember(w);
		
	}
	
	public abstract void refresh();
}
