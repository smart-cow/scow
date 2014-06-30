package amqp.denim;


import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import amqp.AmqpMessage;
import amqp.AmqpReceiver;
import amqp.SimpleAmqpConsumer;






import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;


public class DenimConsumer extends SimpleAmqpConsumer{
	//<wflow name>.<wflow id>.<category>.<action>."user".<username>
	public static final String DENIM_ROUTING_KEY = "#.tasks.take.user.#";
	public DenimConsumer(AmqpReceiver amqpReceiver)
			throws IOException {
		super(amqpReceiver, DENIM_ROUTING_KEY);
		
	}
	public String lastMessage = "";
	
	@Override
	public void handleAmqpMessage(AmqpMessage amqpMessage) {
		if ((lastMessage != null) && lastMessage.equals(amqpMessage.getMessageBody().toString())){
			System.out.println("----------------Duplicate Message Received----------------------");
			System.out.println(lastMessage);
			System.out.println("----------------------------------------------------------------");
			return;
		}
		//Don't consider duplicate messages
		lastMessage = amqpMessage.getMessageBody().toString();
		System.out.println("----------------New Message Received----------------------");
		System.out.println(amqpMessage.toString());
		System.out.println("             --------------------------------------         ");
		if ((amqpMessage != null) && 
				(("mhowansky").equals(amqpMessage.getUser()))	&& 
				(amqpMessage.getWorkflowName().contains("denim_test"))				
				){
			
			System.out.println("Workflow: "  + amqpMessage.getWorkflowName());
			System.out.println("User: "  + amqpMessage.getUser());
			System.out.println("Group: "  + amqpMessage.getGroup());
			System.out.println("Category: "  + amqpMessage.getCategory());
			System.out.println("Action: "  + amqpMessage.getAction());			
			System.out.println("Body: "  + amqpMessage.getMessageBody());
			
			
			try {
				HashMap<String,Object> result = new ObjectMapper().readValue(amqpMessage.getMessageBody(), HashMap.class);
				System.out.println("Body Obj: " + result.toString());
				/*
				Complete name (task)
				delete processInstanceId.split('.')[0]
				Start new WF with vars name 
				*/
				System.out.println("Outcomes: " + result.get("outcomes"));
				if (("[]").equals(result.get("outcomes").toString())){			
					System.out.println("A decision has been made");
					CredentialsProvider credsProvider = new BasicCredentialsProvider();
				    credsProvider.setCredentials(
				        new AuthScope("scout3.mitre.org", AuthScope.ANY_PORT), 
				        new UsernamePasswordCredentials("mhowansky", "matt"));
	
	
				    DefaultHttpClient client = new DefaultHttpClient();
				    client.setCredentialsProvider(credsProvider);
	
				    String completTaskUrl = "http://scout3.mitre.org:8080/cow-server/tasks/" + result.get("id");
				    System.out.println("DELETE call to " + completTaskUrl + " in .5 seconds");
				    HttpDelete completeTask = new HttpDelete(completTaskUrl);
				    
				    ResponseHandler<String> handler = new BasicResponseHandler();
				    Thread.sleep(500);
				    String resp = client.execute(completeTask, handler);
				    System.out.println("Response from DELETE: " + resp);
				    
				    
				    String startWfUrl = "http://scout3.mitre.org:8080/cow-server/processInstances";
				    System.out.println("Post call to " + startWfUrl + " in .5 seconds");
				    HttpPost startWf = new HttpPost(startWfUrl);
				    
				    Thread.sleep(500);
				    
				    String varString = "";
				    
				    ArrayList<LinkedHashMap> vars = (ArrayList<LinkedHashMap>)(((LinkedHashMap)result.get("variables")).get("variables"));
				    System.out.println("Variables:" + vars);
				    for (LinkedHashMap var: vars){
				    	String key = (String) var.get("name");
				    	String value = (String) var.get("value");
				    	System.out.println(key);
				    	System.out.println(value);
				    	varString += "<variable name=\""+key+"\" value=\""+value+"\" />";
				    }
				    System.out.println("Variable String: " + varString);
				    String workflowName = ((String)result.get("name")).split(":::key=")[0];
				    System.out.println("WF to Be Started: " + workflowName);
				    String out = "<processInstance xmlns=\"" + "http://www.wiredwidgets.org/cow/server/schema/service" + "\">";
					out += "<processDefinitionKey>" + workflowName + "</processDefinitionKey>";
					out += "<name>" + workflowName + "</name>";
					out += "<priority>" + 3 + "</priority>";
					out += "<variables>" + varString + "</variables>";
					out += "</processInstance>";
					System.out.println("Post Payload: " + out);
					
					HttpEntity entity = new ByteArrayEntity(out.getBytes("UTF-8"));
					
					startWf.setEntity(entity);
					startWf.setHeader("Content-Type", "application/xml");
					String startWfResp = client.execute(startWf, handler);
				    System.out.println("Response to Start WF: " + startWfResp);
					
				  //Commented out because its annoying for dev work, but it works
				    
				    String workflow = ((String)result.get("processInstanceId")).split("\\.")[0];
				    String deleteWorkflowUrl = "http://scout3.mitre.org:8080/cow-server/processes/" + workflow;
				    HttpDelete deleteWorkflow = new HttpDelete(deleteWorkflowUrl);
				    System.out.println("rest call to " + deleteWorkflowUrl + " in 2 seconds");
				    Thread.sleep(2000);				    
				    String deleteWorkflowResp = client.execute(deleteWorkflow, handler);
				    System.out.println(deleteWorkflowResp);
				    
				    

				    HttpGet groups = new HttpGet("http://scout3.mitre.org:8080/cow-server/groups");
				    System.out.println("GET call to " + groups + " in 2 seconds");
				    Thread.sleep(2000);				    
				    String groupsResp = client.execute(groups, handler);
				    System.out.println("GET Response: " + groupsResp);
				    
				    
				}
				else {
					System.out.println("A DECISION TASK HAS STARTED");
				}
				
				
			} catch (JsonParseException e) {
				System.out.println("JSON PARSE EXCEPTION");
				e.printStackTrace();
			} catch (JsonMappingException e) {
				System.out.println("JSON MAPPING EXCEPTION");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IO EXCEPTION" + e);
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.out.println("INTERRUPTED EXCEPTION");
				e.printStackTrace();
			}
			
			
			
			
		}
		else {
			System.out.println("AMQP MESSAGE NULL?: " + (amqpMessage == null));
			System.out.println("AMQP MESSAGE USER: " + amqpMessage.getUser());
			System.out.println("AMQP MESSAGE denim_test?: " + amqpMessage.getWorkflowName().endsWith("denim_test"));
			System.out.println("AMQP MESSAGE BODY: " + amqpMessage.getMessageBody());
		}
		System.out.println("-----------------END---------------------");
		//Rest call from Java
		
		
		
	}

}
