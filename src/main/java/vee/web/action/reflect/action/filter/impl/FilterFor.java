package vee.web.action.reflect.action.filter.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-04  <br/>
 */
@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
public @interface FilterFor {
    String value() default "*";
    FilterOrder order() default FilterOrder.preFilter;
    int priority() default 0;
}
