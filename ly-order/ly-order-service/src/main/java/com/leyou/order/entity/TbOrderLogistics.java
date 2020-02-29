package com.leyou.order.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TbOrderLogistics extends Model<TbOrderLogistics> {

private static final long serialVersionUID=1L;

    /**
     * 订单id，与订单表一对一
     */
    private Long orderId;

    /**
     * 物流单号
     */
    private String logisticsNumber;

    /**
     * 物流公司名称
     */
    private String logisticsCompany;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 收件人手机号码
     */
    private String phone;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * 街道
     */
    private String street;

    /**
     * 邮编
     */
    private Integer postcode;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 收件人姓名
     */
    private String name;


    @Override
    protected Serializable pkVal() {
        return this.orderId;
    }

}
