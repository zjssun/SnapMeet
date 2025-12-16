package com.snapmeet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.example.snapmeet.mapper")
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class SnapmeetApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnapmeetApplication.class, args);
	}

}
