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

import java.util.ArrayList;

import org.wiredwidgets.cow.webapp.client.PageManager.Pages;
import org.wiredwidgets.cow.webapp.client.bpm.Parse;
import org.wiredwidgets.cow.webapp.client.components.CustomListGrid;
import org.wiredwidgets.cow.webapp.client.page.Admin;
import org.wiredwidgets.cow.webapp.client.page.CreateNewTask;
import org.wiredwidgets.cow.webapp.client.page.EditWorkflow;
import org.wiredwidgets.cow.webapp.client.page.Login;
import org.wiredwidgets.cow.webapp.client.page.ManageWorkflows;
import org.wiredwidgets.cow.webapp.client.page.ManageWorkflows2;
import org.wiredwidgets.cow.webapp.client.page.Tasks;
import org.wiredwidgets.cow.webapp.client.page.ViewActiveWorkflows;
import org.wiredwidgets.cow.webapp.client.page.ViewWorkflow;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.gadgets.client.Gadget;
import com.google.gwt.gadgets.client.UserPreferences;
import com.google.gwt.gadgets.client.Gadget.AllowHtmlQuirksMode;
import com.google.gwt.gadgets.client.Gadget.ModulePrefs;
import com.google.gwt.gadgets.client.Gadget.UseLongManifestName;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.widgets.BaseWidget;
import com.smartgwt.client.widgets.Label;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * onModuleLoad() (or init() for gadgets) is the main() method of GWT applications
 * 
 * The API for making AJAX calls is in this class as well. You must match the type of request
 * you want to make with the appropriate method. In some cases, there are multiple methods for
 * a single request type, such as Post, PostLocation, and PostNoLocation. These all send POST
 * messages but expect different return values. If you use the wrong method, onFailure
 * will be called even if the POST was successful.
 * 
 * If compiling for a gadget, see GWTToGadget.txt. Remember, you must explicitly set
 * the gadget boolean to true. AJAX calls are handled differently internally in gadget
 * mode, but programmers should use the same API as for normal mode. 
 */
/*@ModulePrefs(title = "COW", author = "jstasik", author_email = "jstasik@mitre.org")
@UseLongManifestName(false)
@AllowHtmlQuirksMode(false)*/
public class BpmServiceMain /*extends Gadget<UserPreferences>*/ implements EntryPoint {
	// Create a remote service proxy to talk to the server-side Greeting service.
	private static final BpmServiceAsync bpmService = GWT.create(BpmService.class);
	
	// Name of the current user
	private static String user = "test";
	
	// Whether or not the app is in gadget form
	private static boolean gadget = false;
	
	// Server address
	private static String server = gadget ? "http://localhost:8080/cow-server" : "";
	
	// List of users and groups
	private static ArrayList<String> users;
	private static ArrayList<String> groups;
	
	// Namespaces for XML to send to server
	public static String modelNamespace = "http://www.wiredwidgets.org/cow/server/schema/model-v2";
	public static String serviceNamespace = "http://www.wiredwidgets.org/cow/server/schema/service";
	
	private static int pollingRate;

	/** This is the entry point method */
	public void onModuleLoad() {
		CustomListGrid.viewWorkflow("", true);
		Label templateLabel = new Label();
		templateLabel.setHeight(20);
		Label.setDefaultProperties(templateLabel);
		
		//MATT
		History.addValueChangeHandler(new ValueChangeHandler() {
			
			public void onValueChange(ValueChangeEvent arg0) {

				String[] args = arg0.getValue().toString().split("-ARG-");
				Pages p = Pages.valueOf(args[0]);
				
				
				BaseWidget b = null;
				
				switch (p){
					case TEMPLATE:
						b = new EditWorkflow(Window.Location.getParameter("template"));
						break;
					case TASK:
						b = new Tasks();
						break;
					case LOGIN:
						b = new Login();
						break;
					case WORKFLOW:
							boolean a = Boolean.valueOf(args[2]);
							b = new ViewWorkflow(args[1].toString(), a);

						break;
					case EDITWORKFLOWSTRING:
						b = new EditWorkflow(args[1].toString());
						break;	
					case VIEWACTIVEWORKFLOWS:
						b = new ViewActiveWorkflows();
						break;	
						
					case ADMIN:
						b = new Admin();
						break;
					case CREATENEWTASK:
						b = new CreateNewTask();
						break;
					case EDITWORKFLOWBOOLEAN:
						boolean a1 = Boolean.valueOf(args[2]);
						b = new EditWorkflow(a1);
						break;
					case MANAGEWORKFLOWS2:
						b = new ManageWorkflows2();
						break;
					case MANAGEWORKFLOWS:
						b = new ManageWorkflows();
						break;
						
					case VIEWWORKFLOW:
						
						if ((args.length > 2)){
							boolean a11 = Boolean.valueOf(args[2]);					
							b = new ViewWorkflow(args[1].toString(), a11);
						}
						else 
							b = new ViewWorkflow(args[1].toString());
						break;
					default:
						b = new Login();
						break;
						
				}
				
				PageManager.getInstance().setPage(b);
				

				
			}
		});
		
		if(Window.Location.getParameter("pollingRate") != null) {
			pollingRate = Integer.parseInt(Window.Location.getParameter("pollingRate")) * 1000;
		} else {
			pollingRate = 60000;
		}
		
		if(Window.Location.getParameter("user") != null) {
			user = Window.Location.getParameter("user");
			String[] args = null;
			if(Window.Location.getParameter("template") != null){
				PageManager.getInstance().setPageHistory(Pages.TEMPLATE,args);
			}
			else {
				PageManager.getInstance().setPageHistory(Pages.TASK ,args);
			}
		} else {
			PageManager.getInstance().setPageHistory(Pages.LOGIN ,null);
		}
	}
	
