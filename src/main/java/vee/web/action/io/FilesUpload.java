package vee.web.action.io;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import java.util.List;

/**
 * Created with IntelliJ IDEA. <br/>
 * Author: Francis Yuen    <br/>
 * Date: 2015-06-24  <br/>
 */
public interface FilesUpload {

    List<FileItem> getItems() throws FileUploadException;

}
