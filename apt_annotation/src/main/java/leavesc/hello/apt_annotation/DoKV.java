package leavesc.hello.apt_annotation;

/**
 * Created by：CZY
 * Time：2019/3/11 10:58
 * Desc：
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