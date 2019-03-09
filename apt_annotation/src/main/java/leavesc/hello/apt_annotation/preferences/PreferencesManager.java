package leavesc.hello.apt_annotation.preferences;

/**
 * 作者：leavesC
 * 时间：2019/1/5 1:01
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public enum PreferencesManager {

    INSTANCE;

    private IPreferencesHolder preferencesHolder;

    public static PreferencesManager getInstance() {
        return INSTANCE;
    }

    public void setPreferencesHolder(IPreferencesHolder preferencesHolder) {
        this.preferencesHolder = preferencesHolder;
    }

    public IPreferencesHolder getPreferencesHolder() {
        return preferencesHolder;
    }

}