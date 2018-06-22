package cn.dm.es.type;

public enum ESType{
    es_boolean("boolean"),
    es_date("date"),
    es_double("double"),
    es_integer("integer"),
    es_text("text");
    private String code;

    private ESType(String code) {
        this.code=code;
    }
    public String getCode(){
        return this.code;
    }
}
