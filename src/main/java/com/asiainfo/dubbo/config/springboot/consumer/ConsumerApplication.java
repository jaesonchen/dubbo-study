package com.asiainfo.dubbo.config.springboot.consumer;

import javax.annotation.PostConstruct;

import org.apache.dubbo.config.annotation.Reference;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asiainfo.dubbo.config.service.HelloService;

/**   
 * @Description: TODO
 * 
 * @author chenzq  
 * @date 2019年4月27日 下午11:23:29
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
@SpringBootApplication
@RestController
public class ConsumerApplication {
    
    @Reference(version = "1.0.0")
    private HelloService helloService;

    @RequestMapping("/hello/{name}")
    public String sayHello(@PathVariable("name") String name) {
        return this.helloService.hello(name);
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(new Class<?>[] { ConsumerApplication.class });
        app.setAdditionalProfiles(new String[] { "consumer" });
        app.run(args);
    }
	
    @PostConstruct
    public void init() {
    	System.err.println(helloService.hello("jaeson"));
    }
}
