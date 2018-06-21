package cn.dm.es;

import cn.dm.document.AbatractESDocumentSetting;
import cn.dm.type.ESAnyanalyzerType;
import cn.dm.type.ESType;

import javax.swing.text.AbstractDocument;

public class ItemSearchVoSetting extends AbatractESDocumentSetting {

    @Override
    public void setProperties() {
        this.addESProperties("id", ESType.es_text, ESAnyanalyzerType.whitespace,true);
        this.addESProperties("itemName",ESType.es_text, ESAnyanalyzerType.ik_smart,true);
        this.addESProperties("abstractMessage",ESType.es_text, ESAnyanalyzerType.ik_smart,true);
        this.addESProperties("address",ESType.es_text, ESAnyanalyzerType.ik_smart,true);
        this.addESProperties("areaName",ESType.es_text, ESAnyanalyzerType.whitespace,true);
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
