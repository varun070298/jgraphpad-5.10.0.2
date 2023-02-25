/*
 * @(#)FileSaveAs.java	1.2 30.01.2003
 *
 * Copyright (C) 2001-2004 Gaudenz Alder
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package org.microplatform.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import org.microplatform.ActionCommand;
import org.microplatform.io.ExtensionFilter;
import org.microplatform.io.FileService;
import org.microplatform.io.PreferencesService;
import org.microplatform.loaders.Translator;
import org.microplatform.loaders.PluginInvoker.NamedOutputStream;
import org.microplatform.loaders.PluginInvoker.NamedOutputStreamProvider;

public class FileSaveAs extends ActionCommand implements
		NamedOutputStreamProvider {

	public void actionPerformed(ActionEvent e) {

		String extension = Translator.getString("FileExtension");
		NamedOutputStream out = provideOutput(extension, getCurrentDocument()
				.getDocComponent().getName(), extension.endsWith("gz"));
		if (out != null) {
			try {
				getCurrentDocument().getFile().saveFile(out.out);
				getCurrentDocument().getDocComponent().setName(out.name);
				out.out.close();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			getCurrentDocument().getDocComponent().setName(
					getCurrentDocument().getDocComponent().getName());
			getCurrentDocument().setModified(false);
			mdiContainer.update();
			mdiContainer.invalidate();
		}
	}

	public NamedOutputStream provideOutput(String fileExtension,
			String nameToBeConfirmed, boolean isZipped) {
		PreferencesService preferences = PreferencesService
				.getInstance(FileSaveAs.class);
		ExtensionFilter fileExtensionFilter = new ExtensionFilter(
				nameToBeConfirmed, new String[] { fileExtension });

		ArrayList recentFiles = new ArrayList();
		File lastDir = new File(".");
		String recent = preferences.get("recent", "").trim();
		if (recent.length() > 0) {
			recentFiles.addAll(Arrays.asList(recent.split("[|]")));
			lastDir = new File((String) recentFiles.get(0)).getParentFile();
		}
		FileService fileService = FileService.getInstance(lastDir);

		try {
			FileService.Save save = fileService.save(null, nameToBeConfirmed,
					fileExtensionFilter, null, fileExtension);
			if (save.getOutputStream() == null)
				return null;
			FilterOutputStream out;
			if (isZipped) {
				out = new GZIPOutputStream(save.getOutputStream());
			} else {
				out = new FilterOutputStream(save.getOutputStream());
			}
			return new NamedOutputStream(out, save.getName());

		} catch (IOException exception) {
			exception.printStackTrace();
			return null;
		}
	}
}
