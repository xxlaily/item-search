package cn.dm.module;
import cn.dm.pojo.DmUser;
/***
 * ES 保存的用户信息
 */
public class EsDmUser extends DmUser implements IEsModule{

    @Override
    public String getEsId() {
        return this.getId()+"";
    }
}
