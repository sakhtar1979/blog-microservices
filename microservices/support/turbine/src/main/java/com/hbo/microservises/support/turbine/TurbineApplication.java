package com.hbo.microservises.support.turbine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.turbine.stream.EnableTurbineStream;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableTurbineStream
@EnableDiscoveryClient
public class TurbineApplication {

    private static final Logger LOG = LoggerFactory.getLogger(TurbineApplication.class);

    public static void main(String[] args) {

        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TurbineApplication.class).run(args);

        LOG.info("Connected to RabbitMQ at: {}", ctx.getEnvironment().getProperty("spring.rabbitmq.host"));
    }
}
