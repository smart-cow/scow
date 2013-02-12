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

package org.wiredwidgets.cow.webapp.client.components;

import java.util.ArrayList;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;
import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.bpm.Task;
import org.wiredwidgets.cow.webapp.client.page.Tasks;
import org.wiredwidgets.cow.webapp.client.page.ViewWorkflow;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.viewer.DetailFormatter;
import com.smartgwt.client.widgets.viewer.DetailViewer;
import com.smartgwt.client.widgets.viewer.DetailViewerField;

/**
 * A custom ListGrid that displays a variable list of fields
 * for each item, rather than a fixed set for all items. Also
 * formats some of the fields that have special identifiers
 * before them. Auto-detects URLs and makes them links.
 * 
 * @author JSTASIK
 *
 */
public class CustomListGrid extends ListGrid {

	public CustomListGrid() {
		super();
	}

	public CustomListGrid(JavaScriptObject jsObj) {
		super(jsObj);
	}
	
	@Override
	/**
	 * Used to display Task details on the Tasks page
	 */
	public Canvas getExpansionComponent(ListGridRecord record) {
		DetailViewer dv = new DetailViewer();
		ArrayList<DetailViewerField> dvf = new ArrayList<DetailViewerField>();
		for(String s : record.getAttributes()) {
			//Removes unneccesary variables
			if(s.startsWith("$#") && !s.equals("$#id")) {
				//Created the fields which corresponds to the variable name
				DetailViewerField field;
				if(s.equals("$#processInstanceId"))
					field = new DetailViewerField(s, "workflow");
				else
					field = new DetailViewerField(s, s.substring(2));
				//Formatter allows the instanceId to be a direct link and creates html links when needed
				field.setDetailFormatter(new DetailFormatter() {
					public String format(Object value, Record record, DetailViewerField field) {
						if(value == null) return "";
						String s = value.toString();
						if(field.getName().equals("$#processInstanceId")) {
							String[] splitString = s.split("\\.");
							s = splitString[0] + "." + splitString[1];
							s = "<label style=\"color: blue; text-decoration: underline; cursor:pointer; cursor:hand;\" onclick=\"" +
									"viewWorkflowIFrame('" + s + "');\">" + s + "</label>";
						} else if(!field.getName().equals("$#description")) {
							String[] stringToCheck = s.split(" ");
							s = "";
							for(int i = 0; i < stringToCheck.length; i++) {
								if(isURL(stringToCheck[i])) {
									if(!stringToCheck[i].contains("://"))
										stringToCheck[i] = "http://" + stringToCheck[i];
									stringToCheck[i] = "<a href=\"" + stringToCheck[i] + "\" target=\"_blank\">" + stringToCheck[i] + "</a>";
								} else {
									stringToCheck[i] = BpmServiceMain.xmlEncode(stringToCheck[i]);
								}
								s += (s.equals("") ? stringToCheck[i] : " " + stringToCheck[i]);
							}
						}
						return s;
					}
				});
				dvf.add(field);
			}
		}
		DetailViewerField[] dvfArray = new DetailViewerField[dvf.size()];
		for(int i = 0; i < dvf.size(); i++)
			dvfArray[i] = dvf.get(i);
		dv.setFields(dvfArray);
		//Pairs the newly created fields with their matching data from the record
		dv.setData(new ListGridRecord[]{record});
		return dv;
	}
	
	/**
	 * Checks whether or not a String is a URL
	 * @param url The String to check
	 * @return true if it is a URL, otherwise false
	 */
	public static native boolean isURL(String url) /*-{
		if(url.match(/[-a-zA-Z0-9@:%_\+.~#?&\/\/=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&\/\/=]*)?/gi))
			return true;
		return false;
	}-*/;
	
	public static native void viewWorkflow(String workflow, boolean init) /*-{
		if(init) return;
		@org.wiredwidgets.cow.webapp.client.components.CustomListGrid::switchToWorkflow(Ljava/lang/String;)(workflow);
	}-*/;
	
	public static void switchToWorkflow(String workflow) {
		Object[] args = {workflow,true};
		PageManager.getInstance().setPageHistory(Pages.WORKFLOW, args);
	}
	
	@Override
	/**
	 * Returns Buttons where necessary for the ListGrids on the Tasks page and the Manage page
	 */
	protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {
		String fieldName = getFieldName(colNum);
		if(fieldName.equals("Assign")) {
			IButton button = new IButton();
			button.setTitle("Assign To Me");
			button.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					BpmServiceMain.sendPostNoLocation("/tasks/active/" + ((Task)record.getAttributeAsObject("task")).get("id") + "?assignee=" + BpmServiceMain.getUser(), new AsyncCallback<Void>() {
						public void onFailure(Throwable caught) {
							SC.say("Error. Please ensure that you are connected to the Internet, that the server is currently online, and that the task was not already taken or completed.");
						}
						public void onSuccess(Void result) {
							((Tasks)PageManager.getInstance().getPage()).updateTasks();
						}
					});
				}
			});
			return button;
		}/* else if(fieldName.equals("Manage")) {
			IButton button = new IButton();
			button.setTitle("Manage");
			button.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					PageManager.getInstance().setPage(new ViewWorkflow(record.getAttribute("name")));
				}
			});
			return button;
		}*/ else {
			return super.createRecordComponent(record, colNum);
		}
	}
}
