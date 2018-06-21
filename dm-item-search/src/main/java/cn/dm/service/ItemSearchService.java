package cn.dm.service;

import cn.dm.common.Page;
import cn.dm.item.ItemSearchVo;
import cn.dm.query.ItemQuery;
import java.util.Map;

/***
 * 搜索的service类
 */
public interface ItemSearchService {

    public Page<ItemSearchVo> queryItemList(ItemQuery itemQuery)throws Exception;

    public void importItemList()throws Exception;
}
