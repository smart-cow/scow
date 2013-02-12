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

package org.wiredwidgets.cow.webapp.client.page;

import java.util.ArrayList;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;
import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.bpm.Parse;
import org.wiredwidgets.cow.webapp.client.components.CustomListGrid;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickHandler;

/**
 * A page that lets users view all saved workflows and select
 * one to view or edit.
 * 
 * @author JSTASIK
 *
 */
public class ManageWorkflows extends PageWidget {
	private ListGridRecord recordToRemove;

	public ManageWorkflows() {
		super();
		
		final CustomListGrid grid = new CustomListGrid();
		grid.setWidth100();
		grid.setHeight100();
		grid.setShowFilterEditor(true);
		grid.setFilterOnKeypress(true);
		grid.setShowRecordComponents(true);
		grid.setShowRecordComponentsByCell(true);
		grid.addCellDoubleClickHandler(new CellDoubleClickHandler() {
			public void onCellDoubleClick(CellDoubleClickEvent event) {
				if(event.getColNum() == 2) {
					String[] args= {event.getRecord().getAttribute("name")};
					PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
				}
			}
		}); 
		
		grid.addCellClickHandler(new CellClickHandler() {
			public void onCellClick(CellClickEvent event) {
				if(event.getColNum() == 0) {
					recordToRemove = event.getRecord();
					SC.ask("Are you sure you want to delete " + event.getRecord().getAttribute("name"), new BooleanCallback() {
						public void execute(Boolean value) {
							if(value) {
								String name = recordToRemove.getAttribute("name");
								BpmServiceMain.sendDelete("/processDefinitions?key=" + name, new AsyncCallback<Void>() {
									public void onFailure(Throwable arg0) {
										recordToRemove = null;
										SC.say("Error. Please ensure that you are connected to the Internet, and that the server is currently online.");
									}

									public void onSuccess(Void arg0) {
										if(recordToRemove != null)
											grid.removeData(recordToRemove);
										recordToRemove = null;
									}
								});
							} else {
								recordToRemove = null;
							}
						}
					});
				} else if(event.getColNum() == 1) {
					String[] args= {event.getRecord().getAttribute("name")};
					PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
				}
			}
		});
		
		ListGridField remove = new ListGridField("Delete", "Delete");
		remove.setAlign(Alignment.CENTER);
		remove.setType(ListGridFieldType.ICON);
		remove.setCellFormatter(new CellFormatter() {
			public String format(Object value, ListGridRecord record,
					int rowNum, int colNum) {
				return "<img src=\"images/delete.png\" alt=\"Delete\" style=\"display: block; margin-left: auto; margin-right: auto; cursor: pointer;cursor: hand;\" />";
			}
		});
		
		ListGridField manage = new ListGridField("Manage", "Manage");
		manage.setAlign(Alignment.CENTER);
		manage.setType(ListGridFieldType.ICON);
		manage.setCellFormatter(new CellFormatter() {
			public String format(Object value, ListGridRecord record,
					int rowNum, int colNum) {
				return "<img src=\"images/pencil.png\" alt=\"Manage\" style=\"display: block; margin-left: auto; margin-right: auto; cursor: pointer;cursor: hand;\" />";
			}
		});
		
		grid.setFields(remove, manage, new ListGridField("name", "Name"));
		
		BpmServiceMain.sendGet("/processDefinitions", new AsyncCallback<String>() {
			public void onFailure(Throwable arg0) {
			}
			public void onSuccess(String arg0) {
				ArrayList<String> definitions = Parse.parseProcessDefinitions(arg0);
				ListGridRecord[] records = new ListGridRecord[definitions.size()];
				for(int i = 0; i < definitions.size(); i++) {
					records[i] = new ListGridRecord();
					records[i].setAttribute("name", definitions.get(i));
					records[i].setAttribute("Manage", "Manage");
				}
				grid.setData(records);
				grid.sort(2, SortDirection.ASCENDING);
			}
		});
		
		createPage(grid, PageWidget.PAGE_MANAGEWORKFLOWS);
	}
	
	public void refresh() {
		PageManager.getInstance().setPageHistory(Pages.MANAGEWORKFLOWS, null);
	}
}
