package org.microplatform.loaders;

import java.applet.AppletContext;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.microplatform.ActionCommand;
import org.microplatform.Document;
import org.microplatform.ICommandRegistery;
import org.microplatform.Utilities;
import org.microplatform.gui.DocFrame;
import org.microplatform.gui.MDIContainer;

/**
 * Utilities for JGraphpad to load plugin classes in a loose coupled manner so
 * that they can be included or not dynamically and without generating
 * compilation error. The various kind of classes you try to instanciate via
 * those utilities can be replaced by an appropriate subclasser by any
 * registered plugin if it change the key association in the the properties
 * file.
 * 
 * @author rvalyi
 */
public final class PluginInvoker {
	
	public static String BROWSER_DRIVER = "org.microplatform.web.applet.BrowserDriver";
	public static String BROWSER_LAUNCHER = "org.microplatform.web.BareBonesBrowserLaunch";
	public static String XMLDECODER = "org.microplatform.web.applet.AppletFriendlyXMLDecoder";
	
	public static AppletContext appletContext = null;//only used to redirect the browser in applet, that's why static is acceptable

    public MDIContainer mdiContainer;
    
    private IBrowserDriver browserDriver;
    
    public PluginInvoker(MDIContainer theContainer) {
    	mdiContainer = theContainer;
    	if (mdiContainer.getSessionParameters().isApplet()) {
    		appletContext = mdiContainer.getSessionParameters().getApplet().getAppletContext();
    		try {
    			browserDriver = (IBrowserDriver) instanciateObjectForName(BROWSER_DRIVER);
    		} catch (Exception e) {
    			e.printStackTrace();
    			mdiContainer.error(BROWSER_DRIVER);
    		}
    	}
    }

    /**
     * The package where actions are if no aternative location is specified
     */
    public static String DEFAULT_ACTION_LOOKUP_PATH = Translator
            .getString("DefaultActionsPath") + ".";

    public static interface IPadAwarePlugin {
        public void setGraphpad(MDIContainer pad);
    }

    public static interface IDocAwarePlugin {
        public void setDocument(Document doc);
    }
    
    public static interface IBrowserDriver {
    	 public  void execJavascript(String cmd, MDIContainer mdiContainer);
    	 public void notifyBrowserFileChanged(MDIContainer mdiContainer);
    }

    public final class TrampolineAction extends AbstractAction {
        ActionListener delegate;

        protected String delegateName;

        public void actionPerformed(ActionEvent e) {
        	initDelegate();
        	if (delegate != null)
        		delegate.actionPerformed(e);
        }
        
        public void initDelegate() {
            if (delegate == null) {
                String path = DEFAULT_ACTION_LOOKUP_PATH + delegateName;
                try {
                    String overrider = Translator.getString(delegateName
                            + ".class");
                    if (overrider != null) {
                        if (overrider.equals(""))
                            return;
                        delegate = (Action) Class.forName(overrider)
                                .newInstance();
                        //TODO: this should work and allow lazy loading of toolboxes!
                       /* ICommandRegistery registery = null;
                        if (e.getSource() instanceof Container)
                        	registery = getActionRegistery((Container) e.getSource());
                        if (registery != null)
                        	registery.initCommand(delegate);*/
                    } else
                        delegate = (Action) Class.forName(path).newInstance();
                } catch (Exception ex) {
                    System.out.print("\nCANT'T FIND LOAD ACTION "
                            + delegateName + " I'M CONTINUING...");
                    return;
                }
                if (delegate instanceof ActionCommand)
                    ((ActionCommand) delegate).setMdiContainer(mdiContainer);
            }
        }
    }
    
    public ICommandRegistery getActionRegistery(Container source) {
    		if (source == null )
    			return null;
    		if (source instanceof DocFrame)
    			return ((DocFrame) source).getDocument();
    		if (source instanceof ICommandRegistery)
    			return (ICommandRegistery) source;
    		Container container = source;
    		return getActionRegistery(container.getParent());
    }

