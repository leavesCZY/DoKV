package leavesc.hello.apt;

import android.app.Application;

import com.tencent.mmkv.MMKV;

import leavesc.hello.apt_annotation.DoKV;

/**
 * 作者：leavesC
 * 时间：2019/1/5 1:06
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MMKV.initialize(this);
//        PreferencesManager.getInstance().setPreferencesHolder(new PreferencesMMKVHolder());
        DoKV.init(new PreferencesMMKVHolder());
    }

}
