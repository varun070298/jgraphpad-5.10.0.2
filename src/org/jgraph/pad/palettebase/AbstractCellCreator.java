/*
 * Created on 17 juin 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jgraph.pad.palettebase;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JToggleButton;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphCell;
import org.jgraph.pad.jgraphpad.GPAction;
import org.microplatform.BarFactory;

/**
 * @author Raphaël Valyi
 * 
 * This is the super class of various java beans containing their own data for
 * the creation of a given graph cells once a button is pressed.
 * @see org.jgraph.pad.palettebase.AbstractDefaultVertexnPortsCreator
 * @see org.jgraph.pad.palettebase.AbstractDefaultEdgeCreator
 */
public abstract class AbstractCellCreator extends GPAction {

	private JToggleButton button = new JToggleButton();

	/**
	 * Does nothing by default, override it if you need
	 */
	public void adaptAttributeMap(GraphCell cell, AttributeMap attributeMap) {
	}

	public void actionPerformed(ActionEvent e) {
	}

	public void update() {
		super.update();
		this.getButton().setEnabled(isEnabled());
	}

	public Component getToolComponent(String actionCommand) {
		BarFactory.fillToolbarButton(this.getButton(), getName(),
				actionCommand);
		return this.getButton();
	}

	/**
	 * This hook allow to take a special action just after creating the cell For
	 * instance editing the cell.
	 * 
	 * @param cell
	 */
	public void actionForCell(GraphCell cell) {
		// example:
		// graphpad.getCurrentGraph().startEditingAtCell(cell);
	}

	public JToggleButton getButton() {
		return button;
	}

	public void setButton(JToggleButton button) {
		this.button = button;
	}
}
