package com.leyou.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.leyou.common.utils.RegexPatterns;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TbUser  extends Model<TbUser> {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @Pattern(regexp = RegexPatterns.USERNAME_REGEX, message = "用户名格式不正确")
    private String username;
    @Length(min = 4, max = 30, message = "密码格式不正确")
    private String password;
    @Pattern(regexp = RegexPatterns.PHONE_REGEX, message = "手机号格式不正确")
    private String phone;
    private Date createTime;
    private Date updateTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}

