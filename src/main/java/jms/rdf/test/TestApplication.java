package jms.rdf.test;

import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class TestApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}

	@Value("${spring.activemq.broker-url:tcp://localhost:61616}")
	private String brokerUrl;

	@Value("${spring.activemq.user:artemis}")
	private String brokerUser;

	@Value("${spring.activemq.password:artemis}")
	private String brokerPassword;

	@Bean
	public CachingConnectionFactory cachingConnectionFactory() {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL(brokerUrl);
		connectionFactory.setPassword(brokerUser);
		connectionFactory.setUserName(brokerPassword);
		return new CachingConnectionFactory(connectionFactory);
	}

	@Bean
	public JmsTemplate jmsTemplate() {
		JmsTemplate template = new JmsTemplate(cachingConnectionFactory());
		template.setPubSubDomain(true);
		return template;
	}

	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(cachingConnectionFactory());
		factory.setPubSubDomain(true);
		factory.setConcurrency("1-10");
		return factory;
	}

	@Component
	public class Listener {
		@JmsListener(destination = "rdf-delta-patch", containerFactory = "jmsListenerContainerFactory")
		public void receiveMessage(final Message<String> message) throws JMSException {
			System.out.println(message.getPayload());
		}
	}

}
