package com.Ink.exception;

import com.Ink.result.Result;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
@ResponseBody
@ControllerAdvice
public class GlobalExceptionHandler {
    //全局异常处理，执行的方法
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail().message("执行全局异常处理。。。。");
    }

    //特定异常处理，执行的方法
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public Result error(ArithmeticException e){
        e.printStackTrace();
        return Result.fail().message("执行特定异常处理。。。。");
    }

    //自定义异常处理，执行的方法
    @ExceptionHandler(SpecialException.class)
    @ResponseBody
    public Result error(SpecialException e){
        e.printStackTrace();
        return Result.fail().code(e.getCode()).message(e.getErrorMsg());
    }

    //springSecurity拒绝访问异常
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Result error(AccessDeniedException e){
        e.printStackTrace();
        return Result.fail().code(205).message("拒绝访问，没有操作权限");
    }
}
