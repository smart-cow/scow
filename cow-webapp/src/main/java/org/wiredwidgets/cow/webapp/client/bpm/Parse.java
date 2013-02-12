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

package org.wiredwidgets.cow.webapp.client.bpm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.wiredwidgets.cow.webapp.client.BpmServiceMain;

import com.gwt.components.client.xml.Document;
import com.gwt.components.client.xml.Node;
import com.smartgwt.client.util.SC;

/**
 * A set of static methods used for parsing XML data
 * @author JSTASIK
 *
 */
public class Parse {	
	public static Template parseTemplate(String s) {
		s = clean(s);		
		Document doc = Document.xmlParse(s);
		ArrayList<Node> children = doc.getChildren();
		Node first = children.get(0);
		Node process = null;
		String cname = first.getName();
		Template t = new Template();
		if (cname.endsWith(("process"))){
			process = first;
			t.setName(getAttributeValue(process, "name"));
		}
		else if (cname.endsWith(("processInstance"))){
			ArrayList<Node> nodes = first.getChildNodes();
			process = nodes.get(6);
			String processInstanceName = ((Node)nodes.get(5).getChildNodes().get(0)).getValue();
			t.setName(processInstanceName);
		}
		
		
		
		t.setKey(getAttributeValue(process, "key"));
		ArrayList<Node> nodes = ((ArrayList<Node>)process.getChildNodes());
		for(int i = 0; i < nodes.size(); i++) {
			String name = nodes.get(i).getName();
			if(name.split(":").length > 1)
				name = name.split(":")[1];
			if(name.equals("activities"))
				t.setBase((BaseList)parseActivity(nodes.get(i), true));
		}
		return t;
	}
	
	
	
	public static Template parseTemplateNew(String s) {
		s = clean(s);		
		Document doc = Document.xmlParse(s);
		
		Node process = (Node)((Document) doc.getChildren().get(0)).getChildren().get(6);
		Template t = new Template();
		t.setName(getAttributeValue(process, "name"));
		t.setKey(getAttributeValue(process, "key"));
		ArrayList<Node> nodes = ((ArrayList<Node>)process.getChildNodes());
		for(int i = 0; i < nodes.size(); i++) {
			String name = nodes.get(i).getName();
			if(name.split(":").length > 1)
				name = name.split(":")[1];
			if(name.equals("activities"))
				t.setBase((BaseList)parseActivity(nodes.get(i), true));
		}
		return t;
	}
	
	
	
	public static Map<String, String> parseTemplateCompletion(String s) {
		s = clean(s);		
		Document doc = Document.xmlParse(s);
		ArrayList<Node> children = doc.getChildren();
		Node first = children.get(0);
		Node process = null;
		String cname = first.getName();
		if (cname.endsWith(("process"))){
			process = first;
		}
		else if (cname.endsWith(("processInstance"))){
			ArrayList<Node> nodes = first.getChildNodes();
			process = nodes.get(6);
		}
		Map<String, String> map = new HashMap<String, String>();
		parseNodeCompletion((Node)(process.getChildNodes().get(0)), map);
		return map;
	}
	
	
	
	
	protected static void parseNodeCompletion(Node node, Map<String, String> map) {
		String completion = parseCompletionState(node);
		if(!completion.equals("") && !getAttributeValue(node, "name").equals("base")) {
			map.put(getAttributeValue(node, "name"), completion);
		}
		for(int i = 0; i < node.getChildNodes().size(); i++)
			parseNodeCompletion((Node)(node.getChildNodes().get(i)), map);
	}
	
	public static Activities parseCopyTemplate(String s) {
		s = clean(s);		
		Document doc = Document.xmlParse(s);
		Node process = (Node)doc.getChildren().get(0);
		Activities a = (Activities)parseActivity(((ArrayList<Node>)process.getChildNodes()).get(0));
		a.setName(getAttributeValue(process, "name"));
		a.setKey(getAttributeValue(process, "key"));
		return a;
	}
	
