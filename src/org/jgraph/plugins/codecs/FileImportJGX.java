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

import org.jgraph.JGraph;
import org.jgraph.graph.GraphModel;
import org.jgraph.pad.actionsbase.eager.AbstractActionFile;
import org.microplatform.loaders.PluginInvoker;
import org.microplatform.loaders.PluginInvoker.NamedInputStream;

public class FileImportJGX extends AbstractActionFile {

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
		try {
			NamedInputStream in = PluginInvoker.provideInputStream(".jgx");
			if (in != null) {
				JGraph graph = (JGraph) PluginInvoker.instanciateObjectForKey("JGraph.class");
				graph.setModel((GraphModel) PluginInvoker.instanciateObjectForKey("GraphModel.class"));
				mdiContainer.addDocument(in.name, null);
				JGraphJGX.getInstance(mdiContainer).read(in.in, getCurrentGraph());
				mdiContainer.update();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Empty implementation. This Action should be available each time.
	 */
	public void update() {
	}
}
