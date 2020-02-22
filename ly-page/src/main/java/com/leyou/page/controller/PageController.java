package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller  //这里不用@RestController
public class PageController {

    @Autowired
    private PageService pageService;

    /**
     * 展示商品详细页面
     * http://www.leyou.com/item/88.html
     * @param id 从url路径占位符中取参数必须要用一个@PathVariable注解才能取到
     * 需要返回的内容：
     * categories      spu三级分类集合
     * brand           品牌的对象
     * spuName         spu对象的名称
     * subTitle        spu对象的子标题
     * detail          SPUDetail对象
     * skus            当前spu下的sku集合
     * specs           规格组的集合，每个规格组下都有所属的规格参数的集合
     */
    @GetMapping(value = "/item/{id}.html",name = "展示商品详细页面")
    public String pageDetail(@PathVariable("id") Long id, Model model){
        Map map = pageService.buildDataBySPUId(id);
        model.addAllAttributes(map);
        return "item";
    }
}
