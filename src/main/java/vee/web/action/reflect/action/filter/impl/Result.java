package vee.web.action.reflect.action.filter.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-18  <br/>
 */
@Target( ElementType.PARAMETER )
@Retention( RetentionPolicy.RUNTIME )
public @interface Result {
}
