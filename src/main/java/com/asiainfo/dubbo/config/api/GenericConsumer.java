package com.asiainfo.dubbo.config.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;

import com.asiainfo.dubbo.config.service.User;

/**   
 * @Description: 使用generic api编程的consumer，POJO对象必须实现Serializable
 * 
 * @author chenzq  
 * @date 2019年4月28日 下午6:28:51
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public class GenericConsumer {

    public static void main(String[] args) throws IOException {
        
        // 遇到虚拟机使用ipv4/ipv6双地址时优先返回ipv4地址
        System.setProperty("java.net.preferIPv4Stack", "true");
        //System.setProperty("java.net.preferIPv6Addresses", "true");
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        ApplicationConfig appConfig = new ApplicationConfig("generic-consumer");
        // 这里莫名其妙的是true，启动时报qos绑定地址错误
        appConfig.setQosEnable(false);
        reference.setApplication(appConfig);
        reference.setRegistry(new RegistryConfig("zookeeper://192.168.0.102:2181"));
        reference.setGeneric(true);
        // 弱类型接口名
        reference.setInterface("com.asiainfo.dubbo.config.service.MyGenericService");
        GenericService genericService = reference.get();

        Object message = genericService.$invoke("hello", new String[] { "java.lang.String" }, new Object[] { "jaeson" });
        System.out.println(message);
        
        // 如果客户端使用POJO类型参数, 而服务端没有该类型，则自动转换为Map
        message = genericService.$invoke("save", new String[] { "com.asiainfo.dubbo.config.service.User" }, new Object[] { new User("chenzq", 30) });
        System.out.println(message);
        
        // 如果客户端没有POJO类型，用Map表示POJO参数; 服务端收到的仍然是Map，可能需要自己转换
        Map<String, Object> map = new HashMap<>();
        map.put("userId", "jaesonchen");
        map.put("age", 20);
        message = genericService.$invoke("save", new String[] { "com.asiainfo.dubbo.config.service.User" }, new Object[] { map });
        System.out.println(message);
        
        // 当服务端返回POJO时，如果客户端存在POJO返回类型，则直接使用；否则返回的POJO自动转换为Map
        message = genericService.$invoke("get", new String[] { "java.lang.String" }, new Object[] { "czq" });
        System.out.println(message);
        System.out.println(message.getClass().getName());
    }
}