    public Action getCommand(final String key,
            final ICommandRegistery registery) {
        Action action;
        action = registery.getActionMap().get(key);
        if (action != null)
            return action;// action already registered

        String overrider = Translator.getString(key + ".class");// see if a
        // plugin wants
        // to override
        // the default
        if (overrider == null) {// normal cases of an action where only the
            // actionPerformed method matters
            action = new TrampolineAction();
            ((TrampolineAction) action).delegateName = key;
            registery.getActionMap().put(key, action);// we register this
            // new command
            return action;
        }
        if (overrider.equals(""))
            return null;// means we forced to ignore that key in a plugin
        if (overrider.endsWith("_eager")) {// means that action can't be
            // lazily
            // downloaded/instanciated
            overrider = overrider.replaceAll("_eager", "");
            try {
                action = (Action) Class.forName(overrider).newInstance();
                registery.getActionMap().put(key, action);
                registery.initCommand(action);// we eventually initialize
                // something dealing with this new
                // command

                if (action instanceof ActionCommand)
                    ((ActionCommand) action).setMdiContainer(mdiContainer);
                return action;
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        action = new TrampolineAction();
        ((TrampolineAction) action).delegateName = key;
        return action;
    }

    public static Object instanciateObjectForKey(final String key) {
        Class clazz = null;
        try {
            clazz = getClassForKey(key);
            return clazz.newInstance();
        } catch (Exception e) {
        	MDIContainer.error("CAN'T INSTANCIATE CLASS: " + clazz.toString()
                    + "/nMAY BE THIS CLASS HAS NO PUBLIC EMPTY CONSTRUCTOR\n", null);//TODO doesn't work
            e.printStackTrace();
            return null;
        }
    }

    public static Object instanciateObjectForName(final String name)
            throws ClassNotFoundException {
        Class clazz = null;
        try {
            clazz = Class.forName(name);
            return clazz.newInstance();
        } catch (Exception e) {
        	MDIContainer.error("CAN'T INSTANCIATE CLASS: " + clazz.toString()
                    + "/nMAY BE THIS CLASS HAS NO PUBLIC EMPTY CONSTRUCTOR\n", null);//TODO doesn't work
            e.printStackTrace();
            return null;
        }
    }
    
    public static Class getClassForKey(final String key) {
        try {
            return Class.forName(Translator.getString(key));
        } catch (ClassNotFoundException e) {
            System.out.print("\nCAN'T LOAD CLASS: " + Translator.getString(key)
                    + "\nMAY BE THE REQUIRED PLUGIN IS SIMPLY MISSING");
            return null;
        }
    }

    /**
     * Decorate the document with registered plugins if any. Use the decorator
     * pattern to allow loose coupling with plugins
     * 
     * @param doc
     * @return
     */
    public static Document decorateDocument(Document doc) {
        String values[] = Utilities.tokenize(Translator
                .getString("DocumentDecorators"));
        for (int i = 0; i < values.length; i++) {
            try {
                Object object = Class.forName(values[i]).newInstance();
                if (object instanceof IDocAwarePlugin)
                    ((IDocAwarePlugin) object).setDocument(doc);
            } catch (Exception ex) {
                System.out.print(ex);
            }
        }
        return doc;
    }

    /**
     * Decorate the Graphpad with registered plugins if any. Use the decorator
     * pattern to allow loose coupling with plugins
     * 
     * @param doc
     * @return
     */
    public static MDIContainer decorateGraphpad(MDIContainer pad) {
        String values[] = Utilities.tokenize(Translator
                .getString("GraphpadDecorators"));
        for (int i = 0; i < values.length; i++) {
            try {
                Object object = Class.forName(values[i]).newInstance();
                if (object instanceof IPadAwarePlugin)
                    ((IPadAwarePlugin) object).setGraphpad(pad);
            } catch (Exception ex) {
                System.out.print(ex);
            }
        }
        return pad;
    }

    public static Object instanciateDocAwarePluginForKey(final String key,
            final Document document) {
        Object object = instanciateObjectForKey(key);
        if (object instanceof IDocAwarePlugin)
            ((IDocAwarePlugin) object).setDocument(document);
        return object;
    }

    public static Object instanciatePadAwarePluginForKey(final String key,
            final MDIContainer pad) {
        Object object = instanciateObjectForKey(key);
        if (object instanceof IPadAwarePlugin)
            ((IPadAwarePlugin) object).setGraphpad(pad);
        return object;
    }

    /**
     * Opens the given URL using the browser launcher or the applet context when it exists
     * 
     * @param url
     */
    public static void openURL(String url) {
        try {
        	if (appletContext != null) {
        		appletContext.showDocument(new URL(url),"_blank");
        	}
        	else {
                Class clazz = Class.forName(BROWSER_LAUNCHER);
                Class classes[] = { String.class };
                Object params[] = { new String(url) };
                clazz.getMethod("openURL", classes).invoke(null, params);
        	}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static NamedInputStream provideInputStream(String fileExtension) {
        try {
        	NamedInputStreamProvider provider = (NamedInputStreamProvider) instanciateObjectForKey("InputStreamProvider.class");
        	return provider.provideInput(fileExtension);
        } catch (Exception ex) {
        	MDIContainer.error(Translator.getString("Error.invokation"), null);
            ex.printStackTrace();
            return null;
        }
    }

    public static NamedOutputStream provideOutputStream(String fileExtension,
            String nameToBeConfirmed, boolean isZipped) {
        if (nameToBeConfirmed == null)
            nameToBeConfirmed = "undef";
        try {
        	NamedOutputStreamProvider provider = (NamedOutputStreamProvider) instanciateObjectForKey("OutputStreamProvider.class");
        	return provider.provideOutput(fileExtension,nameToBeConfirmed, isZipped);
        } catch (Exception ex) {
        	MDIContainer.error(Translator.getString("Error.invokation"), null);
            ex.printStackTrace();
            return null;
        }
    }
    
    public static interface NamedOutputStreamProvider {
    	public NamedOutputStream provideOutput(String fileExtension,
                String nameToBeConfirmed, boolean isZipped);
    }
    
    public static interface NamedInputStreamProvider {
    	public NamedInputStream provideInput(String fileExtension);
    }
    
    public static final class NamedOutputStream {
    	public NamedOutputStream(OutputStream output, String theName) {
    		out = output;
    		name = theName;
    	}
    	public OutputStream out;
    	public String name;
    }
    
    public static final class NamedInputStream {
    	public NamedInputStream(InputStream input, String theName) {
    		in = input;
    		name = theName;
    	}
    	public InputStream in;
    	public String name;
    }
    
    public  void execJavascript(String cmd)  {
    	if (browserDriver != null)
	    	browserDriver.execJavascript(cmd, mdiContainer);
    	else {//actually we need to investigate further because browserDriver may be null due to a strange threading bug with the browser
        	if (mdiContainer.getSessionParameters().isApplet()) {
        		appletContext = mdiContainer.getSessionParameters().getApplet().getAppletContext();
        		try {
        			
        	        Class clazz = null;
        	        try {
        	            clazz = Class.forName(BROWSER_DRIVER);
        	            //getClassForName(BROWSER_DRIVER);
        	        } catch (Exception e) {
        	        	mdiContainer.error(e.toString());
        	        }
        			
        			browserDriver = (IBrowserDriver) clazz.newInstance();
        	    	browserDriver.execJavascript(cmd, mdiContainer);
        		} catch (Exception e) {
        			e.printStackTrace();
        			mdiContainer.error(BROWSER_DRIVER);
        		}
        	}
    	}    		
    }
    
    public void notifyBrowser() {
    	if (mdiContainer.getSessionParameters().isApplet()) {
	    	Runnable notification = new Runnable() {
	    		public void run() {
	    	    	browserDriver.notifyBrowserFileChanged(mdiContainer);
	    		}
	    	};
	    	notification.run();
	    }
    }
}
