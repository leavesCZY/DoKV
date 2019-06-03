package leavesc.hello.dokv_imp;

import android.content.Context;
import android.os.Parcelable;
import com.tencent.mmkv.MMKV;
import leavesc.hello.dokv.IDoKVHolder;

/**
 * 作者：leavesC
 * 时间：2019/1/5 1:05
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public class MMKVDoKVHolder implements IDoKVHolder<Parcelable> {

    private MMKV mmkv;

    public MMKVDoKVHolder(Context context) {
        MMKV.initialize(context);
        mmkv = MMKV.defaultMMKV();
    }

    @Override
    public <T extends Parcelable> String serialize(String key, T src) {
        mmkv.encode(key, src);
        return src.toString();
    }

    @Override
    public <T extends Parcelable> T deserialize(String key, Class<T> classOfT) {
        return mmkv.decodeParcelable(key, classOfT);
    }

    @Override
    public void remove(String key) {
        mmkv.remove(key);
    }

    @Override
    public void clear() {
        mmkv.clear();
    }

}