	/*protected void init(UserPreferences preferences) {
		PageManager.getInstance().setPage(new Login());
	}*/

	public static String getUser() {
		return user;
	}

	public static void setUser(String name) {
		user = name;
	}
	
	public static int getPollingRate() {
		return pollingRate;
	}
	
	public static ArrayList<String> getUsers() {
		return users;
	}

	public static BpmServiceAsync getService() {
		return bpmService;
	}
	
	/**
	 * Sends a GET
	 * @param url The GET url
	 * @param call The AsyncCallback of what to do on success/failure
	 */
	public static void sendGet(String url, AsyncCallback<String> call) {
		if(gadget)
			makeRequest(server + url, "", "GET", call);
		else
			bpmService.getForObject(server + url, new String[]{}, call);
	}
	
	/**
	 * Sends a POST
	 * @param url The POST url
	 * @param request The POST data
	 * @param call The AsyncCallback of what to do on success/failure
	 */
	public static void sendPost(String url, String request, AsyncCallback<String> call) {
		if(gadget)
			makeRequest(server + url, request, "POST", call);
		else
			bpmService.postForObject(server + url, request, new String[]{}, call);
	}
	
	/**
	 * Sends a DELETE
	 * @param url The DELETE url
	 * @param call The AsyncCallback of what to do on success/failure
	 */
	public static void sendDelete(String url, AsyncCallback<Void> call) {
		if(gadget)
			makeVoidRequest(server + url, "", "DELETE", call);
		else
			bpmService.delete(server + url, new String[]{}, call);
	}
	
	public static void sendDelete(String url, boolean uri, AsyncCallback<Void> call) {
		if(gadget)
			makeVoidRequest(server + url, "", "DELETE", call);
		else {
			if(uri)
				bpmService.delete(server + url, call);
			else
				bpmService.delete(server + url, new String[]{}, call);
		}
	}
	
	/**
	 * Sends a POST which receives a location instead of a normal response
	 * @param url the POST url
	 * @param request the POST data
	 * @param call The AsyncCallback of what to do on success/failure
	 */
	public static void sendPostLocation(String url, String request, AsyncCallback<String> call) {
		if(gadget)
			makeRequest(server + url, request, "POST", call);
		else
			bpmService.postForLocation(server + url, request, new String[]{}, call);
	}
	
	/**
	 * Sends a POST which receives no response
	 * @param url the POST url
	 * @param request the POST data
	 * @param call The AsyncCallback of what to do on success/failure
	 */
	public static void sendPostNoLocation(String url, AsyncCallback<Void> call) {
		if(gadget)
			makeVoidRequest(server + url, "", "POST", call);
		else
			bpmService.postForNoContent(server + url, new String[]{}, call);
	}
	
	/**
	 * Writes a string to Firebug
	 * @param s The string to write
	 */
	public static native void debug(String s) /*-{
		if(console)
			console.log(s);
	}-*/;
	
	/**
	 * Writes an int to Firebug
	 * @param i The int to write
	 */
	public static native void debug(int i) /*-{
		if(console)
			console.log(i);
	}-*/;
	
	/**
	 * Whether or not the application is flagged to compile as a gadget
	 * @return true if the component will be in gadget mode, otherwise false
	 */
	public static boolean isGadget() {
		return gadget;
	}
	
	public static native String urlEncode(String s) /*-{
		return encodeURIComponent(s);
	}-*/;
	
	public static native String urlDecode(String s) /*-{
		return decodeURIComponent(s);
	}-*/;
	
