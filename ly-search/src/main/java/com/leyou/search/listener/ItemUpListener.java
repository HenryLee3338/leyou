package com.leyou.search.listener;

import com.leyou.search.service.SearchService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.common.constants.RocketMQConstants.CONSUMER.ITEM_PAGE_UP_CONSUMER;
import static com.leyou.common.constants.RocketMQConstants.CONSUMER.ITEM_SEARCH_UP;
import static com.leyou.common.constants.RocketMQConstants.TAGS.ITEM_UP_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.ITEM_TOPIC_NAME;

@Component//交给spring容器管理
@RocketMQMessageListener(consumerGroup = ITEM_SEARCH_UP,topic = ITEM_TOPIC_NAME,selectorExpression = ITEM_UP_TAGS)
public class ItemUpListener implements RocketMQListener<Long> {

    @Autowired
    private SearchService searchService;

    /**
     * 在ES中创建对应的商品索引
     * @param spuId spu的id
     */
    @Override
    public void onMessage(Long spuId) {
        searchService.createGoods(spuId);
    }
}
