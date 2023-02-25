/*
 * @(#)ToolBoxText.java 1.2 05.02.2003
 * 
 * Copyright (C) 2001-2004 Gaudenz Alder
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */
package org.jgraph.pad.palettebase;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphCell;
import org.jgraph.pad.graphcellsbase.JGraphMultilineView;
import org.jgraph.pad.jgraphpad.GPUserObject;
import org.jgraph.pad.subjgraph.GPCellViewFactory;

public class ToolBoxText extends AbstractDefaultVertexnPortsCreator {

	public GraphCell createCell() {
		GPUserObject wrapper = new GPUserObject();
		wrapper.setValue("Type Here");
		return new DefaultGraphCell();
	}

	public void actionForCell(DefaultGraphCell cell) {
		getCurrentGPDocument().getGraph().startEditingAtCell(cell);
	}
	
	public void adaptAttributeMap(GraphCell cell, AttributeMap attributeMap) {
		GPCellViewFactory.setViewClass(attributeMap,
				JGraphMultilineView.class.getName());
	}
}
