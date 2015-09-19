package vee.web.action.reflect.action.filter;

import vee.web.action.reflect.action.AccessPoint;
import vee.web.servlet.RequestContext;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-04  <br/>
 */
@FunctionalInterface
public interface Filter<A extends Annotation> {

    Object apply( A filterAnnotation, Object action, AccessPoint accessPoint, RequestContext context );

}
