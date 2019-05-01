package com.asiainfo.dubbo.notify;

/**   
 * @Description: 自定义通知接口
 * 
 * @author chenzq  
 * @date 2019年5月1日 下午6:18:55
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public interface Notify {
    
    // oninvoke必须具有与真实的被调用方法hello相同的入参列表
    public void oninvoke(String name);
    
    // 至少要有一个入参且第一个入参必须与hello的返回类型相同，接收返回结果。
    // 可以有多个参数，多个参数的情况下，第一个后边的所有参数都是用来接收hello入参的
    public void onreturn(String result, String name);

    // 至少要有一个入参且第一个入参类型为Throwable或其子类，接收返回结果。
    // 可以有多个参数，多个参数的情况下，第一个后边的所有参数都是用来接收hello入参的
    public void onthrow(Throwable ex, String name);
}
