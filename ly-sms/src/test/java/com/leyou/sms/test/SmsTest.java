package com.leyou.sms.test;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static com.leyou.common.constants.RocketMQConstants.TAGS.VERIFY_CODE_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.SMS_TOPIC_NAME;
import static com.leyou.common.constants.SmsConstants.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SmsTest {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Test
    public void testSend(){
//        String phoneNumbers =  map.get(SMS_PARAM_KEY_PHONE).toString();
//        String signName =  map.get(SMS_PARAM_KEY_SIGN_NAME).toString();
//        String templateCode =  map.get(SMS_PARAM_KEY_TEMPLATE_CODE).toString();
//        String templateParam =  map.get(SMS_PARAM_KEY_TEMPLATE_PARAM).toString();
        Map map = new HashMap();

        String numeric = RandomStringUtils.randomNumeric(4);//随机产生四位数字

        map.put(SMS_PARAM_KEY_PHONE,"15192201699");
        map.put(SMS_PARAM_KEY_SIGN_NAME,"亨利旅游网");
        map.put(SMS_PARAM_KEY_TEMPLATE_CODE,"SMS_184110954");
        map.put(SMS_PARAM_KEY_TEMPLATE_PARAM,"{\"code\":\""+numeric+"\"}");
        rocketMQTemplate.convertAndSend(SMS_TOPIC_NAME + ":" + VERIFY_CODE_TAGS,map);

    }
}
