package com.example.shippingservice;

import org.springframework.cloud.bindings.Bindings;
import org.springframework.cloud.bindings.boot.BindingsPropertiesProcessor;
import org.springframework.core.env.Environment;

import java.util.Map;
public class ZipkinBindingsPropertiesProcessor implements BindingsPropertiesProcessor {
  public static final String TYPE = "zipkin";

  @Override
  public void process(Environment environment, Bindings bindings, Map<String, Object> properties) {
      bindings.filterBindings(TYPE).forEach(binding -> {
          properties.putIfAbsent("management.zipkin.tracing.endpoint", binding.getSecret().get("uri") + "/api/v2/spans");
      });
  }
}