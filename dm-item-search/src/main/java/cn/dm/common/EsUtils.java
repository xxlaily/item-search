package cn.dm.common;
import cn.dm.document.AbatractESDocumentSetting;
import cn.dm.document.IESDocument;
import cn.dm.query.AbstractEsQuery;
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
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EsUtils {
    // http请求的IP
    @Value("${elasticsearch.ip}")
    public static String HOST="192.168.9.151";
    // http请求的端口是9200，客户端是9300
    @Value("${elasticsearch.port}")
    public static int PORT=8300 ;
    @Value("${elasticsearch.clusterName}")
    public static String clusterName;
    //请求Client
    public static TransportClient client=null;

    public static Logger logger=Logger.getLogger(EsUtils.class);
    /**
     * getConnection:(获取es连接).
     * @author xbq Date:2018年3月21日上午11:52:02
     * @return
     * @throws Exception
     */
    public static void getConnection() throws Exception {
        // 设置集群名称
        Settings settings = Settings.builder().put("cluster.name", "elasticsearch-application").build();
        if(client==null){
            client= new PreBuiltTransportClient(settings)
                    .addTransportAddresses(new TransportAddress(InetAddress.getByName(HOST), PORT));
        }
    }

    public EsUtils() throws Exception {
        getConnection();
    }

    public boolean initIndex(AbatractESDocumentSetting esIndexSetting){
        boolean flag=false;
        try {
            CreateIndexResponse createIndexResponse =client.admin().indices().prepareCreate(esIndexSetting.getIndexName()).execute().get();
            PutMappingResponse putMappingResponse =client.admin().indices().preparePutMapping(esIndexSetting.getIndexName()).setType(esIndexSetting.getTypeName()).setSource(esIndexSetting.toSettingJson(),XContentType.JSON).get();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return flag;
        }
    }

    public boolean addESModule(IESDocument iesDocument){
        String json= JSONObject.toJSONString(iesDocument);
        IndexResponse indexResponse=client.prepareIndex(iesDocument.getSetting().getIndexName(), iesDocument.getSetting().getTypeName(),iesDocument.getEsId()).setSource(json, XContentType.JSON).get();
        if(indexResponse.status().getStatus()==200){
            return true;
        }
        return false;
    }

    public boolean addBatchESModule(List<IESDocument> esModules) {
        boolean flag=false;
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

    public boolean updateESModule(IESDocument esModule) {
        boolean flag=false;
        String json= JSONObject.toJSONString(esModule);
        UpdateResponse updateResponse=client.prepareUpdate(esModule.getSetting().getIndexName(),esModule.getSetting().getTypeName(),esModule.getEsId()).setDoc(json, XContentType.JSON).get();
        if(updateResponse.status().getStatus()==200){
            flag=true;
        }
        return flag;
    }

    public boolean updateBatchESModule(List<IESDocument> esModules) {
        boolean flag=false;
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

    public boolean deleteESModule(IESDocument esModule) {
        boolean flag=false;
        DeleteResponse deleteResponse=client.prepareDelete(esModule.getSetting().getIndexName(),esModule.getSetting().getTypeName(),esModule.getEsId()).get();
        if(deleteResponse.status().getStatus()==200){
            flag=true;
        }
        return flag;
    }

    public boolean deleteatchESModule(List<IESDocument> esModules) {
        boolean flag=false;
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

    public Page queryPage(AbstractEsQuery dmEsQuery){
        Page page=null;
        List result=null;
        try {
            if(EmptyUtils.isEmpty(dmEsQuery)){
                logger.info(">>>>>>queryPage未设置查询条件>>>>>>>>>");
                return page;
            }
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
                   searchRequestBuilder.setQuery(QueryBuilders.matchQuery(entry.getKey(),entry.getValue()));
                }
            }
            //模糊查询
            if(EmptyUtils.isNotEmpty(dmEsQuery.getLikeMatchParams())){
                for (Map.Entry<String, Object> entry : dmEsQuery.getLikeMatchParams().entrySet()) {
                    searchRequestBuilder.setQuery(QueryBuilders.termQuery(entry.getKey(),entry.getValue()));
                }
            }
            SearchResponse response = searchRequestBuilder.execute().actionGet();
            SearchHits searchHits = response.getHits();
            System.out.println("总数："+searchHits.getTotalHits());
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


    public List queryList(AbstractEsQuery dmEsQuery){
        List result=null;
        try {
            if(EmptyUtils.isEmpty(dmEsQuery)){
                logger.info(">>>>>>queryList未设置查询条件>>>>>>>>>");
                return result;
            }
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
            //判断分页
            if(EmptyUtils.isNotEmpty(dmEsQuery.getPageSize()) && EmptyUtils.isNotEmpty(dmEsQuery.getPageNo())){
                Integer beginPos = (dmEsQuery.getPageNo() - 1) * dmEsQuery.getPageSize();
                searchRequestBuilder.setFrom(beginPos);
                searchRequestBuilder.setSize(dmEsQuery.getPageSize());
            }
            //精准匹配
            if(EmptyUtils.isNotEmpty(dmEsQuery.getMatchParams())){
                for (Map.Entry<String, Object> entry : dmEsQuery.getMatchParams().entrySet()) {
                    searchRequestBuilder.setQuery(QueryBuilders.matchQuery(entry.getKey(),entry.getValue()));
                }
            }
            //模糊查询
            if(EmptyUtils.isNotEmpty(dmEsQuery.getLikeMatchParams())){
                for (Map.Entry<String, Object> entry : dmEsQuery.getLikeMatchParams().entrySet()) {
                    searchRequestBuilder.setQuery(QueryBuilders.termQuery(entry.getKey(),entry.getValue()));
                }
            }
            SearchResponse response = searchRequestBuilder.execute().actionGet();
            SearchHits searchHits = response.getHits();
            System.out.println("总数："+searchHits.getTotalHits());
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
