package vee.web.action.reflect.param.resolve;

import vee.web.action.reflect.param.ParamScope;
import vee.web.servlet.RequestContext;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-18  <br/>
 */
public interface AnnotatedResolver<A extends Annotation> {

    Object resolve( RequestContext context, A a, ParamScope scope, String paramKey, Class<?> type );

}
