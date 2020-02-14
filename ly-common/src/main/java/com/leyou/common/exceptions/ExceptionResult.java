package com.leyou.common.exceptions;

import lombok.Getter;
import org.joda.time.DateTime;

//出现异常时返回值的实体类

@Getter
public class ExceptionResult {
    private int status;
    private String message;
    private String timestamp;

    public ExceptionResult(LyException e) {
        this.status = e.getStatus();
        this.message = e.getMessage();
        this.timestamp = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
    }
}