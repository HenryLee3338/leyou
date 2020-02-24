package com.leyou.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.TbUser;
import com.leyou.user.mapper.TbUserMapper;
import com.leyou.user.service.TbUserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.leyou.common.constants.RocketMQConstants.TAGS.VERIFY_CODE_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.SMS_TOPIC_NAME;
import static com.leyou.common.constants.SmsConstants.*;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbUserServiceImpl extends ServiceImpl<TbUserMapper, TbUser> implements TbUserService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private  BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 查询用户名或者手机号是否存在
     * 因为要从url中取参数，所以要加@PathVariable注解
     * @param data 用户名或者手机号
     * @param type  判断是用户名还是手机号
     * @return Boolean
     */
    @Override
    public Boolean checkData(String data, Integer type) {

        QueryWrapper<TbUser> queryWrapper = new QueryWrapper<>();

        //1.判断type
        //2.查询
        switch (type){
            case 1:{
                //查询的是用户名
                queryWrapper.lambda().eq(TbUser::getUsername,data);
                break;
            }
            case 2:{
                //查询的是手机号
                queryWrapper.lambda().eq(TbUser::getPhone,data);
                break;
            }
            default:{
                new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
            }
        }
        //3.判断是否有数据
        int count = this.count(queryWrapper);
        return count==0?true:false;
    }

    /**
     * 发送短信验证码
     * @param phone 手机号码
     */
    @Override
    public void sendCode(String phone) {
        //1.生产消息放入MQ
        Map map = new HashMap();

        String numeric = RandomStringUtils.randomNumeric(4);//随机产生四位数字

        map.put(SMS_PARAM_KEY_PHONE,phone);
        map.put(SMS_PARAM_KEY_SIGN_NAME,"亨利旅游网");
        map.put(SMS_PARAM_KEY_TEMPLATE_CODE,"SMS_184110954");
        map.put(SMS_PARAM_KEY_TEMPLATE_PARAM,"{\"code\":\""+numeric+"\"}");
        rocketMQTemplate.convertAndSend(SMS_TOPIC_NAME + ":" + VERIFY_CODE_TAGS,map);
        //2.将redis放入redis
        redisTemplate.boundValueOps("ly:sms:registry" + phone).set(numeric,30, TimeUnit.SECONDS);//设置存储时间为30s

    }

    /**
     * 用户注册
     * @param user 用户信息
     * @param code_page 验证码
     */
    @Override
    public void registry(TbUser user, String code_page) {
        //1.比较验证码
        //1.1判断是否能从redis中获取到验证码
        String phone = user.getPhone();
        String code_redis = redisTemplate.boundValueOps("ly:sms:registry" + phone).get();
        if (StringUtils.isEmpty(code_redis)){
            throw new LyException(ExceptionEnum.TIME_OUT_CODE);
        }
        //1.2比较redis中的验证码和前端传来的验证码是否相同
        if (!StringUtils.equals(code_redis,code_page)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //2.密码加密(springSecurity)
        System.out.println(user.toString() + "=============================================================");
        String password = user.getPassword();
        password = bCryptPasswordEncoder.encode(password);
        user.setPassword(password);
        //3.存入数据库
        boolean save = this.save(user);
        if (!save){//如果存入失败
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //4.把redis中的验证码删除
        redisTemplate.delete("ly:sms:registry" + phone);
    }

    /**
     * 根据用户名和密码查询用户
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    @Override
    public UserDTO queryUserByUsernameAndPassword(String username, String password) {
        //1.先根据用户名查询,数据库中用户名有索引，而且参数中的password未加密
        QueryWrapper<TbUser> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(TbUser::getUsername,username);
        TbUser tbUser = this.getOne(queryWrapper);
        if (tbUser == null) {//没有此用户
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //2.再对比密码
        if(!bCryptPasswordEncoder.matches(password, tbUser.getPassword())){// 密码错误
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        return null;
    }
}
