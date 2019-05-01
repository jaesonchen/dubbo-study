package com.asiainfo.dubbo.config.xml;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.asiainfo.dubbo.config.service.HelloService;
import com.asiainfo.dubbo.config.service.User;
import com.asiainfo.dubbo.merge.MenuService;

/**   
 * @Description: 使用xml配置文件的consumer
 * 
 * @author chenzq  
 * @date 2019年4月28日 下午8:53:23
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public class XmlConsumer {

    public static void main(String[] args) throws Exception {
        
        try (ClassPathXmlApplicationContext context = 
                new ClassPathXmlApplicationContext(new String[] { "classpath:consumer.xml" })) {
            context.start();
            HelloService service = context.getBean(HelloService.class);
            System.out.println(service.hello("jaeson"));
            // future
            CompletableFuture<User> future = service.async("chenzq");
            future.whenComplete((value, ex) -> {
                if (ex != null) {
                    ex.printStackTrace();
                } else {
                    System.out.println("Response: " + value);
                }
            });
            System.out.println(future);
            
            // merge
            MenuService menuService = context.getBean(MenuService.class);
            List<String> menus = menuService.getMenu();
            for (String menu : menus) {
                System.out.println("menu:" + menu);
            }
            System.in.read();
        }
    }
}
