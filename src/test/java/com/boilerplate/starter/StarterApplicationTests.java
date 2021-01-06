package com.boilerplate.starter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StarterApplicationTests {

	@Value("${app.url}")
	private  String BASE_URL;

	@Value("${server.port}")
	private  String testValue;

	@Test
	void contextLoads() {
	}

}
