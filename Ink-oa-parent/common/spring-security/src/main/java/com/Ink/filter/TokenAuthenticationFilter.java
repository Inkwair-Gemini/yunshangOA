package com.Ink.filter;

import com.Ink.custom.LoginUserInfoHelper;
import com.Ink.jwt.JwtHelper;
import com.Ink.result.ResponseUtil;
import com.Ink.result.Result;
import com.Ink.result.ResultCodeEnum;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

//大组件2 ：认证过滤器
/*  认证过滤器继承 OncePerRequestFilter，重写方法doFilterInternal，如果是登录直接放行，否则从请求头中获取数据，将其封装存入上下文
    用户没有登录 请求头中没有数据 认证失败*/
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final RedisTemplate redisTemplate;
    public TokenAuthenticationFilter(RedisTemplate redisTemplate) {
        this.redisTemplate=redisTemplate;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        logger.info("uri:"+request.getRequestURI());
        //如果是登录接口，直接放行
        if("/admin/system/index/login".equals(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }
        // 将返回的Authentication存到上下文中
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if(null != authentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } else {
            ResponseUtil.out(response, Result.build(null, ResultCodeEnum.PERMISSION));
        }
    }
    //从请求中获取token
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        // token位于header里
        String token = request.getHeader("token");
        logger.info("token:"+token);
        if (!StringUtils.isEmpty(token)) {
            String username = JwtHelper.getUsername(token);
            logger.info("username:"+username);
            if (!StringUtils.isEmpty(username)) {
                //把当前用户信息放入ThreadLocal
                LoginUserInfoHelper.setUserId(JwtHelper.getUserId(token));
                LoginUserInfoHelper.setUsername(username);
                //通过username从redis中获取权限数据
                String authString= (String) redisTemplate.opsForValue().get(username);
                //把权限数据字符串转为集合类型 List<SimpleGrantedAuthority>
                if(!StringUtils.isEmpty(authString)){
                    List<Map> maps= JSON.parseArray(authString, Map.class);
                    List<SimpleGrantedAuthority> authList=new ArrayList<>();

                    for(Map map:maps){
                        authList.add(new SimpleGrantedAuthority((String) map.get("authority")));
                    }
                    return new UsernamePasswordAuthenticationToken(username,null,authList);
                }else{
                    return new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                }
            }
        }
        return null;
    }
}