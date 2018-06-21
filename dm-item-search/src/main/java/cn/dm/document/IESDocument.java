package cn.dm.document;

/***
 * ES的基础文档接口
 */
public interface IESDocument {

    public String getEsId();

    public AbatractESDocumentSetting getSetting();
}
