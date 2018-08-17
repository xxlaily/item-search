package cn.dm.es.common;
import cn.dm.common.Constants;
import cn.dm.common.EmptyUtils;
import cn.dm.common.Page;
import cn.dm.es.document.IESDocument;
import cn.dm.es.document.AbatractESDocumentSetting;
import cn.dm.es.query.AbstractEsQuery;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EsUtils {
    //请求Client
    private static TransportClient client=null;
    //日志
    private static Logger logger=Logger.getLogger(EsUtils.class);
    @Autowired
    private EsConnection esConnection;
    /**
     * getConnection:(获取es连接).
     * @author xbq Date:2018年3月21日上午11:52:02
     * @return
     * @throws Exception
     */
    public void getConnection() throws Exception {
        // 设置集群名称
        Settings settings = Settings.builder().put("cluster.name", esConnection.getClusterName()).build();
        if(client==null) client = new PreBuiltTransportClient(settings)
                .addTransportAddresses(new TransportAddress(InetAddress.getByName(esConnection.getIp()), esConnection.getPort()));
    }

    public boolean initIndex(AbatractESDocumentSetting esIndexSetting){
        logger.info(esIndexSetting.toSettingJson());
        boolean flag=false;
        try {
            getConnection();
            CreateIndexResponse createIndexResponse =client.admin().indices().prepareCreate(esIndexSetting.getIndexName()).execute().get();
            PutMappingResponse putMappingResponse =client.admin().indices().preparePutMapping(esIndexSetting.getIndexName()).setType(esIndexSetting.getTypeName()).setSource(esIndexSetting.toSettingJson(),XContentType.JSON).get();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return flag;
        }
    }

    public boolean addESModule(IESDocument iesDocument) throws Exception {
        String json= JSONObject.toJSONString(iesDocument);
        getConnection();
        IndexResponse indexResponse=client.prepareIndex(iesDocument.getSetting().getIndexName(), iesDocument.getSetting().getTypeName(),iesDocument.getEsId()).setSource(json, XContentType.JSON).get();
        if(indexResponse.status().getStatus()==200){
            return true;
        }
        return false;
    }

    public boolean addBatchESModule(List<IESDocument> esModules) throws Exception {
        boolean flag=false;
        getConnection();
        BulkRequestBuilder bulkRequest=client.prepareBulk();
        for (IESDocument esModule:esModules){
            String json= JSONObject.toJSONString(esModule);
            bulkRequest.add(client.prepareIndex(esModule.getSetting().getIndexName(),esModule.getSetting().getTypeName(),esModule.getEsId()).setSource(json, XContentType.JSON));
        }
        BulkResponse response=bulkRequest.execute().actionGet();
        if(response.status().getStatus()==200){
            flag=true;
        }
        return flag;
    }

    public boolean updateESModule(IESDocument esModule) throws Exception {
        boolean flag=false;
        String json= JSONObject.toJSONString(esModule);
        getConnection();
        UpdateResponse updateResponse=client.prepareUpdate(esModule.getSetting().getIndexName(),esModule.getSetting().getTypeName(),esModule.getEsId()).setDoc(json, XContentType.JSON).get();
        if(updateResponse.status().getStatus()==200){
            flag=true;
        }
        return flag;
    }

    public boolean updateBatchESModule(List<IESDocument> esModules) throws Exception {
        boolean flag=false;
        getConnection();
        BulkRequestBuilder bulkRequest=client.prepareBulk();
        for (IESDocument esModule:esModules){
            String json= JSONObject.toJSONString(esModule);
            bulkRequest.add(client.prepareUpdate(esModule.getSetting().getIndexName(),esModule.getSetting().getTypeName(),esModule.getEsId()).setDoc(json, XContentType.JSON));
        }
        BulkResponse response=bulkRequest.execute().actionGet();
        if(response.status().getStatus()==200){
            flag=true;
        }
        return flag;
    }

    public boolean deleteESModule(IESDocument esModule) throws Exception {
        boolean flag=false;
        getConnection();
        DeleteResponse deleteResponse=client.prepareDelete(esModule.getSetting().getIndexName(),esModule.getSetting().getTypeName(),esModule.getEsId()).get();
        if(deleteResponse.status().getStatus()==200){
            flag=true;
        }
        return flag;
    }

    public boolean deleteatchESModule(List<IESDocument> esModules) throws Exception {
        boolean flag=false;
        getConnection();
        BulkRequestBuilder bulkRequest=client.prepareBulk();
        for (IESDocument esModule:esModules){
            bulkRequest.add(client.prepareDelete(esModule.getSetting().getIndexName(),esModule.getSetting().getTypeName(),esModule.getEsId()));
        }
        BulkResponse response=bulkRequest.execute().actionGet();
        if(response.status().getStatus()==200){
            flag=true;
        }
        return flag;
    }

    public Page queryPage(AbstractEsQuery dmEsQuery) throws Exception {
        Page page=null;
        List result=null;

        List<MatchQueryBuilder> matchQueryBuilders=new ArrayList<MatchQueryBuilder>();
        List<TermQueryBuilder> termQueryBuilders=new ArrayList<TermQueryBuilder>();
        List<RangeQueryBuilder> rangeQueryBuilders=new ArrayList<RangeQueryBuilder>();
        try {
            if(EmptyUtils.isEmpty(dmEsQuery)){
                logger.info(">>>>>>queryPage未设置查询条件>>>>>>>>>");
                return page;
            }
            getConnection();
            SearchRequestBuilder searchRequestBuilder= client.prepareSearch(dmEsQuery.getIndexName());
            searchRequestBuilder.setTypes(dmEsQuery.getTypeName());
            searchRequestBuilder.setSearchType(SearchType.QUERY_THEN_FETCH);
            searchRequestBuilder.setExplain(true);
            //判断倒序
            if(EmptyUtils.isNotEmpty(dmEsQuery.getDesc())){
                searchRequestBuilder.addSort(dmEsQuery.getDesc(), SortOrder.DESC);
            }
            //判断正序
            if(EmptyUtils.isNotEmpty(dmEsQuery.getAsc())){
                searchRequestBuilder.addSort(dmEsQuery.getAsc(), SortOrder.ASC);
            }

            if(EmptyUtils.isEmpty(dmEsQuery.getPageSize())){
                dmEsQuery.setPageSize(Constants.DEFAULT_PAGE_SIZE);
            }

            if(EmptyUtils.isEmpty(dmEsQuery.getPageNo())){
                dmEsQuery.setPageNo(Constants.DEFAULT_PAGE_NO);
            }
            //判断分页
            Integer beginPos = (dmEsQuery.getPageNo() - 1) * dmEsQuery.getPageSize();
            searchRequestBuilder.setFrom(beginPos);
            searchRequestBuilder.setSize(dmEsQuery.getPageSize());
            //模糊匹配
            if(EmptyUtils.isNotEmpty(dmEsQuery.getMatchParams())){
                for (Map.Entry<String, Object> entry : dmEsQuery.getMatchParams().entrySet()) {
                    matchQueryBuilders.add(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));
                }
            }
            //精准查询
            if(EmptyUtils.isNotEmpty(dmEsQuery.getLikeMatchParams())){
                for (Map.Entry<String, Object> entry : dmEsQuery.getLikeMatchParams().entrySet()) {
                    termQueryBuilders.add(QueryBuilders.termQuery(entry.getKey(),entry.getValue()));
                }
            }
            //范围匹配 大于
            if(EmptyUtils.isNotEmpty(dmEsQuery.getLteParams())){
                for (Map.Entry<String, Object> entry : dmEsQuery.getLteParams().entrySet()) {
                    rangeQueryBuilders.add(QueryBuilders.rangeQuery(entry.getKey()).lte(entry.getValue()));
                }
            }

            //范围匹配 小于
            if(EmptyUtils.isNotEmpty(dmEsQuery.getGteParams())){
                for (Map.Entry<String, Object> entry : dmEsQuery.getGteParams().entrySet()) {
                    rangeQueryBuilders.add(QueryBuilders.rangeQuery(entry.getKey()).gte(entry.getValue()));
                }
            }
            BoolQueryBuilder boolQueryBuilder=null;

            for (MatchQueryBuilder matchQueryBuilder:matchQueryBuilders){
                if(EmptyUtils.isEmpty(boolQueryBuilder)){
                    boolQueryBuilder=QueryBuilders.boolQuery().must(matchQueryBuilder);
                }else{
                    boolQueryBuilder=boolQueryBuilder.must(matchQueryBuilder);
                }
            }

            for (TermQueryBuilder termQueryBuilder:termQueryBuilders){
                if(EmptyUtils.isEmpty(boolQueryBuilder)){
                    boolQueryBuilder=QueryBuilders.boolQuery().must(termQueryBuilder);
                }else{
                    boolQueryBuilder=boolQueryBuilder.must(termQueryBuilder);
                }
            }
            for (RangeQueryBuilder rangeQueryBuilder:rangeQueryBuilders){
                if(EmptyUtils.isEmpty(boolQueryBuilder)){
                    boolQueryBuilder=QueryBuilders.boolQuery().must(rangeQueryBuilder);
                }else{
                    boolQueryBuilder=boolQueryBuilder.must(rangeQueryBuilder);
                }
            }
            searchRequestBuilder.setQuery(boolQueryBuilder);
            logger.info("elasticSearch sql :"+searchRequestBuilder.toString());
            SearchResponse response = searchRequestBuilder.execute().actionGet();
            SearchHits searchHits = response.getHits();
            logger.info(" elasticSearch search total ："+searchHits.getTotalHits());
            page=new Page(dmEsQuery.getPageNo(),dmEsQuery.getPageSize(),new Long(searchHits.getTotalHits()).intValue());
            SearchHit[] hits = searchHits.getHits();
            if(EmptyUtils.isNotEmpty(hits)){
                result=new ArrayList();
                for (SearchHit hit : hits) {
                    String json = hit.getSourceAsString();
                    IESDocument t= (IESDocument) JSONObject.parseObject(json,dmEsQuery.getModuleClass());
                    result.add(t);
                }
                page.setRows(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return page;
    }


    public List queryList(AbstractEsQuery dmEsQuery) throws Exception {
        List result=null;
        List<MatchQueryBuilder> matchQueryBuilders=new ArrayList<MatchQueryBuilder>();
        List<TermQueryBuilder> termQueryBuilders=new ArrayList<TermQueryBuilder>();
        List<RangeQueryBuilder> rangeQueryBuilders=new ArrayList<RangeQueryBuilder>();
        try {
            if(EmptyUtils.isEmpty(dmEsQuery)){
                logger.info(">>>>>>queryList未设置查询条件>>>>>>>>>");
                return result;
            }
            getConnection();
            SearchRequestBuilder searchRequestBuilder= client.prepareSearch(dmEsQuery.getIndexName());
            searchRequestBuilder.setTypes(dmEsQuery.getTypeName());
            searchRequestBuilder.setSearchType(SearchType.QUERY_THEN_FETCH);
            searchRequestBuilder.setExplain(true);
            //判断倒序
            if(EmptyUtils.isNotEmpty(dmEsQuery.getDesc())){
                searchRequestBuilder.addSort(dmEsQuery.getDesc(), SortOrder.DESC);
            }
            //判断正序
            if(EmptyUtils.isNotEmpty(dmEsQuery.getAsc())){
                searchRequestBuilder.addSort(dmEsQuery.getAsc(), SortOrder.ASC);
            }

            if(EmptyUtils.isEmpty(dmEsQuery.getPageSize())){
                dmEsQuery.setPageSize(Constants.DEFAULT_PAGE_SIZE);
            }

            if(EmptyUtils.isEmpty(dmEsQuery.getPageNo())){
                dmEsQuery.setPageNo(Constants.DEFAULT_PAGE_NO);
            }
            //判断分页
            Integer beginPos = (dmEsQuery.getPageNo() - 1) * dmEsQuery.getPageSize();
            searchRequestBuilder.setFrom(beginPos);
            searchRequestBuilder.setSize(dmEsQuery.getPageSize());
            //精准匹配
            if(EmptyUtils.isNotEmpty(dmEsQuery.getMatchParams())){
                for (Map.Entry<String, Object> entry : dmEsQuery.getMatchParams().entrySet()) {
                    matchQueryBuilders.add(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));
                }
            }
            //模糊查询
            if(EmptyUtils.isNotEmpty(dmEsQuery.getLikeMatchParams())){
                for (Map.Entry<String, Object> entry : dmEsQuery.getLikeMatchParams().entrySet()) {
                    termQueryBuilders.add(QueryBuilders.termQuery(entry.getKey(),entry.getValue()));
                }
            }
            //范围匹配 大于
            if(EmptyUtils.isNotEmpty(dmEsQuery.getLteParams())){
                for (Map.Entry<String, Object> entry : dmEsQuery.getLteParams().entrySet()) {
                    rangeQueryBuilders.add(QueryBuilders.rangeQuery(entry.getKey()).lte(entry.getValue()));
                }
            }

            //范围匹配 小于
            if(EmptyUtils.isNotEmpty(dmEsQuery.getGteParams())){
                for (Map.Entry<String, Object> entry : dmEsQuery.getGteParams().entrySet()) {
                    rangeQueryBuilders.add(QueryBuilders.rangeQuery(entry.getKey()).lte(entry.getValue()));
                }
            }
            BoolQueryBuilder boolQueryBuilder=null;
            for (MatchQueryBuilder matchQueryBuilder:matchQueryBuilders){
                if(EmptyUtils.isEmpty(boolQueryBuilder)){
                    boolQueryBuilder=QueryBuilders.boolQuery().must(matchQueryBuilder);
                }else{
                    boolQueryBuilder=boolQueryBuilder.must(matchQueryBuilder);
                }
            }
            for (TermQueryBuilder termQueryBuilder:termQueryBuilders){
                if(EmptyUtils.isEmpty(boolQueryBuilder)){
                    boolQueryBuilder=QueryBuilders.boolQuery().must(termQueryBuilder);
                }else{
                    boolQueryBuilder=boolQueryBuilder.must(termQueryBuilder);
                }
            }
            for (RangeQueryBuilder rangeQueryBuilder:rangeQueryBuilders){

                if(EmptyUtils.isEmpty(boolQueryBuilder)){
                    boolQueryBuilder=QueryBuilders.boolQuery().must(rangeQueryBuilder);
                }else{
                    boolQueryBuilder=boolQueryBuilder.must(rangeQueryBuilder);
                }
            }
            searchRequestBuilder.setQuery(boolQueryBuilder);
            logger.info("elasticSearch sql :"+searchRequestBuilder.toString());
            SearchResponse response = searchRequestBuilder.execute().actionGet();
            SearchHits searchHits = response.getHits();
            logger.info(" elasticSearch search total ："+searchHits.getTotalHits());
            SearchHit[] hits = searchHits.getHits();
            if(EmptyUtils.isNotEmpty(hits)){
                result=new ArrayList();
                for (SearchHit hit : hits) {
                    String json = hit.getSourceAsString();
                    IESDocument t= (IESDocument) JSONObject.parseObject(json,dmEsQuery.getModuleClass());
                    result.add(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
