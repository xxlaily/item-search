package cn.dm.handller;
import cn.dm.common.EmptyUtils;
import cn.dm.common.Page;
import cn.dm.module.EsDmUser;
import cn.dm.module.IEsModule;
import cn.dm.pojo.DmUser;
import cn.dm.query.DmEsQuery;
import cn.dm.setting.ESAnyanalyzerType;
import cn.dm.setting.ESIndexSetting;
import cn.dm.setting.ESType;
import cn.dm.setting.PropertiesSetting;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
/**
 * ClassName:GetConnection Date: 2018年3月20日 下午8:12:07
 *
 * @author xbq
 * @version
 * @since JDK 1.8
 */
public class ESHandler{

    public final static String HOST = "192.168.9.151";

    public final static int PORT = 8300;

    //全局client
    private TransportClient client = null;

    //日志处理
    static private Logger logger=Logger.getLogger(ESHandler.class);
    /**
     * 获取es connection
     * @return
     * @throws Exception
     */
    private void openConnection() throws Exception {
        // 设置集群名称
        Settings settings = Settings.builder().put("cluster.name", "elasticsearch-application").build();
        // 创建client
        client = new PreBuiltTransportClient(settings).addTransportAddresses(new TransportAddress(InetAddress.getByName(HOST), PORT));
    }

