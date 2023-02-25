package org.microplatform;

import java.applet.Applet;
import java.awt.Color;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.microplatform.loaders.ImageLoader;
import org.microplatform.loaders.Translator;


/**
 * Unified (for standalone, applet or webstart) container for parameters that can change from one session to an other
 * without recompilation neither even any change in any property file.
 * 
 * While JGraphpad gets most of its parameters from the properties file, some
 * other parameters can be given at the session time. Those parameters can only
 * be strings and they should be passed as argument for the main class, either
 * when invoking JGraphpad as a standalone application, as an applet or via
 * webstart.
 * 
 * You can even gain full control of JGraphpad during the session by providing a
 * new properties file as an argument!
 * 
 * @author rvalyi
 */
public class SessionParameters {

	/**
	 * This is where we store every sort of session parameters
	 */
	private Map sessionParameters = new Hashtable(10);

	/**
	 * This is a static map providing the appropriate key for command line
	 * arguments
	 */
	public static Map paramCommands;

    /**
     * A reference to the applet if there is one
     */
	private Applet applet;

	public static String DEFAULT_PROPERTIES_FILE;

	public static String DEFAULT_IMAGES_PATH;

	// session parameters:

	public static final String HOSTNAME = "host";

	public static final String HOSTPORT = "port";

	public static final String PROTOCOL = "protocol";

	public static final String DOWNLOADPATH = "drawpath";

	public static final String UPLOADPATH = "savepath";

	public static final String UPLOADFILE = "basename";

	public static final String MAPFILE = "mapfile";//(upload path)

	public static final String VIEWPATH = "viewpath";//(upload path)

	public static final String CUSTOMCONFIG = "customconfig";
	
	public static final String READONLY = "readonly";

	/**
	 * "no" means no plugin. See default properties file to see how to register a plugin path;
	 * Warning, when using the  PLUGINS parameter, paths should be separated by | instead of space!
	 */
	public static final String PLUGINS = "plugins";
	
	/**
	 * true means that the applet is a button and the JGraphpad will popup in a specific frame.
	 */
	public static final String NEWFRAME = "newFrame";
	
	/**
	 * The id of the applet. Used by call back to the javascript methods of the current HTML page.
	 */
	public static final String ID = "id";
	
	public static final String BACKGROUNDCOLOR = "backgroundcolor";
	
	public static final String FOREGROUNDCOLOR = "foregroundcolor";

	// command line for the session parameters:

	public static final String HOSTNAME_SHORT = "-h";

	public static final String HOSTPORT_SHORT = "-p";

	public static final String PROTOCOL_SHORT = "-t";

	public static final String DOWNLOADPATH_SHORT = "-d";

	public static final String UPLOADPATH_SHORT = "-u";

	public static final String UPLOADFILE_SHORT = "-f";

	public static final String MAPFILE_SHORT = "-m";

	public static final String VIEWPATH_SHORT = "-v";

	public static final String CUSTOMCONFIG_SHORT = "-c";

	public static final String PLUGINS_SHORT = "-n";
	
	public static final String BACKGROUNDCOLOR_SHORT = "-b";
	
	public static final String FOREGROUNDCOLOR_SHORT = "-e";
	
	public static final String READONLY_SHORT = "-r";
	

	static {
		paramCommands = new Hashtable(10);
		paramCommands.put(HOSTNAME_SHORT, HOSTNAME);
		paramCommands.put(HOSTPORT_SHORT, HOSTPORT);
		paramCommands.put(PROTOCOL_SHORT, PROTOCOL);
		paramCommands.put(DOWNLOADPATH_SHORT, DOWNLOADPATH);
		paramCommands.put(UPLOADPATH_SHORT, UPLOADPATH);
		paramCommands.put(UPLOADFILE_SHORT, UPLOADFILE);
		paramCommands.put(MAPFILE_SHORT, MAPFILE);
		paramCommands.put(VIEWPATH_SHORT, VIEWPATH);
		paramCommands.put(CUSTOMCONFIG_SHORT, CUSTOMCONFIG);
		paramCommands.put(PLUGINS_SHORT, PLUGINS);
		paramCommands.put(READONLY_SHORT, READONLY);
	}

	/**
	 * Hint:  applet, args and customProperties can be null
	 * @param args
	 * @param applet
	 * @param customProperties
	 * @param defaultPropertiesPath
	 * @param defaultImagesPath
	 */
	public SessionParameters(String[] args, Applet applet, String customProperties, String defaultPropertiesPath, String defaultImagesPath) {
		DEFAULT_PROPERTIES_FILE = defaultPropertiesPath;
		DEFAULT_IMAGES_PATH = defaultImagesPath;
        putApplicationParameters(args);
		this.applet = applet;
        initLoaders(customProperties);
	}

