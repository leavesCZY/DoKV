package leavesc.hello.dokv_processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import leavesc.hello.dokv_annotation.DoKV;
import leavesc.hello.dokv_annotation.IPreferencesHolder;
import leavesc.hello.dokv_annotation.Preferences;
import leavesc.hello.dokv_processor.utils.ElementUtils;
import leavesc.hello.dokv_processor.utils.StringUtils;

/**
 * Created by：CZY
 * Time：2019/1/3 17:35
 * Desc：
 */
@AutoService(Processor.class)
public class PreferencesProcessor extends AbstractProcessor {

    private Elements elementUtils;

    private static final String SUFFIX = "Preferences";

    private static final String INSTANCE = "INSTANCE";

    private static final String KEY_NAME = "KEY";

    private static final ClassName serializeManagerClass = ClassName.get(DoKV.class);

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> hashSet = new HashSet<>();
        hashSet.add(Preferences.class.getCanonicalName());
        return hashSet;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //获取所有包含 Preferences 注解的元素
        Set<? extends Element> elementSet = roundEnvironment.getElementsAnnotatedWith(Preferences.class);
        Map<TypeElement, List<VariableElement>> elementListHashMap = new HashMap<>();
        for (Element element : elementSet) {
            TypeElement typeElement = (TypeElement) element;
            List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
            if (enclosedElements != null && enclosedElements.size() > 0) {
                for (Element enclosedElement : enclosedElements) {
                    if (enclosedElement instanceof VariableElement) {
                        if (checkModifier(enclosedElement.getModifiers())) {
                            List<VariableElement> variableElementList = elementListHashMap.get(typeElement);
                            if (variableElementList == null) {
                                variableElementList = new ArrayList<>();
                                elementListHashMap.put(typeElement, variableElementList);
                            }
                            variableElementList.add((VariableElement) enclosedElement);
                        }
                    }
                }
            }
        }
        for (TypeElement key : elementListHashMap.keySet()) {
            List<VariableElement> variableElementList = elementListHashMap.get(key);
            String packageName = ElementUtils.getPackageName(elementUtils, key);
            JavaFile javaFile = JavaFile.builder(packageName, generateCodeByPoet(key, variableElementList)).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private boolean checkModifier(Set<Modifier> modifiers) {
        for (Modifier modifier : modifiers) {
            switch (modifier) {
                case ABSTRACT:
                case STATIC:
                case FINAL: {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 生成 Java 类
     *
     * @param typeElement         注解对象上层元素对象，即 Java 对象
     * @param variableElementList Java 对象包含的注解对象以及注解的目标对象
     * @return
     */
    private TypeSpec generateCodeByPoet(TypeElement typeElement, List<VariableElement> variableElementList) {
        //自动生成的文件以 Java 类名 + Preferences 进行命名
        TypeSpec.Builder builder = TypeSpec.classBuilder(typeElement.getSimpleName().toString() + SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.bestGuess(typeElement.getQualifiedName().toString()))
                .addField(generateKeyField(typeElement))
                .addMethod(generateConstructorMethod())
                .addMethod(generateInstanceHolderMethod(typeElement))
                .addMethod(generateGetPreferencesHolderMethod())
                .addMethod(generateSerializeMethod(typeElement))
                .addMethod(generateDeserializeMethod(typeElement))
                .addMethod(generateGetInstanceMethod(typeElement))
                .addMethod(generateGetInstanceNotNullMethod(typeElement))
                .addMethod(generateSetInstanceMethod(typeElement))
                .addMethod(generateRemoveKeyMethod());
        for (VariableElement variableElement : variableElementList) {
            builder.addMethod(generateGetFieldMethod(typeElement, variableElement));
            builder.addMethod(generateSetFieldMethod(typeElement, variableElement));
        }
        return builder.build();
    }

    /**
     * 定义该注解类在序列化时使用的 Key
     *
     * @param typeElement 注解对象上层元素对象，即 Java 对象
     * @return
     */
    private FieldSpec generateKeyField(TypeElement typeElement) {
        return FieldSpec.builder(String.class, KEY_NAME)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(CodeBlock.builder().addStatement("$S", typeElement.getQualifiedName().toString() + SUFFIX).build())
                .build();
    }

    /**
     * 构建内部静态类，用于持有单例对象
     *
     * @param typeElement 注解对象上层元素对象，即 Java 对象
     * @return
     */
    private TypeSpec generateInstanceHolderClass(TypeElement typeElement) {
        //包名
        String packageName = ElementUtils.getPackageName(elementUtils, typeElement);
        //自动构造的类的类名
        String enclosingClassName = getGenerateEnclosingClassName(typeElement);
        //静态内部类类名
        String staticClassName = ElementUtils.getStaticClassName(typeElement);
        ClassName className = ClassName.get(packageName, enclosingClassName);
        //构建实例变量
        FieldSpec instance = FieldSpec.builder(className, INSTANCE)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(CodeBlock.builder().addStatement("new $L()", enclosingClassName).build())
                .build();
        return TypeSpec.classBuilder(staticClassName)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addField(instance)
                .build();
    }

    /**
     * 构建获取唯一实例的 Get 方法
     *
     * @param typeElement 注解对象上层元素对象，即 Java 对象
     * @return
     */
    private MethodSpec generateInstanceHolderMethod(TypeElement typeElement) {
        //包名
        final String packageName = ElementUtils.getPackageName(elementUtils, typeElement);
        //自动构造的类的类名
        final String enclosingClassName = getGenerateEnclosingClassName(typeElement);
        ClassName className = ClassName.get(packageName, enclosingClassName);
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(className)
                .addStatement("return new $L()", enclosingClassName);
        return methodBuilder.build();
    }

    private MethodSpec generateConstructorMethod() {
        MethodSpec.Builder methodBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE);
        return methodBuilder.build();
    }

    /**
     * 构造用于获取 IPreferencesHolder 实例的方法
     *
     * @return IPreferencesHolder
     */
    private MethodSpec generateGetPreferencesHolderMethod() {
        //方法名
        String methodName = "getPreferencesHolder";
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PRIVATE)
                .returns(IPreferencesHolder.class)
                .addStatement("return $T.getInstance().getPreferencesHolder()", serializeManagerClass);
        return builder.build();
    }

    /**
     * 构造用于序列化的方法
     *
     * @return IPreferencesHolder
     */
    private MethodSpec generateSerializeMethod(TypeElement parameter) {
        //方法名
        String methodName = "serialize";
        //方法参数名
        String keyName = "_" + KEY_NAME;
        String instanceName = "_" + ElementUtils.getEnclosingClassName(parameter);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PRIVATE)
                .returns(String.class)
                .addParameter(String.class, keyName)
                .addParameter(ClassName.get(parameter.asType()), instanceName)
                .addStatement("return getPreferencesHolder().serialize($L, $L)", keyName, instanceName);
        return builder.build();
    }

    /**
     * 构造用于反序列化的方法
     *
     * @return MethodSpec
     */
    private MethodSpec generateDeserializeMethod(TypeElement parameter) {
        //方法名
        String methodName = "deserialize";
        //方法参数名
        String keyName = "_" + KEY_NAME;
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PRIVATE)
                .returns(ClassName.get(parameter))
                .addParameter(String.class, keyName)
                .addStatement("return getPreferencesHolder().deserialize($L, $L.class)", keyName, ElementUtils.getEnclosingClassName(parameter));
        return builder.build();
    }

    /**
     * 构造用于获取整个序列化对象的方法，返回值可能为 null
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
                .returns(ClassName.get(typeElement.asType()))
                .addStatement("return deserialize($L)", KEY_NAME);
        return builder.build();
    }

    /**
     * 构造用于获取整个序列化对象的方法，返回值不为 null
     *
     * @param typeElement 注解对象上层元素对象，即 Java 对象
     * @return
     */
    private MethodSpec generateGetInstanceNotNullMethod(TypeElement typeElement) {
        //顶层类类名
        String enclosingClassName = ElementUtils.getEnclosingClassName(typeElement);
        //方法名
        String methodName = "get" + StringUtils.toUpperCaseFirstChar(enclosingClassName) + "NotNull";
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PRIVATE)
                .returns(ClassName.get(typeElement.asType()))
                .addStatement("$L variable = deserialize($L)", enclosingClassName, KEY_NAME)
                .addStatement("if(variable != null) { return variable; } return new $L()", enclosingClassName);
        return builder.build();
    }

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
                .addParameter(ClassName.get(typeElement.asType()), fieldName)
                .addStatement("if($L == null) { remove(); return \"\"; }", fieldName)
                .addStatement("return serialize($L,$L)", KEY_NAME, fieldName);
        return builder.build();
    }

    /**
     * 构造用于移除该序列化对象的方法
     *
     * @return
     */
    private MethodSpec generateRemoveKeyMethod() {
        //方法名
        String methodName = "remove";
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("getPreferencesHolder().remove($L)", KEY_NAME);
        return builder.build();
    }

    /**
     * 重写包含 Preferences 注解的字段的 Get 方法
     *
     * @param typeElement     注解对象上层元素对象，即 Java 对象
     * @param variableElement 包含 Preferences 注解的字段
     * @return
     */
    private MethodSpec generateGetFieldMethod(TypeElement typeElement, VariableElement variableElement) {
        //顶层类类名
        String enclosingClassName = ElementUtils.getEnclosingClassName(typeElement);
        //字段名
        String fieldName = variableElement.getSimpleName().toString();
        //方法名
        String methodName = "get" + StringUtils.toUpperCaseFirstChar(fieldName);
        //方法名
        String getInstanceMethodName = "get" + StringUtils.toUpperCaseFirstChar(ElementUtils.getEnclosingClassName(typeElement)) + "()";
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(variableElement.asType()))
                .addAnnotation(Override.class)
                .addStatement("$L variable = $L", enclosingClassName, getInstanceMethodName)
                .addStatement("if(variable != null) { return variable.$L(); } return super.$L() ", methodName, methodName);
        return builder.build();
    }

    /**
     * 重写包含 Preferences 注解的字段的 Set 方法
     *
     * @param variableElement 包含 Preferences 注解的字段
     * @return
     */
    private MethodSpec generateSetFieldMethod(TypeElement typeElement, VariableElement variableElement) {
        //顶层类类名
        String enclosingClassName = ElementUtils.getEnclosingClassName(typeElement);
        //字段名
        String fieldName = variableElement.getSimpleName().toString();
        String upperCaseFieldName = StringUtils.toUpperCaseFirstChar(fieldName);
        //set方法名
        String setMethodName = "set" + upperCaseFieldName;
        //get方法名
        String getMethodName = "get" + upperCaseFieldName;
        //序列化对象名
        String serializeObjName = "_" + StringUtils.toLowerCaseFirstChar(enclosingClassName);
        //方法名
        String methodName = "get" + StringUtils.toUpperCaseFirstChar(enclosingClassName) + "NotNull()";
        MethodSpec.Builder builder = MethodSpec.methodBuilder(setMethodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get(variableElement.asType()), "_" + fieldName)
                .addAnnotation(Override.class)
                .addStatement("super.$L($L)", setMethodName, "_" + fieldName)
                .addStatement("$L $L = $L", enclosingClassName, serializeObjName, methodName)
                .addStatement("$L.$L(super.$L())", serializeObjName, setMethodName, getMethodName)
                .addStatement("serialize($L,$L)", KEY_NAME, serializeObjName);
        return builder.build();
    }

    //获取自动构造的类的类名
    private static String getGenerateEnclosingClassName(TypeElement typeElement) {
        return ElementUtils.getEnclosingClassName(typeElement) + SUFFIX;
    }

}