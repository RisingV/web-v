package vee.web.action.reflect.action.filter;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-04  <br/>
 */
@FunctionalInterface
public interface FilterChain<A extends Annotation> {

    FilterPoint<A> next();

}
