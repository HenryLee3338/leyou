package com.leyou.order.enums;

public enum BusinessTypeEnum {
    MALL(1,"商城"),
    SEC_KILL(2,"秒杀")
    ;

    /**
     * 业务类型
     */
    private Integer type;
    /**
     * 描述
     */
    private String desc;

    BusinessTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer value(){
        return this.type;
    }
    public String desc(){
        return this.desc;
    }
}