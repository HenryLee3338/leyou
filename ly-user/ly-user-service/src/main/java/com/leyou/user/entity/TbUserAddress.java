package com.leyou.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 用户收货地址表
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TbUserAddress extends Model<TbUserAddress> {

private static final long serialVersionUID=1L;

    /**
     * 地址id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 收货人名称
     */
    private String name;

    /**
     * 收货人电话
     */
    private String phone;

    /**
     * 收货人省份
     */
    private String province;

    /**
     * 收货人市
     */
    private String city;

    /**
     * 收货人区
     */
    private String district;

    /**
     * 收货人街道
     */
    private String street;

    /**
     * 收货人详细地址
     */
    private String address;

    /**
     * 收货人邮编
     */
    private String postcode;

    /**
     * 是否默认 0-不是  1-是
     */
    private Integer isDefault;

    private Date createTime;

    private Date updateTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
