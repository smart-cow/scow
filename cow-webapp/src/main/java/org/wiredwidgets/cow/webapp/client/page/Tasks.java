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
import java.util.Date;
import java.util.Map;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;
import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.bpm.Parse;
import org.wiredwidgets.cow.webapp.client.bpm.Task;
import org.wiredwidgets.cow.webapp.client.components.CustomCloseClickHandler;
import org.wiredwidgets.cow.webapp.client.components.CustomListGrid;
import org.wiredwidgets.cow.webapp.client.components.MyTasksListGrid;


import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ExpansionMode;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.RowEndEditAction;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.events.SubmitValuesEvent;
import com.smartgwt.client.widgets.form.events.SubmitValuesHandler;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.SubmitItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickHandler;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;

/**
 * Home page that lets users see all their
 * assigned tasks to complete, assign public
 * tasks to themself, or view tasks they've
 * recently completed.
 * 
 * 
 * @author JSTASIK
 *
 */
public class Tasks extends PageWidget {
	protected Window confirm;
	protected Timer timer;
	protected ListGridRecord[] records1;
	protected ListGridRecord[] records2;
	protected ListGridRecord[] records3;
	protected static int MAX_TASKS = 100;
	protected SectionStackSection[] sections;
	protected SubmitItem submitItem;
	
	public Tasks() {
		super();
		
		SectionStack sectionStack = new SectionStack();
		sectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
		sectionStack.setWidth100();
		sectionStack.setHeight100();
		
		sections = new SectionStackSection[3];
		for(int i = 0; i < 2; i++) {
			sections[i] = new SectionStackSection(i == 0 ? "My Tasks" : "Available Tasks");
			sections[i].setExpanded(true);
			sections[i].setResizeable(false);
				
			
			ListGridField complete = new ListGridField("complete", " ");
			complete.setType(ListGridFieldType.BOOLEAN);
			complete.setCanEdit(true);
			complete.setCanToggle(true);
			
			
			if(i == 0) {
				
				final MyTasksListGrid taskgrid = new MyTasksListGrid();
				taskgrid.setWidth100();
				taskgrid.setHeight100();
				taskgrid.setCanExpandRecords(true);
				taskgrid.setCanExpandMultipleRecords(false);
				taskgrid.setExpansionMode(ExpansionMode.DETAILS);
				taskgrid.setShowAllRecords(true);
				taskgrid.setExpansionCanEdit(true);
				taskgrid.addCellDoubleClickHandler(new CellDoubleClickHandler() {
					public void onCellDoubleClick(CellDoubleClickEvent event) {
						taskgrid.expandRecord(taskgrid.getRecord(event.getRowNum()));
					}
				});
				
				ListGridField processIntanceID = new ListGridField("$#processInstanceId", "Workflow");
				processIntanceID.setBaseStyle("linkLabel");
				processIntanceID.addRecordClickHandler(new RecordClickHandler(){
					public void onRecordClick(RecordClickEvent event) {    	  
						Object[] args= {event.getValue(),true};
						PageManager.getInstance().setPageHistory(Pages.VIEWWORKFLOW, args );
					}
				});
				
				
				
				taskgrid.setFields(complete, new ListGridField("taskname", "Name"), new ListGridField("$#createTime", "Time"), new ListGridField("$#priority", "Priority"),
						new ListGridField("$#assignee", "Assignee"), new ListGridField("$#activityName", "ActivityName"), processIntanceID);
				sections[i].addItem(taskgrid);
			} else {
				CustomListGrid grid = new CustomListGrid();
				grid.setWidth100();
				grid.setHeight100();
				grid.setCanExpandRecords(true);
				grid.setCanExpandMultipleRecords(false);
				grid.setExpansionMode(ExpansionMode.DETAILS);
				grid.setShowAllRecords(true);
				
				complete.addRecordClickHandler(getRecordClickHandler(grid, i == 1));

				grid.setShowRecordComponents(true);
				grid.setShowRecordComponentsByCell(true);
				ListGridField assign = new ListGridField("Assign", "Assign");
				assign.setAlign(Alignment.CENTER);
				grid.setFields(complete, new ListGridField("taskname", "Name"), new ListGridField("$#createTime", "Time"), new ListGridField("$#priority", "Priority"), assign);
				sections[i].addItem(grid);
			}
			
		}
		
		sections[2] = new SectionStackSection("Event Log");
		sections[2].setExpanded(true);
		sections[2].setResizeable(false);
		ListGrid grid = new ListGrid();
		grid.setWidth100();
		grid.setHeight100();
		grid.setCanExpandRecords(false);
		grid.setShowAllRecords(true);
		grid.setCanEdit(false);
		grid.setFields(new ListGridField("$#endTime", "Time"), new ListGridField("$#id", "Task"), new ListGridField("$#assignee", "User"), 
				new ListGridField("$#state", "Action"),	new ListGridField("outcome", "Outcome"), new ListGridField("$#processInstanceId", "Workflow"));
		sections[2].addItem(grid);
		
		sectionStack.addSection(sections[0]);
		sectionStack.addSection(sections[1]);
		sectionStack.addSection(sections[2]);
		
		BpmServiceMain.sendGet("/processInstances/tasks?assignee=" + BpmServiceMain.getUser(), new AsyncCallback<String>() {
			public void onFailure(Throwable arg0) {
			}
			public void onSuccess(String arg0) {				
				ArrayList<Task> tasks = Parse.parseProcessTasks(arg0);
				
				records1 = new ListGridRecord[MAX_TASKS];
				int i = 0;
				for(Task t : tasks) {
					records1[i++] = createLGR(false, t);
				}
				((MyTasksListGrid)sections[0].getItems()[0]).setData(records1);
				
			}
		});
		BpmServiceMain.sendGet("/processInstances/tasks?candidate=" + BpmServiceMain.getUser(), new AsyncCallback<String>() {
			public void onFailure(Throwable arg0) {
			}
			public void onSuccess(String arg0) {
				ArrayList<Task> tasks = Parse.parseProcessTasks(arg0);
				records2 = new ListGridRecord[MAX_TASKS];
				int i = 0;
				for(Task t : tasks) {
					records2[i++] = createLGR(false, t);
				}
				((CustomListGrid)sections[1].getItems()[0]).setData(records2);
			}
		});
		
		int dayLength = 24*60*60*1000;
		Date d = new Date(); 
		d.setTime(d.getTime() + dayLength);
		String end = (1900 + d.getYear()) + "-" + (d.getMonth() < 9 ? "0" : "") + (d.getMonth() + 1) + "-" + (d.getDate() < 9 ? "0" : "") + d.getDate();
		d = new Date();
		d.setTime(d.getTime() - dayLength * 3);
		String start = (1900 + d.getYear()) + "-" + (d.getMonth() < 9 ? "0" : "") + (d.getMonth() + 1) + "-" + (d.getDate() < 9 ? "0" : "") + d.getDate();
		BpmServiceMain.sendGet("/tasks/history?assignee=" + BpmServiceMain.getUser() + "&start=" + start + "&end=" + end, new AsyncCallback<String>() {
			public void onFailure(Throwable arg0) {
			}
			public void onSuccess(String arg0) {
				ArrayList<Task> tasks = Parse.parseTasks(arg0);
				records3 = new ListGridRecord[MAX_TASKS];
				int i = 0;
				for(Task t : tasks) {
					records3[i++] = createLGR(true, t);
				}
				((ListGrid)sections[2].getItems()[0]).setData(records3);
				((ListGrid)sections[2].getItems()[0]).sort(0, SortDirection.DESCENDING);
			}
		});
		
		createPage(sectionStack, PageWidget.PAGE_TASKS);
		
		timer = new Timer() {
			public void run() {
				updateTasks();
			}
		};
		timer.scheduleRepeating(BpmServiceMain.getPollingRate());
	}
	
