package cn.dm.setting;
import cn.dm.common.EmptyUtils;
import cn.dm.module.EsDmUser;
import cn.dm.module.IEsModule;
import cn.dm.pojo.DmUser;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.*;

/***
 * ES 索引设置文件
 */
public class ESIndexSetting implements Serializable{

    //设置类型
    private Class clazz;

    //字段设置
    private Map<String,PropertiesSetting> properties;

    public ESIndexSetting(Class clazz){
        this.clazz=clazz;
    }

    public String getIndexName() {
        return clazz.getSimpleName().toLowerCase();
    }

    public String getTypeName() {
        return clazz.getName();
    }



    public ESIndexSetting addESProperties(String name,PropertiesSetting propertiesSetting){
        if(EmptyUtils.isEmpty(properties)){
            properties=new HashMap<String,PropertiesSetting>();
        }
        properties.put(name,propertiesSetting);
        return this;
    }

    //转化为json
    public String toSettingJson(){
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

    public static void main(String[] args) {
        ESIndexSetting esIndexSetting=new ESIndexSetting(IEsModule.class);
        esIndexSetting.addESProperties("name",new PropertiesSetting(ESType.es_text,ESAnyanalyzerType.whitespace,true));
        esIndexSetting.addESProperties("age",new PropertiesSetting(ESType.es_text,ESAnyanalyzerType.whitespace,true));
        esIndexSetting.addESProperties("desc",new PropertiesSetting(ESType.es_text,ESAnyanalyzerType.whitespace,true));
        esIndexSetting.addESProperties("name",new PropertiesSetting(ESType.es_text,ESAnyanalyzerType.whitespace,true));
        System.out.println(esIndexSetting.toSettingJson());
        System.out.println("\"tweet\": {\n" +
                "  \"properties\": {\n" +
                "    \"message\": {\n" +
                "      \"type\": \"text\"\n" +
                "    }\n" +
                "  }\n" +
                "}");
    }
}
