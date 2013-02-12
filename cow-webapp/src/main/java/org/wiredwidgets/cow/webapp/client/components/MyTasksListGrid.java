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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;
import org.wiredwidgets.cow.webapp.client.PageManager;
import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.bpm.Task;
import org.wiredwidgets.cow.webapp.client.page.PageWidget;
import org.wiredwidgets.cow.webapp.client.page.Tasks;
import org.wiredwidgets.cow.webapp.client.page.ViewWorkflow;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.RowEndEditAction;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.BaseWidget;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
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
public class MyTasksListGrid extends ListGrid {

	public MyTasksListGrid() {
		super();
	}

	public MyTasksListGrid(JavaScriptObject jsObj) {
		super(jsObj);
	}
	
	@Override
	/**
	 * Used to display Task details on the Tasks page
	 */
	public Canvas getExpansionComponent(final ListGridRecord record) {
        final ListGrid grid = this;  
        final ArrayList<String> outcomeString = new ArrayList<String>();
        outcomeString.add("");
        
        VLayout layout = new VLayout(5);  
        layout.setPadding(5); 
        HLayout hlayoutUpper = new HLayout();
        

        final ListGrid taskGrid = new ListGrid(){
           //Doesn't allow the varible name or required flag to be changed unless a user has just added it
        	@Override
            protected boolean canEditCell (int rowNum, int colNum){
            	ListGridRecord existingRecord = getRecord(rowNum);
            	if (existingRecord != null && 
            			existingRecord.getAttribute("varname") != null && 
            			//Used to verify GWT hasn't added a non-breaking space for the table
            			existingRecord.getAttribute("varname").replace("\u00a0","").replace("&nbsp;","").length() >= 0) {
            		//Ensures this is the value
            		return (colNum ==1) && super.canEditCell(rowNum, colNum) ;            	    	
            	    	
            	    }
            	return true;
            	    
            	    
        	
        }; 
  
        };
        taskGrid.setWidth(750);  
        taskGrid.setHeight(130);  
        taskGrid.setCellHeight(22);  
         
       
        

        taskGrid.setCanEdit(true);  
        //taskGrid.setModalEditing(true);
        taskGrid.setEditByCell(true);
        taskGrid.setEditEvent(ListGridEditEvent.CLICK);  
        taskGrid.setListEndEditAction(RowEndEditAction.NEXT);  
        taskGrid.setAutoSaveEdits(false);  
        ListGridField variable = new ListGridField("varname", "Variable Name");
        //variable.setCanEdit(false);
        CellFormatter formatter = new CellFormatter() {
        	public String format(Object value, ListGridRecord record, int rowNum, int colNum) {
				if(value == null) return "";
				String s = value.toString();
				if(!record.getAttribute(s).equals("$#description")) {
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

	
		};
		
		ListGridField required = new ListGridField("required", "Required");
		required.setType(ListGridFieldType.BOOLEAN);
        taskGrid.setFields(variable, new ListGridField("value", "Value"),required);
        taskGrid.setCellFormatter(formatter);
        Integer count = 0;
        ListGridRecord[] records = new ListGridRecord[record.getAttributes().length];
        ListGridRecord lgr = null;
        for(String s : record.getAttributes()) {
        	lgr = new ListGridRecord();
    		//lgr.setAttribute("taskname", t.get("id"));
        
        	lgr.setAttribute("varname", s);
        	lgr.setAttribute("value", record.getAttribute(s));
            lgr.setAttribute("required",((Boolean)(s.charAt(0) < 'p')));
        	records[count] = lgr;
        	

        	count++;
        }
        taskGrid.setData(records);
        
        
        layout.addMember(taskGrid);  

        HLayout hLayout = new HLayout(10);  
        hLayout.setAlign(Alignment.LEFT);  

        IButton saveButton = new IButton("Save");  
        saveButton.setTop(250);  
        saveButton.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) {  
            	
            	taskGrid.saveAllEdits(); 
            	//TODO currently can only SAVE Variables back to server when completing - so that is what this does
            	String varString = "";
            	ListGridRecord[] recs = taskGrid.getRecords();
            	for (ListGridRecord rec: recs){
            		String var = rec.getAttribute("varname");
            		//Current vars must start with $# 
            		if (var.startsWith("$#") && var!=("$#id")){
            			var = var.substring(2);
            			varString += "&var=" + BpmServiceMain.urlEncode(var+ ":"+rec.getAttribute("value"));
            		}
            	}
            	
            	
            	
            	
        		
        		Task t = (Task)record.getAttributeAsObject("task");     
        		if (t.getOutcomes().size() > 0){
        			if (!(outcomeString.get(0).startsWith("?"))){
        				SC.say("Error. Please make your decision.");
        				return;
        			}
        		}
        		if(outcomeString.get(0).equals(""))
        			varString = varString.replaceFirst("&", "?");
        		final String tempString = record.getAttribute("id") + outcomeString.get(0) + varString;
        		BpmServiceMain.sendDelete("/tasks/active/" + tempString, true, new AsyncCallback<Void>() {
    				public void onFailure(Throwable arg0) {
    					SC.say("Error. Please ensure that you are connected to the Internet, that the server is currently online, and that the task was not already completed.");
    				}
    				public void onSuccess(Void arg0) {
    					((Tasks) PageManager.getInstance().getPage()).updateTasks();
    				}
    			});
            	
 
            }  
        });  
        hLayout.addMember(saveButton);  

        IButton discardButton = new IButton("Discard");  
        discardButton.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) {  
            	taskGrid.discardAllEdits();  
            }  
        });  
        hLayout.addMember(discardButton);  

        IButton closeButton = new IButton("Close");  
        closeButton.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) {  
                grid.collapseRecord(record);  
            }  
        });  
        hLayout.addMember(closeButton);  
        
        
        IButton addRowButton = new IButton("Add Variable");  
        addRowButton.addClickHandler(new ClickHandler() {  
            public void onClick(ClickEvent event) {  
            	taskGrid.startEditingNew();
                
            }  
        });  
        hLayout.addMember(addRowButton);  
        
                                         
        layout.addMember(hLayout);  
        
        hlayoutUpper.addMember(layout);
        Task t = (Task)record.getAttributeAsObject("task");
		ArrayList<String> outcomes = t.getOutcomes();
		if (outcomes.size() > 0 ){
	        final DynamicForm form = new DynamicForm();  
	        form.setWidth(300);  
	          
	        SelectItem item = new SelectItem();  
	        item.setTitle("Decision");  
	        
	        item.setValueMap(outcomes.toArray(new String[outcomes.size()]));
	        //item.setValueMap((String[]) outcomes.toArray());
	        
	        item.setWidth(250);  
	        item.setPickListWidth(250);  
	        ChangedHandler handler = new ChangedHandler(){
	        	public void onChanged(ChangedEvent event){
	        		outcomeString.set(0, "?outcome=" + BpmServiceMain.urlEncode((String)event.getValue()));
	        	}
	        };
			item.addChangedHandler(handler);
	        form.setItems(item);  
	  
	        hlayoutUpper.addMember(form);  
	        hlayoutUpper.setAlign(Alignment.LEFT);
	        
		}
        
        
        
        
        return hlayoutUpper;  
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
	

}
