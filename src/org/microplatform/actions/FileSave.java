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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.microplatform.ActionCommand;
import org.microplatform.loaders.Translator;

/**
 * Action opens a dialog to select the file. After that the action saves the
 * current graph to the selected file.
 */
public class FileSave extends ActionCommand {

	public void actionPerformed(ActionEvent e) {
		{
			if (mdiContainer.getSessionParameters().isApplet()) {//upload rather than save to local disk...
				mdiContainer.getCommand("FileUploadToServer").actionPerformed(e);
				return;
			}
			String fileName = getCurrentDocument().getDocComponent().getName();
			if (fileName == null || fileName.startsWith(Translator.getString("NewGraph"))) {
				mdiContainer.getCommand("FileSaveAs").actionPerformed(e);
				return;
			}
			try {
				OutputStream out = new FileOutputStream(fileName);
				if (fileName.endsWith("gz"))
					out = new GZIPOutputStream(out);
				getCurrentDocument().getFile().saveFile(out);
				getCurrentDocument().setModified(false);
			} catch (SecurityException ex) {//we are probably a webstart app; here we will for the user to choose a name again. Doing so isn't optimpal, but the use case is so rare that it's not worth the coding effort
				mdiContainer.getCommand("FileSaveAs").actionPerformed(e);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
