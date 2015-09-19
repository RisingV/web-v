package vee.web.action.io;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-26  <br/>
 */
public interface View {

    String getTemplateName();

    String getModelName();

    Object getModel();

    default String getEncoding() {
        return "utf-8";
    }

    default String getMimeType() {
        return "text/html";
    }

}
