package org.microplatform.gui;
/*
 * @(#)GPDocFrame.java 1.0 06.08.2003
 *
 * Copyright (C) 2003 sven_luzar
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.KeyStroke;

import org.microplatform.loaders.LocaleChangeAdapter;


/**
 * One Layer between the JInternalFrame 
 * and our implementation. 
 * Currently we add a load and store 
 * management for the window position.
 * and a locale change listener support 
 */
public class InternalFrame extends JInternalFrame {

    /** Key description for the ESC key.
     * 
     */
    protected KeyStroke escKeystroke =
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

    /** Calls the super constructor
     *  and adds the instance to the position manager
     *  and the locale change adapter
     * 
     */
    public InternalFrame() {
        super();
        initInternalFrame();
    }

    /** Calls the super constructor
     *  and adds the instance to the position manager
     *  and the locale change adapter
     * 
     * @param title
     */
    public InternalFrame(String title) {
        super(title);
        initInternalFrame();
    }

    /** Calls the super constructor
     *  and adds the instance to the position manager
     *  and the locale change adapter
     *
     * @param title
     * @param resizable
     */
    public InternalFrame(String title, boolean resizable) {
        super(title, resizable);
        initInternalFrame();
    }

    /** Calls the super constructor
     *  and adds the instance to the position manager
     *  and the locale change adapter
     *
     * @param title
     * @param resizable
     * @param closable
     */
    public InternalFrame(String title, boolean resizable, boolean closable) {
        super(title, resizable, closable);
        initInternalFrame();
    }

    /** Calls the super constructor
     *  and adds the instance to the position manager
     *  and the locale change adapter
     *
     * @param title
     * @param resizable
     * @param closable
     * @param maximizable
     */
    public InternalFrame(
        String title,
        boolean resizable,
        boolean closable,
        boolean maximizable) {
        super(title, resizable, closable, maximizable);
        initInternalFrame();
    }

    /** Calls the super constructor
     *  and adds the instance to the position manager
     *  and the locale change adapter
     *
     * @param title
     * @param resizable
     * @param closable
     * @param maximizable
     * @param iconifiable
     */
    public InternalFrame(
        String title,
        boolean resizable,
        boolean closable,
        boolean maximizable,
        boolean iconifiable) {
        super(title, resizable, closable, maximizable, iconifiable);
        initInternalFrame();
    }
    
    /**
     *  Adds the instance to the position manager and to
     *  the locale change adapter. 
     *  Registers the default esc action.
     * 
     */
    
    private void initInternalFrame(){
    	Dimension d;
				Container cont = this.getParent();
				if (cont == null
						|| (cont.getSize().width == 0 && cont.getSize().height == 0)) {
					// use the dimension if no more information is available
					d = new Dimension(600, 400);
				} else {
					// will use the desktop pane size
					// if available
					d = cont.getSize();
				}
				
				int h = d.height;
				int w = d.width;
				int height = (int) (h * 0.66);
				int width = (int) (w * 0.66);
				int x = (int) ((double) (h - height) / 2);
				int y = (int) ((double) (w - width) / 2);
				
				Rectangle r = new Rectangle(x, y, width, height);
				setBounds(r);
    }  

    /** makes an update for the locale
     *  dependent values from the whole
     *  container and calls
     *  the super implementation 
     *  
     * @see java.awt.Component#validate()
     * @see java.awt.Container#validate()
     */
    public void validate() {
        LocaleChangeAdapter.updateContainer(this);
        super.validate();
    }

    /** Registers the specified
     *  action for a esc action 
     *  of this frame. 
     * 
     *  @param action the action 
     *  
     */
    public void registerEscAction(Action action) {
        this.getRootPane().registerKeyboardAction(
            action,
            escKeystroke,
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** Unregisters the esc action 
     *  of this frame. 
     *  
     */
    public void unregisterEscAction() {
        this.getRootPane().unregisterKeyboardAction(escKeystroke);

    }
    
    /** Registers the specified button
     *  for the default button.
     * 
     * @param button
     */ 
    public void setDefaultButton(JButton button){
        getRootPane().setDefaultButton(button);
    }

}

