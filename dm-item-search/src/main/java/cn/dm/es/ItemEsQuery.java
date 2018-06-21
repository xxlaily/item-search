package cn.dm.es;
import cn.dm.query.AbstractEsQuery;

public class ItemEsQuery extends AbstractEsQuery {

    @Override
    public String getIndexName() {
        return "dm";
    }

    @Override
    public String getTypeName() {
        return "item";
    }

    @Override
    public Class getModuleClass() {
        return ItemSearchVo.class;
    }
}
