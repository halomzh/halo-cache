package com.halo.cache.example;

import com.halo.cache.annotation.CacheGet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shoufeng
 */

@SpringBootApplication
@RestController
@RequestMapping("/example")
@Slf4j
public class ExampleApp {
	public static void main(String[] args) {
		SpringApplication.run(ExampleApp.class, args);
	}

	@GetMapping("/get")
	@CacheGet(name = "#id", nameSpace = "app")
	public String get(@RequestParam(name = "id") String id) {
		log.info("进入get程序");
		return "12312312312312312#" + id;
	}

}