	public static HashMap<String, String> parseVariables(Node variables) {
		HashMap<String, String> vars = new HashMap<String, String>();
		for(Node n : (ArrayList<Node>)variables.getChildNodes()) {
			vars.put(getAttributeValue(n, "name"), getAttributeValue(n, "value"));
		}
		return vars;
	}
	
	public static Activity parseActivity(Node activity) {
		return parseActivity(activity, false);
	}
	
	public static Activity parseActivity(Node activity, boolean base) {
		String name = activity.getName();
		if(name.split(":").length > 1)
			name = name.split(":")[1];
		boolean bypass = Boolean.parseBoolean(getAttributeValue(activity, "bypassable"));
		if(name.equals("activities") && base) {
			BaseList b = new BaseList();
			for(Node n : (ArrayList<Node>)activity.getChildNodes()) {
				String s = n.getName();
				if(s.split(":").length > 1)
					s = s.split(":")[1];
				if(s.equals("description")) {
					if(n.getChildNodes().size() > 0)
						b.setDescription(getValue(n), true);
				} else
					b.addActivity(parseActivity(n));
			}
			b.setCompletion(parseCompletionState(activity));
			return b;
		} else if(name.equals("activities")) {
			Activities a = new Activities(getAttributeValue(activity, "name"), getAttributeValue(activity, "key"), getAttributeValue(activity, "sequential"));
			for(Node n : (ArrayList<Node>)activity.getChildNodes()) {
				String s = n.getName();
				if(s.split(":").length > 1)
					s = s.split(":")[1];
				if(s.equals("description")) {
					if(n.getChildNodes().size() > 0)
						a.setDescription(getValue(n), true);
				} else
					a.addActivity(parseActivity(n));
			}
			a.setBypass(bypass);
			a.setCompletion(parseCompletionState(activity));
			return a;
		} else if(name.equals("task") || name.equals("loopTask")) {
			Task t = new Task(getAttributeValue(activity, "name"), getAttributeValue(activity, "key"));
			for(Node n : (ArrayList<Node>)activity.getChildNodes()) {
				String s = n.getName();
				if(s.split(":").length > 1)
					s = s.split(":")[1];
				if(s.equals("assignee")) {
					t.set("assignee", getValue(n));
					t.set("assigneeType", "User");
				} else if(s.equals("candidateGroups")) {
					t.set("assignee", getValue(n));
					t.set("assigneeType", "Group");
				} else if(s.equals("description")) {
					if(n.getChildNodes().size() > 0)
						t.setDescription(getValue(n), true);
				} else if(s.equals("variables")) {
					ArrayList<Node> variables = n.getChildNodes();
					for(int k = 0; k < variables.size(); k++) {
						t.setVariable(getAttributeValue(variables.get(k), "name"), getAttributeValue(variables.get(k), "value"));
					}
				} 
			}
			t.setBypass(bypass);
			t.setCompletion(parseCompletionState(activity));
			return t;
		} else if(name.equals("exit")) {
			Exit e = new Exit(getAttributeValue(activity, "name"), getAttributeValue(activity, "key"));
			e.setReason(getAttributeValue(activity, "state"));
			for(Node n : (ArrayList<Node>)activity.getChildNodes()) {
				String s = n.getName();
				if(s.split(":").length > 1)
					s = s.split(":")[1];
				if(s.equals("description")) {
					if(n.getChildNodes().size() > 0)
						e.setDescription(getValue(n), true);
				}
			}
			e.setCompletion(parseCompletionState(activity));
			return e;
		} else if(name.equals("serviceTask")) {
			ServiceTask t = new ServiceTask(getAttributeValue(activity, "name"), getAttributeValue(activity, "key"));
			for(Node n : (ArrayList<Node>)activity.getChildNodes()) {
				String s = n.getName();
				if(s.split(":").length > 1)
					s = s.split(":")[1];
				if(s.equals("url")) {
					t.setServiceUrl(getValue(n));
				} else if(s.equals("content")) {
					t.setContent(getValue(n));
				} else if(s.equals("var")) {
					t.setVar(getValue(n));
				} else if(s.equals("method")) {
					t.setMethod(getValue(n));
				} else if(s.equals("description")) {
					if(n.getChildNodes().size() > 0)
						t.setDescription(getValue(n), true);
				}
			}
			t.setBypass(bypass);
			t.setCompletion(parseCompletionState(activity));
			return t;
		} else if(name.equals("decision")) {
			Decision d = new Decision(getAttributeValue(activity, "name"), getAttributeValue(activity, "key"));
			for(Node n : (ArrayList<Node>)activity.getChildNodes()) {
				String s = n.getName();
				if(s.split(":").length > 1)
					s = s.split(":")[1];
				if(s.equals("task"))
					d.setTask((Task)parseActivity(n));
				else if(s.equals("option"))
					d.addOption(new Option(d, (Activities)parseActivity((Node)n.getChildNodes().get(0)), getAttributeValue(n, "name")));
				else if(s.equals("description")) {
					if(n.getChildNodes().size() > 0)
						d.setDescription(getValue(n), true);
				}
			}
			d.setBypass(bypass);
			d.setCompletion(parseCompletionState(activity));
			return d;
		} else if(name.equals("loop")) {
			Loop l = new Loop(getAttributeValue(activity, "name"), getAttributeValue(activity, "key"));
			for(Node n : (ArrayList<Node>)activity.getChildNodes()) {
				String s = n.getName();
				if(s.split(":").length > 1)
					s = s.split(":")[1];
				if(s.equals("loopTask"))
					l.setTask((Task)parseActivity(n));
				else if(s.equals("description")) {
					if(n.getChildNodes().size() > 0)
						l.setDescription(getValue(n), true);
				} else
					l.setActivities((Activities)parseActivity(n));
			}
			l.setBypass(bypass);
			l.setCompletion(parseCompletionState(activity));
			return l;
		} else if(name.equals("subProcess")) {
			SubProcess s = new SubProcess(getAttributeValue(activity, "name"), getAttributeValue(activity, "key"));
			s.setWorkflow(getAttributeValue(activity, "sub-process-key"));
			s.setBypass(bypass);
			s.setCompletion(parseCompletionState(activity));
			return s;
		}
		
		return null;
	}
	
