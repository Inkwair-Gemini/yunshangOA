package com.Ink.filter;
import com.Ink.custom.CustomUser;
import com.Ink.jwt.JwtHelper;
import com.Ink.result.ResponseUtil;
import com.Ink.result.Result;
import com.Ink.result.ResultCodeEnum;
import com.Ink.vo.system.LoginVo;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//大组件1 ：登录过滤器
/* 登录过滤器 继承UsernamePasswordAuthenticationFilter，重写登录方法，登录成功时把信息存入token。
    用户登录 认证信息后把信息加密后返回，前端放入cookie，以后访问从cookie中取出放入请求头
*/
public class TokenLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final RedisTemplate redisTemplate;
    //构造方法
    public TokenLoginFilter(AuthenticationManager authenticationManager, RedisTemplate redisTemplate) {
        this.setAuthenticationManager(authenticationManager);
        this.setPostOnly(false);
        //指定登录接口及提交方式，可以指定任意路径
        this.setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher("/admin/system/index/login", "POST"));
        this.redisTemplate=redisTemplate;
    }

    //登录认证
    //获取输入的用户名和密码，调用方法认证
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException {
        try {
            //输入信息
            LoginVo loginVo = new ObjectMapper().readValue(req.getInputStream(), LoginVo.class);
            // 生成一个包含账号密码的认证信息
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginVo.getUsername(), loginVo.getPassword());
            //认证 框架完成这个过程 我们需要重写认证方法接口
            // AuthenticationManager校验这个认证信息，返回一个已认证的Authentication
            return this.getAuthenticationManager().authenticate(authenticationToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // 认证成功调用的方法
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {
        // 获取当前用户
        CustomUser customUser = (CustomUser) auth.getPrincipal();
        // 生成token
        String token = JwtHelper.createToken(customUser.getSysUser().getId(),
                customUser.getSysUser().getUsername());

        //获取权限数据，放入redis中 key：username,value:权限数据
        redisTemplate.opsForValue().set(customUser.getUsername(), JSON.toJSONString(customUser.getAuthorities()));
        // 返回
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        //不在controller里 返回数据要利用工具类ResponseUtil进行返回
        ResponseUtil.out(response, Result.ok(map));
    }

    // 认证失败调用的方法
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException e) throws IOException, ServletException {

        if(e.getCause() instanceof RuntimeException) {
            ResponseUtil.out(response, Result.build(null, ResultCodeEnum.DATA_ERROR));
        } else {
            ResponseUtil.out(response, Result.build(null, ResultCodeEnum.LOGIN_AUTH));
        }
    }
}
