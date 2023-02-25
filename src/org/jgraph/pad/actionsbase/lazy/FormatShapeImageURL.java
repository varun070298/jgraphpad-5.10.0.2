/*
 * @(#)FileLibraryOpen.java	1.2 29.01.2003
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

package org.jgraph.pad.actionsbase.lazy;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphConstants;
import org.jgraph.pad.actionsbase.eager.AbstractActionFile;
import org.microplatform.loaders.Translator;

/**
 * Action that opens a library from a file.
 */
public class FormatShapeImageURL extends AbstractActionFile {

	public void actionPerformed(ActionEvent e) {
		String name =
			JOptionPane.showInputDialog(Translator.getString("URLDialog", new Object[]{"image.jpg"}));
		if (name != null) {
			try {
				try {
					URL location = new URL(name);
					ImageIcon icon = new ImageIcon(location);
					AttributeMap map = new AttributeMap();
					GraphConstants.setIcon(map, icon);
					setSelectionAttributes(map);
				} catch (Exception ex) {
					mdiContainer.error(ex.toString());
					ex.printStackTrace();
				}
			} catch (Exception ex) {
				mdiContainer.error(ex.toString());
			} finally {
				mdiContainer.repaint();
			}
		}
	}

}
