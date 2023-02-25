/*
 * @(#)GPAbstractActionDefault.java 1.2 29.01.2003
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

package org.microplatform;

import java.awt.Component;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;

import org.microplatform.gui.MDIContainer;
import org.microplatform.loaders.TranslatorConstants;

/**
 * An abstract GPGraphpad action. The base class for all GPGraphpad actions.
 * Warning: actions to be loaded by GPGraphpad are listed in the settings.xml
 * file. In order you can use it in the GUI, you should also register them in
 * the toolkit propertie file.
 */
public abstract class ActionCommand extends AbstractAction implements
		TranslatorConstants {

	/**
	 * A reference back to the graphpad. If an action was performed the Actions
	 * applies the changes to the current Document at the graphpad.
	 * 
	 * 
	 */
	protected MDIContainer mdiContainer;

	/**
	 * Constructor for GPAbstractActionDefault. The Abstract action uses the
	 * class name without package prefix as action name.
	 * 
	 * @see Action#NAME
	 */
	public ActionCommand() {
		putValue(Action.NAME, Utilities.getClassNameWithoutPackage(getClass()));
	}

	public final String getName() {
		return (String) getValue(NAME);
	}

	public Document getCurrentDocument() {
		return getMdiContainer().getCurrentDocument();
	}

	/**
	 * Creates by default an arry with one entry. The entry contains a JMenuItem
	 * which joins the instance of this Action.
	 */
	public Component[] getMenuComponents() {
		return new Component[] { getMenuComponent(null) };
	}

	/**
	 * Returns by default a list with one JButton. The button joints this
	 * action.
	 * 
	 * 
	 */
	public Component[] getToolComponents() {
		return new Component[] { getToolComponent(null) };
	}

	/**
	 * Returns a JMenuItem with a link to this action.
	 */
	protected Component getMenuComponent(final String actionCommand) {
		JMenuItem item = new JMenuItem(this);
		BarFactory.fillMenuButton(item, getName(), actionCommand);
		final String presentationText = getPresentationText(actionCommand);
		if (presentationText != null)
			item.setText(presentationText);

		return item;
	}

	/**
	 * Returns a clean JButton which has a link to this action.
	 */
	public Component getToolComponent(final String actionCommand) {
		final AbstractButton b = new JButton(this) {
			public float getAlignmentY() {
				return 0.5f;
			}
		};
		return BarFactory.fillToolbarButton(b, getName(), actionCommand);
	}

	/**
	 * empty implementation for this type of action
	 */
	public void update() {
		if (getMdiContainer().getCurrentDocument() == null)
			setEnabled(false);
		else
			setEnabled(true);
	}

	/**
	 * Should return presentation Text for the action command or null for the
	 * default
	 */
	public String getPresentationText(final String actionCommand) {
		return null;
	}

	/**
	 * Sets the graphpad.
	 * 
	 * @param appletPad
	 *            The graphpad to set
	 */
	public final void setMdiContainer(final MDIContainer gp) {
		mdiContainer = gp;
	}

	/**
	 * Returns the graphpad.
	 * 
	 * @return GPGraphpad
	 */
	public MDIContainer getMdiContainer() {
		return mdiContainer;
	}

}
