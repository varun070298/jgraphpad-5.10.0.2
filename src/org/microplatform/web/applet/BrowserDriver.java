package org.microplatform.web.applet;

import java.lang.reflect.Method;

import org.microplatform.SessionParameters;
import org.microplatform.gui.MDIContainer;
import org.microplatform.loaders.PluginInvoker.IBrowserDriver;

public class BrowserDriver implements IBrowserDriver {
	
	 /**
     * Execute a javascript command in the browser when used as an applet
     * @param cmd
     */
    public  void execJavascript(String cmd, MDIContainer mdiContainer) {
		try {
			Class c =
			    Class.forName("netscape.javascript.JSObject");
			
			try {
				Method getWindow = null, eval = null;
				Method ms[] = c.getMethods();
				  for (int i = 0; i < ms.length; i++) {
				      if (ms[i].getName().compareTo("getWindow") == 0)
				    	  getWindow = ms[i];
				      else if (ms[i].getName().compareTo("eval") == 0)
				         eval = ms[i];
				      }
				try {
					Object wind = getWindow.invoke(c, new Object[] {mdiContainer.getSessionParameters().getApplet()});
					eval.invoke(wind, new Object[] {cmd});
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * You could override this to rather update the server repository...
     */
    public void notifyBrowserFileChanged(MDIContainer mdiContainer) {
		String id = mdiContainer.getSessionParameters().getParam(SessionParameters.ID, false);
    	execJavascript("clientChanged('" + id + "')", mdiContainer);
    }
}
