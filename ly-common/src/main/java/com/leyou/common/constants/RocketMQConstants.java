package com.leyou.common.constants;


public abstract class RocketMQConstants {

    public static final class TOPIC {
        /**
         * 商品服务Topic名称
         */
        public static final String ITEM_TOPIC_NAME = "ly-item-topic";
        /**
         * 短信服务
         */
        public static final String SMS_TOPIC_NAME = "ly-sms-topic";
        /**
         * 秒杀服务
         */
        public static final String SECKILL_TOPIC_NAME = "ly-seckill-topic";
        /**
         * 订单
         */
        public static final String ORDER_TOPIC_NAME = "ly-order-topic";
    }

    public static final class TAGS {
        /**
         * 商品上架的TAGS
         */
        public static final String ITEM_UP_TAGS = "item-up";
        /**
         * 商品下架的TAGS
         */
        public static final String ITEM_DOWN_TAGS = "item-down";
        /**
         * 发送短信验证码TAGS
         */
        public static final String VERIFY_CODE_TAGS = "sms-verify-code";
        /**
         * 秒杀开始TAGS
         */
        public static final String SECKILL_BEGIN_TAGS = "seckill-begin";
        /**
         * 秒杀业务创建订单TAGS
         */
        public static final String SECKILL_ORDER_TAGS = "seckill.order";
        /**
         * 订单超时TAGS
         */
        public static final String ORDER_OVERTIME_TAGS = "order.overtime";
        /**
         * 秒杀订单超时TAGS
         */
        public static final String SECKILL_ORDER_OVERTIME_TAGS = "seckillOrder.overtime";
    }
    public static final class CONSUMER{
        /**
         * 短信发送 的消费者
         */
        public static final String SMS_VERIFY_CODE_CONSUMER = "SMS_VERIFY_CODE_CONSUMER";
        /**
         * 秒杀 订单创建
         */
        public static final String SECKILL_ORDER_CONSUMER = "SECKILL_ORDER_CONSUMER";
        /**
         * 秒杀开始
         */
        public static final String SECKILL_BEGIN_CONSUMER = "SECKILL_BEGIN_CONSUMER";
        /**
         * 订单清理
         */
        public static final String ORDER_OVERTIME_CONSUMER = "ORDER_OVERTIME_CONSUMER";
        /**
         * 秒杀订单清理
         */
        public static final String SECKILL_ORDER_OVERTIME_CONSUMER = "SECKILL_ORDER_OVERTIME_CONSUMER";
        /**
         * 静态页商品上架
         */
        public static final String ITEM_PAGE_UP_CONSUMER = "ITEM_PAGE_UP_CONSUMER";
        /**
         * 静态页商品下架
         */
        public static final String ITEM_PAGE_DOWN_CONSUMER = "ITEM_PAGE_DOWN_CONSUMER";
        /**
         * 搜索商品上架
         */
        public static final String  ITEM_SEARCH_UP = "ITEM_SEARCH_UP";
        /**
         * 搜索商品下架
         */
        public static final String ITEM_SEARCH_DOWN = "ITEM_SEARCH_DOWN";

    }
}
