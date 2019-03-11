package leavesc.hello.apt_annotation;

/**
 * 作者：leavesC
 * 时间：2019/3/11 10:58
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public enum DoKV {

    INSTANCE;

    private IPreferencesHolder preferencesHolder;

    public static DoKV getInstance() {
        return INSTANCE;
    }

    public static void init(IPreferencesHolder preferencesHolder) {
        getInstance().preferencesHolder = preferencesHolder;
    }

    public IPreferencesHolder getPreferencesHolder() {
        return preferencesHolder;
    }

}