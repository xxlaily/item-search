package cn.dm.service;

import cn.dm.common.Page;
import cn.dm.es.ItemSearchVo;
import cn.dm.query.ItemQuery;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/***
 * 搜索的service类
 */
public interface ItemSearchService {

    public Page<ItemSearchVo> queryItemList(ItemQuery itemQuery)throws Exception;

    public void importItemList(Map<String,Object> params)throws Exception;
}
