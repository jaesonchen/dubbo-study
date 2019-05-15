package com.asiainfo.dubbo.config.api;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.RpcContext;

import com.asiainfo.dubbo.config.service.HelloService;
import com.asiainfo.dubbo.config.service.User;

/**   
 * @Description: 使用api编程的异步consumer
 * 
 * @author chenzq  
 * @date 2019年4月28日 下午9:02:43
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public class AsyncConsumer {

    public static void main(String[] args) throws IOException {
        
        // 遇到虚拟机使用ipv4/ipv6双地址时优先返回ipv4地址
        System.setProperty("java.net.preferIPv4Stack", "true");
        //System.setProperty("java.net.preferIPv6Addresses", "true");
        ReferenceConfig<HelloService> reference = new ReferenceConfig<>();
        ApplicationConfig appConfig = new ApplicationConfig("async-consumer");
        // 同一台电脑上同时跑provider/consumer，不配置启动时报qos绑定地址错误
        appConfig.setQosEnable(false);
        reference.setApplication(appConfig);
        reference.setRegistry(new RegistryConfig("zookeeper://192.168.0.102:2181"));
        reference.setInterface(HelloService.class);
        // call async
        reference.setAsync(true);
        reference.setTimeout(6000);
        HelloService helloService = reference.get();

        Object message = helloService.hello("chenzq");
        RpcContext.getContext()
            .getCompletableFuture()
            .whenComplete((value, ex) -> {
                if (ex != null) {
                    ex.printStackTrace();
                } else {
                    System.out.println("Response: " + value);
                }
            });
        System.out.println(message);
        
        CompletableFuture<User> future = helloService.async("czq");
        future.whenComplete((value, ex) -> {
                if (ex != null) {
                    ex.printStackTrace();
                } else {
                    System.out.println("Response: " + value);
                }
            });
        System.out.println("end");
        System.in.read();
    }
}
