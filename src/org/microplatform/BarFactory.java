/*
 * Copyright (C) 2001-2004 Gaudenz Alder
 * 
 * 6/01/2006: I, Raphpael Valyi, changed back the header of this file to LGPL
 * because nobody changed the file significantly since the last
 * 3.0 version of GPGraphpad that was LGPL. By significantly, I mean: 
 *  - less than 3 instructions changes could honnestly have been done from an old fork,
 *  - license or copyright changes in the header don't count
 *  - automaticaly updating imports don't count,
 *  - updating systematically 2 instructions to a library specification update don't count.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package org.microplatform;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.microplatform.loaders.ImageLoader;
import org.microplatform.loaders.LocaleChangeAdapter;
import org.microplatform.loaders.Translator;
import org.microplatform.loaders.TranslatorConstants;

/**
 * The bar factory creates the menubars and the toolbars.
 * 
 * For Framework users: You can insert you own bar entries by register each
 * entry at the static method <tt>addBarEntry</tt>.<br>
 * <br>
 * Example:
 * 
 * <pre>
 * GPBarFactory.addBarEntry(new BarEntry(&quot;File&quot;, 15, &quot;FileCopy&quot;));
 * </pre>
 * 
 */
public class BarFactory implements TranslatorConstants {

	/**
	 * Main key for the menu bar
	 */
	public static final String MENUBAR = "Menubar";

	/**
	 * Main key for the toolbars
	 */
	public static final String PADTOOLBARS = "PadToolbars";

	/**
	 * Key for the default document toolbars
	 */
	public static final String DOCTOOLBARS = "DocToolbars";

	/**
	 * Main key for the graph popup menu
	 */
	public static final String GRAPH_POPUP = "GraphPopup";

	/**
	 * Main key for the library popup menu
	 */
	public static final String LIBRARY_POPUP = "LibraryPopup";

	/**
	 * ArrayList with Bar entries
	 */
	protected  final Hashtable barEntries = new Hashtable();
	
	private static BarFactory _instance;

	/**
	 * singleton constructor
	 */
	private BarFactory() {
	}
	
	public static BarFactory getInstance() {
		if (null == _instance) {
			_instance = new BarFactory();
		}
		return _instance;
	}
	
	public static void setInstance(BarFactory barFactory) {
		_instance = barFactory;
	}

	/**
	 * This is the hook through which all menu items are created. It registers
	 * the result with the menuitem hashtable so that it can be fetched with
	 * getMenuItem().
	 */
	protected Component[] createMenuItem(final String cmd,  final ICommandRegistery actionRegistery
			 ) {
		if (cmd == null)
			return new Component[] {};

		String subMenu = Translator.getString(cmd + SUFFIX_MENU);
		if (subMenu != null) {
			String[] itemKeys = tokenize(cmd + SUFFIX_MENU, subMenu);
			return new Component[] { createMenu(cmd, itemKeys, actionRegistery) };
		}
		Action a = getAction(cmd, actionRegistery);

		if (a == null)
			return new Component[] {};

		if (a instanceof ActionCommand) {
			return ((ActionCommand) a).getMenuComponents();
		}
		JMenuItem item = new JMenuItem();
		item.setAction(a);
		fillMenuButton(item, cmd, "");
		return new Component[] { item };
	}

	/**
	 * Create the menubar for the app. By default this pulls the definition of
	 * the menu from the associated resource file.
	 */
	public JMenuBar createMenubar( final ICommandRegistery actionRegistery ) {
		final JMenuBar mb = new JMenuBar();

		final String[] menuKeys = tokenize(MENUBAR, Translator
				.getString(MENUBAR));
		for (int i = 0; i < menuKeys.length; i++) {
			final String itemKey = Translator.getString(menuKeys[i]
					+ SUFFIX_MENU);
			if (itemKey == null) {
				System.err.println("Can't find MenuKey: '" + menuKeys[i]
						+ "'. I'm ignoring the MenuKey!");
				continue;
			}
			final String[] itemKeys = tokenize(menuKeys[i], itemKey);
			final JMenu m = createMenu(menuKeys[i], itemKeys, actionRegistery);
			if (m != null)
				mb.add(m);
		}
		return mb;
	}

	/**
	 * creates the popup menu for the graph
	 */
	public JPopupMenu createGraphPopupMenu( final ICommandRegistery actionRegistery
			 ) {
		return createPopupMenu(GRAPH_POPUP, actionRegistery);
	}

	/**
	 * creates the popup menu for the library
	 */
	public JPopupMenu createLibraryPopupMenu( final ICommandRegistery actionRegistery
			 ) {
		return createPopupMenu(LIBRARY_POPUP, actionRegistery);
	}

	/**
	 * creates a popup menu for the specified key.
	 */
	public JPopupMenu createPopupMenu(final String key,  final ICommandRegistery actionRegistery
			 ) {
		final JPopupMenu pop = new JPopupMenu();
		final String[] itemKeys = tokenize(key, Translator.getString(key));
		for (int i = 0; i < itemKeys.length; i++) {
			if (itemKeys[i].equals("-")) {
				pop.addSeparator();
			} else {
				final Component[] mi = createMenuItem(itemKeys[i], actionRegistery
						);
				for (int j = 0; j < mi.length; j++) {
					pop.add(mi[j]);
				}
			}
		}

		LocaleChangeAdapter.updateContainer(pop);

		return pop;
	}

