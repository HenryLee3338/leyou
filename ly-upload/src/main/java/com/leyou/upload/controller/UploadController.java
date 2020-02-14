package com.leyou.upload.controller;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;

    /**
     * 图片上传到本地
     * @param file 前端传来的文件，名称必须和前端的名称相同
     * @return ResponseEntity<String>
     */
    @PostMapping(value = "/image",name = "图片上传到本地")
    public ResponseEntity<String> uploadImage(MultipartFile file){
        String imageUrl = uploadService.uploadImage(file);
        return ResponseEntity.ok(imageUrl);
    }

    @GetMapping(value = "/signature",name = "ossweb端直传需要的签名")
    public ResponseEntity<Map<String,String>> signature(){
        Map<String,String> signatureMap = uploadService.signature();
        return  ResponseEntity.ok(signatureMap);
    }
}
