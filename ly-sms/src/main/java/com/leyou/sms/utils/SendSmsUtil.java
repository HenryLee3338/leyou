package com.leyou.sms.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.leyou.sms.constants.SmsConstants.*;

/*
pom.xml
<dependency>
  <groupId>com.aliyun</groupId>
  <artifactId>aliyun-java-sdk-core</artifactId>
  <version>4.0.3</version>
</dependency>
*/
@Component
@Slf4j //方便写log日志
public class SendSmsUtil {

    @Autowired
    private IAcsClient client;

    @Autowired
    private SmsProperties prop;

    /**
     * 发送短信
     * @param phoneNumbers 电话号码
     * @param signName 签名名称
     * @param templateCode 短信模板
     * @param templateParam 验证码
     */
    public void sendSms(String phoneNumbers,String signName,String templateCode,String templateParam) {
//        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "<accessKeyId>", "<accessSecret>");
//        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain(prop.getDomain());
        request.setVersion(prop.getVersion());
        request.setAction(prop.getAction());

        //为避免写错第一个参数，全部定义成常量,在SmsConstants里
        request.putQueryParameter("RegionId", prop.getRegionID());
        request.putQueryParameter(SMS_PARAM_KEY_PHONE, phoneNumbers);
        request.putQueryParameter(SMS_PARAM_KEY_SIGN_NAME, signName);//亨利旅游网
        request.putQueryParameter(SMS_PARAM_KEY_TEMPLATE_CODE, templateCode);//SMS_184110954
        request.putQueryParameter(SMS_PARAM_KEY_TEMPLATE_PARAM, templateParam);//"{\"code\":\""+numeric+"\"}"

        System.out.println("验证码" + templateParam);//打印验证码

        try {
            //发送，并返回数据
            CommonResponse response = client.getCommonResponse(request);
            //判断短信是否发送成功
            String data = response.getData();
            Map<String, String> responseDataMap = JsonUtils.toMap(data, String.class, String.class);
            String message = responseDataMap.get(SMS_RESPONSE_KEY_MESSAGE);
            //如果发送失败
            if (!StringUtils.equals(OK,message)){
                log.error("短信发送失败");
            }

            System.out.println(response.getData());//如果是ok，则发送成功
        } catch (ServerException e) {//服务端异常
            log.error("服务端异常");
        } catch (ClientException e) {//客户端异常
//            log.error("客户端异常");
            e.printStackTrace();
        }
    }
}