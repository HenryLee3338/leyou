package com.leyou.page.service.impl;

import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 展示商品详细页面
     * @param spuId
     * 需要返回的内容：
     * categories      spu三级分类集合
     * brand           品牌的对象
     * spuName         spu对象的名称
     * subTitle        spu对象的子标题
     * detail          SPUDetail对象
     * skus            当前spu下的sku集合
     * specs           规格组的集合，每个规格组下都有所属的规格参数的集合
     */
    @Override
    public Map buildDataBySPUId(Long spuId) {
        Map dataMap = new HashMap<>();
        //1.根据id查出spu，将spuName和subTitle放入map当中
        SpuDTO spu = itemClient.findSpuBySpuId(spuId);
        dataMap.put("spuName",spu.getName());
        dataMap.put("subTitle",spu.getSubTitle());
        //2.取出spu中的categoryIds，查询对应的分类的集合
        List<Long> categoryIds = spu.getCategoryIds();
        List<CategoryDTO> categoryList = itemClient.findCategoryListByCategoryIdList(categoryIds);
        dataMap.put("categories",categoryList);
        //3.取出spu中的brandId，查询对应的品牌
        Long brandId = spu.getBrandId();
        BrandDTO brand = itemClient.findBrandByBrandId(brandId);
        dataMap.put("brand",brand);
        //4.根据spuId查询SpuDetail
        SpuDetailDTO detail = itemClient.findSpuDetailBySpuId(spuId);
        dataMap.put("detail",detail);
        //5.根据spuId查询Skus
        List<SkuDTO> skus = itemClient.findSkuDTOListBySpuId(spuId);
        dataMap.put("skus",skus);
//        //6.根据第三级分类id来查询对应的参数组和参数
        List<SpecGroupDTO> specGroupWithParams = itemClient.findSpecGroupWithParamsByCategoryId(spu.getCid3());
        dataMap.put("specs",specGroupWithParams);
        return dataMap;
    }

    /**
     * 删除对应的html页面
     * @param spuId spu的id
     */
    @Override
    public void removeHtml(Long spuId) {
        File file = new File("E:\\Develop\\nginx-1.16.0\\html\\item\\"+ spuId +".html");
        file.delete();
        System.out.println("静态页面删除成功");
    }

    /**
     * 创建对应的html页面
     * @param spuId spu的id
     */
    @Override
    public void createHtml(Long spuId) {
//        - Context：运行上下文             数据
//        - TemplateResolver：模板解析器   模板文件
//        - TemplateEngine：模板引擎  把数据和模板文件结合生成一个html文件
        Context context = new Context();
        context.setVariables(this.buildDataBySPUId(spuId));
        try(PrintWriter writer = new PrintWriter("E:\\Develop\\nginx-1.16.0\\html\\item\\"+ spuId +".html")) {  //流的关闭是自动的
            templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("静态页面创建成功");
    }
}
