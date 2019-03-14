package leavesc.hello.dokv.model;

import android.support.annotation.NonNull;

/**
 * 作者：leavesC
 * 时间：2019/1/5 15:11
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public class Book {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return "Book{" +
                "name='" + name + '\'' +
                '}';
    }

}