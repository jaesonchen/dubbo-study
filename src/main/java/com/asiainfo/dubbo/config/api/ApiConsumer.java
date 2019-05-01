package com.asiainfo.dubbo.config.api;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;

import com.asiainfo.dubbo.config.service.HelloService;
import com.asiainfo.dubbo.config.service.User;

/**   
 * @Description: 使用api编程的consumer
 * 
 * @author chenzq  
 * @date 2019年4月28日 下午6:20:31
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public class ApiConsumer {

    public static void main(String[] args) {
        
        // 遇到虚拟机使用ipv4/ipv6双地址时优先返回ipv4地址
        System.setProperty("java.net.preferIPv4Stack", "true");
        //System.setProperty("java.net.preferIPv6Addresses", "true");
        ReferenceConfig<HelloService> reference = new ReferenceConfig<>();
        ApplicationConfig appConfig = new ApplicationConfig("api-consumer");
        // 这里莫名其妙的是true，启动时报qos绑定地址错误
        appConfig.setQosEnable(false);
        reference.setApplication(appConfig);
        reference.setRegistry(new RegistryConfig("zookeeper://192.168.0.102:2181"));
        reference.setInterface(HelloService.class);
        HelloService helloService = reference.get();

        Object message = helloService.hello("chenzq");
        System.out.println(message);
        
        message = helloService.save(new User("chenzq", 30));
        System.out.println(message);
    }
}
