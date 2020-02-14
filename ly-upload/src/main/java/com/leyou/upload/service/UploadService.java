package com.leyou.upload.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.upload.config.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

@Service
public class UploadService {

    //支持的图片mime类型集合
    private List<String> imageMimeType = Arrays.asList("image/jpeg","image/jpg","image/gif","image/png");

    /**
     * 图片上传到本地
     * @param file 前端传来的文件，名称必须和前端的名称相同
     * @return ResponseEntity<String>
     */
    public String uploadImage(MultipartFile file) {
        //1.判断是否是图片(mime类型)
        String contentType = file.getContentType();
        if (!imageMimeType.contains(contentType)) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

        //2.判断图片真假(实际内容)，通过工具类ImageIO
        try {
            BufferedImage read = ImageIO.read(file.getInputStream());
            if (read==null){//没有读到内容
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        //3.上传是否出现异常
        String filename = UUID.randomUUID().toString() + file.getOriginalFilename();
        try {
            file.transferTo(new File("E:\\Develop\\nginx-1.16.0\\html\\" + filename));
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
        return "http://image.leyou.com/" + filename;
    }

    @Autowired
    private OSSProperties ossProperties;

    @Autowired
    private OSS client;

    /**
     * 生成一个签名，封装并返回
     * @return Map<String, String>
     */
    public Map<String, String> signature() {
        try {
            long expireTime = ossProperties.getExpireTime(); //超时时间,签名有效时间
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, ossProperties.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessId", ossProperties.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", ossProperties.getDir());//bucket的某个子目录
            respMap.put("host", ossProperties.getHost());//访问oss的域名，bucket + endpoint
            respMap.put("expire", String.valueOf(expireEndTime));//超时时间 这里不要除以1000
            // respMap.put("expire", formatISO8601Date(expiration));
            return respMap;
        }catch (Exception e){
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
    }
}
