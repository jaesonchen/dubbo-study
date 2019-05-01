package com.asiainfo.dubbo.filter;

import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

/**   
 * @Description: dubbo plugin, 使用spi，需要配置对应的 META-INF/dubbo/org.apache.dubbo.rpc.Filter
 * 
 * @author chenzq  
 * @date 2019年5月1日 下午5:01:13
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public class LegacyBlockFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result result = invoker.invoke(invocation);
        System.out.println("LegacyBlockFilter: This is the default return value: " + result.getValue());
        if (result.hasException()) {
            System.out.println("LegacyBlockFilter: This will only happen when the real exception returns: " + result.getException());
        }
        System.out.println("LegacyBlockFilter: This msg should not be blocked.");
        return result;
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        Result res = Filter.super.onResponse(result, invoker, invocation);
        System.out.println("LegacyBlockFilter: Filter get the return value: " + res.getValue());
        return result;
    }
}
