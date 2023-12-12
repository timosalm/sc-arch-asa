package com.example.orderservice.order;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class ShippingService {

    private static final Logger log = LoggerFactory.getLogger(ShippingService.class);

    @Value("${order.shipping-queue-name}")
    private String orderShippingQueueName;
    private final JmsTemplate jmsTemplate;
    private Consumer<OrderStatusUpdate> orderStatusUpdateConsumer;

    ShippingService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    void shipOrder(Order order) {
        if (StringUtils.isEmpty(orderShippingQueueName)) {
            throw new RuntimeException("order.shipping-queue-name not set");
        }
        jmsTemplate.convertAndSend(orderShippingQueueName, order, postProcessor -> {
            // The shipping-service based on Spring Cloud Stream will forward it automatically from the input to the output of the java.util.function.Function
            // postProcessor.setJMSType didn't work
            postProcessor.setStringProperty("_type", OrderStatusUpdate.class.getCanonicalName());
            return postProcessor;
        });
    }

    @JmsListener(destination = "${order.delivered-queue-name}", containerFactory = "jmsListenerContainerFactory")
    private void updateStatus(OrderStatusUpdate statusUpdate) {
        log.info("updateStatus called for order id: " + statusUpdate.getId() + " with status "
                + statusUpdate.getStatus());
        if (orderStatusUpdateConsumer != null) {
            orderStatusUpdateConsumer.accept(statusUpdate);
        }
    }

    void setOrderStatusUpdateConsumer(Consumer<OrderStatusUpdate> orderStatusUpdateConsumer) {
        this.orderStatusUpdateConsumer = orderStatusUpdateConsumer;
    }
}