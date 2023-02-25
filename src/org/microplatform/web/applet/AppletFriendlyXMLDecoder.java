package org.microplatform.web.applet;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.beans.ObjectHandler;

/**
 * Almost an exact copy of the sun jdk XMLDecoder. But actually the original
 * XMLDecoder dramatically slow down the object reading because it will create
 * at least 4 sockets when looking up for the SaxParserFactortyimplementation.
 * Unfortunately that's really annoying when you want to build a very reactive
 * applet that interact with server side files as soon as you open it.
 * 
 * A first workarround perfectly suitable when deploying for java 1.5 and 1.6
 * only is to add two single line file in the META-INF:services directory of the
 * jar, respectively: "javax.xml.parsers.SAXParserFactory" and containaining the
 * single line "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl"
 * and an other one called
 * "com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration" and
 * containing "com.sun.org.apache.xerces.internal.parsers.XML11Configuration".
 * 
 * Unfortunately, the SAXParserFactory has been moved: it was
 * org.apache.crimson.jaxp.SAXParserFactoryImpl for jre 1.4)! So unless you
 * produce a new specific META-INF to target only java 1.4, this workarround
 * won't work with java 1.4 also.
 * 
 * So finally we will use that AppletFriendlyXMLDecoder that shunt the lookup
 * for java 1.5 and 1.6 while we will fallback to the default XMLDecoder if a
 * jre 1.4 is detected. But in that last case we will have our META INF file
 * (with values for JRE 1.4) that will avoid the lookup!
 * 
 * WARNING, IF YOU CALL THAT CLASS BY AN OTHER WAY THAN REFLECTION YOU'LL GET AN
 * UNCAUGHT NOCLASSDEFFOUND EXCEPTION WITH JAVA 1.4 AND THEN YOU WON'T EVEN BE
 * ABLE TO FALLBACK TO XMLDECODER!
 * 
 * HINT: while this class won't compile with java 1.4, you can still compile with java 1.5+ and target
 * java 1.4 and indeed use the jre 1.4; it will work because in the case of java 1.4 we will
 * fall back the lookup to the classes mentionned in the META-INF/services directory.
 * 
 * TODO: use XMLDecoder instead with the META-INF workarround when you don't
 * care about the jre 1.4 anymore.
 * 
 * @author Raphael Valyi (adapted from Sun).
 */
public class AppletFriendlyXMLDecoder extends XMLDecoder {
	private InputStream in;

	private Object owner;

	private ObjectHandler handler;

	private Reference clref;

	public AppletFriendlyXMLDecoder(InputStream in) {
		this(in, null);
	}

	public AppletFriendlyXMLDecoder(InputStream in, Object owner) {
		this(in, owner, null);
	}

	public AppletFriendlyXMLDecoder(InputStream in, Object owner,
			ExceptionListener exceptionListener) {
		this(in, owner, exceptionListener, null);
	}

	public AppletFriendlyXMLDecoder(InputStream in, Object owner,
			ExceptionListener exceptionListener, ClassLoader cl) {
		super(in, owner, exceptionListener, cl);// required change
		this.in = in;
		setOwner(owner);
		setExceptionListener(exceptionListener);
		setClassLoader(cl);
	}

	private void setClassLoader(ClassLoader cl) {
		if (cl != null) {
			this.clref = new WeakReference(cl);
		}
	}

	private ClassLoader getClassLoader() {
		if (clref != null) {
			return (ClassLoader) clref.get();
		}
		return null;
	}

	public void close() {
		if (in != null) {
			getHandler();
			try {
				in.close();
			} catch (IOException e) {
				getExceptionListener().exceptionThrown(e);
			}
		}
	}

	public Object readObject() {
		if (in == null) {
			return null;
		}
		return getHandler().dequeueResult();
	}

	public void setOwner(Object owner) {
		this.owner = owner;
	}

	public Object getOwner() {
		return owner;
	}

	/**
	 * Applet friendly changed method
	 * @return
	 */
	private ObjectHandler getHandler() {
		if (handler == null) {
			try {
				handler = new ObjectHandler(this, getClassLoader());
				InputSource input = new InputSource(in);
				com.sun.org.apache.xerces.internal.parsers.SAXParser parser2 = new com.sun.org.apache.xerces.internal.parsers.SAXParser(new com.sun.org.apache.xerces.internal.parsers.XML11Configuration());
	            parser2.setDocumentHandler(handler);
	            parser2.setEntityResolver(handler);
	            parser2.setErrorHandler(handler);
	            parser2.setDTDHandler(handler);
	            parser2.parse(input);
	            
			} catch (SAXException se) {
				Exception e = se.getException();
				if (e == null) {
					e = se;
				}
				getExceptionListener().exceptionThrown(e);
			} catch (IOException ioe) {
				getExceptionListener().exceptionThrown(ioe);
			}
		}
		return handler;
	}
}
