package amqp;

import java.io.IOException;
import java.net.ConnectException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;

public class AmqpReceiver {

	private Channel channel_;
	private String exchangeName_;

	public AmqpReceiver(String host, int port,String username, String password,  
						String exchangeName) throws IOException, InterruptedException 
	{
		ConnectionFactory connFactory = new ConnectionFactory();
		connFactory.setHost(host);
		connFactory.setPort(port);
		connFactory.setUsername(username);
		connFactory.setPassword(password);
		
		Connection connection = connectWithRetry(connFactory);
		channel_ = connection.createChannel();	
		exchangeName_ = exchangeName;
	}
	
	private Connection connectWithRetry(ConnectionFactory connFactory) 
			throws IOException, InterruptedException {
		int reconnectDelay = 500;
		
		while (true) {
			try {
				return connFactory.newConnection();
			}
			catch (ConnectException e) {
				//300000ms = 5 minutes 
				if (reconnectDelay < 300000) { 
					reconnectDelay *= 2;
				}
				System.err.printf(
						"Connection to AMQP server failed.\nWill retry connection in %d seconds\n", 
						reconnectDelay / 1000 );
				Thread.sleep(reconnectDelay);
			}
		}
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
