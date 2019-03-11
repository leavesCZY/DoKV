package leavesc.hello.apt.model;

import android.support.annotation.NonNull;

import java.util.List;

import leavesc.hello.apt_annotation.Preferences;

/**
 * 作者：leavesC
 * 时间：2019/1/5 0:12
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public class User {

    @Preferences
    private String name;

    @Preferences
    private int age;

    @Preferences
    private String sex;

    @Preferences
    private Book book;

    @Preferences
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