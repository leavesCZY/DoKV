package leavesc.hello.dokv_annotation;

/**
 * 作者：leavesC
 * 时间：2019/1/5 1:01
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public interface IPreferencesHolder {

    //序列化
    String serialize(String key, Object src);

    //反序列化
    <T> T deserialize(String key, Class<T> classOfT);

    //移除指定对象
    void remove(String key);

}
