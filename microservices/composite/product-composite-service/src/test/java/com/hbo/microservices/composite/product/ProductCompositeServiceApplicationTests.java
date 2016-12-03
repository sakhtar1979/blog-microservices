package com.hbo.microservices.composite.product;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
// Instruct embedded Tomcat to run on a random free port and skip talking to the Config, Bus and Discovery server
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ProductCompositeServiceApplication.class, properties = {"server.port=0", "spring.cloud.config.enabled=false", "spring.cloud.bus.enabled=false", "spring.cloud.discovery.enabled=false"})
public class ProductCompositeServiceApplicationTests {

	@Test
	public void contextLoads() {
	}

}
