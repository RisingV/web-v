package vee.web.action.tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-18  <br/>
 */
@Target( value = ElementType.TYPE )
@Retention( value = RetentionPolicy.RUNTIME )
public @interface Action {
    String value();
}
