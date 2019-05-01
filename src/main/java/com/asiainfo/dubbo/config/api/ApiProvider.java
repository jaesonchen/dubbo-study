package com.asiainfo.dubbo.config.api;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;

import com.asiainfo.dubbo.config.service.HelloService;
import com.asiainfo.dubbo.config.service.HelloServiceImpl;

/**   
 * @Description: 使用api编程的provider
 * 
 * @author chenzq  
 * @date 2019年4月28日 下午6:08:19
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public class ApiProvider {

    public static void main(String[] args) throws Exception {
        
        // 遇到虚拟机使用ipv4/ipv6双地址时优先返回ipv4地址
        System.setProperty("java.net.preferIPv4Stack", "true");
        //System.setProperty("java.net.preferIPv6Addresses", "true");
        // 该实例很重量，里面封装了所有与注册中心及服务提供方连接，请缓存
        ServiceConfig<HelloService> service = new ServiceConfig<>();
        service.setApplication(new ApplicationConfig("api-provider"));
        service.setRegistry(new RegistryConfig("zookeeper://192.168.0.102:2181"));
        service.setInterface(HelloService.class);
        service.setRef(new HelloServiceImpl());
        service.export();
        System.out.println("api-provider is running.");
        System.in.read();
    }
}
