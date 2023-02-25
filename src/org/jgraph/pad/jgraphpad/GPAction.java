/*
 * @(#)GPAbstractActionDefault.java	1.2 29.01.2003
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

package org.jgraph.pad.jgraphpad;

import java.util.Map;

import org.jgraph.JGraph;
import org.jgraph.graph.GraphLayoutCache;
import org.microplatform.ActionCommand;

/** An abstract GPGraphpad action.
 * 	The base class for
 *  all GPGraphpad actions. Warning: actions to be loaded
 *  by GPGraphpad are listed in the settings.xml file. In order
 *  you can use it in the GUI, you should also register them
 *  in the toolkit propertie file.
 */
public abstract class GPAction
	extends ActionCommand {
    
    public GPDocument getCurrentGPDocument() {
        try {
            return (GPDocument) mdiContainer.getCurrentDocument();
        } catch (RuntimeException e) {
            mdiContainer.error("This action cant't be applied to this document!");
            e.printStackTrace();
            return null;
        }
    }
    
	public JGraph getCurrentGraph() {
		return getCurrentGPDocument().getGraph();
	}

	public GraphLayoutCache getCurrentGraphLayoutCache() {
		return getCurrentGPDocument().getGraphLayoutCache();
	}
    

    public void setSelectionAttributes(final Map map) {
        if (getCurrentGPDocument() != null)
            getCurrentGPDocument().setSelectionAttributes(map);
    }

    public void setFontSizeForSelection(final float size) {
        getCurrentGPDocument().setFontSizeForSelection(size);
    }

    public void setFontStyleForSelection(final int style) {
        getCurrentGPDocument().setFontStyleForSelection(style);
    }

    public void setFontNameForSelection(final String fontName) {
        getCurrentGPDocument().setFontNameForSelection(fontName);
    }
}
