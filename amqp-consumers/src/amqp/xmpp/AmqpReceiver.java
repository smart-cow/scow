package amqp.xmpp;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;

public class AmqpReceiver {

	private Channel channel_;
	private String exchangeName_;

	public AmqpReceiver(String host, String exchangeName) throws IOException 
	{
		ConnectionFactory connFactory = new ConnectionFactory();
		connFactory.setHost(host);
		channel_ = connFactory.newConnection().createChannel();
		exchangeName_ = exchangeName;
	}
	
	
    public static String getAssignee(String routingKey, String type)
    {
        if (routingKey.matches("(^|.*\\.)" + type + "\\.*$")) {
            return "";
        }

        Pattern pattern = Pattern.compile("(^|\\.)" + type + "\\.(\\w+)");
        Matcher matcher = pattern.matcher(routingKey);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(2);
    }
    
    
    public Channel getChannel() 
    {
    	return channel_;
    }
    
    public void addConsumer(Consumer consumer, String routingKey, Channel channel) 
    		throws IOException 
    {
    	String queueName = channel.queueDeclare().getQueue();
    	channel.queueBind(queueName, exchangeName_, routingKey);
    	channel.basicConsume(queueName, true, consumer);
    }
    
    public void addConsumer(Consumer consumer, String routingKey) throws IOException {
    	addConsumer(consumer, routingKey, channel_);
    }
}