    //初始化索引
    private boolean initIndex(ESIndexSetting esIndexSetting){
        boolean flag=false;
        try {
            openConnection();
            CreateIndexResponse createIndexResponse =client.admin().indices().prepareCreate(esIndexSetting.getIndexName()).execute().get();
            PutMappingResponse putMappingResponse =client.admin().indices().preparePutMapping(esIndexSetting.getIndexName()).setType(esIndexSetting.getTypeName()).setSource(esIndexSetting.toSettingJson(),XContentType.JSON).get();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return flag;
        }
    }
    //关闭连接
    private void closeConnection() {
        try {
           if(EmptyUtils.isNotEmpty(client)){
               client.close();
           }
       }catch (Exception e){
           e.printStackTrace();
       }
    }
    /***
     * 批量插入
     * @param esModules
     * @return
     */
    public List<String> addBatchDocument(List<IEsModule> esModules){
        List<String> failIds=null;
        try{
            openConnection();
            for (IEsModule esModule:esModules){
                String json=JSONObject.toJSONString(esModule);
                IndexResponse response=client.prepareIndex(esModule.getClass().getSimpleName().toLowerCase(),esModule.getClass().getName(),esModule.getEsId()).setSource(json, XContentType.JSON).get();
                if(response.status().getStatus()==200){
                    logger.info("insert id:"+esModule.getEsId()+" success!");
                }else{
                    if(EmptyUtils.isEmpty(failIds)){
                        failIds=new ArrayList<String>();
                    }
                    failIds.add(esModule.getEsId());
                    logger.info("insert id:"+esModule.getEsId()+" fail!");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeConnection();
            return failIds;
        }
    }


    /***
     * 添加Document
     * @param esModule
     * @throws Exception
     */
    public boolean addDocument(IEsModule esModule){
        boolean flag=false;
        try{
            openConnection();
           String json=JSONObject.toJSONString(esModule);
           logger.info(">>>>>>addDocument:"+json);
           IndexResponse response = client.prepareIndex(esModule.getClass().getSimpleName().toLowerCase(),esModule.getClass().getName(),esModule.getEsId()).setSource(json, XContentType.JSON).get();
           if(response.status().getStatus()==200){
               flag=true;
           }
       }catch (Exception e){
           e.printStackTrace();
       }finally {
           closeConnection();
           return flag;
       }
    }
    /***
     * 更新文档
     * @param t
     * @param index
     * @param id
     * @return
     * @throws Exception
     */
    public boolean updateDocument(IEsModule t,String index,String id){
        boolean flag=false;
        try{
            openConnection();
            String json=JSONObject.toJSONString(t);
            UpdateResponse response = client.prepareUpdate(index, t.getClass().getName(), id).setDoc(json, XContentType.JSON).get();
            if(response.status().getStatus()==200){
                flag=true;
            }
        }catch (Exception e){
            flag=false;
            e.printStackTrace();
        }finally {
            closeConnection();
            return flag;
        }
    }
    /***
     * 删除文档
     * @param index
     * @param type
     * @param id
     * @return
     * @throws Exception
     */
    public boolean deleteDocument(String index,String type,String id){
        boolean flag=false;
        try{
            openConnection();
            DeleteResponse response = client.prepareDelete(index, type, id).get();
            if(response.status().getStatus()==200){
                flag=true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeConnection();
            return flag;
        }
    }
    /***
     * 分页查询
     * @param dmEsQuery
     * @param tClass
     * @return
     */
    public Page<IEsModule>  queryPage(DmEsQuery dmEsQuery, Class<IEsModule> tClass){
        Page<IEsModule> page=new Page<IEsModule>();
        List<IEsModule> esModuleList=queryList(dmEsQuery,tClass);
        page.setCurPage(dmEsQuery.getPageNo());
//        page.setTotal();
        page.setRows(esModuleList);
        return null;
    }
    /***
     * 查询分页数据
     * @return
     * @throws Exception
     */
    public List<IEsModule> queryList(DmEsQuery dmEsQuery, Class<IEsModule> tClass){
        List<IEsModule> result=null;
        try {
            openConnection();
            if(EmptyUtils.isEmpty(dmEsQuery) || EmptyUtils.isEmpty(dmEsQuery.getClazz()) || EmptyUtils.isEmpty(dmEsQuery.getIndex()) ){
                logger.info(">>>>>>queryPage未设置查询条件>>>>>>>>>");
                return result;
            }
            SearchRequestBuilder searchRequestBuilder= client.prepareSearch(dmEsQuery.getIndex());
            searchRequestBuilder.setTypes(dmEsQuery.getClazz());
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
                    IEsModule t=JSONObject.parseObject(json,tClass);
                    result.add(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            closeConnection();
            return result;
        }
    }


    public static void main(String[] args) throws Exception {
        EsDmUser dmUser1=new EsDmUser();
        EsDmUser dmUser2=new EsDmUser();
        EsDmUser dmUser3=new EsDmUser();
        EsDmUser dmUser4=new EsDmUser();
        EsDmUser dmUser5=new EsDmUser();

        dmUser1.setId(new Long(1));
        dmUser1.setNickName("我是北京天安门");
        dmUser1.setRealName("我是鸟哥");

        dmUser2.setId(new Long(2));
        dmUser2.setNickName("我是西安");
        dmUser2.setRealName("我是菜歌");

        dmUser3.setId(new Long(3));
        dmUser3.setNickName("我是长安");
        dmUser3.setRealName("我是尼奥");

        dmUser4.setId(new Long(4));
        dmUser4.setNickName("我是张三");
        dmUser4.setRealName("我是喜马拉雅");

        dmUser5.setId(new Long(5));
        dmUser5.setNickName("我是李四");
        dmUser5.setRealName("我是大红门");

        ESHandler dmUserESHandler=new ESHandler();
        ESIndexSetting esIndexSetting=new ESIndexSetting(EsDmUser.class);

        esIndexSetting.addESProperties("realName",new PropertiesSetting(ESType.es_text, ESAnyanalyzerType.whitespace,true));
        esIndexSetting.addESProperties("nickName",new PropertiesSetting(ESType.es_text, ESAnyanalyzerType.ik_smart,true));
        dmUserESHandler.initIndex(esIndexSetting);

        dmUserESHandler.addDocument(dmUser1);

//        dmUserESHandler.addDocument(dmUser2,dmUser2.getId().toString());
//        dmUserESHandler.addDocument(dmUser3,dmUser3.getId().toString());
//        dmUserESHandler.addDocument(dmUser4,dmUser4.getId().toString());
//        dmUserESHandler.addDocument(dmUser5,dmUser5.getId().toString());
//
//        dmUser2.setNickName("lisi2");
//
//        boolean flag=dmUserESHandler.updateDocument(dmUser2,"dmuser",dmUser2.getId().toString());
//
//        boolean flag2=dmUserESHandler.deleteDocument("dmuser",DmUser.class.getName(),dmUser2.getId().toString());
//
//        System.out.println(flag2);
    }
}