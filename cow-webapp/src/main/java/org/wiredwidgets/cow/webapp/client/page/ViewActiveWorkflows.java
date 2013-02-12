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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;
import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.bpm.Activities;
import org.wiredwidgets.cow.webapp.client.bpm.Activity;
import org.wiredwidgets.cow.webapp.client.bpm.Decision;
import org.wiredwidgets.cow.webapp.client.bpm.Loop;
import org.wiredwidgets.cow.webapp.client.bpm.Option;
import org.wiredwidgets.cow.webapp.client.bpm.Parse;
import org.wiredwidgets.cow.webapp.client.bpm.Task;
import org.wiredwidgets.cow.webapp.client.bpm.Template;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickHandler;

public class ViewActiveWorkflows extends PageWidget {
	HashMap<String, String> map;

	public ViewActiveWorkflows() {
			BpmServiceMain.sendGet("/processInstances/active", new AsyncCallback<String>() {
			public void onFailure(Throwable arg0) {
				createPage(new Label("Couldn't access list of active workflows"), PageWidget.PAGE_VIEWACTIVEWORKFLOWS);
			}
			public void onSuccess(String arg0) {
				ArrayList<String> names = Parse.parseTemplateInstances(arg0);
				ArrayList<String> ids = Parse.parseTemplateInstancesIds(arg0);
				generateBody(names);
			}
		});
	}
	