	/**
	 * creates a menu for the specified key
	 */
	protected JMenu createMenu(final String key,
			final ICommandRegistery actionRegistery) {

		return createMenu(key, tokenize(key, Translator.getString(key)), actionRegistery
				);
	}

	/**
	 * Create a menu for the app. By default this pulls the definition of the
	 * menu from the associated resource file.
	 */
	protected JMenu createMenu(final String key, final String[] itemKeys,  final ICommandRegistery actionRegistery) {
		final JMenu menu = new JMenu();
		menu.setName(key);
		for (int i = 0; i < itemKeys.length; i++) {
			if (itemKeys[i].equals("-")) {
				menu.addSeparator();
			} else {
				final Component[] mi = createMenuItem(itemKeys[i],
						actionRegistery);
				for (int j = 0; j < mi.length; j++) {
					if (mi[j] != null)
						menu.add(mi[j]);
				}
			}
		}

		final ImageIcon icon = ImageLoader.getImageIcon(Translator
				.getString(key + SUFFIX_IMAGE));
		if (icon != null) {
			menu.setHorizontalTextPosition(JButton.RIGHT);
			menu.setIcon(icon);
		}

		// set mnemonic for the JMenus
		final String mnemonic = Translator.getString(key + SUFFIX_MNEMONIC);
		if (mnemonic != null && mnemonic.length() > 0)
			menu.setMnemonic(mnemonic.toCharArray()[0]);

		return menu;
	}

	/**
	 * creates a panel with the toolbars into. For each toolbar a panel was
	 * created. The inner panel is the return value. The outside panel is the
	 * parameter.
	 * 
	 * @param toolBarMainPanel
	 *            The outside panel.
	 * @param barKey
	 * @return The inner panel
	 */
	public JPanel createToolBars(final JPanel toolBarMainPanel,
			final String barKey, final ICommandRegistery actionRegistery) {
		final String toolBarsKey = Translator.getString(barKey);
		if (toolBarsKey == null) {
			System.err
					.println("Can't find Key: 'toolbars'. I'm ignoring the MenuKey!");
			return toolBarMainPanel;
		}

		final String[] toolBars = tokenize(barKey, toolBarsKey);
		JPanel innerPanel = toolBarMainPanel;
		for (int i = 0; i < toolBars.length; i++) {
			final String label = Translator.getString(toolBars[i]
					+ SUFFIX_LABEL);
			innerPanel.add(createToolbar(toolBars[i], label, actionRegistery),
					BorderLayout.NORTH);
			final JPanel oldInnerPanel = innerPanel;
			innerPanel = new JPanel(new BorderLayout());
			oldInnerPanel.add(innerPanel, BorderLayout.CENTER);
		}
		return innerPanel;
	}

	/**
	 * Create the toolbar. By default this reads the resource file for the
	 * definition of the toolbar.
	 */
	protected Component createToolbar(final String key, final String label, final ICommandRegistery actionRegistery
			 ) {
		final JToolBar toolbar = new JToolBar(label);
		// toolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		// toolbar.setFloatable(false);
		final String toolKey = Translator.getString(key);
		if (toolKey == null) {
			System.err.println("Can't find ToolBarKey: '" + toolKey
					+ "'. I'm ignoring the MenuKey!");
			return toolbar;
		}
		final String[] toolKeys = tokenize(key, toolKey);
		for (int i = 0; i < toolKeys.length; i++) {
			if (toolKeys[i].equals("-")) {
				toolbar.add(Box.createHorizontalStrut(5));
			} else {
				final Component[] comps = createTool(toolKeys[i], actionRegistery
						);
				for (int j = 0; j < comps.length; j++) {
					toolbar.add(comps[j]);
				}
			}
		}
		toolbar.add(Box.createHorizontalGlue());
		return toolbar;
	}

	/**
	 * Hook through which every toolbar item is created.
	 * @param actionRegistery TODO
	 */
	protected Component[] createTool(final String key, ICommandRegistery actionRegistery
			) {
		return createToolbarButton(key, actionRegistery);
	}

	/**
	 * Create a button to go inside of the toolbar. By default this will load an
	 * image resource. The image filename is relative to the classpath
	 * (including the '.' directory if its a part of the classpath), and may
	 * either be in a JAR file or a separate file.
	 * 
	 * @param key
	 *            The key in the resource file to serve as the basis of lookups.
	 * @param actionRegistery TODO
	 */
	protected Component[] createToolbarButton(final String key, ICommandRegistery actionRegistery
			) {
		final Action a = actionRegistery.getCommand(key);
		if (a instanceof ActionCommand) {
			return ((ActionCommand) a).getToolComponents();
		}
		final JButton item = new JButton();
		item.setAction(a);
		fillToolbarButton(item, key, "");
		return new Component[] { item };
	}

