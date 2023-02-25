package org.microplatform.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IFile {
	
	public IFile read(InputStream in) throws IOException;
	
	public void saveFile(OutputStream out) ;
	
	public String getFileAsXML();

}
