# Android_APT

APT(**Annotation Processing Tool**) 即注解处理器，是一种注解处理工具，用来在编译期扫描和处理注解，通过注解来生成 Java 文件。即以注解作为桥梁，通过预先规定好的代码生成规则来自动生成 Java 文件。此类注解框架的代表有 **ButterKnife、Dragger2、EventBus** 等

Java API 已经提供了扫描源码并解析注解的框架，开发者可以通过继承 **AbstractProcessor** 类来实现自己的注解解析逻辑。APT 的原理就是在注解了某些代码元素（如字段、函数、类等）后，在编译时编译器会检查 **AbstractProcessor** 的子类，并且自动调用其 **process()** 方法，然后将添加了指定注解的所有代码元素作为参数传递给该方法，开发者再根据注解元素在编译期输出对应的 Java 代码

### 一、实现一个轻量的 “ButterKnife”
这里以 **ButterKnife** 为实现目标，在讲解 **Android APT** 的内容的同时，逐步实现一个轻量的**控件绑定框架**，即通过注解来自动生成如下所示的 **findViewById()** 代码
```
package hello.leavesc.apt;

public class MainActivityViewBinding {
    public static void bind(MainActivity _mainActivity) {
        _mainActivity.btn_serializeSingle = (android.widget.Button) (_mainActivity.findViewById(2131165221));
        _mainActivity.tv_hint = (android.widget.TextView) (_mainActivity.findViewById(2131165333));
        _mainActivity.btn_serializeAll = (android.widget.Button) (_mainActivity.findViewById(2131165220));
        _mainActivity.btn_remove = (android.widget.Button) (_mainActivity.findViewById(2131165219));
        _mainActivity.btn_print = (android.widget.Button) (_mainActivity.findViewById(2131165218));
        _mainActivity.et_userName = (android.widget.EditText) (_mainActivity.findViewById(2131165246));
        _mainActivity.et_userAge = (android.widget.EditText) (_mainActivity.findViewById(2131165245));
        _mainActivity.et_singleUserName = (android.widget.EditText) (_mainActivity.findViewById(2131165244));
        _mainActivity.et_bookName = (android.widget.EditText) (_mainActivity.findViewById(2131165243));
    }
}
```
控件绑定的方式如下所示
```
    @BindView(R.id.et_userName)
    EditText et_userName;

    @BindView(R.id.et_userAge)
    EditText et_userAge;

    @BindView(R.id.et_bookName)
    EditText et_bookName;
```
#### 1.1、建立 Module
首先在工程中新建一个 **Java Library**，命名为 **apt_processor**，用于存放 **AbstractProcessor** 的实现类。再新建一个 **Java Library**，命名为 **apt_annotation** ，用于存放各类注解

当中，**apt_processor** 需要导入如下依赖
```
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.google.auto.service:auto-service:1.0-rc2'
    implementation 'com.squareup:javapoet:1.10.0'
    implementation project(':apt_annotation')
}
```
当中，**JavaPoet** 是 square 开源的 Java 代码生成框架，可以很方便地通过其提供的 API 来生成指定格式（修饰符、返回值、参数、函数体等）的代码。**auto-service** 是由 Google 开源的注解注册处理器

实际上，上面两个依赖库并不是必须的，可以通过硬编码代码生成规则来替代，但还是建议使用这两个库，因为这样代码的可读性会更高，且能提高开发效率

**app Module** 需要依赖这两个 Java Library
```
    implementation project(':apt_annotation')
    annotationProcessor project(':apt_processor')
```
这样子，我们需要的所有基础依赖关系就搭建好了

