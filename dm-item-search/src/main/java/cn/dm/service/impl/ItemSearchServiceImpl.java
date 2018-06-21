package cn.dm.service.impl;
import cn.dm.client.RestDmCinemaClient;
import cn.dm.client.RestDmImageClient;
import cn.dm.client.RestDmItemClient;
import cn.dm.common.Constants;
import cn.dm.common.EmptyUtils;
import cn.dm.common.EsUtils;
import cn.dm.common.Page;
import cn.dm.document.IESDocument;
import cn.dm.es.ItemEsQuery;
import cn.dm.es.ItemSearchVo;
import cn.dm.pojo.DmCinema;
import cn.dm.pojo.DmImage;
import cn.dm.pojo.DmItem;
import cn.dm.query.AbstractEsQuery;
import cn.dm.query.ItemQuery;
import cn.dm.service.ItemSearchService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class ItemSearchServiceImpl implements ItemSearchService {

    @Resource
    private RestDmItemClient restDmItemClient;
    @Resource
    private RestDmImageClient restDmImageClient;
    @Resource
    private RestDmCinemaClient restDmCinemaClient;

    //最后更新时间
    private Date lastUpdatedTime=null;

    @Override
    public Page<ItemSearchVo> queryItemList(ItemQuery itemQuery) throws Exception {
        EsUtils esUtils=new EsUtils();
        AbstractEsQuery abstractEsQuery=new ItemEsQuery();
        return esUtils.queryPage(abstractEsQuery);
    }

    /***
     * 导入item数据
     * @throws Exception
     */
    public void importItemList(Map<String,Object> params)throws Exception{
        EsUtils esUtils=new EsUtils();
        List<DmItem> dmItemList=restDmItemClient.getDmItemListByMap(params);
        List<IESDocument> itemSearchVoList=new ArrayList<IESDocument>();
        if(EmptyUtils.isNotEmpty(dmItemList)){
            for (DmItem dmItem:dmItemList){
                ItemSearchVo itemSearchVo=new ItemSearchVo();
                BeanUtils.copyProperties(dmItem,itemSearchVo);
                //更新图片、区域名称、地址
                List<DmImage> dmImages=restDmImageClient.queryDmImageList(dmItem.getId(), Constants.Image.ImageType.normal,Constants.Image.ImageCategory.item);
                itemSearchVo.setImgUrl(EmptyUtils.isEmpty(dmImages)?null:dmImages.get(0).getImgUrl());
                DmCinema dmCinema=restDmCinemaClient.getDmCinemaById(dmItem.getCinemaId());
                itemSearchVo.setAddress(dmCinema.getAddress());
                itemSearchVo.setAreaName(dmCinema.getAreaName());
                itemSearchVoList.add(itemSearchVo);
            }
        }
        esUtils.addBatchESModule(itemSearchVoList);
    }


}
