package vee.web.action.io.impl;

import vee.web.action.io.FilesUpload;
import vee.web.action.reflect.param.ParamScope;
import vee.web.action.reflect.param.resolve.PreTypeResolver;
import vee.web.servlet.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-24  <br/>
 */
public class FilesUploadParamResolver implements PreTypeResolver {

    @Override
    public Object resolve( RequestContext context, String paramKey, ParamScope scope, Class<?> type ) {
        HttpServletRequest request = context.getRequest();
        DiskFileItemFactory factory = new DiskFileItemFactory();

        Integer limit = (Integer) context.getContextObject( "uploadFileFlushSize" );
        if ( null == limit ) limit = 512 * 1024; // 512k is default threshold.
        Long sizeMax = (Long) context.getSession().getAttribute( "uploadFileSizeLimit" );
        if ( null == sizeMax ) sizeMax = 64l * 1024l * 1024l; //64M is default max upload size.

        factory.setSizeThreshold( limit );

        final ServletFileUpload upload = new ServletFileUpload( factory );
        upload.setSizeMax( sizeMax );

        return (FilesUpload) () -> upload.parseRequest( request );
    }

}
