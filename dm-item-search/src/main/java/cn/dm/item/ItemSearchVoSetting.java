package cn.dm.item;
import cn.dm.es.document.AbatractESDocumentSetting;
import cn.dm.es.type.ESAnyanalyzerType;
import cn.dm.es.type.ESType;

public class ItemSearchVoSetting extends AbatractESDocumentSetting {

    @Override
    public void setProperties() {
        this.addESProperties("id", ESType.es_integer, null,true);
        this.addESProperties("itemName",ESType.es_text, ESAnyanalyzerType.ik_smart,true);
        this.addESProperties("abstractMessage",ESType.es_text, ESAnyanalyzerType.ik_smart,true);
        this.addESProperties("address",ESType.es_text, ESAnyanalyzerType.ik_smart,true);
        this.addESProperties("itemTypeId1",ESType.es_integer, null,true);
        this.addESProperties("itemTypeId2",ESType.es_integer, null,true);
        this.addESProperties("areaName",ESType.es_text, ESAnyanalyzerType.whitespace,true);
        this.addESProperties("areaId",ESType.es_text, null,true);
        this.addESProperties("startTime",ESType.es_text, ESAnyanalyzerType.whitespace,true);
        this.addESProperties("endTime",ESType.es_text, ESAnyanalyzerType.whitespace,true);
        this.addESProperties("createdTimeLong",ESType.es_integer, ESAnyanalyzerType.whitespace,true);
        this.addESProperties("endTimeLong",ESType.es_integer, ESAnyanalyzerType.whitespace,true);
        this.addESProperties("endTimeLong",ESType.es_integer, ESAnyanalyzerType.whitespace,true);
    }

    @Override
    public String getIndexName() {
        return "dm";
    }

    @Override
    public String getTypeName() {
        return "item";
    }


}
