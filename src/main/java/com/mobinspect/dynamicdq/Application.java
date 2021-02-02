package com.mobinspect.dynamicdq;

import com.irontechspace.dynamicdq.service.ConfigService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Log4j2
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.mobinspect.dynamicdq", "com.irontechspace.dynamicdq", "com.common"})
public class Application {

//	@Autowired
//	ConfigService configService;
//
//	@Scheduled(fixedRate = 5000)
//	private void updateConfig(){
//		configService.getConfigs();
//		log.info("updateConfig");
//	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
