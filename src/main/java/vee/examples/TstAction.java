package vee.examples;

import org.springframework.beans.factory.annotation.Autowired;
import vee.examples.pojo.DummyObject0;
import vee.examples.pojo.DummyObject1;
import vee.examples.service.IEchoService;
import vee.web.action.ActionBase;
import vee.web.action.reflect.action.filter.FilterStatus;
import vee.web.action.reflect.action.filter.impl.FilterFor;
import vee.web.action.reflect.action.filter.impl.FilterOrder;
import vee.web.action.reflect.action.filter.impl.Result;
import vee.web.action.tag.Action;
import vee.web.action.tag.Optional;
import vee.web.action.tag.Path;
import vee.web.action.tag.convert.JsonObject;
import vee.web.action.tag.scope.Body;
import vee.web.action.tag.scope.Cookie;
import vee.web.action.tag.scope.Header;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-08-12  <br/>
 * <p>
 * this class is just for test purpose.
 */
@Action( "tst" )
public class TstAction extends ActionBase {

    //injection is supported when vee.web.action.SpringActionLoadListener add to web.xml after org.springframework.web.context.ContextLoaderListener
    @Autowired
    private IEchoService echoService;

    @Override
    public void init() {
        //pre-type resolver example
        registerPreTypeResolver( DummyObject1.class, ( context, paramKey, scope, type ) -> {
            try {
                DummyObject1 o = (DummyObject1) type.newInstance();
                o.setF1( context.getParam( paramKey ) );
                return o;
            } catch ( InstantiationException | IllegalAccessException ignored ) {
            }
            return null;
        } );
    }

    // url: /{webapp}/tst/pre-type-example?arg=abc
    @Path( "pre-type-example" )
    public Object hanlex( DummyObject1 arg ) {
        return arg;
    }

    // url: /{webapp}/tst/echo -d "http body here."
    @Path( "echo" )
    public Object echo( @Body String body ) {
        return body;
    }

    // url: /{webapp}/tst/tst0/0
    @Path( "tst0/0" )
    public Object tst00() {
        return "tst00";
    }

    // url: /{webapp}/tst/tst0/0/1
    @Path( "tst0/0/1" )
    public Object tst001() {
        return "tst001";
    }

    // url: /{webapp}/tst/tst0/0/1/2
    @Path( "tst0/0/1/2" )
    public Object tst0012() {
        return "tst0012";
    }

    // url: /{webapp}/tst/invoke-service?arg0=abc&arg1=123
    @Path( "invoke-service" )
    public Object send( String arg0, Integer arg1, @Optional Double arg2 ) {
        return arg0 + arg1 + arg2;
    }

    // resolve parameter from header, cooke, session and etc.
    @Path( "url0" )
    public Object handle( @Header( "Content-Type" ) String contentType, @Cookie( "key0" ) Integer key ) {
        return echoService.echo( contentType + key );
    }

    //parse from json pass by http body
    @Path( "url1" )
    public Object handle1( @Body @JsonObject DummyObject0 dummy ) {
        return dummy;
    }

    //inject HttpServletRequest and HttpServletResponse to parameter list
    @Path( "url2" )
    public Object handle2( HttpServletRequest request, HttpServletResponse response ) {
        //do something
        return null;
    }

    //before biz handle method;
    @FilterFor( value = ".*" )
    public String prefilter0( String arg0, Integer arg2, FilterStatus status ) {
        //do filter
        status.set( false );// filtered flag, current method return value write to http response.
        return "request denny";
    }

    //before biz handle method;
    @FilterFor( value = ".*" )
    public Object prefilter1( @Result Object result ) {
        //do filter
        return result;
    }

    //after biz handle method;
    @FilterFor( value = ".*", order = FilterOrder.postFiler )
    public Object postfilter( @Result Object result, Integer arg0, String arg1 ) {
        //do filter
        return result;
    }

}
