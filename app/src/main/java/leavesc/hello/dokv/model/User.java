package leavesc.hello.dokv.model;

import android.support.annotation.NonNull;

import java.util.List;

import leavesc.hello.dokv.annotation.DoKV;

/**
 * 作者：leavesC
 * 时间：2019/1/5 0:12
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
@DoKV
public class User {

    private String name;

    private int age;

    private String sex;

    private Book book;

    private List<String> stringList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", book=" + book +
                ", stringList=" + stringList +
                '}';
    }

}