![](https://upload-images.jianshu.io/upload_images/2552605-96736229e277435a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 1.2、编写代码生成规则


首先观察自动生成的代码，可以归纳出几点需要实现的地方：

1、文件和源 Activity 处在同个包名下

2、类名以 **Activity名 + ViewBinding** 组成

3、**bind()** 方法通过传入 Activity 对象来获取其声明的控件对象来对其进行实例化，这也是 ButterKnife 要求需要绑定的控件变量不能声明为 **private** 的原因
```
package hello.leavesc.apt;

public class MainActivityViewBinding {
    public static void bind(MainActivity _mainActivity) {
        _mainActivity.btn_serializeSingle = (android.widget.Button) (_mainActivity.findViewById(2131165221));
        _mainActivity.tv_hint = (android.widget.TextView) (_mainActivity.findViewById(2131165333));
        ...
    }
}
```

在 **apt_processor** Module 中创建 **BindViewProcessor** 类并继承 **AbstractProcessor** 抽象类，该抽象类含有一个抽象方法 **process()** 以及一个非抽象方法 **getSupportedAnnotationTypes()** 需要由我们来实现
```
/**
 * 作者：leavesC
 * 时间：2019/1/3 14:32
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> hashSet = new HashSet<>();
        hashSet.add(BindView.class.getCanonicalName());
        return hashSet;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        return false;
    }

}
```
**getSupportedAnnotationTypes()** 方法用于指定该 **AbstractProcessor** 的目标注解对象，**process()** 方法则用于处理包含指定注解对象的代码元素

**BindView** 注解的声明如下所示，放在 **apt_annotation** 中，注解值 **value** 用于声明 **viewId**
```
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface BindView {
    int value();
}
```

要自动生成 **findViewById()** 方法，则需要获取到**控件变量的引用**以及对应的 **viewid**，所以需要先遍历出每个 **Activity** 包含的所有注解对象

```
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //获取所有包含 BindView 注解的元素
        Set<? extends Element> elementSet = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        Map<TypeElement, Map<Integer, VariableElement>> typeElementMapHashMap = new HashMap<>();
        for (Element element : elementSet) {
            //因为 BindView 的作用对象是 FIELD，因此 element 可以直接转化为 VariableElement
            VariableElement variableElement = (VariableElement) element;
            //getEnclosingElement 方法返回封装此 Element 的最里层元素
            //如果 Element 直接封装在另一个元素的声明中，则返回该封装元素
            //此处表示的即 Activity 类对象
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            Map<Integer, VariableElement> variableElementMap = typeElementMapHashMap.get(typeElement);
            if (variableElementMap == null) {
                variableElementMap = new HashMap<>();
                typeElementMapHashMap.put(typeElement, variableElementMap);
            }
            //获取注解值，即 ViewId
            BindView bindAnnotation = variableElement.getAnnotation(BindView.class);
            int viewId = bindAnnotation.value();
            //将每个包含了 BindView 注解的字段对象以及其注解值保存起来
            variableElementMap.put(viewId, variableElement);
        }
        ...
        return true;
    }
```
当中，**Element** 用于代表程序的一个元素，这个元素可以是：包、类、接口、变量、方法等多种概念。这里以 Activity 对象作为 Key ，通过 map 来存储不同 Activity 下的所有注解对象

获取到所有的注解对象后，就可以来构造 **bind()** 方法了

**MethodSpec** 是 **JavaPoet** 提供的一个概念，用于抽象出生成一个**函数**时需要的**基础元素**，直接看以下方法应该就可以很容易理解其含义了

通过 **addCode()** 方法把需要的参数元素填充进去，循环生成每一行 **findView** 方法
```
    /**
     * 生成方法
     *
     * @param typeElement        注解对象上层元素对象，即 Activity 对象
     * @param variableElementMap Activity 包含的注解对象以及注解的目标对象
     * @return
     */
    private MethodSpec generateMethodByPoet(TypeElement typeElement, Map<Integer, VariableElement> variableElementMap) {
        ClassName className = ClassName.bestGuess(typeElement.getQualifiedName().toString());
        //方法参数名
        String parameter = "_" + StringUtils.toLowerCaseFirstChar(className.simpleName());
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(className, parameter);
        for (int viewId : variableElementMap.keySet()) {
            VariableElement element = variableElementMap.get(viewId);
            //被注解的字段名
            String name = element.getSimpleName().toString();
            //被注解的字段的对象类型的全名称
            String type = element.asType().toString();
            String text = "{0}.{1}=({2})({3}.findViewById({4}));";
            methodBuilder.addCode(MessageFormat.format(text, parameter, name, type, parameter, String.valueOf(viewId)));
        }
        return methodBuilder.build();
    }
```
完整的代码声明如下所示
```
/**
 * 作者：leavesC
 * 时间：2019/1/3 14:32
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> hashSet = new HashSet<>();
        hashSet.add(BindView.class.getCanonicalName());
        return hashSet;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //获取所有包含 BindView 注解的元素
        Set<? extends Element> elementSet = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        Map<TypeElement, Map<Integer, VariableElement>> typeElementMapHashMap = new HashMap<>();
        for (Element element : elementSet) {
            //因为 BindView 的作用对象是 FIELD，因此 element 可以直接转化为 VariableElement
            VariableElement variableElement = (VariableElement) element;
            //getEnclosingElement 方法返回封装此 Element 的最里层元素
            //如果 Element 直接封装在另一个元素的声明中，则返回该封装元素
            //此处表示的即 Activity 类对象
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            Map<Integer, VariableElement> variableElementMap = typeElementMapHashMap.get(typeElement);
            if (variableElementMap == null) {
                variableElementMap = new HashMap<>();
                typeElementMapHashMap.put(typeElement, variableElementMap);
            }
            //获取注解值，即 ViewId
            BindView bindAnnotation = variableElement.getAnnotation(BindView.class);
            int viewId = bindAnnotation.value();
            //将每个包含了 BindView 注解的字段对象以及其注解值保存起来
            variableElementMap.put(viewId, variableElement);
        }
        for (TypeElement key : typeElementMapHashMap.keySet()) {
            Map<Integer, VariableElement> elementMap = typeElementMapHashMap.get(key);
            String packageName = ElementUtils.getPackageName(elementUtils, key);
            JavaFile javaFile = JavaFile.builder(packageName, generateCodeByPoet(key, elementMap)).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 生成 Java 类
     *
     * @param typeElement        注解对象上层元素对象，即 Activity 对象
     * @param variableElementMap Activity 包含的注解对象以及注解的目标对象
     * @return
     */
    private TypeSpec generateCodeByPoet(TypeElement typeElement, Map<Integer, VariableElement> variableElementMap) {
        //自动生成的文件以 Activity名 + ViewBinding 进行命名
        return TypeSpec.classBuilder(ElementUtils.getEnclosingClassName(typeElement) + "ViewBinding")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(generateMethodByPoet(typeElement, variableElementMap))
                .build();
    }

    /**
     * 生成方法
     *
     * @param typeElement        注解对象上层元素对象，即 Activity 对象
     * @param variableElementMap Activity 包含的注解对象以及注解的目标对象
     * @return
     */
    private MethodSpec generateMethodByPoet(TypeElement typeElement, Map<Integer, VariableElement> variableElementMap) {
        ClassName className = ClassName.bestGuess(typeElement.getQualifiedName().toString());
        //方法参数名
        String parameter = "_" + StringUtils.toLowerCaseFirstChar(className.simpleName());
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(className, parameter);
        for (int viewId : variableElementMap.keySet()) {
            VariableElement element = variableElementMap.get(viewId);
            //被注解的字段名
            String name = element.getSimpleName().toString();
            //被注解的字段的对象类型的全名称
            String type = element.asType().toString();
            String text = "{0}.{1}=({2})({3}.findViewById({4}));";
            methodBuilder.addCode(MessageFormat.format(text, parameter, name, type, parameter, String.valueOf(viewId)));
        }
        return methodBuilder.build();
    }

}
```

### 1.3、注解绑定效果

首先在 **MainActivity** 中声明两个 **BindView** 注解，然后 **Rebuild Project**，使编译器根据 **BindViewProcessor** 生成我们需要的代码

```
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_hint)
    TextView tv_hint;

    @BindView(R.id.btn_hint)
    Button btn_hint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}
```
**rebuild** 结束后，就可以在 **generatedJava** 文件夹下看到 **MainActivityViewBinding** 类自动生成了


![](https://upload-images.jianshu.io/upload_images/2552605-df85b12ec6a2b6d3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

此时有两种方式可以用来触发 **bind()** 方法
1. 在 **MainActivity** 方法中直接调用 **MainActivityViewBinding** 的 **bind()** 方法
2. 因为 **MainActivityViewBinding** 的包名路径和 **Activity** 是相同的，所以也可以通过反射来触发 **MainActivityViewBinding** 的 **bind()** 方法
```
/**
 * 作者：leavesC
 * 时间：2019/1/3 14:34
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public class ButterKnife {

    public static void bind(Activity activity) {
        Class clazz = activity.getClass();
        try {
            Class bindViewClass = Class.forName(clazz.getName() + "ViewBinding");
            Method method = bindViewClass.getMethod("bind", activity.getClass());
            method.invoke(bindViewClass.newInstance(), activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
```
两种方式各有优缺点。第一种方式在每次 **build project** 后才会生成代码，在这之前无法引用到对应的 **ViewBinding** 类。第二种方式可以用固定的方法调用方式，但是相比方式一，反射会略微多消耗一些性能

但这两种方式的运行结果是完全相同的


```
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivityViewBinding.bind(this);
        tv_hint.setText("leavesC Success");
        btn_hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Hello", Toast.LENGTH_SHORT).show();
            }
        });
    }
```


![](https://upload-images.jianshu.io/upload_images/2552605-927c625d0111f8ba.gif?imageMogr2/auto-orient/strip)

### 二、对象 持久化+序列化+反序列化 框架
通过第一节的内容，读者应该了解到了 APT 其强大的功能了 。这一节再来实现一个可以方便地将 **对象进行持久化+序列化+反序列** 的框架
#### 2.1、确定目标

通常，我们的应用都会有很多配置项需要进行缓存，比如用户信息、设置项开关、服务器IP地址等。如果采用原生的 **SharedPreferences** 来实现的话，则很容易就写出如下丑陋的代码，不仅需要维护多个数据项的 key 值，而且每次存入和取出数据时都会有一大片重复的代码，不易维护

```
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPreferencesName", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("IP", "192.168.0.1");
        editor.commit();
        String userName = sharedPreferences.getString("userName", "");
        String ip = sharedPreferences.getString("IP", "");
```

因此，这里就来通过 APT 来实现一个可以方便地对数据进行 **持久化+序列化+反序列化** 的框架，具体的目标有以下几点：

1、可以将 Object 进行序列化，并且提供反序列化为 Object 的方法

2、Object 的序列化结果可以持久化保存到本地

3、持久化数据时需要的唯一 key 值由框架内部自动进行维护

4、序列化、反序列化、持久化的具体过程由框架外部实现，框架只负责搭建操作逻辑



目标1可以通过 **Gson** 来实现，目标2则可以通过使用腾讯开源的 **MMKV** 框架来实现，需要导入以下两个依赖
```
    implementation 'com.google.code.gson:gson:2.8.5'
    implementation 'com.tencent:mmkv:1.0.16'
```
#### 2.2、效果预览

这里先预先看下框架的使用方式。新的注解以 **Preferences** 命名，假设 **User** 类中有三个字段值需要进行本地缓存，因此都为其加上 **Preferences** 注解
```
public class User {

    @Preferences
    private String name;

    @Preferences
    private int age;

    @Preferences
    private Book book;

    ...

}
```
而我们要做的，就是通过 APT 自动为 **User** 类来生成一个 **UserPreferences** 子类，之后的数据缓存操作都是通过 **UserPreferences** 来进行

缓存整个对象
```
    User user = new User();
    UserPreferences.get().setUser(user);
```
缓存单个属性值
```
    String userName = et_singleUserName.getText().toString();
    UserPreferences.get().setName(userName);
```
获取缓存的对象
```
    User user = UserPreferences.get().getUser();
```
移除缓存的对象
```
    UserPreferences.get().remove();
```

可以看到，整个操作都是十分的简洁，之后就来开工吧

#### 2.3、实现操作接口
为了实现目标4，需要先定义好操作接口，并由外部传入具体的实现
```
public interface IPreferencesHolder {

    //序列化
    String serialize(String key, Object src);

    //反序列化
    <T> T deserialize(String key, Class<T> classOfT);

    //移除指定对象
    void remove(String key);

}
```
以上三个操作对于框架内部来说应该是唯一的，因此可以通过单例模式来全局维护。APT 生成的代码就通过此入口来调用 **持久化+序列化+反序列化** 方法
```
public class PreferencesManager {

    private IPreferencesHolder preferencesHolder;

    private PreferencesManager() {
    }

    public static PreferencesManager getInstance() {
        return PreferencesManagerHolder.INSTANCE;
    }

    private static class PreferencesManagerHolder {
        private static PreferencesManager INSTANCE = new PreferencesManager();
    }

    public void setPreferencesHolder(IPreferencesHolder preferencesHolder) {
        this.preferencesHolder = preferencesHolder;
    }

    public IPreferencesHolder getPreferencesHolder() {
        return preferencesHolder;
    }

}
```
在 **Application** 的 **onCreate()** 方法中传入具体的实现
```
 PreferencesManager.getInstance().setPreferencesHolder(new PreferencesMMKVHolder());
```
```
public class PreferencesMMKVHolder implements IPreferencesHolder {

    @Override
    public String serialize(String key, Object src) {
        String json = new Gson().toJson(src);
        MMKV kv = MMKV.defaultMMKV();
        kv.putString(key, json);
        return json;
    }

    @Override
    public <T> T deserialize(String key, Class<T> classOfT) {
        MMKV kv = MMKV.defaultMMKV();
        String json = kv.decodeString(key, "");
        if (!TextUtils.isEmpty(json)) {
            return new Gson().fromJson(json, classOfT);
        }
        return null;
    }

    @Override
    public void remove(String key) {
        MMKV kv = MMKV.defaultMMKV();
        kv.remove(key);
    }

}
```
#### 2.4、编写代码生成规则
一样是需要继承 **AbstractProcessor** 类，子类命名为 **PreferencesProcessor**

首先，**PreferencesProcessor** 类需要生成一个序列化整个对象的方法。例如，需要为 **User** 类生成一个子类 **UserPreferences** ，**UserPreferences** 包含一个 **setUser(User instance)** 方法

```
    public String setUser(User instance) {
        if (instance == null) {
            PreferencesManager.getInstance().getPreferencesHolder().remove(KEY);
            return "";
        }
        return PreferencesManager.getInstance().getPreferencesHolder().serialize(KEY, instance);
    }
```
对应的方法生成规则如下所示。可以看出来，大体规则还是和第一节类似，一样是需要通过字符串来拼接出完整的代码。当中，**$L、$T** 都是**替代符**，作用类似于 **MessageFormat**
```
   /**
     * 构造用于序列化整个对象的方法
     *
     * @param typeElement 注解对象上层元素对象，即 Java 对象
     * @return
     */
    private MethodSpec generateSetInstanceMethod(TypeElement typeElement) {
        //顶层类类名
        String enclosingClassName = ElementUtils.getEnclosingClassName(typeElement);
        //方法名
        String methodName = "set" + StringUtils.toUpperCaseFirstChar(enclosingClassName);
        //方法参数名
        String fieldName = "instance";
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addParameter(ClassName.get(typeElement.asType()), fieldName);
        builder.addStatement("if ($L == null) { $T.getInstance().getPreferencesHolder().remove(KEY); return \"\"; }", fieldName, serializeManagerClass);
        builder.addStatement("return $T.getInstance().getPreferencesHolder().serialize(KEY, $L)", serializeManagerClass, fieldName);
        return builder.build();
    }
```

此外，还需要一个用于反序列化本地缓存的数据的方法
```
    public User getUser() {
        return PreferencesManager.getInstance().getPreferencesHolder().deserialize(KEY, User.class);
    }
```
对应的方法生成规则如下所示
```
    /**
     * 构造用于获取整个序列化对象的方法
     *
     * @param typeElement 注解对象上层元素对象，即 Java 对象
     * @return
     */
    private MethodSpec generateGetInstanceMethod(TypeElement typeElement) {
        //顶层类类名
        String enclosingClassName = ElementUtils.getEnclosingClassName(typeElement);
        //方法名
        String methodName = "get" + StringUtils.toUpperCaseFirstChar(enclosingClassName);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(typeElement.asType()));
        builder.addStatement("return $T.getInstance().getPreferencesHolder().deserialize(KEY, $L.class)", serializeManagerClass, enclosingClassName);
        return builder.build();
    }
```

为了实现目标三（持久化数据时需要的唯一 key 值由框架内部自动进行维护），在持久化时使用的 key 值由当前的 **包名路径+类名** 来决定，由此保证 key 值的唯一性

例如，**UserPreferences** 类缓存数据使用的 key 值是
```
private static final String KEY = "leavesc.hello.apt.model.UserPreferences";
```
对应的方法生成规则如下所示
```
    /**
     * 定义该注解类在序列化时使用的 Key
     *
     * @param typeElement 注解对象上层元素对象，即 Java 对象
     * @return
     */
    private FieldSpec generateKeyField(TypeElement typeElement) {
        return FieldSpec.builder(String.class, "KEY")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("\"" + typeElement.getQualifiedName().toString() + SUFFIX + "\"")
                .build();
    }

```
其他相应的 **get** 和 **set** 方法生成规则就不再赘述了，有兴趣研究的同学可以下载源码阅读

#### 2.5、实际体验

修改 MainActivity 的布局
```
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.et_userName)
    EditText et_userName;

    @BindView(R.id.et_userAge)
    EditText et_userAge;

    ···

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ButterKnife.bind(this);
        MainActivityViewBinding.bind(this);
        btn_serializeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = et_userName.getText().toString();
                String ageStr = et_userAge.getText().toString();
                int age = 0;
                if (!TextUtils.isEmpty(ageStr)) {
                    age = Integer.parseInt(ageStr);
                }
                String bookName = et_bookName.getText().toString();
                User user = new User();
                user.setAge(age);
                user.setName(userName);
                Book book = new Book();
                book.setName(bookName);
                user.setBook(book);
                UserPreferences.get().setUser(user);
            }
        });
        btn_serializeSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = et_singleUserName.getText().toString();
                UserPreferences.get().setName(userName);
            }
        });
        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserPreferences.get().remove();
            }
        });
        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = UserPreferences.get().getUser();
                if (user == null) {
                    tv_hint.setText("null");
                } else {
                    tv_hint.setText(user.toString());
                }
            }
        });
    }
}

```
![](https://upload-images.jianshu.io/upload_images/2552605-98fbcb48fea64c27.gif?imageMogr2/auto-orient/strip)

数据的整个存取过程自我感觉还是十分的简单的，不用再自己去维护臃肿的 key 表，且可以做到存取路径的唯一性，还是可以提高一些开发效率的

### **有兴趣看具体实现的可以点传送门：[Android_APT](https://github.com/leavesC/Android_APT)**

### **更多的开发知识点可以看这里：[Java_Kotlin_Android_Learn](https://github.com/leavesC/Java_Kotlin_Android_Learn)**
