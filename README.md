## DoKV

DoKV 是一个小巧而强大的 **Key-Value** 管理框架，其设计初衷是为了解决 Android 平台下各种**繁琐且丑陋**的配置类代码

### 一、介绍

之所以说小巧，是因为 DoKV 的实现仅依赖于**一个注解、一个接口、四个类**。当然，其实现基础不仅仅如此，还需要 APT 技术的支持，关于 APT 的知识我在以前的一篇博客中也有所介绍，点击查看：[APT](https://www.jianshu.com/p/cc8379522c5e) 

![](https://upload-images.jianshu.io/upload_images/2552605-c5119a7ba7544e72.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

之所以说强大，是因为通过使用 DoKV 后，你基本是可以抛弃如下类型的代码了

```java
 		SharedPreferences sharedPreferences = getSharedPreferences("SharedPreferencesName", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("IP", "192.168.0.1");
        editor.commit();
        String userName = sharedPreferences.getString("userName", "");
        String ip = sharedPreferences.getString("IP", "");
```

通常，我们的应用都会有很多配置项需要进行缓存，比如用户信息、设置项开关、服务器IP地址等。如果采用原生的 **SharedPreferences** 来实现的话，则很容易就写出如上所示那样丑陋的代码，不仅需要维护多个数据项的 key 值，而且每次存入和取出数据时都会有一大片重复的代码，不易维护

那 DoKV 的表现如何呢？

很简单

假设你的应用包含了一个 User 类用于缓存用户信息，首先，为该类加上一个注解：**@DoKV**

```java
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

	···

}
```

**build** 下工程，DoKV 就会自动为你生成一个以 **类名+DoKV结尾** 的类，之后你就可以通过以下这种形式来进行数据存取了，而你无需关心其内部是如何保存的

```java
        //缓存整个对象
        User user = new User();
        user.setAge(24);
        user.setName("leavesC");
        UserDoKV.get().setUser(user);

        //获取缓存的对象
        User user1 = UserDoKV.get().getUser();

        //更新本地已缓存的数据的某个设置项
        //如果之前没有缓存过，则会自动 new 一个对象并自动赋值
        //因为 DoKV 要求注解类必须包含一个无参构造函数，并且包含的字段有对应的 Get 和 Set 方法
        UserDoKV.get().setName("leavesCZY");
        UserDoKV.get().setAge(28);

        //移除缓存数据
        UserDoKV.get().remove();
```

### 二、引入

为了获得更高的自由度， DoKV 默认将数据持久化的实现方案交由外部来实现，即由使用者来决定如何将对象序列化保存到本地，此时你就可以选择只依赖以下两个引用

```groovy
dependencies {
    implementation 'leavesc.hello:dokv:0.1.6'
    annotationProcessor 'leavesc.hello:dokv-compiler:0.1.6'
}
```

然后，外部将 IDoKVHolder 实例传给 DoKV 即可

```java
	   DoKV.init(new IDoKVHolder() {

            //序列化
            @Override
            public String serialize(String key, Object src) {
                return null;
            }

            //反序列化
            @Override
            public <T> T deserialize(String key, Class<T> classOfT) {
                return null;
            }

            //移除指定对象
            @Override
            public void remove(String key) {

            }
        });
```

如果你不想自己实现 IDoKVHolder ，DoKV 也提供了一个默认实现，此时你就只要多引用如下一个依赖即可，其内部是通过 Gson + MMKV 来实现序列化方案

```java
dependencies {
    implementation 'leavesc.hello:dokv-impl:0.1.6'
}
```

进行初始化，之后就可以自由地玩耍了

```java
	DoKV.init(new MMKVDoKVHolder(Context));
```

### 三、结尾

本开源库的 GitHub 主页在这里：[DoKV](https://github.com/leavesC/DoKV)

APK 下载体检：[DoKV](https://www.pgyer.com/DoKV)

我的博客主页：[leavesC](https://www.jianshu.com/u/9df45b87cfdf)
