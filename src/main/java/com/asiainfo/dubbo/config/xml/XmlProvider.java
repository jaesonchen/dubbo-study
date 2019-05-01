package com.asiainfo.dubbo.config.xml;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**   
 * @Description: 使用xml配置文件的provider
 * 
 * @author chenzq  
 * @date 2019年4月28日 下午8:53:14
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public class XmlProvider {

    public static void main(String[] args) throws IOException {
        
        try (ClassPathXmlApplicationContext context = 
                new ClassPathXmlApplicationContext(new String[] { "classpath:provider.xml" })) {
            context.start();
            System.in.read();
        }
    }
}
