package vee.web.action.reflect.action.filter;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-07-04  <br/>
 */
@FunctionalInterface
public interface FilterStatus {

    void set( boolean isPass );

}