	//Click handler for Check box associated with completing a task
	protected RecordClickHandler getRecordClickHandler(final CustomListGrid grid, final boolean assign) {
		return new RecordClickHandler() {
			public void onRecordClick(RecordClickEvent event) {
				if(!event.getRecord().getAttributeAsBoolean("complete")) {
					confirm = new Window();
					confirm.setAlign(Alignment.CENTER);
					confirm.setWidth(250);
					confirm.setTitle("Complete Task");
					confirm.setIsModal(true);
					confirm.setShowModalMask(true);
					confirm.setShowMinimizeButton(false);
					confirm.addCloseClickHandler(new CustomCloseClickHandler(new Integer(event.getRecordNum())) {
						public void onCloseClick(CloseClientEvent event) {
							grid.setEditValue((Integer)o, 1, false);
							grid.saveAllEdits();
							confirm.destroy();
						}
					});
					confirm.centerInPage();
					DynamicForm form = new DynamicForm();
					form.setWidth(225);
					form.setLayoutAlign(Alignment.CENTER);
					Task t = (Task)event.getRecord().getAttributeAsObject("task");
					form.setValue("taskID", t.get("id"));
					ArrayList<String> outcomes = t.getOutcomes();
					FormItem[] fields = null;
					int outcomeMod = 0;
					confirm.setHeight(220+20*outcomes.size());
					if(outcomes.size() > 0) {
						outcomeMod++;
						fields = new FormItem[12];
						RadioGroupItem radios = new RadioGroupItem();
						radios.setName("Choose");
						form.setValue("mustChoose", "true");
						String[] values = new String[outcomes.size()];
						for(int i = 0; i < outcomes.size(); i++)
							values[i] = outcomes.get(i);
						radios.setValueMap(values);
						fields[0] = radios;
					}
					if(fields == null)
						fields = new FormItem[11];
					for(int i = 0; i < 5; i++) { 
						TextItem key = new TextItem();
						key.setName("key"+i);
						key.setShowTitle(false);
						key.setEmptyDisplayValue("Note Name");
						key.addKeyPressHandler(new KeyPressHandler() {
							public void onKeyPress(KeyPressEvent event) {
								if(event.getKeyName().equals("Enter"))
									submit(event.getForm().getValues(), assign);
							}
						});
						TextItem value = new TextItem();
						value.setEmptyDisplayValue("Note or URL");
						value.setShowTitle(false);
						value.setName("value"+i);
						value.addKeyPressHandler(new KeyPressHandler() {
							public void onKeyPress(KeyPressEvent event) {
								if(event.getKeyName().equals("Enter"))
									submit(event.getForm().getValues(), assign);
							}
						});
						key.setWidth(100);
						value.setWidth(100);
						fields[i*2+outcomeMod] = key;
						fields[i*2+1+outcomeMod] = value;
					}
					submitItem = new SubmitItem();
					fields[10+outcomeMod] = submitItem;
					form.addSubmitValuesHandler(new SubmitValuesHandler() {
						public void onSubmitValues(SubmitValuesEvent event) {
							submit(event.getValuesAsMap(), assign);
						}
					});
					form.setFields(fields);
					confirm.addItem(form);
					confirm.show();
				}
			}
		};
	}
	
