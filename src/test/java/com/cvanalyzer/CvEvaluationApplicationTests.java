package com.cvanalyzer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CvEvaluationApplicationTests {
	@Test
	@Disabled("Context testi DB bağlantısı gerektiriyor")
	void contextLoads() {
	}
}