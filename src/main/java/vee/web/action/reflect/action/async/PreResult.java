package vee.web.action.reflect.action.async;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-15  <br/>
 */
@FunctionalInterface
public interface PreResult<T> {

    T get();

}
