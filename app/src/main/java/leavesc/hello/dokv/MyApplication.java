package leavesc.hello.dokv;

import android.app.Application;
import leavesc.hello.dokv_imp.MMKVDoKVHolder;

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
        DoKV.init(new MMKVDoKVHolder(this));
//        MMKV.initialize(this);
    }

}