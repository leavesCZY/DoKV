package leavesc.hello.dokv.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import leavesc.hello.dokv.annotation.DoKV;

/**
 * 作者：leavesC
 * 时间：2019/4/3 13:41
 * 描述：
 */
@DoKV(key = "CustomKeyUser_Key")
public class CustomKeyUser implements Parcelable {

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.key);
        dest.writeString(this.value);
    }

    public CustomKeyUser() {
    }

    protected CustomKeyUser(Parcel in) {
        this.key = in.readString();
        this.value = in.readString();
    }

    public static final Parcelable.Creator<CustomKeyUser> CREATOR = new Parcelable.Creator<CustomKeyUser>() {
        @Override
        public CustomKeyUser createFromParcel(Parcel source) {
            return new CustomKeyUser(source);
        }

        @Override
        public CustomKeyUser[] newArray(int size) {
            return new CustomKeyUser[size];
        }
    };
}
