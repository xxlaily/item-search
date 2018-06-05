package cn.dm.setting;

public class PropertiesSetting {
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

    public PropertiesSetting(ESType type, ESAnyanalyzerType analyzer, boolean index) {
        this.type = type.getCode();
        this.analyzer = analyzer.getCode();
        this.index = index+"";
    }
}
