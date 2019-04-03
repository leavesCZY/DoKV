package leavesc.hello.dokv_imp;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.tencent.mmkv.MMKV;

import leavesc.hello.dokv.IDoKVHolder;

/**
 * 作者：leavesC
 * 时间：2019/1/5 1:05
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public class MMKVDoKVHolder implements IDoKVHolder {

    private MMKV mmkv;

    private Gson gson;

    public MMKVDoKVHolder(Context context) {
        MMKV.initialize(context);
        mmkv = MMKV.defaultMMKV();
        gson = new Gson();
    }

    @Override
    public String serialize(String key, Object src) {
        String json = gson.toJson(src);
        mmkv.putString(key, json);
        return json;
    }

    @Override
    public <T> T deserialize(String key, Class<T> classOfT) {
        String json = mmkv.decodeString(key, "");
        if (!TextUtils.isEmpty(json)) {
            return gson.fromJson(json, classOfT);
        }
        return null;
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