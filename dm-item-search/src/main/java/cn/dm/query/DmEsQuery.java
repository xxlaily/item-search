package cn.dm.query;

import cn.dm.common.EmptyUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/***
 * 用于设置ES查询的类
 */
public abstract class DmEsQuery implements Serializable{

    private Integer pageNo;

    private Integer pageSize;

    private String clazz;

    private String index;

    private String asc;

    private String desc;

    private String keyword;

    //精确匹配
    private Map<String,Object> matchParams;

    //模糊匹配
    private Map<String,Object> likeMatchParams;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getAsc() {
        return asc;
    }

    public void setAsc(String asc) {
        this.asc = asc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    //设置精确匹配参数
    public void setMatchParams(String key,Object value){
        if(EmptyUtils.isEmpty(matchParams)){
            matchParams=new HashMap<String, Object>();
        }
        matchParams.put(key,value);
    }

    //设置模糊匹配参数
    public void setLikeMatchParams(String key,Object value){
        if(EmptyUtils.isEmpty(likeMatchParams)){
            likeMatchParams=new HashMap<String, Object>();
        }
        likeMatchParams.put(key,value);
    }

    public Map<String, Object> getMatchParams() {
        return matchParams;
    }

    public Map<String, Object> getLikeMatchParams() {
        return likeMatchParams;
    }
}
