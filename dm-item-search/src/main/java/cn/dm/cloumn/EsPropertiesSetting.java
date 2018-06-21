package cn.dm.cloumn;

import cn.dm.type.ESAnyanalyzerType;
import cn.dm.type.ESType;

import java.io.Serializable;

public class EsPropertiesSetting implements Serializable{
    private String type;
    private String analyzer;
    private String index;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public EsPropertiesSetting(ESType type, ESAnyanalyzerType analyzer, boolean index) {
        this.type = type.getCode();
        this.analyzer = analyzer.getCode();
        this.index = index+"";
    }
}
