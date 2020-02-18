package com.leyou.search.test;

import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.entity.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticSearchInitTest {
    //初始化ES索引数据

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodsRepository goodsRepository;

    @Test
    public void initEs() {
        //1.查询所有的SPU
        //通过feign调用item微服务提供的方法
        //每次查询100条上架的商品,查不到数据为止
        int page = 1;
        while (true) {
            PageResult<SpuDTO> pageResult = itemClient.findSpuByPage(page, 100, null, true);
            List<SpuDTO> spuDTOList = pageResult.getItems();
            if (CollectionUtils.isEmpty(spuDTOList)) {
                break;//查不到数据为止
            }
            //2.把SPU转成Goods
            //在searchService中保存，业务复杂
            List<Goods> goodsList = new ArrayList<>();
            for (SpuDTO spuDTO : spuDTOList) {
                Goods goods = searchService.buildGoods(spuDTO);
                goodsList.add(goods);
            }
            //3.保存Goods到ES中
            //通过GoodsRepository的保存方法
            goodsRepository.saveAll(goodsList);
            page++;
        }
    }
}
