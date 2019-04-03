package leavesc.hello.dokv.model;

import android.support.annotation.NonNull;

import leavesc.hello.dokv.annotation.DoKV;

/**
 * 作者：leavesC
 * 时间：2019/4/3 13:41
 * 描述：
 */
@DoKV(key = "CustomKeyUser_Key")
public class CustomKeyUser {

    private String key;

    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return "CustomKeyUser{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

}
