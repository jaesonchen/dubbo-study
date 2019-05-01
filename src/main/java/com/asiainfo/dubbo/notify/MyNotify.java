package com.asiainfo.dubbo.notify;

/**   
 * @Description: notify实现
 * 
 * @author chenzq  
 * @date 2019年5月1日 下午6:19:50
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public class MyNotify implements Notify {

    @Override
    public void oninvoke(String name) {
        System.out.println("oninvoke: param = " + name);
    }

    @Override
    public void onreturn(String result, String name) {
        System.out.println("onreturn: param = " + name + ", result = " + result);
    }

    @Override
    public void onthrow(Throwable ex, String name) {
        System.out.println("onthrow: param = " + name);
        ex.printStackTrace();
    }
}