	//Submit button on Completion Variables pop-up
	protected void submit(Map<String, String> map, boolean assign) {
		String varString = "", outcomeString = "";
		if(map.get("Choose") != null) {
			outcomeString = "?outcome=" + map.get("Choose");
		} else if(map.get("mustChoose") != null) {
			SC.say("Please choose one of the available options before submitting.");
			return;
		}
		for(int i = 0; i < 5; i++) {
			String key = map.get("key"+i), value = map.get("value"+i);
			if(key != null && !key.equals("Note Name")) {
				//key = BpmServiceMain.xmlEncode(key);
				if(value == null || value == "Note or URL") {
					SC.say("The name for note #" + (i+1) + " is filled in, but there is no value");
					return;
				}
				//value = BpmServiceMain.xmlEncode(value);
				varString += "&var=" + BpmServiceMain.urlEncode(key+":"+value);
			} else if((key == null || key.equals("Note Name")) && (value != null && !value.equals("Note or URL"))) {
				SC.say("The value for note #" + (i+1) + " is filled in, but there is no name");
				return;
			}
		}
		if(outcomeString.equals(""))
			varString = varString.replaceFirst("&", "?");
		final String tempString = map.get("taskID") + outcomeString + varString;
		if(assign) {
			submitItem.disable();
			BpmServiceMain.sendPostNoLocation("/tasks/active/" + map.get("taskID") + "?assignee=" + BpmServiceMain.getUser(), new AsyncCallback<Void>() {
				public void onFailure(Throwable arg0) {
					SC.say("Error. Please ensure that you are connected to the Internet, that the server is currently online, and that the task was not already assigned.");
					submitItem.enable();
				}
				public void onSuccess(Void arg0) {
					BpmServiceMain.sendDelete("/tasks/active/" + tempString, true, new AsyncCallback<Void>() {
						public void onFailure(Throwable arg0) {
							SC.say("Error. Please ensure that you are connected to the Internet, that the server is currently online, and that the task was not already completed.");
							confirm.destroy();
							updateTasks();
						}
						public void onSuccess(Void arg0) {
							confirm.destroy();
							updateTasks();
						}
					});
				}
			});
		} else {
			BpmServiceMain.sendDelete("/tasks/active/" + tempString, true, new AsyncCallback<Void>() {
				public void onFailure(Throwable arg0) {
					SC.say("Error. Please ensure that you are connected to the Internet, that the server is currently online, and that the task was not already completed.");
				}
				public void onSuccess(Void arg0) {
					confirm.destroy();
					updateTasks();
				}
			});
		}
	}
	
	
	
	protected ListGridRecord createLGR(boolean history, Task t) {
		ListGridRecord lgr = new ListGridRecord();
		lgr.setAttribute("id", t.get("id"));
		for (Map.Entry<String, String> entry : t.getFields().entrySet()) {
			String key = entry.getKey();
			if(key.equals("assigneeType"))
				continue;
			String value = entry.getValue();
			lgr.setAttribute("$#" + key, value);
		}
		if(history) {
			if(t.getOutcomes().get(0).equals("jbpm_no_task_outcome_specified_jbpm"))
				lgr.setAttribute("outcome", "N/A");
			else
				lgr.setAttribute("outcome", t.getOutcomes().get(0));
		} else {
			lgr.setAttribute("name", t.getName());
			if(t.get("processName") != null && !t.get("processName").equals(""))
				lgr.setAttribute("taskname", t.get("processName") + ": " + t.getName());
			else
				lgr.setAttribute("taskname", t.get("processInstanceId") + ": " + t.getName());
			lgr.setAttribute("complete", false);
			lgr.setAttribute("task", t);
			String description = t.getHtmlDescription();
			if(description != null && !description.equals(""))
				lgr.setAttribute("$#description", description);
		}
		
		return lgr;
	}
	
