package vee.web.action.tag.scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-26  <br/>
 */
@Target( ElementType.PARAMETER )
@Retention( RetentionPolicy.RUNTIME )
public @interface Body {

    boolean binary() default false;

    String encoding() default "utf-8";

}