	/**
	 * push the default properties file and image search path, then push the plugins
	 * bundles and finally the custom properties file. Warning, we take care of taking
	 * the plugins paths from the custom properties file, but we push the custom
	 * properties file only at last so that it can potentially override everything.
	 * @param customProperties
	 */
	public void initLoaders(String customProperties) {
		// the minimal configuration of JGraphpad:
		Translator.pushPropertiesBundle(DEFAULT_PROPERTIES_FILE);
		ImageLoader.pushSearchPath(DEFAULT_IMAGES_PATH);
        
		String pluginPaths = null;
		ResourceBundle resourcebundle = null;
        
		if (customProperties != null) {//else may be a custom properties file wants to register its plugins:
			try {
				customProperties=customProperties+".properties";
				resourcebundle = new PropertyResourceBundle(
						   Translator.class.getResourceAsStream(customProperties));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            //resourcebundle = PropertyResourceBundle.getBundle(customProperties);
            pluginPaths = resourcebundle.getString("PluginPropertiesPath");
        }
        else {   //by defaulf we register the default plugins:
            String sessionPluginPaths = getParam(PLUGINS, false);
            if ( sessionPluginPaths.equals("no"))//no plugin
            	pluginPaths = "";
            else if (sessionPluginPaths.equals(""))//default plugins
            	pluginPaths = Translator.getString("PluginPropertiesPath");
            else           // plugin paths has been set up via command line or as applet parameters:
        		pluginPaths = sessionPluginPaths.replaceAll(",", " ");//required because Firefox at least can't pass an applet param containing spaces; so comma separators are required!
        }

		String[] values;
		values = Utilities.tokenize(pluginPaths);
		for (int i = 0; i < values.length; i++) {
			try {
				Translator.pushPropertiesBundle(values[i]);
			} catch (Exception ex) {
				System.out.print("\nCAN'T FIND PROPERTIES FILE :" + values[i]
						+ " ; THE PLUGIN MIGHT BE SIMPLY MISSING");
			}
		}
		
		// finally, the custom properties file overrides the plugins and the default properties:
		if (resourcebundle != null) {
			Translator.getBundles().push(resourcebundle);
			Translator.getBundleNames().push(customProperties);
		}

		// custom image search paths properties if any:
		values = Utilities.tokenize(Translator.getString("ImagePaths"));
		for (int i = 0; i < values.length; i++) {
			try {
				ImageLoader.pushSearchPath(values[i]);
			} catch (MissingResourceException ex) {
				System.out.print("\nCAN'T FIND PROPERTIES FILE :" + values[i]
						+ ".properties; THE PLUGIN MIGHT BE SIMPLY MISSING");
			}
		}

		// translations if any:
		values = Utilities.tokenize(Translator.getString("TranslationsPath"));
		for (int i = 0; i < values.length; i++) {
			try {
				Translator.pushLocalizedBundle(values[i]);
			} catch (MissingResourceException ex) {
				System.out.print(ex);
			}
		}
	}

	public void setParamWithCommand(String command, String value) {
		String key = (String) paramCommands.get(command);
		setParam(key, value);
	}

	public void setParam(String key, String value) {
		sessionParameters.put(key, value);
		if (key.equals(CUSTOMCONFIG)) {
			Translator.pushPropertiesBundle(value);
		}
	}
	
	private void putApplicationParameters(String[] args) {
		if (args == null)
			return;
		for (int i = 0; i < args.length; i = i + 2) {
			if (args[i].indexOf("-") < 0) {// simple precaution in case
											// arguments aren't well formed
				i = i - 1;
				continue;
			}
			setParamWithCommand(args[i], args[i + 1]);
		}
	}

	/**
	 * Try to get the parameter from the applet if any. If not, then it looks if
	 * there is one in the session parameters map. Finally, if it's not defined
	 * and if we allow it, then we ask the user to enter the parameter.
	 * 
	 * @param key
	 * @param askIfNone
	 * @return
	 */
	public String getParam(String key, boolean askIfNone) {
		String value;
		if (isApplet()) {
			value = applet.getParameter(key);
			if (value != null)
				return value;

			// default parameters if none is passed:
			if (key == HOSTPORT)
				return Integer.toString(applet.getCodeBase().getPort());
			if (key == HOSTNAME)
				return applet.getCodeBase().getHost();
			if (key == PROTOCOL)
				return applet.getCodeBase().getProtocol();
		} 
		Object object = sessionParameters.get(key);
		if (object != null) {
			return (String) object;
		}
		if (askIfNone) {
			return JOptionPane.showInputDialog(null,

			"the parameter '" + key + "' is missing, please enter a value:",

			"parameter dialog box",

			JOptionPane.QUESTION_MESSAGE);
		} else {
			if (key == HOSTPORT)
				return "80";
			if (key == PROTOCOL)
				return "http";
			return "";
		}
	}
	
	public Color getBackgroundColor() {
		String p = getParam(BACKGROUNDCOLOR, false);
		Color bc = Color.WHITE;
		if (p != null && p != "") {
			bc = Color.decode(p);
		}
		return bc;
	}
	
	public Color getForegroundColor() {
		String p = getParam(FOREGROUNDCOLOR, false);
		Color fc = Color.BLACK;
		if (p != null && p != "") {
			fc = Color.decode(p);
		}
		return fc;
	}

	public Map getSessionParameters() {
		return sessionParameters;
	}

	public void setSessionParameters(Map sessionParameters) {
		this.sessionParameters = sessionParameters;
	}

	public Applet getApplet() {
		return applet;
	}

	public void setApplet(Applet applet) {
		this.applet = applet;
	}

	public boolean isApplet() {
		return (applet != null);
	}
}
