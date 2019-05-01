package com.asiainfo.dubbo.config.api;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.rpc.service.GenericService;

import com.asiainfo.dubbo.config.service.GenericServiceImpl;

/**   
 * @Description: 使用generic api编程的provider，POJO对象必须实现Serializable
 * 
 * @author chenzq  
 * @date 2019年4月28日 下午6:38:39
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public class GenericProvider {
    
    public static void main(String[] args) throws Exception {
        
        // 遇到虚拟机使用ipv4/ipv6双地址时优先返回ipv4地址
        System.setProperty("java.net.preferIPv4Stack", "true");
        //System.setProperty("java.net.preferIPv6Addresses", "true");
        ServiceConfig<GenericService> service = new ServiceConfig<>();
        service.setApplication(new ApplicationConfig("generic-provider"));
        service.setRegistry(new RegistryConfig("zookeeper://192.168.0.102:2181"));
        // 弱类型接口名，provider 端不需要真的存在MyGenericService接口类型
        service.setInterface("com.asiainfo.dubbo.config.service.MyGenericService");
        service.setRef(new GenericServiceImpl());
        // 暴露及注册服务
        service.export();
        System.out.println("generic-provider is running.");
        System.in.read();
    }
}
