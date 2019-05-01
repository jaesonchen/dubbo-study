package com.asiainfo.dubbo.config.service;

import java.util.concurrent.CompletableFuture;

import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;

/**   
 * @Description: provider service 实现
 * 
 * @author chenzq  
 * @date 2019年4月27日 下午11:04:04
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
//@Service(version = "1.0.0", methods = { @Method(name = "save", timeout = 3000, retries = 0) })
@Service(version = "1.0.0")
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        System.out.println("consumer.index = " + RpcContext.getContext().getAttachment("index"));
        return "hello " + name;
    }

    @Override
    public boolean save(User user) {
        System.out.println("save user: " + user);
        return true;
    }

    @Override
    public CompletableFuture<User> async(String userId) {
        final CompletableFuture<User> future = new CompletableFuture<>();
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // ignore
            }
            future.complete(new User(userId, 100));
        }).start();
        return future;
    }
}
