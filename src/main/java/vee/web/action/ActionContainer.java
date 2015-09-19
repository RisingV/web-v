package vee.web.action;

import java.util.Set;

/**
 * Created with IntelliJ IDEA. <br/>
 * User: francis    <br/>
 * Date: 13-11-5    <br/>
 * Time: 15:55  <br/>
 */
public interface ActionContainer {

    Set<String> actionNames();

    Object getAction( String name );

}