	protected static String parseCompletionState(Node activity) {
		String state = getAttributeValue(activity, "completionState");
		if(state == null)
			return "";
		else if(state == "not started")
			return "notStarted";
		return state;
	}
	
	public static ArrayList<Task> parseTasks(String xml) {
		ArrayList<Task> tasks = new ArrayList<Task>();
		xml = clean(xml);

		Document doc = Document.xmlParse(xml);
		Node root = (Node)doc.getChildren().get(0);
		for(int i = 0; i < root.getChildNodes().size(); i++) {
			Node task = ((ArrayList<Node>)root.getChildNodes()).get(i);
			ArrayList<Node> children = task.getChildNodes();
			tasks.add(new Task());
			tasks.get(i).set("id", getAttributeValue(task, "id"));
			for(int j = 0; j < task.getChildNodes().size(); j++) {
				String s = children.get(j).getName();
				if(s.split(":").length > 1)
					s = s.split(":")[1];
				if(s.equals("executionId")) {
					tasks.get(i).set("processInstanceId", getValue(children.get(j)));
				} else if(s.equals("name")) {
					tasks.get(i).setName(getValue(children.get(j)));
				} else if(s.equals("description")) {
					if(children.get(j).getChildNodes().size() > 0) {
						tasks.get(i).setDescription(getValue(children.get(j)));
						if(tasks.get(i).getName() == null || tasks.get(i).getName().equals(""))
							tasks.get(i).setName(getValue(children.get(j)));
					}
				} else if(s.equals("outcome")) {
					tasks.get(i).addOutcome(getValue(children.get(j)));
				} else if(s.equals("variables")) {
					ArrayList<Node> variables = children.get(j).getChildNodes();
					for(int k = 0; k < variables.size(); k++) {
						tasks.get(i).set(getAttributeValue(variables.get(k), "name"), getAttributeValue(variables.get(k), "value"));
					}
				} else {
					tasks.get(i).set(s, getValue(children.get(j)));
				}
			}
		}
		return tasks;
	}
	
