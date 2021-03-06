package com.leyou.search.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.entity.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.*;
import java.util.stream.Collectors;


@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private ElasticsearchTemplate esTemplate; //这是SpringDataElasticSearch查询的对象

    @Autowired
    private GoodsRepository goodsRepository;

    /**
     * 在es中创建good
     * @param spu spu
     * @return Goods
     */
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

    /**
     * 根据查询的需求分页查询对应的商品
     * 主要使用SpringDataElasticSearch来查询，结合ES原生查询
     * 根据关键字match查询，查询结果只要id，subTitle，skus，和GoodsDTO相对应
     * @param searchRequest 查询需求 包含 filterMap:{"品牌":8557,"内存":6GB}，key:"手机"，page:1 三个属性
     * @return <PageResult<GoodsDTO>>
     *------------------------------------------------------------------------------------------------------------------
     * 参数格式：
     *{key: "小米", page: 1, filterMap: {}}
     * key: "小米"
     * page: 1
     * filterMap: {}
     *------------------------------------------------------------------------------------------------------------------
     *通过kibana查询的语法：
     *GET /leyou/_search
     * {
     *   "_source": {
     *     "includes": ["id","subTitle","skus"]
     *   },
     *   "query": {
     *     "match": {
     *       "all": "华为(HUAWEI)"
     *     }
     *   },
     *   "from": 0,
     *   "size": 20
     * }
     *------------------------------------------------------------------------------------------------------------------
     *要返回的结果：
     * {"items":
     *  [
     *      {"id":81,
     *       "subTitle":"",
     *       "skus":"[{},{},{},{}]"}
     *  ]"   ,
     *  "total":121,
     *  "totalPage":7}
     */
    @Override
    public PageResult<GoodsDTO> findGoodsByPage(SearchRequest searchRequest) {
        String key = searchRequest.getKey(); //关键字
        Integer page = searchRequest.getPage();//起始位置
        Integer size = searchRequest.getSize();//每页显示条数
        if (StringUtils.isEmpty(key)){
            return null;
        }
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();//这是ES原生查询的对象
        //1.设置显示字段的过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //2.设置match查询条件
        buildBasicQuery(searchRequest, queryBuilder);
        //3.设置分页起始位置(不是当前页码)和每页显示条数
        queryBuilder.withPageable(PageRequest.of(page - 1,size));//这里的起始位置是从0开始的，page - 1
//        如果需要排序：
//        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.Direction.DESC, "createTime");//根据创建时间降序，新品按钮
//        queryBuilder.withPageable(pageRequest);
        //4.处理查询得到的数据
        AggregatedPage<Goods> aggregatedPage = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
        List<Goods> goodsList = aggregatedPage.getContent();//当前页的数据
        long total = aggregatedPage.getTotalElements();//数据总条数
        Integer totalPages = aggregatedPage.getTotalPages();//总页数
        if (CollectionUtils.isEmpty(goodsList)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        List<GoodsDTO> goodsDTOList = BeanHelper.copyWithCollection(goodsList, GoodsDTO.class);//需要的是DTO
        long totalPagesLong = totalPages.longValue();//需要的是Long类型的
        return new PageResult<GoodsDTO>(total,totalPagesLong,goodsDTOList);
    }

    /**
     * 查询搜索对应的过滤条件
     * 因为参数相关的东西要通过分类id查询，所以先把品牌和分类查出来，然后再去差参数
     * @param searchRequest 查询需求
     * @return <PageResult<GoodsDTO>>
     *------------------------------------------------------------------------------------------------------------------
     * 参数格式：
     *{key: "小米", page: 1, filterMap: {}}
     * key: "小米"
     * page: 1
     * filterMap: {}
     *------------------------------------------------------------------------------------------------------------------
     *通过kibana查询的语法：
     * GET /leyou/_search
     * {
     *   "query": {
     *     "match": {
     *       "all": "手机"
     *     }
     *   },
     *   "from": 0,
     *   "size": 0
     *   , "aggs": {
     *     "brandAgg": {
     *       "terms": {
     *         "field": "brandId",
     *         "size": 20
     *       }
     *     },
     *         "CPU核数AGG": {
     *       "terms": {
     *         "field": "specs.CPU核数",
     *         "size": 20
     *       }
     *     },
     *     "后置摄像头AGG ":{
     *       "terms": {
     *         "field": "specs.后置摄像头",
     *         "size": 20
     *       }
     *     },
     *     .....
     *   }
     * }
     * 通过kibana查询的结果：
     * {
     *   .....
     *   "aggregations": {
     *     "brandAgg": {
     *       "doc_count_error_upper_bound": 0,
     *       "sum_other_doc_count": 0,
     *       "buckets": [
     *         {
     *           "key": 8557,
     *           "doc_count": 79
     *         },
     *         {
     *
     *         },
     *         .....
     *      },
     *       "CPU核数AGG": {
     *       "doc_count_error_upper_bound": 0,
     *       "sum_other_doc_count": 0,
     *       "buckets": [
     *         {
     *           "key": "八核",
     *           "doc_count": 131
     *         },
     *         {
     *           "key": "四核",
     *           "doc_count": 15
     *         },
     *         .....
     *       ]
     *     },
     *     .....
     *   }
     *------------------------------------------------------------------------------------------------------------------
     *要返回的结果：
     * {
     *   "分类": [
     *     {
     *       "id": 76,
     *       "name": "手机",
     *       "parentId": 75,
     *       "isParent": false,
     *       "sort": 1
     *     }
     *   ],
     *   "品牌": [
     *     {
     *       "id": 8557,
     *       "name": "华为（HUAWEI）",
     *       "image": "http://img10.360buyimg.com/popshop/jfs/t5662/36/8888655583/7806/1c629c01/598033b4Nd6055897.jpg",
     *       "letter": "H"
     *     }
     *   ],
     *    "操作系统": [
     *    "Android"
     *   ],
     *   ......
     * }
     */
    @Override
    public Map<String, List<?>> filter(SearchRequest searchRequest) {
        //?的范围比Object更大，brandList无法转成Object那就用?来做泛型
        Map<String, List<?>> resultMap = new HashMap<>();

        String key = searchRequest.getKey(); //关键字
        Integer page = searchRequest.getPage();//起始位置
        Integer size = searchRequest.getSize();//每页显示条数
        if (StringUtils.isEmpty(key)){
            return null;
        }
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();//这是ES原生查询的对象
        //1.设置显示字段的过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(null,null));//不需要显示任何字段,只要聚合结果
        //2.设置match查询条件
        buildBasicQuery(searchRequest, queryBuilder);
        //3.设置分页起始位置(不是当前页码)和每页显示条数
        queryBuilder.withPageable(PageRequest.of(page - 1,1));//不需要查询结果中的字段,这里size写0会报错，需要写1
        //==============================================================================================================
        //4.构建品牌和分类的聚合条件
        queryBuilder.addAggregation(AggregationBuilders.terms("brandAgg").field("brandId").size(20));//品牌聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("categoryAgg").field("categoryId").size(20));//分类聚合
        //5.处理品牌和分类的数据
        AggregatedPage<Goods> aggregatedPage = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = aggregatedPage.getAggregations();//聚合的结果在aggregations字段里面
        //5.1.从聚合结果中获取品牌--------------------------------------------------------------------------------------
        Terms brandTerms = aggregations.get("brandAgg");//这里的参数要和上面的brandAgg一样,用一个子接口Terms来接受
        List<? extends Terms.Bucket> brandBuckets = brandTerms.getBuckets();//buckets是一个集合
        //buckets是一个集合，里面有聚合好的组和组中的数量，现在需要的是聚合好的组(brandId)的一个集合
        List<Long> brandIdList = brandBuckets.stream().map(Terms.Bucket::getKeyAsNumber).//将每一个bucket里的key(brandId)取出来
                map(Number::longValue).//将品牌id转换成Long类型
                collect(Collectors.toList());//将数据转换成一个list集合
        //通过Feign调用根据品牌id集合查询品牌对象集合的方法
        List<BrandDTO> brandList = itemClient.findBrandListByBrandIdList(brandIdList);
        resultMap.put("品牌",brandList);
        //5.2.从聚合结果中获取分类--------------------------------------------------------------------------------------
        Terms categoryTerms = aggregations.get("categoryAgg");//这里的参数要和上面的categoryAgg一样
        List<? extends Terms.Bucket> categoryBuckets = categoryTerms.getBuckets();//buckets是一个集合
        //buckets是一个集合，里面有聚合好的组和组中的数量，现在需要的是聚合好的组(categoryId)的一个集合
        List<Long> categoryIdList = categoryBuckets.stream().map(Terms.Bucket::getKeyAsNumber).//将每一个bucket里的key(categoryId)取出来
                map(Number::longValue).//将分类id转换成Long类型
                collect(Collectors.toList());//将数据转换成一个list集合
        //通过Feign调用根据分类id集合查询分类对象集合的方法
        List<CategoryDTO> categoryList = itemClient.findCategoryListByCategoryIdList(categoryIdList);
        resultMap.put("分类",categoryList);
        //==============================================================================================================
        //6.聚合spec规格参数
        // 因为参数相关的东西要通过分类id查询，所以先把品牌和分类查出来，然后再去聚合参数
        if (categoryIdList != null && categoryIdList.size() > 0){
            //6.1根据分类查询需要搜索的规格参数-------------------------------------------------------------------------
            Long categoryId = categoryIdList.get(0);//取分类的第一个去查询规格参数
            List<SpecParamDTO> paramList = itemClient.findSpecParamByCategoryIdOrGroupId(null, categoryId, true);
            //6.2构建spec规格参数的聚合条件-----------------------------------------------------------------------------
            NativeSearchQueryBuilder paramQueryBuilder = new NativeSearchQueryBuilder();//这是ES原生查询的对象
            paramQueryBuilder.withSourceFilter(new FetchSourceFilter(null,null));//不需要显示任何字段,只要聚合结果
            buildBasicQuery(searchRequest,paramQueryBuilder);//执行基本查询
            paramQueryBuilder.withPageable(PageRequest.of(0,1));//不需要查询结果中的字段,这里size写0会报错，需要写1
            for (SpecParamDTO specParam : paramList) {
                //构建每个参数的聚合条件
                //"field": "specs.CPU核数"
                String paramName = specParam.getName();
                paramQueryBuilder.addAggregation(AggregationBuilders.terms(paramName + "Agg").field("specs." + paramName).size(20));
            }
            //6.3从结果中获取聚合后的参数-------------------------------------------------------------------------------
            AggregatedPage<Goods> aggParamPage = esTemplate.queryForPage(paramQueryBuilder.build(), Goods.class);
            Aggregations paramPageAggregations = aggParamPage.getAggregations();
            for (SpecParamDTO specParam : paramList) {
                //取出每一个聚合
                String paramName = specParam.getName();
                Terms terms = paramPageAggregations.get(paramName + "Agg");//用一个子接口Terms来接受
                List<? extends Terms.Bucket> paramBuckets = terms.getBuckets();//buckets是一个集合
                List<String> paramAggList = paramBuckets.stream().// 一个bucket: {"key": "八核","doc_count": 131}
                        map(Terms.Bucket::getKeyAsString)//将buckets里的每一个bucket的key取出来
                        .collect(Collectors.toList());//转成一个list集合
                resultMap.put(paramName,paramAggList);//放入返回的map当中
            }
        }

        return resultMap;

    }

    /**
     * 在ES中创建对应的商品索引
     * @param spuId spu的id
     */
    @Override
    public void createGoods(Long spuId) {
        //1.根据spuId查询spu对象，构建goods
        SpuDTO spu = itemClient.findSpuBySpuId(spuId);
        //此时spu中的brandName和categoryName为空，itemClient.findSpuByPage()方法里设置了，但是itemClient.findSpuBySpuId(spuId)里没有设置
        BrandDTO brand = itemClient.findBrandByBrandId(spu.getBrandId());//得到brand
        spu .setBrandName(brand.getName());//设置brandName
        List<CategoryDTO> categoryList = itemClient.findCategoryListByCategoryIdList(spu.getCategoryIds());//得到categoryList集合
        String categoryNames = categoryList.stream().map(CategoryDTO::getName).collect(Collectors.joining("/"));//取出一个categoryNames的集合
        spu.setCategoryName(categoryNames);//设置categoryName;
        //2.根据Spu得到Goods
        Goods goods = this.buildGoods(spu);
        //3.存储
        goodsRepository.save(goods);

        System.out.println("索引数据创建成功");
    }

    /**
     * 在ES中删除对应的商品索引
     * @param spuId spu的id
     */
    @Override
    public void removeGoods(Long spuId) {
        goodsRepository.deleteById(spuId);
        System.out.println("索引数据删除成功");
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

    //构建queryBuilder的基本查询
    private void buildBasicQuery(SearchRequest searchRequest, NativeSearchQueryBuilder queryBuilder) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();//使用布尔查询
        boolQuery.must(QueryBuilders.matchQuery("all",searchRequest.getKey()));//布尔查询里要有match查询
        Map<String, Object> filterMap = searchRequest.getFilterMap();
        if (filterMap != null){
            //遍历filterMap,map里有多少个key,就应该有多少个filter
            for (String key : filterMap.keySet()) {
                if (key.equals("品牌")){//如果是品牌的话，转成brandId,
                    boolQuery.filter(QueryBuilders.termQuery("brandId",filterMap.get(key)));
                }else if (key.equals("分类")){//如果是分类的话，转成categoryId
                    boolQuery.filter(QueryBuilders.termQuery("categoryId",filterMap.get(key)));
                }else {//如果是参数的话，要加上"specs."
                    boolQuery.filter(QueryBuilders.termQuery("specs." + key,filterMap.get(key)));
                }
            }
        }



        queryBuilder.withQuery(boolQuery);
    }
}
