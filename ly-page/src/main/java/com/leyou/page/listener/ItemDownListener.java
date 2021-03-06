package com.leyou.page.listener;

import com.leyou.page.service.PageService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.common.constants.RocketMQConstants.CONSUMER.ITEM_PAGE_DOWN_CONSUMER;
import static com.leyou.common.constants.RocketMQConstants.TAGS.ITEM_DOWN_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.ITEM_TOPIC_NAME;

@Component//交给spring容器管理
@RocketMQMessageListener(consumerGroup = ITEM_PAGE_DOWN_CONSUMER,topic = ITEM_TOPIC_NAME,selectorExpression = ITEM_DOWN_TAGS)
public class ItemDownListener implements RocketMQListener<Long> {

    @Autowired
    private PageService pageService;

    /**
     * 删除对应的html页面
     * @param spuId spu的id
     */
    @Override
    public void onMessage(Long spuId) {
        pageService.removeHtml(spuId);
    }
}
