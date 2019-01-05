package leavesc.hello.apt;

import android.app.Activity;

import java.lang.reflect.Method;

/**
 * 作者：leavesC
 * 时间：2019/1/3 14:34
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public class ButterKnife {

    public static void bind(Activity activity) {
        Class clazz = activity.getClass();
        try {
            Class bindViewClass = Class.forName(clazz.getName() + "ViewBinding");
            Method method = bindViewClass.getMethod("bind", activity.getClass());
            method.invoke(bindViewClass.newInstance(), activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
