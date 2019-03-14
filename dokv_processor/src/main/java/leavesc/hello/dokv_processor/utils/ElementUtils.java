package leavesc.hello.dokv_processor.utils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * 作者：leavesC
 * 时间：2019/1/5 14:49
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public class ElementUtils {

    //获取包名
    public static String getPackageName(Elements elementUtils, TypeElement typeElement) {
        return elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
    }

    //获取顶层类类名
    public static String getEnclosingClassName(TypeElement typeElement) {
        return typeElement.getSimpleName().toString();
    }

    //获取静态内部类类名
    public static String getStaticClassName(TypeElement typeElement) {
        return getEnclosingClassName(typeElement) + "Holder";
    }

}