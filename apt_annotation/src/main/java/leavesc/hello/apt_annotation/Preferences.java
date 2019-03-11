package leavesc.hello.apt_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by：CZY
 * Time：2019/1/3 17:33
 * Desc：
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Preferences {

}