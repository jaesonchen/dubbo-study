package com.asiainfo.dubbo.merge;

import java.util.ArrayList;
import java.util.List;

/**   
 * @Description: TODO
 * 
 * @author chenzq  
 * @date 2019年5月1日 下午6:43:37
 * @version V1.0
 * @Copyright: Copyright(c) 2019 jaesonchen.com Inc. All rights reserved. 
 */
public class MenuServiceImpl implements MenuService {

    @Override
    public List<String> getMenu() {
        List<String> menus = new ArrayList<String>();
        menus.add("menu-1");
        menus.add("menu-2");
        return menus;
    }
}