	public static native String xmlEncode(String s) /*-{
		if(s == null) return "";
		s = s.replace(/&/g, '&amp;');
		s = s.replace(/\"/g, '&quot;');
		s = s.replace(/\'/g, '&apos;');
		s = s.replace(/</g, '&lt;');
		s = s.replace(/>/g, '&gt;');
		s = s.replace(/\n/g, '&#10;');
		return s;
	}-*/;
	
	public static native String xmlDecode(String s) /*-{
		if(s == null) return "";
		s = s.replace(/&amp;/g, '&');
		s = s.replace(/&quot;/g, '\"');
		s = s.replace(/&apos;/g, '\'');
		s = s.replace(/&lt;/g, '<');
		s = s.replace(/&gt;/g, '>');
		s = s.replace(/&#10;/g, '\n');
		return s;
	}-*/;
	
	/**
	 * Used to make requests in gadget form
	 * @param url
	 * @param postdata
	 * @param type
	 * @param callback
	 */
	protected static native void makeRequest(String url, String postdata, String type, AsyncCallback<String> callback) /*-{
		var params = {};
		params[$wnd.gadgets.io.RequestParameters.REFRESH_INTERVAL] = 1;
		if(type == "POST") {
            params[$wnd.gadgets.io.RequestParameters.METHOD] = $wnd.gadgets.io.MethodType.POST; //(1)
            var headers = {};
            headers['Content-Type'] = 'application/xml';
            params[$wnd.gadgets.io.RequestParameters.HEADERS]= headers;
            params[$wnd.gadgets.io.RequestParameters.POST_DATA]= postdata; //(2)
        } else if(type == "GET")
            params[$wnd.gadgets.io.RequestParameters.METHOD] = $wnd.gadgets.io.MethodType.GET; //(1)
        else if(type == "DELETE")
			params[$wnd.gadgets.io.RequestParameters.METHOD] = $wnd.gadgets.io.MethodType.DELETE; //(1)
        $wnd.gadgets.io.makeRequest(url, response, params); //(3)
	
    	function response(obj) {
          	@org.wiredwidgets.cow.webapp.client.BpmServiceMain::onSuccessInternal(Lorg/wiredwidgets/cow/webapp/client/GadgetResponse;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(obj,callback);
    	};
	}-*/;
	
	/**
	 * Used to make requests in gadget form
	 * @param url
	 * @param postdata
	 * @param type
	 * @param callback
	 */
	protected static native void makeVoidRequest(String url, String postdata, String type, AsyncCallback<Void> callback) /*-{
		var params = {};
		params[$wnd.gadgets.io.RequestParameters.REFRESH_INTERVAL] = 1;
		if(type == "POST") {
        	params[$wnd.gadgets.io.RequestParameters.METHOD] = $wnd.gadgets.io.MethodType.POST; //(1)
           	params[$wnd.gadgets.io.RequestParameters.POST_DATA]= postdata; //(2)
           	var headers = {};
           	headers['Content-Type'] = 'application/xml';
           	params[$wnd.gadgets.io.RequestParameters.HEADERS]= headers;
        } else if(type == "GET")
           	params[$wnd.gadgets.io.RequestParameters.METHOD] = $wnd.gadgets.io.MethodType.GET; //(1)
        else if(type == "DELETE")
			params[$wnd.gadgets.io.RequestParameters.METHOD] = $wnd.gadgets.io.MethodType.DELETE; //(1)
        $wnd.gadgets.io.makeRequest(url, response, params); //(3)
		
		function response(obj) {
      		@org.wiredwidgets.cow.webapp.client.BpmServiceMain::onSuccessInternalVoid(Lorg/wiredwidgets/cow/webapp/client/GadgetResponse;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(obj,callback);
		};
	}-*/;
	
	/**
	 * Used to handle requests in gadget form
	 * @param response
	 * @param callback
	 */
	protected static void onSuccessInternal(final GadgetResponse response, AsyncCallback<String> callback) {
		try {
			if(response.getText().startsWith("<html><head>")) { 
				callback.onFailure(null);
			} else {
				callback.onSuccess(response.getText());
			}
		} catch (Exception e) {
			callback.onFailure(e);
		}		
	}
	
	/**
	 * Used to handle requests in gadget form
	 * @param response
	 * @param callback
	 */
	protected static void onSuccessInternalVoid(final GadgetResponse response, AsyncCallback<String> callback) {
		try {
			if(response.getText().startsWith("<html><head>")) { 
				callback.onFailure(null);
			} else {
				callback.onSuccess(null);
			}
		} catch (Exception e) {
			callback.onFailure(e);
		}		
	}
}

