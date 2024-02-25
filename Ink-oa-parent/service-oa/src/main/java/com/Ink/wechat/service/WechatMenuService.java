package com.Ink.wechat.service;
import com.Ink.model.wechat.Menu;
import com.baomidou.mybatisplus.extension.service.IService;

public interface WechatMenuService extends IService<Menu> {
    Object findWechatMenuInfo();

    void syncWechatMenu();

    void removeMenu();
}
