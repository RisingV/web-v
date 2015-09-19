package vee.web.action.tag.scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-24  <br/>
 */
@Target( ElementType.PARAMETER )
@Retention( RetentionPolicy.RUNTIME )
public @interface Context {
    String value();
}
