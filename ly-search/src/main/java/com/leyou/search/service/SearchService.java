package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.search.entity.Goods;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;


@Service
public class SearchService {

    @Autowired
    ItemClient itemClient;

    public Goods buildGoods(SpuDTO spu){

        Goods goods = new Goods();

//======================================================================================================================
        //1.将id，品牌id，分类id，创建时间，all，副标题等属性放入goods当中
        goods.setId(spu.getId());
        goods.setBrandId(spu.getBrandId());
        goods.setCategoryId(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime().getTime()); //时间的毫秒值
        String all = spu.getName()+spu.getBrandName()+spu.getCategoryName();
        goods.setAll(all);  //商品名称 、 商品的品牌名称 、 商品的分类名称
        goods.setSubTitle(spu.getSubTitle());
//======================================================================================================================
        //2.将skus放入goods属性当中
//        [
//               { id:1  title:"AAAA" price:100, image:""},
//               { id:2  title:"BBB" price:100, image:""},
//               { id:3  title:"CCC" price:200, image:""},
//               { id:4 title:"DDD" price:100, image:""},
//          ]
//        当前spu的sku列表呢
        List<SkuDTO> skuDTOList = itemClient.findSkuDTOListBySpuId(spu.getId());

        List<Map> skus = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOList) {
            Map map = new HashMap();
            map.put("id",skuDTO.getId());
            map.put("title",skuDTO.getTitle());
            map.put("price",skuDTO.getPrice());
            map.put("image", StringUtils.substringBefore(skuDTO.getImages(),",")); //取第一张图片
//            map.put("image", skuDTO.getImages().split(",")[1]);
            skus.add(map);
        }
        goods.setSkus(JsonUtils.toString(skus));
//======================================================================================================================
        //3.收集每个sku的价格放入到一个set集合中
        Set<Long> priceSet = skuDTOList.stream().map(SkuDTO::getPrice).collect(Collectors.toSet());
        goods.setPrice(priceSet);
//======================================================================================================================
        //4.把spDetail的GenericSpec和SpecialSpec合并到一个Map当中
        //GenericSpec : {"1":"华为","2":"G9青春版（全网通版）","3":2016,"5":143,"6":"陶瓷","7":"Android"}
        //SpecialSpec : {"4":["白色","金色","玫瑰金"],"12":["3GB"],"13":["16GB"]}
//----------------------------------------------------------------------------------------------------------------------
        //4.1: GenericSpec:
        SpuDetailDTO spuDetail = itemClient.findSpuDetailBySpuId(spu.getId());
        String genericSpec = spuDetail.getGenericSpec();
        Map<Long,Object> genericSpecMap = JsonUtils.toMap(genericSpec, Long.class,Object.class);
//        Map<Long,String> genericSpecMap = JsonUtils.toBean(genericSpec, Map.class);
//----------------------------------------------------------------------------------------------------------------------
        //4.2: SpecialSpec:
        String specialSpec = spuDetail.getSpecialSpec();
        Map<Long, List<String>> specialSpecMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<String>>>() {});
//----------------------------------------------------------------------------------------------------------------------
        //4.3: 合并
        Map<String,Object> specsMap = new HashMap<>();
        //只放入ES用来搜索的规格参数
        List<SpecParamDTO> specParamList = itemClient.findSpecParamByCategoryIdOrGroupId(null, spu.getCid3(), true);
        for (SpecParamDTO specParam : specParamList) {
            String key = specParam.getName();//以规格参数的名字为键，比如1：品牌，2：型号，3：上市年份，4：机身颜色
            Object value = "";//可能是字符串"华为"，也可能是集合["白色","金色","玫瑰金"]
            //判断参数是generic的还是special的--------------------------------------------------------------------------
            if (specParam.getGeneric()){
                //如果generic=1，那就是genericSpec的属性
                value = genericSpecMap.get(specParam.getId());
            }else {
                //否则就是SpecialSpec的属性
                value = specialSpecMap.get(specParam.getId());
            }
            //判断是否是数值型------------------------------------------------------------------------------------------
            if (specParam.getIsNumeric()){
                //如果是数值型，需要凑成区间查询: 0-1,1-2,2-3
                value = chooseSegment(value,specParam);
            }
            specsMap.put(key,value);
        }
        goods.setSpecs(specsMap);
//======================================================================================================================
        return goods;
    }





//======================================================================================================================
//======================================================================================================================
    //将数值型参数转换成区间样式的方法
    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }
}
