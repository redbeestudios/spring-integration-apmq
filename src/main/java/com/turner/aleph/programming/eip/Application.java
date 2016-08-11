package com.turner.aleph.programming.eip;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.messaging.MessageChannel;


/**
 * Class Application: Application intended to importing records into
 * grupocarsa's contingency database
 * 
 * @author gustavo
 *
 */
@SpringBootApplication
@EnableIntegration
//@ComponentScan(basePackages="com.grupocarsa.contingency")
@IntegrationComponentScan
@Import({  })
public class Application {

	@Autowired ConnectionFactory rabbitConnectionFactory;
	
	@Autowired 
	@Qualifier("amqpTemplate")
	AmqpTemplate amqpTemplate;
	
    public static void main(String[] args) {
         ConfigurableApplicationContext context =
                  new SpringApplicationBuilder(Application.class)
                          .web(false)
                          .run(args);
         MyGateway gateway = context.getBean(MyGateway.class);
         gateway.sendToRabbit("foo");
    }
    
    @Bean(name="headers")
    Map<String,Object> headers(){
    	Map<String, Object> hdrs =  new HashMap<String,Object>();
    	hdrs.put("correlationIDString", "correlationId");
    	hdrs.put("Content-type","");
    	return hdrs;
    }

    @Bean
    public IntegrationFlow amqpOutbound(AmqpTemplate amqpTemplate) {
        return IntegrationFlows.from(amqpOutboundChannel())
        		.enrichHeaders(headers())
                .handle(Amqp.outboundAdapter(amqpTemplate)
                			.defaultDeliveryMode(MessageDeliveryMode.PERSISTENT)
                			.exchangeName("amq.topic")
                            .routingKey("foo")) // topic exchange - route to queue 'foo'
                .get();
    }

    @Bean
    public MessageChannel amqpOutboundChannel() {
    	return new DirectChannel();
    }
    
    @Bean(name="amqpTemplate")
    public AmqpTemplate amqpTemplate(){
    	
    	RabbitTemplate template = new RabbitTemplate(this.rabbitConnectionFactory);
    	template.setQueue("foo.queue");
    	
    	return template;
    }

    @Bean
    public ConnectionFactory rabbitConnectionFactoty(){
    	
    	CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
    	connectionFactory.setHost("172.17.0.3");
    	connectionFactory.setPort(5672);
    	connectionFactory.setUsername("guest");
    	connectionFactory.setPassword("guest");
    	connectionFactory.setPublisherConfirms(false);
    	connectionFactory.setPublisherReturns(false);
  
    	return connectionFactory;
    	
    }
    
    @MessagingGateway(defaultRequestChannel = "amqpOutboundChannel")
    
    public interface MyGateway {

    	
        void sendToRabbit(String data);

    }
}