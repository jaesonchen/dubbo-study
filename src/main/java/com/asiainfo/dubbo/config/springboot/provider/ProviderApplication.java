package com.asiainfo.dubbo.config.springboot.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**   
 * @Description: TODO
 * 
 * @author chenzq  
 * @date 2019年4月27日 下午11:23:16
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
@SpringBootApplication
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(new Class<?>[] { ProviderApplication.class });
        app.setAdditionalProfiles(new String[] { "provider" });
        app.run(args);
    }
}
