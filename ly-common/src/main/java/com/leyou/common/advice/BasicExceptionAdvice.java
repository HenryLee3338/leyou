package com.leyou.common.advice;

import com.leyou.common.exceptions.ExceptionResult;
import com.leyou.common.exceptions.LyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

//自己定义的异常处理器，用来捕获自定义异常，例如LyException

@ControllerAdvice   //经过controller的方法都会进入到这个类中
public class BasicExceptionAdvice {
    @ExceptionHandler
    public ResponseEntity<ExceptionResult> exceptionHandler(LyException e){//出了LyException异常会进入到该方法当中

        //参数e中包含了status属性和message属性
        return ResponseEntity.status(e.getStatus()).body(new ExceptionResult(e));
    }
}
