 /*
 * @(#)Graphpad.java	1.2 11/11/02
 *
 * Copyright (C) 2001-2004 Gaudenz Alder
 *
 * GPGraphpad is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * GPGraphpad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPGraphpad; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.jgraph.pad.jgraphpad;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.jgraph.graph.AbstractCellView;
import org.jgraph.graph.DefaultGraphCellEditor;
import org.jgraph.graph.EdgeRenderer;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.PortView;
import org.microplatform.Document;
import org.microplatform.SessionParameters;
import org.microplatform.SwingWorker;
import org.microplatform.actions.FileOpenURL;
import org.microplatform.gui.DocFrame;
import org.microplatform.gui.MDIContainer;
import org.microplatform.loaders.ImageLoader;
import org.microplatform.loaders.PluginInvoker;
import org.microplatform.loaders.Translator;

/**
 * A class with some static methods (including main and init) to properly
 * instanciate a GPGraphpad (actually a GPGraphpad JPanel) which is the main
 * JPanel where mutli JGraph document and GUI are displayed. You can do it
 * either as an application, either as an applet.
 * 
 * @see org.microplatform.gui.MDIContainer
 */
public class JGraphpad extends Applet {
	
	/**
	 * is properly set by the ant buildfile to ensure the source version match
	 * the binary version
	 */
	public static final String VERSION = "JGraphpad (v5.10.0.2)";

	/**
	 * Having a static reference to the GPGraphpad is useful when using
	 * LiveConnect to interact between HTML and the GPGraphpad; however you
	 * could use several GPGraphpad at the same time if you prefer
	 */
	public MDIContainer appletPad;

	/**
	 * Useful reference to the applet launcher button when used along with the
	 * LiveConnect technology (to trigger a startup from an HTML event)
	 */
	public  JButton appletStartButton;

	/**
	 * entry point when the JGraphpad application is deployed either offline,
	 * either via webstart
	 */
	public static void main(String[] args) {
		JFrame gpframe = new JFrame();
		MDIContainer pad = createPad(new SessionParameters(args, null, null,
				"/org/jgraph/pad/resources/Graphpad",
		"/org/jgraph/pad/resources"), gpframe);
		gpframe.setVisible(true);
		reload(pad);
	}

	/**
	 * Automatic entry point when deploying JGraphpad as an applet
	 */
	public void init() {		
		final  JFrame gpframe = new JFrame() ;
		setVisible(true);		
		
		appletPad = createPad( new SessionParameters(null, this, null,
				"/org/jgraph/pad/resources/Graphpad",
		"/org/jgraph/pad/resources"), gpframe);

		setLayout(new BorderLayout());
		if (this.getParameter(SessionParameters.NEWFRAME) != null) {
			appletStartButton = new JButton("Edit diagram...");
			appletStartButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					reload(appletPad);
					gpframe.setVisible(true);
				}
			});
			add(appletStartButton);
		} else {
			add(gpframe.getContentPane());
			reload(appletPad);
		}
		
		//HTML page notification:
		String id = this.getParameter(SessionParameters.ID);
		String jsNotif = "javascript:started('" + id + "')";
		try {
			appletPad.getPluginInvoker().execJavascript(jsNotif);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void reload(final MDIContainer  pad) {
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				((FileOpenURL) pad.getCommand("FileOpenURL"))
						.tryToLoadFile(true, null);
				return null;
			}
		};
		worker.start();
	}

	/**
	 * This method is called when the user press the applet button and also at
	 * the applet start up. The button is especially handy to refresh the applet
	 * without having to reload the page.
	 */
	public static MDIContainer createPad(SessionParameters sessionParameters, JFrame gpframe) {
		MDIContainer pad = new MDIContainer(sessionParameters) {
			public void addDocument(String name, InputStream input) {
				GPGraphpadFile file = null;

				if (input != null) {
					try {
						file = new GPGraphpadFile();
						file = (GPGraphpadFile) file.read(input);
					} catch (IOException e) {
						this.error(e.toString());
						e.printStackTrace();
					}
				}

				boolean needUpdate = (getCurrentDocument() == null);
				Document doc = new GPDocument(this, name, file);// concrete
				// minimal
				// document
				doc = PluginInvoker.decorateDocument(doc);// addition of the
				// registered
				// decorators
				DocFrame iframe = new DocFrame(doc);
				addGPInternalFrame(iframe);
				iframe.show();
				doc.makeDocBar();
				if (needUpdate)
					update();
				iframe.validate();
			}
		};
		pad.init();

		// create the application frame:
		gpframe.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		gpframe.setLocation(0, 0);
		gpframe.setIconImage(ImageLoader.getImageIcon(
				Translator.getString("Icon")).getImage());
		gpframe.setTitle(Translator.getString("Title"));

		gpframe.getContentPane().add(pad);
		gpframe.addWindowListener(pad.getAppCloser());
		String plaf = Translator.getString("LookAndFell.class");

		if (plaf != null && !plaf.equals("")) {
			try {
				UIManager.setLookAndFeel(plaf);
			} catch (Exception e) {
			}
		}
		PortView.allowPortMagic = false;
		
		//statix fix against applet Sun bug and this is also an editor that will wrapp a GPUserObject:
		AbstractCellView.cellEditor = new DefaultGraphCellEditor() {
			public Object getCellEditorValue() {
				Object value = realEditor.getCellEditorValue();
				if (value instanceof GPUserObject)
					return value;
				else if (value instanceof String) {
					GPUserObject wrapper = new GPUserObject();
					wrapper.setValue(value);
					return wrapper;
				}
				else return value;
			}
		};
		
		// static fix::
		EdgeView.renderer = new EdgeRenderer();
		
		return pad;
	}
	
	public void stop() {
		if ( getParameter(SessionParameters.READONLY) != null && !getParameter(SessionParameters.READONLY).equals("true"))
			appletPad.getCommand("FileExit").actionPerformed(null);
	}
}