	public static ArrayList<Task> parseProcessTasks(String xml) {
		ArrayList<Task> tasks = new ArrayList<Task>();
		xml = clean(xml);

		Document doc = Document.xmlParse(xml);
		Node root = (Node)doc.getChildren().get(0);
		for(int a = 0; a < root.getChildNodes().size(); a++) {
			Node processInstance = ((ArrayList<Node>)root.getChildNodes()).get(a);
			String name = "";
			for(int i = 0; i < processInstance.getChildNodes().size(); i++) {
				Node task = ((ArrayList<Node>)processInstance.getChildNodes()).get(i);
				String taskTag = task.getName();
				if(taskTag.split(":").length > 1)
					taskTag = taskTag.split(":")[1];
				if(taskTag.equals("task")) {
					ArrayList<Node> children = task.getChildNodes();
					Task t = new Task();
					tasks.add(t);
					t.set("id", getAttributeValue(task, "id"));
					t.set("processName", name);
					for(int j = 0; j < task.getChildNodes().size(); j++) {
						String s = children.get(j).getName();
						if(s.split(":").length > 1)
							s = s.split(":")[1];
						if(s.equals("executionId")) {
							t.set("processInstanceId", getValue(children.get(j)));
						} else if(s.equals("name")) {
							t.setName(getValue(children.get(j)));
						} else if(s.equals("description")) {
							if(children.get(j).getChildNodes().size() > 0) {
								t.setDescription(getValue(children.get(j)));
								if(t.getName() == null || t.getName().equals(""))
									t.setName(getValue(children.get(j)));
							}
						} else if(s.equals("outcome")) {
							t.addOutcome(getValue(children.get(j)));
						} else if(s.equals("variables")) {
							ArrayList<Node> variables = children.get(j).getChildNodes();
							for(int k = 0; k < variables.size(); k++) {
								t.set(getAttributeValue(variables.get(k), "name"), getAttributeValue(variables.get(k), "value"));
							}
						} else {
							t.set(s, getValue(children.get(j)));
						}
					}
				} else if(taskTag.equals("name")) {
					name = getValue(task);
				}
			}	
		}
		return tasks;
	}
	
