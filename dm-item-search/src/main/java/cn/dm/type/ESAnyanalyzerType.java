package cn.dm.type;

/***
 * ES 解析类型
 */
public enum ESAnyanalyzerType {
    ik_smart("ik_smart"),
    whitespace("whitespace");

    private String code;
    private ESAnyanalyzerType(String code) {
        this.code=code;
    }
    public String getCode(){
        return this.code;
    }
}
