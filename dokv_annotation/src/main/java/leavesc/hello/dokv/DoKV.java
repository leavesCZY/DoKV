package leavesc.hello.dokv;

/**
 * 作者：leavesC
 * 时间：2019/3/11 10:58
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public enum DoKV {

    INSTANCE;

    private IDoKVHolder doKVHolder;

    public static DoKV getInstance() {
        return INSTANCE;
    }

    public static void init(IDoKVHolder doKVHolder) {
        getInstance().doKVHolder = doKVHolder;
    }

    public static void clear() {
        getInstance().doKVHolder.clear();
    }

    public IDoKVHolder getDoKVHolder() {
        return doKVHolder;
    }

}