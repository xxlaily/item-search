package cn.dm.module;

/***
 * 抽象的BaseEsModule 需要使用ES 保存的数据，都需集成这个类
 */
public interface IEsModule {
    /***
     * 返回ES id
     * @return
     */
    public abstract String getEsId();
}
