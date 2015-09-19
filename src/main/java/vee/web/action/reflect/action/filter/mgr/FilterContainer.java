package vee.web.action.reflect.action.filter.mgr;

import vee.web.action.reflect.action.filter.Filter;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-04  <br/>
 */
public interface FilterContainer {

    <A extends Annotation> Filter<A> getFilter( Class<A> annotationType );

}
