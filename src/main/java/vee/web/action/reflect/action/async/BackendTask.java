package vee.web.action.reflect.action.async;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-15  <br/>
 */
@FunctionalInterface
public interface BackendTask<T> {

    T execute( final long timeout );

}
