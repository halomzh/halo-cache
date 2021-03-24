package com.halo.cache.example;

import com.halo.cache.annotation.CacheEvict;
import com.halo.cache.annotation.CacheGet;
import com.halo.cache.annotation.CachePut;
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
	@CacheGet(nameSpace = "example", name = "'id:' + #id", condition = "#id != '5'")
	public String get(@RequestParam(name = "id") String id, @RequestParam(name = "name") String name) {
		log.info("进入get程序");
		return name + "#12312312312312312#" + id;
	}

	@GetMapping("/put")
	@CachePut(nameSpace = "example", name = "'id:' + #id", condition = "#id != '5'")
	public String put(@RequestParam(name = "id") String id, @RequestParam(name = "name") String name) {
		log.info("进入put程序");
		return name + "#12312312312312312#" + id;
	}

	@GetMapping("/evict")
	@CacheEvict(nameSpace = "example", names = "'id:' + #id")
	public void evict(@RequestParam(name = "id") String id) {
		log.info("删除缓存: id[{}]", id);
	}

	@GetMapping("/evictAll")
	@CacheEvict(nameSpace = "example", allEntries = true, condition = "#del")
	public void evictAll(@RequestParam(name = "del") boolean del) {
		log.info("删除所有缓存");
	}

}
