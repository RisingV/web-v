package vee.web.action.reflect.action.filter.mgr;

import vee.web.action.reflect.action.filter.Filter;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-04  <br/>
 */
public interface FilterRegistry {

    <A extends Annotation> void registerFilter( Class<A> annotationType, Filter<A> filter );

    FilterContainer getFilterContainer();

}
