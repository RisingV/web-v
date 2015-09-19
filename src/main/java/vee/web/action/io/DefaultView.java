package vee.web.action.io;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-29  <br/>
 */
public class DefaultView implements View {

    private String templateName;
    private String modelName;
    private Object model;

    @Override
    public Object getModel() {
        return model;
    }

    public void setModel( Object model ) {
        this.model = model;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    public void setModelName( String modelName ) {
        this.modelName = modelName;
    }

    @Override
    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName( String templateName ) {
        this.templateName = templateName;
    }

}
