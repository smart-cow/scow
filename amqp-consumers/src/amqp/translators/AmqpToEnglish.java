package amqp.translators;

import amqp.AmqpMessage;

public class AmqpToEnglish implements ITranslateAmqpMessage {

	StringBuilder sb_;
	
	@Override
	public String translate(AmqpMessage message) {
		sb_ = new StringBuilder("New notification\n");
		
		addItem("Workflow name", message.getWorkflowName());
		addItem("Workflow id", message.getWorkflowId());
		addItem("Category", message.getCategory());
		addItem("Action", message.getAction());
		return sb_.toString();
	}

	private void addItem(String key, String value) {
		sb_.append('\t');
		sb_.append(key);
		sb_.append(": ");
		sb_.append(value);
		sb_.append('\n');
	}

	
}
