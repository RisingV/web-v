package vee.web.action.reflect.action.filter;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-13  <br/>
 */
@FunctionalInterface
public interface FilterChainGetter<A extends Annotation> {

    FilterChain<A> getChain();

}
