package cn.dm.es.query;

import java.io.Serializable;

/***
 * 查詢商品的实体类
 */
public class ItemQuery implements Serializable{
    private Long itemTypeId1;
    private Long itemTypeId2;
    private Long areaId;
    private String startTime;
    private String endTime;
    private String sort;
    private String keyword;
    private Integer currentPage;
    private Integer pageSize;

    public Long getItemTypeId1() {
        return itemTypeId1;
    }

    public void setItemTypeId1(Long itemTypeId1) {
        this.itemTypeId1 = itemTypeId1;
    }

    public Long getItemTypeId2() {
        return itemTypeId2;
    }

    public void setItemTypeId2(Long itemTypeId2) {
        this.itemTypeId2 = itemTypeId2;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
