package cn.dm.es.document;

import com.fasterxml.jackson.annotation.JsonIgnore;

/***
 * ES的基础文档接口
 */
public interface IESDocument {

    public String getEsId();

    public AbatractESDocumentSetting getSetting();
}
