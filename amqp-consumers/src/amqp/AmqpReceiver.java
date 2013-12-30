package amqp;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;

public class AmqpReceiver {

	private Channel channel_;
	private String exchangeName_;

	public AmqpReceiver(String host, int port,String username, String password,  
						String exchangeName) throws IOException 
	{
		ConnectionFactory connFactory = new ConnectionFactory();
		connFactory.setHost(host);
		connFactory.setPort(port);
		connFactory.setUsername(username);
		connFactory.setPassword(password);
		
		channel_ = connFactory.newConnection().createChannel();	
		exchangeName_ = exchangeName;
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
