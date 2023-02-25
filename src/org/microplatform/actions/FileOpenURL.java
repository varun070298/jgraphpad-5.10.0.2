/*
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

package org.microplatform.actions;

import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import javax.swing.JOptionPane;

import org.microplatform.ActionCommand;
import org.microplatform.SessionParameters;
import org.microplatform.loaders.Translator;

public class FileOpenURL extends ActionCommand {

	/**
	 * Shows a file chooser with the file filters from the file formats to
	 * select a file.
	 * 
	 * Furthermore the method uses the selected file format for the read
	 * process.
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String url = JOptionPane.showInputDialog(Translator.getString(
				"URLDialog", new Object[] { "foo.xml" }));
		tryToLoadFile(false, url);
	}

	/**
	 * If a file has been specified by command line, applet or webstart
	 * argument, then we try to open it. If the url is null, we try to upload
	 * the file specified in the applet parameters or by command line.
	 * 
	 * @param newIfNone
	 * @param url
	 */
	public void tryToLoadFile(boolean newIfNone, String url) {
		try {
			URL theUrl;
			if (url != null)
				theUrl = new URL(url);
			else {
				try {
					url = mdiContainer.getSessionParameters().getParam(
							SessionParameters.DOWNLOADPATH, false);
					theUrl = new URL(mdiContainer.getSessionParameters().getParam(
							SessionParameters.PROTOCOL, false), mdiContainer
							.getSessionParameters().getParam(
									SessionParameters.HOSTNAME, false), Integer
							.parseInt(mdiContainer.getSessionParameters().getParam(
									SessionParameters.HOSTPORT, false)),
							url);
				} catch (RuntimeException e) {
					if (newIfNone)
						mdiContainer.addDocument(null, null);
					return; //error while parsing, probably not worth reporting it
				}
				if (url.equals("")) {
					if (newIfNone)
						mdiContainer.addDocument(null, null);
					return;
				}
			}
			mdiContainer.getStatusBar().setMessage("Downloading file from server, please wait...");
			URLConnection connec = theUrl.openConnection();
			connec.setUseCaches(false);//very important to account for the last updates
			InputStream input = connec.getInputStream();
			if (url.endsWith("gz")) {// compressed
				input = new GZIPInputStream(input);
			}
			mdiContainer.addDocument(url, input);
			return;
		} catch (Exception ex) {
			mdiContainer.error(ex.toString());
			ex.printStackTrace();
			if (newIfNone)
				mdiContainer.addDocument(null, null);
			return;
		}
	}

	/**
	 * Empty implementation. This Action should be available each time.
	 */
	public void update() {
	};
}
