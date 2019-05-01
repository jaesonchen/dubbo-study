package com.asiainfo.dubbo.config.service;

import org.apache.dubbo.rpc.service.GenericException;
import org.apache.dubbo.rpc.service.GenericService;

/**   
 * @Description: generic provider 服务实现
 * 
 * @author chenzq  
 * @date 2019年4月28日 下午6:41:18
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public class GenericServiceImpl implements GenericService {

    @Override
    public Object $invoke(String method, String[] parameterTypes, Object[] args) throws GenericException {
        if ("hello".equals(method)) {
            return "Welcome " + args[0];
        } else if ("save".equals(method)) {
            System.out.println("save user: param.type=" + parameterTypes[0] + ", args.type=" + args[0].getClass() + ", args=" + args[0]);
            return true;
        } else if ("get".equals(method)) {
            return new User(String.valueOf(args[0]), 20);
        }
        throw new GenericException(this.getClass().getName(), "no such method");
    }
}
