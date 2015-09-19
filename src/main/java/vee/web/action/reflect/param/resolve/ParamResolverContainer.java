package vee.web.action.reflect.param.resolve;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-23  <br/>
 */
public interface ParamResolverContainer extends InnerParamResolverContainer {

    TypeCaster getTypeCaster( String key );

    PreTypeResolver getPreTypeResolver( Class<?> type );

    ParamResolver getParamResolver( String key );

    <A extends Annotation> AnnotatedResolver<A> getAnnotatedResolver( Class<A> type );

}
