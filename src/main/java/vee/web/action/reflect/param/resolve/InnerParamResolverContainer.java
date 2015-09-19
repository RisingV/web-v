package vee.web.action.reflect.param.resolve;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-27  <br/>
 */
public interface InnerParamResolverContainer {

    InnerParamResolver getInnerParamResolver( String key );

    InnerParamResolver getInnerParamResolver( Class<?> type );

}
