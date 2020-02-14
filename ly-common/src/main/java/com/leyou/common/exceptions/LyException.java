package com.leyou.common.exceptions;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Getter;

//自定义的异常，没有人进行捕获，需要自己写一个异常处理器来捕获该异常

@Getter
public class LyException extends RuntimeException {

    private int status;
    public LyException(int status,String message) {
        super(message);
        this.status = status;
    }


    public LyException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMessage());
        this.status = exceptionEnum.getStatus();
    }
}
