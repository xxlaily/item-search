package cn.dm.controller;
import cn.dm.common.Dto;
import cn.dm.common.DtoUtil;
import cn.dm.common.Page;
import cn.dm.es.query.ItemQuery;
import cn.dm.item.ItemSearchVo;
import cn.dm.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/p/list")
public class ItemListController {

    @Autowired
    private ItemSearchService itemSearchService;

    @RequestMapping("/queryItemList")
    public Dto<Page<ItemSearchVo>> queryItemList(@RequestBody ItemQuery itemQuery) throws Exception {
       Page<ItemSearchVo> searchVoList=itemSearchService.queryItemList(itemQuery);
       return DtoUtil.returnDataSuccess(searchVoList);
    }

    @RequestMapping("/importItemList")
    public Dto<Page<ItemSearchVo>> importItemList() throws Exception {
        itemSearchService.importItemList();
        return DtoUtil.returnDataSuccess(1);
    }
}
