package vee.web.action.reflect.action.filter;

import vee.web.action.reflect.action.AccessPoint;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-04  <br/>
 */
public class FilterPoint<A extends Annotation> {

    final A filterAnnotation;
    final Filter<A> filter;
    final Class<?> resultType;
    final AccessPoint accessPoint;
    final int priority;
    final int index;

    public FilterPoint( AccessPoint accessPoint, Filter<A> filter, A filterAnnotation, int index, int priority, Class<?> resultType ) {
        this.accessPoint = accessPoint;
        this.filter = filter;
        this.filterAnnotation = filterAnnotation;
        this.index = index;
        this.priority = priority;
        this.resultType = resultType;
    }

    public int getPriority() {
        return priority;
    }

}
