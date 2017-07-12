package com.cyan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyan.container.Container;
import com.cyan.container.service.FirstService;

@SpringBootApplication
@RestController
public class Application extends SpringBootServletInitializer {
	
	private static Logger logger = LogManager.getLogger();

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		logger.debug("=====================First Intial");
		return application.sources(Application.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}
	
	@RequestMapping(value="/f")
	public String first() {
		logger.debug("=====================Somebody hit me hard");
		Container.init();
		FirstService service = (FirstService) Container.getComponent("firstService");

		return service.firstService();
	}
}
