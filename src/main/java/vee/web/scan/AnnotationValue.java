package vee.web.scan;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-04  <br/>
 */
@FunctionalInterface
public interface AnnotationValue<V, A extends Annotation> {

    V value( A annotation );

}
