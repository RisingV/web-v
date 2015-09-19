package vee.web.action.reflect.result;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-24  <br/>
 */
public interface ResultWriterRegistry {

    void registerResultWriter( Class<?> resultType, ResultWriter resultWriter );

    void setDefault( ResultWriter resultWriter );

    ResultWriterContainer getResultWriterContainer();
}
