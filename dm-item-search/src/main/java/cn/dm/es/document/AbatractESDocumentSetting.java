package cn.dm.es.document;
import cn.dm.common.EmptyUtils;
import cn.dm.es.cloumn.EsPropertiesSetting;
import cn.dm.es.type.ESAnyanalyzerType;
import cn.dm.es.type.ESType;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class AbatractESDocumentSetting implements Serializable{
    /****
     * 属性值集合
     */
    private Map<String,EsPropertiesSetting> properties;
    /***
     * 添加属性到属性值集合
     * @param columnName
     * @return
     */
    public AbatractESDocumentSetting addESProperties(String columnName, ESType type, ESAnyanalyzerType esAnyanalyzerType, boolean isIndex){
        EsPropertiesSetting propertiesSetting=new EsPropertiesSetting(type,esAnyanalyzerType,isIndex);
        if(EmptyUtils.isEmpty(properties)){
            properties=new HashMap<String,EsPropertiesSetting>();
        }
        properties.put(columnName,propertiesSetting);
        return this;
    }

    public abstract void setProperties();

    public String toSettingJson(){
        setProperties();//设置属性
        Map<String,Object> typeMapping=new HashMap<String,Object>();
        typeMapping.put("properties",properties);
        String json= JSONObject.toJSONString(typeMapping);
        json=format(json);
        return json;
    }

    public static String format(String jsonStr) {
        int level = 0;
        StringBuffer jsonForMatStr = new StringBuffer();
        for(int i=0;i<jsonStr.length();i++){
            char c = jsonStr.charAt(i);
            if(level>0&&'\n'==jsonForMatStr.charAt(jsonForMatStr.length()-1)){
                jsonForMatStr.append(getLevelStr(level));
            }
            switch (c) {
                case '{':
                case '[':
                    jsonForMatStr.append(c+"\n");
                    level++;
                    break;
                case ',':
                    jsonForMatStr.append(c+"\n");
                    break;
                case '}':
                case ']':
                    jsonForMatStr.append("\n");
                    level--;
                    jsonForMatStr.append(getLevelStr(level));
                    jsonForMatStr.append(c);
                    break;
                default:
                    jsonForMatStr.append(c);
                    break;
            }
        }
        return jsonForMatStr.toString();
    }

    private static String getLevelStr(int level){
        StringBuffer levelStr = new StringBuffer();
        for(int levelI = 0;levelI<level ; levelI++){
            levelStr.append("    ");
        }
        return levelStr.toString();
    }

    public abstract String getIndexName();

    public abstract String getTypeName();

}
