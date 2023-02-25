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

package org.microplatform;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.microplatform.gui.DocFrame;
import org.microplatform.gui.MDIContainer;
import org.microplatform.io.IFile;
import org.microplatform.loaders.Translator;

/**
 * A Document represents a single instance of a graph view with associated
 * library and overview panes. The document deals with a lot of the listening
 * required on the graph, prompting for save if modified, undo handling and top
 * level UI issues relating to pane positioning.
 * 
 * You could subclass GPDocument, but often subclassing the objects GPDocument
 * deals with should be sufficient. In order to ensure subclassers will be used
 * by GPDocuments, critical objects are instanciated by a factory where you can
 * register custom subclassers.
 * 
 * There is also a hook for custom plugins to ba able to redefine/decorate the
 * rootPane of the document and also to register themself in the pluginsMap.
 */
public abstract class Document implements
        ComponentListener, Printable,
        PropertyChangeListener, ICommandRegistery {
    
    /**
     * use to put associate aribtrary plugins to the document
     */
    protected Map pluginsMap;
    
    protected JComponent docComponent;
    
    protected IFile file;

    /**
     * A reference to the top level component
     */
    protected MDIContainer mdiContainer;

    /**
     * True if this documents graph model was modified since last save.
     */
    protected boolean modified = false;

    /**
     * true if the current graph is Metric. default is true.
     */
    protected static boolean isMetric = true;

    /**
     * Action used for fitting the size
     */
    protected Action fitAction;

    /**
     * contains the find pattern for this document
     */
    protected String findPattern;

    /**
     * contains the last found object
     */
    protected Object lastFound;

    /**
     * a reference to the internal Frame
     */
    protected DocFrame internalFrame;
    
    protected JPanel toolBarMainPanel;
    
    protected Image backgroundImage;
    
    protected boolean pagevisible = false;
    
    protected transient PageFormat pageFormat = new PageFormat();

    public Document(MDIContainer gp, String name, IFile file) {
        pluginsMap = new Hashtable();//empty by default
        docComponent = new JPanel();
        docComponent.setName(name);
    }
    
    public abstract IFile getFile();
    
    public void createComponents() {
    }
    
    public void makeDocBar() {
    	BarFactory.getInstance().createToolBars(
                toolBarMainPanel, BarFactory.DOCTOOLBARS, this);
    }

    public void setModified(boolean modified) {
        this.modified = modified;
        update();
        mdiContainer.getPluginInvoker().notifyBrowser();
    }

    // -----------------------------------------------------------------
    // Component Listener
    // -----------------------------------------------------------------
    public void setResizeAction(AbstractAction e) {
        fitAction = e;
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        if (fitAction != null)
            fitAction.actionPerformed(null);
    }

    public void componentShown(ComponentEvent e) {
        componentResized(e);
    }

    // ----------------------------------------------------------------------
    // Printable
    // ----------------------------------------------------------------------

    /**
     * not from Printable interface, but related
     */
    public void updatePageFormat() {
    }

    public int print(Graphics g, PageFormat pF, int page) {
       return 0;
    }

    // ----------------------------------------------------------------------
    // Listeners
    // ----------------------------------------------------------------------

    // PropertyChangeListener
    public void propertyChange(PropertyChangeEvent evt) {
        if (mdiContainer != null)
            update();
    }

    protected void update() {
        updateFrameTitle();
        mdiContainer.update();
        mdiContainer.getStatusBar().setMessage(getDocumentStatus());
    }
    
    /* Return the status of this document as a string. */
    protected String getDocumentStatus() {
       return "no status available";
    }

    /**
     * Returns the graphpad.
     * 
     * @return GPGraphpad
     */
    public MDIContainer getMdiContainer() {
        return mdiContainer;
    }

    /**
     * Sets the graphpad.
     * 
     * @param graphpad
     *            The graphpad to set
     */
    public void setMdiContainer(MDIContainer graphpad) {
        this.mdiContainer = graphpad;
    }

    /**
     * Returns true if the user really wants to close. Gives chance to save
     * work.
     */
    public boolean close(boolean showConfirmDialog) {
        // set default to save on close
        int r = JOptionPane.YES_OPTION;

        if (modified) {
            if (showConfirmDialog)
                r = JOptionPane.showConfirmDialog(mdiContainer.getFrame(),
                        Translator.getString("SaveChangesDialog"), Translator
                                .getString("Title"),
                        JOptionPane.YES_NO_CANCEL_OPTION);

            // if yes, then save and close
            if (r == JOptionPane.YES_OPTION) {
            	if (mdiContainer.getSessionParameters().isApplet())
            		 mdiContainer.getCommand( "FileUploadToServer").actionPerformed(new ActionEvent(this, 0, "sameThread"));
            	else
            		mdiContainer.getCommand( "FileSave").actionPerformed(null);
  
                return true;
            }
            // if no, then don't save and just close
            else if (r == JOptionPane.NO_OPTION) {
                return true;
            }
            // all other conditions (cancel and dialog's 'X' button)
            // don't save and don't close
            else
                return false;

        }
        return true;
    }

    /**
     * Returns the findPattern.
     * 
     * @return String
     */
    public String getFindPattern() {
        return findPattern;
    }

    /**
     * Sets the findPattern.
     * 
     * @param findPattern
     *            The findPattern to set
     */
    public void setFindPattern(String findPattern) {
        this.findPattern = findPattern;
    }

    /**
     * Returns the lastFound.
     * 
     * @return Object
     */
    public Object getLastFound() {
        return lastFound;
    }

    /**
     * Sets the lastFound.
     * 
     * @param lastFound
     *            The lastFound to set
     */
    public void setLastFound(Object lastFound) {
        this.lastFound = lastFound;
    }

    /**
     * Returns the internalFrame.
     * 
     * @return GPDocFrame
     */
    public DocFrame getInternalFrame() {
        return internalFrame;
    }

    /**
     * Sets the internalFrame.
     * 
     * @param internalFrame
     *            The internalFrame to set
     */
    public void setInternalFrame(DocFrame internalFrame) {
        this.internalFrame = internalFrame;
    }

    protected void updateFrameTitle() {
        if (this.internalFrame != null) {
            this.internalFrame.setTitle(getFrameTitle());
        }

    }

    public String getFrameTitle() {
        if (docComponent.getName() == null)
            docComponent.setName(Translator.getString("NewGraph") + mdiContainer.getAllFrames().length);
        return docComponent.getName() + (modified ? "*" : "");
    }

    public Action getCommand(String key) {
        return mdiContainer.getPluginInvoker().getCommand(key, this);
    }
    
    public ActionMap getActionMap() {
        return docComponent.getActionMap();
    }
    
    public void initCommand(Action action) {
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(Image backGroundImage) {
        this.backgroundImage = backGroundImage;
    }

    public boolean isPageVisible() {
        return pagevisible;
    }

    public void setPageVisible(boolean pagevisible) {
        this.pagevisible = pagevisible;
    }

    public PageFormat getPageFormat() {
        return pageFormat;
    }

    public void setPageFormat(PageFormat pageFormat) {
        this.pageFormat = pageFormat;
    }

    public Action getFitAction() {
        return fitAction;
    }

    public Map getPluginsMap() {
        return pluginsMap;
    }

    public void setPluginsMap(Map pluginsMap) {
        this.pluginsMap = pluginsMap;
    }

    public JComponent getDocComponent() {
        return docComponent;
    }

    public void setDocComponent(JComponent docComponent) {
        this.docComponent = docComponent;
    }

	public boolean isModified() {
		return modified;
	}
}

