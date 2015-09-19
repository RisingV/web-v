package vee.web.action;

import vee.web.action.reflect.result.DefaultResultWriterMgr;
import vee.web.action.reflect.result.ResultWriter;
import vee.web.action.reflect.result.ResultWriterContainer;
import vee.web.action.reflect.result.ResultWriterRegistry;
import vee.web.action.reflect.param.resolve.*;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-23  <br/>
 */
public abstract class ActionBase implements ParamResolverRegistry, ResultWriterRegistry {

    private ParamResolverRegistry localParamResolverRegistry;
    private ResultWriterRegistry localResultWriterRegistry;

    public ActionBase() {
        localParamResolverRegistry = DefaultParamResolverMgr.newInstance();
        localResultWriterRegistry = DefaultResultWriterMgr.newInstance();
        init();
    }

    //do register work in this method
    public void init() {
    }

    @Override
    public void registerTypeCaster( String key, TypeCaster typeCaster ) {
        localParamResolverRegistry.registerTypeCaster( key, typeCaster );
    }

    @Override
    public void registerPreTypeResolver( Class<?> type, PreTypeResolver preTypeResolver ) {
        localParamResolverRegistry.registerPreTypeResolver( type, preTypeResolver );
    }

    @Override
    public void registerParamResolver( String key, ParamResolver paramResolver ) {
        localParamResolverRegistry.registerParamResolver( key, paramResolver );
    }

    @Override
    public ParamResolverContainer getParamResolverContainer() {
        return localParamResolverRegistry.getParamResolverContainer();
    }

    @Override
    public void registerResultWriter( Class<?> resultType, ResultWriter resultWriter ) {
        localResultWriterRegistry.registerResultWriter( resultType, resultWriter );
    }

    @Override
    public <A extends Annotation> void registerAnnotatedResolver( Class<A> type, AnnotatedResolver<A> annotatedResolver ) {
        localParamResolverRegistry.registerAnnotatedResolver( type, annotatedResolver );
    }

    @Override
    public ResultWriterContainer getResultWriterContainer() {
        return localResultWriterRegistry.getResultWriterContainer();
    }

    @Override
    public void setDefault( ResultWriter resultWriter ) {
        localResultWriterRegistry.setDefault( resultWriter );
    }

}
