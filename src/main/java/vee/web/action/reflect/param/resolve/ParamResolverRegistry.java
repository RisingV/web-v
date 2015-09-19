package vee.web.action.reflect.param.resolve;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-23  <br/>
 */
public interface ParamResolverRegistry {

    void registerTypeCaster( String key, TypeCaster typeCaster );

    void registerPreTypeResolver( Class<?> type, PreTypeResolver preTypeResolver );

    void registerParamResolver( String key, ParamResolver paramResolver );

    <A extends Annotation> void registerAnnotatedResolver( Class<A> type, AnnotatedResolver<A> annotatedResolver );

    ParamResolverContainer getParamResolverContainer();

}