	/**
	 * returns the action for the cmd key. The method inspects the action map at
	 * the graph pad to get the correct action.
	 * @param actionRegistery TODO
	 */
	protected Action getAction(final String cmd, ICommandRegistery actionRegistery
			  ) {
		if (cmd == null)
			return null;
		return actionRegistery.getCommand(cmd);
	}

	/**
	 * fills the abstract button with values from the properties files.
	 * 
	 */
	public static AbstractButton fillMenuButton(final AbstractButton button,
			final String key, final String actionCommand) {
		button.putClientProperty(LocaleChangeAdapter.DONT_SET_TOOL_TIP_TEXT,
				Boolean.TRUE);
		return fillAbstractButton(button, key, actionCommand);
	}

	/**
	 * fills the abstract button with values from the properties files.
	 * 
	 */
	public static AbstractButton fillToolbarButton(final AbstractButton button,
			final String key, final String actionCommand) {
		button.putClientProperty(LocaleChangeAdapter.DONT_SET_MNEMONIC,
				Boolean.TRUE);
		button.putClientProperty(
				LocaleChangeAdapter.SET_TEXT_IF_ICON_NOT_AVAILABLE,
				Boolean.TRUE);
		return fillAbstractButton(button, key, actionCommand);
	}

	/**
	 * The method fills the AbstractButton with the localized label, the image,
	 * the accelerator and the mnemonic.
	 * 
	 */
	public static AbstractButton fillAbstractButton(
			final AbstractButton button, final String key,
			final String actionCommand) {

		if (key != null) {
			button.setName(key);
		}

		button.setActionCommand(actionCommand);

		return button;
	}

	/**
	 * Tokenizes the value for the key and integrates bar entries.
	 * 
	 * @see #integrateBarEntries(String, String[])
	 */
	protected final String[] tokenize(final String key, final String value) {
		String[] values = Utilities.tokenize(value);
		values = integrateBarEntries(key, values);
		return values;
	}

	/**
	 * Integrates bar entries, if available, for the key. If the position is out
	 * of the array the method ignores the bar entry.
	 * 
	 * @param key
	 *            Current key for the values
	 * @param values
	 *            The tokenized values for the key.
	 * 
	 */
	protected String[] integrateBarEntries(final String key,
			final String[] values) {
		Enumeration e_num;

		// get the bar entries for the key
		final Vector ArrayList4BarKey = (Vector) barEntries.get(key);

		// if there is no bar entry return
		if (ArrayList4BarKey == null || ArrayList4BarKey.size() == 0)
			return values;

		// build a mutable list with the old values
		final ArrayList listValues = new ArrayList();
		for (int i = 0; i < values.length; i++) {
			listValues.add(values[i]);
		}

		// insert the bar entries
		e_num = ArrayList4BarKey.elements();
		while (e_num.hasMoreElements()) {
			BarEntry barEntry = (BarEntry) e_num.nextElement();
			try {
				listValues.add(barEntry.getPos(), barEntry.getBarValue());
			} catch (Exception ex) {
				System.err.println("Error while integrating Bar Entry"
						+ barEntry);
				System.err.println(ex.getMessage());
			}
		}

		// build the new array with the old values and the
		// bar entries
		String[] newValues = new String[listValues.size()];
		for (int i = 0; i < listValues.size(); i++) {
			newValues[i] = (String) listValues.get(i);
		}
		return newValues;

	}
    
    public final class BarEntry {
        
        /** The name of the bar key where to insert this 
         *  bar entry.
         */
        String barKey;
        
        /** The position where to insert this 
         *  bar entry.
         */
        int pos;
        
        
        /** The name of this bar value. Must be equal with
         *  the Action name. 
         */
        String barValue;
        
        /**
         * Constructor for BarEntry.
         */
        public BarEntry(String barKey, int pos, String barValue) {
            this.barKey = barKey;
            this.pos = pos;
            this.barValue = barValue;
        }
        
        
        /**
         * Returns the barKey.
         * @return String
         */
        public String getBarKey() {
            return barKey;
        }
        
        /**
         * Returns the barValue.
         * @return String
         */
        public String getBarValue() {
            return barValue;
        }
        
        /**
         * Returns the pos.
         * @return int
         */
        public int getPos() {
            return pos;
        }
        
        /**
         * Sets the barKey.
         * @param barKey The barKey to set
         */
        public void setBarKey(String barKey) {
            this.barKey = barKey;
        }
        
        /**
         * Sets the barValue.
         * @param barValue The barValue to set
         */
        public void setBarValue(String barValue) {
            this.barValue = barValue;
        }
        
        /**
         * Sets the pos.
         * @param pos The pos to set
         */
        public void setPos(int pos) {
            this.pos = pos;
        }
        
        /** Prints the Entry with all properties.
         * 
         */ 
        public String toString(){
            StringBuffer b = new StringBuffer();
            b.append("BarEntry: barKey=");
            b.append(barKey);
            b.append("; pos=");
            b.append(pos);
            b.append("; barValue=");
            b.append(barValue);
            return b.toString() ;
        }
        
    }
}
