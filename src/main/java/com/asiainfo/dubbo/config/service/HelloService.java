package com.asiainfo.dubbo.config.service;

import java.util.concurrent.CompletableFuture;

/**   
 * @Description: provider 服务接口
 * 
 * @author chenzq  
 * @date 2019年4月27日 下午11:02:43
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public interface HelloService {
    
    /**
     * provider 服务接口方法
     * @author chenzq
     * @date 2019年4月27日 下午11:03:17
     * @param name
     * @return
     */
    String hello(String name);
    
    /**
     * provider 服务接口方法，使用POJO返回值
     * @author chenzq
     * @date 2019年4月28日 下午7:00:56
     * @param userId
     * @return
     */
    boolean save(User user);
    
    /**
     * provider 服务接口方法，异步调用
     * @author chenzq
     * @date 2019年4月28日 下午9:00:42
     * @param userId
     * @return
     */
    CompletableFuture<User> async(String userId);
}