	public void refresh() {
		PageManager.getInstance().setPageHistory(Pages.TASK ,null);
	}
	
	public void destroy() {
		if(timer != null)
			timer.cancel();
		super.destroy();
	}
	
	protected static int contains(ListGridRecord[] records, String id) {
		boolean found = false;
		int count = 0;
		for(ListGridRecord r : records) {
			if(r == null) break;
			if(r.getAttributeAsString("id").equals(id)) {
				found = true;
				break;
			}
			count++;
		}
		if(found) return count;
		return -1;
	}
	
	protected static int contains(ArrayList<Task> tasks, String id) {
		boolean found = false;
		int count = 0;
		for(Task t : tasks) {
			if(t.get("id").equals(id)) {
				found = true;
				break;
			}
			count++;
		}
		if(found) return count;
		return -1;
	}
	
	protected static void addRecordToArray(ListGridRecord[] records, ListGridRecord record) {
		for(int i = 0; i < MAX_TASKS; i++) {
			if(records[i] == null) {
				records[i] = record;
				return;
			}
		}
	}
	
	protected static void removeRecordFromArray(ListGridRecord[] records, ListGridRecord r) {
		int index = contains(records, r.getAttributeAsString("id"));
		removeRecordFromArray(records, index);
	}
	
	protected static void removeRecordFromArray(ListGridRecord[] records, int index) {
		records[index] = null;
		for(int i = index; i < MAX_TASKS-1; i++) {
			if(records[i+1] == null) return;
			records[i] = records[i+1];
			records[i+1] = null;
		}
	}
	
	public void updateTasks() {
		int dayLength = 24*60*60*1000;
		Date d = new Date(); 
		d.setTime(d.getTime() + dayLength);
		String end = (1900 + d.getYear()) + "-" + (d.getMonth() < 9 ? "0" : "") + (d.getMonth() + 1) + "-" + (d.getDate() < 9 ? "0" : "") + d.getDate();
		d = new Date();
		d.setTime(d.getTime() - dayLength * 3);
		String start = (1900 + d.getYear()) + "-" + (d.getMonth() < 9 ? "0" : "") + (d.getMonth() + 1) + "-" + (d.getDate() < 9 ? "0" : "") + d.getDate();
		
		BpmServiceMain.sendGet("/processInstances/tasks?assignee=" + BpmServiceMain.getUser(), new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
			}
			public void onSuccess(String result) {
				ArrayList<Task> tasks = Parse.parseProcessTasks(result);
				for(Task t : tasks) {
					if(t == null) break;
					if(contains(records1, t.get("id")) == -1)
						addRecordToArray(records1, createLGR(false, t));
				}
				for(ListGridRecord r : records1) {
					if(r == null) break;
					if(contains(tasks, r.getAttributeAsString("id")) == -1)
						removeRecordFromArray(records1, r);
				}
				((ListGrid)sections[0].getItems()[0]).setData(records1);
			}
		});
		
		BpmServiceMain.sendGet("/processInstances/tasks?candidate=" + BpmServiceMain.getUser(), new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
			}
			public void onSuccess(String result) {
				ArrayList<Task> tasks = Parse.parseProcessTasks(result);
				for(Task t : tasks) {
					if(t == null) break;
					if(contains(records2, t.get("id")) == -1)
						addRecordToArray(records2, createLGR(false, t));
				}
				for(ListGridRecord r : records2) {
					if(r == null) break;
					if(contains(tasks, r.getAttributeAsString("id")) == -1)
						removeRecordFromArray(records2, r);
				}
				((ListGrid)sections[1].getItems()[0]).setData(records2);
			}
		});
		
		BpmServiceMain.sendGet("/tasks/history?assignee=" + BpmServiceMain.getUser() + "&start=" + start + "&end=" + end, new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
			}
			public void onSuccess(String result) {
				ArrayList<Task> tasks = Parse.parseTasks(result);
				for(Task t : tasks) {
					if(t == null) break;
					if(contains(records3, t.get("id")) == -1)
						addRecordToArray(records3, createLGR(true, t));
				}
				for(ListGridRecord r : records3) {
					if(r == null) break;
					if(contains(tasks, r.getAttributeAsString("id")) == -1)
						removeRecordFromArray(records3, r);
				}
				((ListGrid)sections[2].getItems()[0]).setData(records3);
			}
		});
	}
}
