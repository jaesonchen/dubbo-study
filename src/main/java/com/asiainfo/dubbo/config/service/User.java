package com.asiainfo.dubbo.config.service;

import java.io.Serializable;

/**   
 * @Description: TODO
 * 
 * @author chenzq  
 * @date 2019年4月28日 下午6:58:57
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private int age;
    
    public User() {}
    public User(String userId, int age) {
        this.userId = userId;
        this.age = age;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    @Override
    public String toString() {
        return "User [userId=" + userId + ", age=" + age + "]";
    }
}
