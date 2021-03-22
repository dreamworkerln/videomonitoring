package ru.kvanttelecom.tv.videomonitoring.tbot.configurations.amqp;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.kvanttelecom.tv.videomonitoring.tbot.services.amqp.CameraEventReceiver;

@Configuration
public class AMQPConfigurationBot {


//    @Bean
//    public MessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
//        DirectMessageListenerContainer container = new DirectMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.setQueueNames("camera.update");
//        container.setMessageListener(listenerAdapter);
//        return container;
//    }
//
//    @Bean
//    MessageListenerAdapter listenerAdapter(CameraEventReceiver receiver, MessageConverter jsonMessageConverter) {
//        MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, jsonMessageConverter);
//        adapter.setDefaultListenerMethod("receive");
//        return adapter;
//    }
}
