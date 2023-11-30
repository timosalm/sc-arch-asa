package com.example.orderservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.AbstractJmsListenerContainerFactory;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.web.client.RestTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.cache.annotation.EnableCaching;

//@EnableCaching
@SpringBootApplication
public class OrderServiceApplication {

    @LoadBalanced
    @Bean
    RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    MappingJackson2MessageConverter jacksonJmsMessageConverter() {
        final MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        // https://github.com/Azure/azure-sdk-for-java/issues/36497
        converter.setTargetType(MessageType.BYTES);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
/*
    @Bean
    public MappingJackson2MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        var jacksonMessageConverter = new MappingJackson2MessageConverter();
        jacksonMessageConverter.setObjectMapper(objectMapper);
        jacksonMessageConverter.setSerializedPayloadClass(String.class);
        jacksonMessageConverter.setStrictContentTypeMatch(true);
        return jacksonMessageConverter;
    }*/

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory, DefaultJmsListenerContainerFactoryConfigurer configurer) {
        if (connectionFactory instanceof CachingConnectionFactory cachingConnectionFactory) {
            cachingConnectionFactory.setCacheProducers(false);
        }
        var listenerFactory = new DefaultJmsListenerContainerFactory();
        configurer.configure(listenerFactory, connectionFactory);
        listenerFactory.setTransactionManager(null);
        listenerFactory.setSessionTransacted(false);
        return listenerFactory;
    }

    @Bean
    BeanPostProcessor applyJacksonJmsMessageConverterMessageConverter(MappingJackson2MessageConverter jacksonJmsMessageConverter) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof JmsTemplate) {
                    ((JmsTemplate) bean).setMessageConverter(jacksonJmsMessageConverter);
                } else if (bean instanceof AbstractJmsListenerContainerFactory) {
                    ((AbstractJmsListenerContainerFactory) bean).setMessageConverter(jacksonJmsMessageConverter);
                } else if (bean instanceof CachingConnectionFactory) {
                    // Fixes link is closed error, see https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-amqp-troubleshoot
                    ((CachingConnectionFactory) bean).setCacheProducers(false);
                }
                return bean;
            }
        };
    }

    @Bean
    public HttpExchangeRepository htttpTraceRepository() {
      return new InMemoryHttpExchangeRepository();
    }

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
