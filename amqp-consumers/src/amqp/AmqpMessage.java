package amqp;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that parses AMQP routing key and contains getters for AMQP parts.
 * 
 * @author brosenberg
 *
 */
public class AmqpMessage {
	
	private boolean isToAllGroups_;	
	private boolean isToAllUsers_;
		
	private Map<String, String> variables_ = new HashMap<String, String>();
	
	
	public AmqpMessage(String routingKey, String messageBody) {
		variables_.put("messageBody", messageBody);
		
		/*
		 * Possible Routing key formats
		 * 	  v2-test.15.tasks.takeTask - length = 4 
		 *    v2-test.15.tasks.takeTask.user - length = 5
		 *    v2-test.15.tasks.takeTask.user.brian length = 6
		 */
		String[] rkParts = routingKey.split("\\.");
		if (rkParts.length < 4) {
			throw new IllegalArgumentException("Routing key not in proper format");
		}
		variables_.put("workflowName", rkParts[0]);
		variables_.put("workflowId", rkParts[1]);
		variables_.put("category", rkParts[2]);
		variables_.put("action", rkParts[3]);

		
		/*
		 * Done because to assignee info on key
		 * 		v2-test.15.tasks.takeTask - length = 4 
		 */
		if (rkParts.length < 5) {
			return;
		}
		
		
		/*
		 *  Key has either user or group info
		 *      v2-test.15.tasks.takeTask.user - length = 5
		 *      v2-test.15.tasks.takeTask.user.brian length = 6
		 */
		boolean isToGroup = rkParts[4].toLowerCase().equals("group");
		boolean isToUser  = rkParts[4].toLowerCase().equals("user");
		
		/*
		 * Key has no specific user or group
		 *      v2-test.15.tasks.takeTask.user - length = 5
		 */
		if (rkParts.length == 5) {
			isToAllGroups_ = isToGroup;
			isToAllUsers_ = isToUser;
			return;
		}
		
		/*
		 * Full length key, has specific assignee information.
		 *      v2-test.15.tasks.takeTask.user.brian length = 6
		 */
		if (isToGroup) {
			variables_.put("groupName", rkParts[5]);
			return;
		}
		if (isToUser) {
			variables_.put("username", rkParts[5]);
		}
	}
	
	
	public String getWorkflowName() {
		return variables_.get("workflowName");
	}
	
	public String getWorkflowId() {
		return variables_.get("workflowId");
	}
	
	public String getCategory() {
		return variables_.get("category");
	}
	
	public String getAction() {
		return variables_.get("action");
	}
	
	public boolean hasAssignees() {
		return isToAllGroups_ || isToAllUsers_ || variables_.containsKey("groupName") || 
				variables_.containsKey("username");
	}
	
	
	public boolean isToSingleGroup() {
		return variables_.containsKey("groupName");
	}
	
	
	public boolean isToAllGroups() {
		return isToAllGroups_;
	}
	
	public String getGroup() {
		return variables_.get("groupName");
	}
	
	public boolean isToAllUsers() {
		return isToAllUsers_;
	}
	
	public boolean isToSingleUser() {
		return variables_.containsKey("username");
	}
	
	public String getUser() {
		return variables_.get("username");
	}
	
	
	public String getMessageBody() {
		return variables_.get("messageBody");
	}
	
	public Map<String, String> getVariables() {
		return variables_;
	}
	

	


}
