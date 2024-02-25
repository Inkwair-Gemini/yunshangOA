package com.Ink.wechat.controller;

import com.Ink.auth.service.SysUserService;
import com.Ink.jwt.JwtHelper;
import com.Ink.model.system.SysUser;
import com.Ink.result.Result;
import com.Ink.vo.wechat.BindPhoneVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/wechat")
@CrossOrigin//跨域
public class WechatController {
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private SysUserService sysUserService;

    @Value("${wechat.userInfoUrl}")//yml中获取值
    private String userInfoUrl;

    //用户先授权 设置哪个路径处理获取的授权信息
    @GetMapping("/authorize")
    public String authorize(@RequestParam("returnUrl") String returnUrl){
        //buildAuthorizationUrl
        //第一个参数：在哪个路径获取授权信息
        //第二个参数：授权类型
        //第三个参数：跳转路径
        String redirectUrl = null;
        redirectUrl = wxMpService.getOAuth2Service().buildAuthorizationUrl(
                userInfoUrl,//认证路径
                WxConsts.OAuth2Scope.SNSAPI_USERINFO,
                URLEncoder.encode(returnUrl.replace("oa", "#"), StandardCharsets.UTF_8));
        return "redirect:"+redirectUrl;
    }

    //查询数据库 如果有这个人就返回 不存在就去绑定
    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("code") String code,
                           @RequestParam("state") String returnUrl) throws Exception {
        //获取accessToken
        WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
        //获取openId
        String openId = accessToken.getOpenId();
        //获取微信用户信息
        WxOAuth2UserInfo wxMpUser = wxMpService.getOAuth2Service().getUserInfo(accessToken, null);
        //根据openId查询用户表
        SysUser sysUser = sysUserService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getOpenId, openId));
        String token = "";
        //null != sysUser 说明已经绑定，反之为建立账号绑定，去页面建立账号绑定
        if(null != sysUser) {
            token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
        }
        if(!returnUrl.contains("?")) {
            //不含?
            return "redirect:" + returnUrl + "?token=" + token + "&openId=" + openId;
        } else {
            //自带?
            return "redirect:" + returnUrl + "&token=" + token + "&openId=" + openId;
        }
    }

    //绑定 如果数据库有手机号 绑定; 否则公司没这个人
    @ApiOperation(value = "微信账号绑定手机")
    @PostMapping("bindPhone")
    @ResponseBody
    public Result bindPhone(@RequestBody BindPhoneVo bindPhoneVo) {
        SysUser sysUser = sysUserService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, bindPhoneVo.getPhone()));
        if(null != sysUser) {
            sysUser.setOpenId(bindPhoneVo.getOpenId());
            sysUserService.updateById(sysUser);
            String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
            return Result.ok(token);
        } else {
            return Result.fail("手机号码不存在，绑定失败");
        }
    }
}
