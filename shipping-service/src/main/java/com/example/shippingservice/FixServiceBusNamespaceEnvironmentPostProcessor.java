package com.example.shippingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

@Order
public class FixServiceBusNamespaceEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String NAMESPACE_PROPERTY = "spring.cloud.azure.servicebus.namespace";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        var namespace = environment.getProperty(NAMESPACE_PROPERTY);
        if (namespace != null) {
            System.setProperty(NAMESPACE_PROPERTY,
                    namespace.replace("." + environment.getProperty("shipping.servicebus-fqn"), ""));
        }
    }
}