	public static ArrayList<String> parseProcessDefinitions(String xml) {
		xml = clean(xml);
		ArrayList<String> processDefinitions = new ArrayList<String>();
		
		Document doc = Document.xmlParse(xml);
		Node root = (Node)doc.getChildren().get(0);
		for(int i = 0; i < root.getChildNodes().size(); i++) {
			Node process = ((ArrayList<Node>)root.getChildNodes()).get(i);
			ArrayList<Node> children = process.getChildNodes();
			for(int j = 0; j < process.getChildNodes().size(); j++) {
				String s = children.get(j).getName();
				if(s.split(":").length > 1)
					s = s.split(":")[1];
				if(s.equals("name")) {
					processDefinitions.add(getValue(children.get(j)));
					break;
				}
			}
		}
		Collections.sort(processDefinitions, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		return processDefinitions;
	}

	public static ArrayList<String> parseTemplateInstances(String xml) {
		xml = clean(xml);
		ArrayList<String> templateInstances = new ArrayList<String>();

		Document doc = Document.xmlParse(xml);
		Node root = (Node)doc.getChildren().get(0);
		for(int i = 0; i < root.getChildNodes().size(); i++) {
			Node instance = ((ArrayList<Node>)root.getChildNodes()).get(i);
			ArrayList<Node> children = instance.getChildNodes();
			String id = "";
			boolean foundName = false;
			for(int j = 0; j < instance.getChildNodes().size(); j++) {
				String s = children.get(j).getName();
				if(s.split(":").length > 1)
					s = s.split(":")[1];
				if(s.equals("name")) {
					foundName = true;
					templateInstances.add(getValue(children.get(j)));
				} else if(s.equals("id")) {
					id = getValue(children.get(j));
				}
			}
			if(!foundName)
				templateInstances.add(id);
		}
		return templateInstances;
	}
	
	public static ArrayList<String> parseTemplateInstancesIds(String xml) {
		xml = clean(xml);
		ArrayList<String> templateInstances = new ArrayList<String>();

		Document doc = Document.xmlParse(xml);
		Node root = (Node)doc.getChildren().get(0);
		for(int i = 0; i < root.getChildNodes().size(); i++) {
			Node instance = ((ArrayList<Node>)root.getChildNodes()).get(i);
			ArrayList<Node> children = instance.getChildNodes();
			for(int j = 0; j < instance.getChildNodes().size(); j++) {
				String s = children.get(j).getName();
				if(s.split(":").length > 1)
					s = s.split(":")[1];
				if(s.equals("id"))
					templateInstances.add(getValue(children.get(j)));
			}
		}
		return templateInstances;
	}
	
	public static ArrayList<String> parseGroups(String xml) {
		xml = clean(xml);
		ArrayList<String> groups = new ArrayList<String>();

		Document doc = Document.xmlParse(xml);
		Node root = (Node)doc.getChildren().get(0);
		for(int i = 0; i < root.getChildNodes().size(); i++) {
			Node group = ((ArrayList<Node>)root.getChildNodes()).get(i);
			ArrayList<Node> children = group.getChildNodes();
			for(int j = 0; j < children.size(); j++) {
				String s = children.get(j).getName();
				if(s.split(":").length > 1)
					s = s.split(":")[1];
				if(s.equals("name")) {
					groups.add(getValue(children.get(j)));
					break;
				}				
			}
		}
		return groups;
	}
	
	public static ArrayList<String> parseUsers(String xml) {
		xml = clean(xml);
		ArrayList<String> users = new ArrayList<String>();

		Document doc = Document.xmlParse(xml);
		Node root = (Node)doc.getChildren().get(0);
		for(int i = 0; i < root.getChildNodes().size(); i++) {
			Node user = ((ArrayList<Node>)root.getChildNodes()).get(i);
			ArrayList<Node> children = user.getChildNodes();
			for(int j = 0; j < children.size(); j++) {
				String s = children.get(j).getName();
				if(s.split(":").length > 1)
					s = s.split(":")[1];
				if(s.equals("id")) {
					users.add(getValue(children.get(j)));
					break;
				}				
			}
		}
		return users;
	}
	
	/**
	 * Strips XML of unnecessary whitespace that causes parsing problems
	 * @param xml The XML to clean
	 * @return The cleaned XML, as a new String
	 */
	protected static String clean(String xml) {
		/*xml = xml.replaceAll("\r", "");
		xml = xml.replaceAll("\n", "");
		return xml.replaceAll("   ", "");*/
		return xml.replaceAll("\r\n", "\n");
	}
	
	/**
	 * Gets the text value of a node
	 * @param n The node to get the value for
	 * @return The text from the node
	 */
	protected static String getValue(Node n) {
		String s = ((Node)n.getChildNodes().get(0)).getValue();
		//if(s != null)
		//	s = BpmServiceMain.xmlDecode(s);
		return s;
	}
	
	protected static String getAttributeValue(Node n, String attribute) {
		String s = n.getAttribute(attribute);
		//if(s != null)
		//	s = BpmServiceMain.xmlDecode(s);
		return s;
	}
}
