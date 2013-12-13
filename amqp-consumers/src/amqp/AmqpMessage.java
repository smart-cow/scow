package amqp;

/**
 * Class that parses AMQP routing key and contains getters for AMQP parts.
 * 
 * @author brosenberg
 *
 */
public class AmqpMessage {
	
	private String workflowName_;
	private String workflowId_;
	private String category_;
	private String action_;
	
	private boolean isToAllGroups_;
	private String groupName_;
	
	private boolean isToAllUsers_;
	private String username_;
	
	private String messageBody_;
	
	
	public AmqpMessage(String routingKey, String messageBody) {
		messageBody_ = messageBody;
		
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
		
		workflowName_ = rkParts[0];
		workflowId_   = rkParts[1];
		category_     = rkParts[2];
		action_       = rkParts[3];
		
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
			groupName_ = rkParts[5];
			return;
		}
		if (isToUser) {
			username_ = rkParts[5];
		}
	}
	
	
	public String getWorkflowName() {
		return workflowName_;
	}
	
	public String getWorkflowId() {
		return workflowId_;
	}
	
	public String getCategory() {
		return category_;
	}
	
	public String getAction() {
		return action_;
	}
	
	public boolean hasAssignees() {
		return isToAllGroups_ || isToAllUsers_ || groupName_ != null || username_ != null;
	}
	
	public boolean isToSingleGroup() {
		return groupName_ != null;
	}
	
	public boolean isToAllGroups() {
		return isToAllGroups_;
	}
	
	public String getGroup() {
		return groupName_;
	}
	
	public boolean isToAllUsers() {
		return isToAllUsers_;
	}
	
	public boolean isToSingleUser() {
		return username_ != null;
	}
	
	public String getUser() {
		return username_;
	}
	
	
	public String getMessageBody() {
		return messageBody_;
	}
	

	


}
