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
package org.jgraph.plugins.codecs;

import java.awt.event.ActionEvent;

import org.jgraph.pad.actionsbase.eager.AbstractActionFile;
import org.microplatform.loaders.PluginInvoker;
import org.microplatform.loaders.PluginInvoker.NamedOutputStream;

/**
 * Action opens a dialog to select the file. After that the action saves the
 * current graph to the selected file.
 */
public class FileExportJGX extends AbstractActionFile {

	public void actionPerformed(ActionEvent e) {
		try {
			NamedOutputStream out = PluginInvoker.provideOutputStream(".jgx", getCurrentGPDocument().getDocComponent().getName(), false);
			getCurrentGPDocument().getDocComponent().setName(out.name);
			JGraphJGX.getInstance(mdiContainer).write(out.out, getCurrentGPDocument());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