	protected void generateBody(ArrayList<String> names) {
		if(names.size() == 0) {
			createPage(new Label("No active workflows"), PageWidget.PAGE_VIEWACTIVEWORKFLOWS);
		} else {
			final ArrayList<String> statusList = new ArrayList<String>(Arrays.asList("precluded", "completed", "contingent", "planned", "notStarted", "open"));
			final ListGrid grid = new ListGrid(){
				protected String getBaseStyle(ListGridRecord record, int rowNum, int colNum) {
					String value = record.getAttribute(this.getFields()[colNum].getName());
					
					if (colNum == 0){
						return "linkLabel";
					}
					if (statusList.contains(value)){
						return value;
					}					
	                return super.getBaseStyle(record, rowNum, colNum);
	            }
				
			};
			
			
			

			grid.setWidth100();
			grid.setHeight100();
			grid.setShowFilterEditor(true);
			grid.setFilterOnKeypress(true);
			grid.setShowRecordComponents(true);
			grid.setShowRecordComponentsByCell(true);
			grid.addCellDoubleClickHandler(new CellDoubleClickHandler() {
				public void onCellDoubleClick(CellDoubleClickEvent event) {
					if(event.getColNum() > 0) {
						Object[] args= {event.getRecord().getAttribute("id"),true};
						PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
					}
				}
			}); 
			
			grid.addCellClickHandler(new CellClickHandler() {
				public void onCellClick(CellClickEvent event) {
					if(event.getColNum() == 0) {
						Object[] args= {event.getRecord().getAttribute("id"),true};
						PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
					}
					
				
				
				}
			});
			
			CellFormatter formatter = new CellFormatter() {
	        	public String format(Object value, ListGridRecord record, int rowNum, int colNum) {
					if(value == null) return "";
					String s = value.toString();
					if (statusList.contains(s)){
						return "";
					}
					return s;
				}

		
			};
			
			grid.setCellFormatter(formatter);
			final ListGridField id = new ListGridField("id", "ID");   
	        id.setCellFormatter(new CellFormatter() {  
	            public String format(Object value, ListGridRecord record, int rowNum, int colNum) {  
	            	return value.toString(); 
	            }  
	        });
	        
	        
			
			
			BpmServiceMain.sendGet("/processInstances/active", new AsyncCallback<String>() {
				public void onFailure(Throwable arg0) {
				}
				public void onSuccess(String arg0) {
					
					ArrayList<String> names = Parse.parseTemplateInstances(arg0);
					ArrayList<String> ids = Parse.parseTemplateInstancesIds(arg0);
					ListGridRecord[] rs = new ListGridRecord[names.size()];
					Set<ListGridField> gridfields = new HashSet<ListGridField>();
					gridfields.add(new ListGridField("activeWorkflow", "Active Workflow"));
					gridfields.add(id);
					grid.setFields(gridfields.toArray(new ListGridField[gridfields.size()]));
					for(int i = 0; i < names.size(); i++) {
						rs[i] = new ListGridRecord();
					}
					grid.setData(rs);
					for(int i = 0; i < names.size(); i++) {
						rs[i].setAttribute("activeWorkflow", names.get(i));
						rs[i].setAttribute("id", ids.get(i));
						
						//
						BpmServiceMain.sendGet("/processInstances/active/" + BpmServiceMain.urlEncode(ids.get(i)) + "/status", new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								SC.say("Error. Please ensure that you are connected to the Internet, and that the server is currently online.");
							}
							public void onSuccess(String result) {
								
								
								Template template = (result == null || result.equals("") ? new Template() : Parse.parseTemplate(result));

								
								RecordList rs = grid.getDataAsRecordList();
								//For every Record
								for(int x = 0;x <rs.getLength(); x++){
									ArrayList<Activity> acts = template.getBase().getActivities();
									if (rs.get(x).getAttribute("id") == template.getName()){
										updateGrid(rs,x,acts);
									
									}
								}
							
								
								
								
								
								
								
							}
							private void updateGrid(RecordList rs, int x,
									ArrayList<Activity> acts) {
								String attr = null;
								String n = null;
								//For every activity in that record
								
								for(int j = 0; j < acts.size(); j++){
									Object act = acts.get(j);
									
									
									
									if(act instanceof Task) {
										Task t = (Task)act;										
										attr = t.get("assignee");
										//n = t.getName();
										n = t.getCompletion();
										
										

									} else if(act instanceof Loop) {
										Loop l = (Loop)act;
										attr = l.getLoopTask().get("assignee");
										n = l.getCompletion();
										updateGrid(rs,x,l.getActivities().getActivities());


									} else if(act instanceof Decision) {
										Decision d = (Decision)act;
										attr = d.getTask().get("assignee");
										//n = d.getCompletion(); (This would be consistent with stoplight)
										n = d.getTask().getCompletion();
										ArrayList<Option> opts = d.getOptions();
										for (Option op : opts){
											updateGrid(rs,x, op.getActivities().getActivities());
										}
									}
									 else if(act instanceof Activities) {
										 updateGrid(rs,x,((Activities) act).getActivities());
									 }
								if (attr != null){
										ListGridField[] oldFields = grid.getFields();
										boolean exists = false;
										//Check if field exists
										for (ListGridField f: oldFields){
											if (f.getName() == attr){
												exists = true;
												break;
											}
										}
										//If it doesn't exist, create it.
										if (!exists){
											Set<ListGridField> gridFields = new HashSet<ListGridField>(Arrays.asList(oldFields));
											ListGridField newField = new ListGridField(attr, attr);
											gridFields.add(newField);
											grid.setFields(gridFields.toArray(new ListGridField[gridFields.size()]));
											
										}
										String status = getCellStatus(rs.get(x).getAttribute(attr), n);
										rs.get(x).setAttribute(attr, status);
										grid.refreshFields();
										
									
									//break;
								}
								}
								
							}
							private String getCellStatus(String newStatus,
									String currentStatus) {
								if (statusList.indexOf(currentStatus) > statusList.indexOf(newStatus)){
									newStatus = currentStatus;
								}
								
	
								return newStatus;
							}
						});
						//
						
						
					}
					grid.setFields((ListGridField[]) gridfields.toArray());
					grid.sort(2, SortDirection.ASCENDING);
					
				}
			});
			
			
			createPage(grid, PageWidget.PAGE_VIEWACTIVEWORKFLOWS);}
		}


	public void refresh() {
		PageManager.getInstance().setPageHistory(Pages.VIEWACTIVEWORKFLOWS,null);
	}

}
