package com.Ink.wechat.service.impl;

import com.Ink.model.wechat.Menu;
import com.Ink.vo.wechat.MenuVo;
import com.Ink.wechat.mapper.WechatMenuMapper;
import com.Ink.wechat.service.WechatMenuService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WechatMenuServiceImpl extends ServiceImpl<WechatMenuMapper, Menu> implements WechatMenuService {
    @Autowired
    private WxMpService wxMpService;

    @Override
    public List<MenuVo> findWechatMenuInfo() {
        List<MenuVo> list=new ArrayList<>();
        //1 查询list集合
        List<Menu> menus = baseMapper.selectList(null);
        //2 查询一级菜单
        List<Menu> oneMenuList = menus.stream().filter(menu -> menu.getParentId().longValue() == 0)
                .collect(Collectors.toList());
        //3 遍历取二级菜单
        for (Menu oneMenu : oneMenuList) {
            //一级菜单menu->menuVo
            MenuVo oneMenuVo=new MenuVo();
            BeanUtils.copyProperties(oneMenu,oneMenuVo);
            List<Menu> twoMenuList = menus.stream().filter(menu -> menu.getParentId().longValue() == oneMenu.getId())
                    .collect(Collectors.toList());
            //4 把二级菜单封装到一级菜单children
            List<MenuVo> children =new ArrayList<>();
            for (Menu twoMenu : twoMenuList) {
                MenuVo twoMenuVo=new MenuVo();
                BeanUtils.copyProperties(twoMenu,twoMenuVo);
                children.add(twoMenuVo);
            }
            oneMenuVo.setChildren(children);
            list.add(oneMenuVo);
        }
        return list;
    }

    //同步菜单，微信上显示菜单
    @Override
    public void syncWechatMenu() {
        //1 封装格式 菜单集
        List<MenuVo> menuVoList = this.findWechatMenuInfo();
        //菜单按钮集
        JSONArray buttonList = new JSONArray();
        for(MenuVo oneMenuVo : menuVoList) {
            JSONObject one = new JSONObject();
            one.put("name", oneMenuVo.getName());
            if(CollectionUtils.isEmpty(oneMenuVo.getChildren())) {
                one.put("type", oneMenuVo.getType());
                one.put("url", "http://weichat.free.svipss.top/#"+oneMenuVo.getUrl());
            } else {
                JSONArray subButton = new JSONArray();
                for(MenuVo twoMenuVo : oneMenuVo.getChildren()) {
                    JSONObject view = new JSONObject();
                    view.put("type", twoMenuVo.getType());
                    if(twoMenuVo.getType().equals("view")) {
                        view.put("name", twoMenuVo.getName());
                        //H5页面地址
                        view.put("url", "http://weichat.free.svipss.top/#" +twoMenuVo.getUrl());
                    } else {
                        view.put("name", twoMenuVo.getName());
                        view.put("key", twoMenuVo.getMenuKey());
                    }
                    subButton.add(view);
                }
                one.put("sub_button", subButton);
            }
            buttonList.add(one);
        }
        //推送菜单
        JSONObject button = new JSONObject();
        button.put("button", buttonList);
        //2 实现推送
        try {
            wxMpService.getMenuService().menuCreate(button.toString());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    //微信上移除菜单
    @Override
    public void removeMenu() {
        try {
            wxMpService.getMenuService().menuDelete();
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }
}
