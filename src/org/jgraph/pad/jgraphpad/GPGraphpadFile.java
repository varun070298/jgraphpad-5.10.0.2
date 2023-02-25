package org.jgraph.pad.jgraphpad;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.security.AccessControlException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphModel;
import org.microplatform.io.IFile;
import org.microplatform.loaders.PluginInvoker;
import org.microplatform.loaders.Translator;
import org.microplatform.web.applet.AppletFriendlyXMLEncoder;

/**
 * A Javabean whose properties are meant to be persisted or retrieved from an
 * XML file. See the JGraph manual for more details about how to use XMLEncoder
 * with JGraph
 * 
 * @author rvalyi (after the JGraph manual) - LGPL
 */
public class GPGraphpadFile implements org.microplatform.io.IFile {
    /**
     * Set that contains all root cells of this model.
     */
    protected List roots = null;

    protected ConnectionSet connectionSet = null;

    /**
     * The model's own attributes as a map. Defaults to an empty Hashtable.
     */
    protected AttributeMap attributes = null;

	public static final String GRAPH_LAYOUT_CACHE = "GraphLayoutCache";

	/**
	 * Reads a GPGraphpadFile file
	 * 
	 * @param in
	 *            the input stream to read
	 * @return the graph that is read in
	 */
	public IFile read(InputStream in) throws IOException {
		XMLDecoder decoder;
		if (System.getProperty ( "java.vm.version" ).indexOf("1.4") > -1) {
			decoder = new XMLDecoder(in);//now way to use AppletFriendlyXMLDecoder,
			//but with an appropriate META-INF package we will speed up the reading anyway.
		}
		else {
				try {//java 1.5 or 1.6, then AppletFriendlyXMLDecoder will work faster than the original XMLDecoder under applet conditions
					Class dclass = Class.forName(PluginInvoker.XMLDECODER);
					Constructor construct = dclass.getConstructor(new Class[] {InputStream.class});
					decoder = (XMLDecoder) construct.newInstance(new Object[] {in});
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				} 
		}
		
		Object decoded = decoder.readObject();
		in.close();
		if (decoded instanceof GPGraphpadFile) {
			return (GPGraphpadFile) decoded;
		}
		if (decoded instanceof JGraph) {
			return null;//TODO
		}
		if (decoded instanceof DefaultGraphModel) {
            GPGraphpadFile file = new GPGraphpadFile();
            file.roots = ((DefaultGraphModel) decoded).getRoots();
            file.connectionSet = ((DefaultGraphModel) decoded).getConnectionSet();
            file.attributes = ((DefaultGraphModel) decoded).getAttributes(null);
			return file;
		}
        JOptionPane.showMessageDialog(new JFrame(), "Unrecognized file format! Try to use import or alter you XML file...", Translator.getString("Title"),
                JOptionPane.ERROR_MESSAGE);
		return null;
	}

	/**
	 * Saves the current graph in a file. We use long-term bean persistence to
	 * save the program data. See
	 * http://java.sun.com/products/jfc/tsc/articles/persistence4/index.html for
	 * an overview.
	 * 
	 * @param out
	 *            the stream for saving
	 */
	public void saveFile(OutputStream out) {
		XMLEncoder encoder = new AppletFriendlyXMLEncoder(out);

		// Better debugging output, in case you need it
		encoder.setExceptionListener(new ExceptionListener() {
			public void exceptionThrown(Exception e) {
				if (!(e instanceof AccessControlException))// happens in
															// unsigned
															// conditions,
															// benign
					e.printStackTrace();
			}
		});
		
		encoder.writeObject(this);
		encoder.close();
	}
	
	/**
	 * Specially handy to use from liveconnect
	 */
	public String getFileAsXML() {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		saveFile(bo);
		return bo.toString();
	}
	
	/**
	 * Specially handy to use from liveconnect
	 */
	public void openFileFromXML(String xml) {
		ByteArrayInputStream in =new ByteArrayInputStream(xml.getBytes());
		try {
			read(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public AttributeMap getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributeMap attributes) {
        this.attributes = attributes;
    }

    public ConnectionSet getConnectionSet() {
        return connectionSet;
    }

    public void setConnectionSet(ConnectionSet connectionSet) {
        this.connectionSet = connectionSet;
    }

    public List getRoots() {
        return roots;
    }

    public void setRoots(List roots) {
        this.roots = roots;
    }
}
