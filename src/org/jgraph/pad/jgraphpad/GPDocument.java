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

package org.jgraph.pad.jgraphpad;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.MissingResourceException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ToolTipManager;

import org.jgraph.JGraph;
import org.jgraph.event.GraphLayoutCacheEvent;
import org.jgraph.event.GraphLayoutCacheListener;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewFactory;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphUndoManager;
import org.jgraph.graph.Port;
import org.jgraph.pad.jgraphpad.util.Rule;
import org.jgraph.plaf.GraphUI;
import org.microplatform.BarFactory;
import org.microplatform.Document;
import org.microplatform.gui.MDIContainer;
import org.microplatform.loaders.PluginInvoker;
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
public class GPDocument extends Document implements GraphSelectionListener, GraphModelListener, GraphLayoutCacheListener {

    protected boolean enableTooltips;

    /**
     * Container for the graph so that you can scroll over the graph
     */
    protected JScrollPane scrollPane;

    /**
     * The joint graph for this document
     */
    protected JGraph graph;

    /**
     * The column rule for the graph
     */
    protected Rule columnRule;

    /**
     * The row rule for the graph
     */
    protected Rule rowRule;

    /**
     * The graphUndoManager manager for the joint graph.
     * 
     * @see #graph
     */
    protected GraphUndoManager graphUndoManager;

    /**
     * The graphUndoManager handler for the current document. Each document has
     * his own handler. So you can make an graphUndoManager seperate for each
     * document.
     * 
     */
    protected GPUndoHandler undoHandler;

    /**
     * true if the current graph is Metric. default is true.
     */
    protected static boolean isMetric = true;

    /**
     * true if the ruler show is activated
     */
    protected static boolean showRuler = true;

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

    protected ArrayList edgeCreators = new ArrayList();

    protected ArrayList vertexnPortsCreators = new ArrayList();

    public GPDocument(MDIContainer gp, String name, GPGraphpadFile file) {
        super(gp, name, file);
    	pluginsMap = new Hashtable();//empty by default
        isMetric = new Boolean(Translator.getString("IsMetric")).booleanValue();
        showRuler = new Boolean(Translator.getString("ShowRuler"))
                .booleanValue();
        enableTooltips = new Boolean(Translator.getString("IsEnableTooltips")).booleanValue();
        docComponent = new JPanel();
        docComponent.setDoubleBuffered(true);
        docComponent.updateUI();
        docComponent.setName(name);//we take care of setting a proper name later if it's null
        mdiContainer = gp;
        undoHandler = new GPUndoHandler(this);
        graphUndoManager = createGraphUndoManager();
        
        graph = createGraph(file);
        
        createComponents();
        
        registerListeners(graph);
    }
    
    public void createComponents() {
    	docComponent.setBorder(BorderFactory.createEtchedBorder());
    	docComponent.setLayout(new BorderLayout());
        toolBarMainPanel = new JPanel(new BorderLayout());
        docComponent.add(BorderLayout.NORTH, toolBarMainPanel);
        docComponent.add(BorderLayout.CENTER, createScrollPane());
        update();
    }
    
    public void makeDocBar() {
    	BarFactory.getInstance().createToolBars(
                toolBarMainPanel, BarFactory.DOCTOOLBARS, this);
        graph.setMarqueeHandler((BasicMarqueeHandler) PluginInvoker
                .instanciateDocAwarePluginForKey("MarqueeHandler.class",
                        this));
    }
    
    /*
     * Create a JGraph with JGraph primitives or custom subclassers registered in the properties file
     */
    protected JGraph createGraph(GPGraphpadFile file) {
        //JGraph subclasser:
        graph = (JGraph) PluginInvoker
                .instanciateObjectForKey("JGraph.class");
        
        //pluggable Look and Feel:
        graph.setUI((GraphUI) PluginInvoker
                .instanciateDocAwarePluginForKey("GraphUI.class", this));
        
       //default setup:
        graph.setDragEnabled(false);
        graph.setJumpToDefaultPort(true);
        graph.setInvokesStopCellEditing(true);
        graph.setBackground(mdiContainer.getSessionParameters().getBackgroundColor());
        graph.setCloneable(true);       
        //view factory:
        graph.getGraphLayoutCache().setFactory((CellViewFactory) PluginInvoker
                .instanciateObjectForKey("ViewFactory.class"));

        DefaultGraphModel model;
        if (file != null)
            model = new DefaultGraphModel(file.getRoots(), file.getAttributes(), file.getConnectionSet());
        else
            model = new DefaultGraphModel();
        graph.setModel(model);

        //view factory:
        graph.getGraphLayoutCache().setFactory((CellViewFactory) PluginInvoker
                .instanciateObjectForKey("ViewFactory.class"));
        
        return graph;
    }

    protected Component createScrollPane() {
        scrollPane = new JScrollPane(graph);
        JViewport port = scrollPane.getViewport();
        try {
            String vpFlag = Translator.getString("ViewportBackingStore");
            Boolean bs = new Boolean(vpFlag);
            if (bs.booleanValue()) {
                port.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
            } else {
                port.setScrollMode(JViewport.BLIT_SCROLL_MODE);
            }
        } catch (MissingResourceException mre) {
            // just use the viewport default
        }
        columnRule = new Rule(Rule.HORIZONTAL, isMetric, graph);
        rowRule = new Rule(Rule.VERTICAL, isMetric, graph);
        if (showRuler) {
            scrollPane.setColumnHeaderView(columnRule);
            scrollPane.setRowHeaderView(rowRule);
        }
        return scrollPane;
    }

    protected GraphUndoManager createGraphUndoManager() {
        return new GraphUndoManager();
    }

    /**
     * Returns the model of the graph
     */
    public GraphModel getModel() {
        return graph.getModel();
    }

    /**
     * Returns the view from the current graph
     * 
     */
    public GraphLayoutCache getGraphLayoutCache() {
        return graph.getGraphLayoutCache();
    }

    /* Add this documents listeners to the specified graph. */
    private void registerListeners(JGraph graph) {
        graph.getModel().addUndoableEditListener(undoHandler);
        docComponent.addComponentListener(this);
        graph.getSelectionModel().addGraphSelectionListener(this);
        graph.getModel().addGraphModelListener(this);
        graph.getGraphLayoutCache().addGraphLayoutCacheListener(this);
        graph.addPropertyChangeListener(this);
        graph.getGraphLayoutCache().addGraphLayoutCacheListener(this);
    }

    /* Return the scale of this document as a string. */
    protected String getDocumentScale() {
        return Integer.toString((int) (graph.getScale() * 100)) + "%";
    }

    /* Sets the attributes of the selected cells. */
    public void setSelectionAttributes(Map map) {
        map = new Hashtable(map);
        map.remove(GraphConstants.BOUNDS);
        map.remove(GraphConstants.POINTS);
        graph.getGraphLayoutCache().edit(graph.getSelectionCells(), map);
    }

    /* Sets the attributes of the selected cells. */
    public void setFontSizeForSelection(float size) {
        Object[] cells = DefaultGraphModel.getDescendants(graph.getModel(),
                graph.getSelectionCells()).toArray();
        // Filter ports out
        java.util.List list = new ArrayList();
        for (int i = 0; i < cells.length; i++)
            if (!(cells[i] instanceof Port))
                list.add(cells[i]);
        cells = list.toArray();

        Map nested = new Hashtable();
        for (int i = 0; i < cells.length; i++) {
            CellView view = graph.getGraphLayoutCache().getMapping(cells[i],
                    false);
            if (view != null) {
                Font font = GraphConstants.getFont(view.getAllAttributes());
                AttributeMap attr = new AttributeMap();
                GraphConstants.setFont(attr, font.deriveFont(size));
                nested.put(cells[i], attr);
            }
        }
        graph.getGraphLayoutCache().edit(nested, null, null, null);
    }

    /* Sets the attributes of the selected cells. */
    public void setFontStyleForSelection(int style) {
        Object[] cells = DefaultGraphModel.getDescendants(graph.getModel(),
                graph.getSelectionCells()).toArray();
        // Filter ports out
        java.util.List list = new ArrayList();
        for (int i = 0; i < cells.length; i++)
            if (!(cells[i] instanceof Port))
                list.add(cells[i]);
        cells = list.toArray();

        Map nested = new Hashtable();
        for (int i = 0; i < cells.length; i++) {
            CellView view = graph.getGraphLayoutCache().getMapping(cells[i],
                    false);
            if (view != null) {
                Font font = GraphConstants.getFont(view.getAllAttributes());
                AttributeMap attr = new AttributeMap();
                GraphConstants.setFont(attr, font.deriveFont(style));
                nested.put(cells[i], attr);
            }
        }
        graph.getGraphLayoutCache().edit(nested, null, null, null);
    }

    /* Sets the attributes of the selected cells. */
    public void setFontNameForSelection(String name) {
        Object[] cells = DefaultGraphModel.getDescendants(graph.getModel(),
                graph.getSelectionCells()).toArray();
        // Filter ports out
        java.util.List list = new ArrayList();
        for (int i = 0; i < cells.length; i++)
            if (!(cells[i] instanceof Port))
                list.add(cells[i]);
        cells = list.toArray();

        Map nested = new Hashtable();
        for (int i = 0; i < cells.length; i++) {
            CellView view = graph.getGraphLayoutCache().getMapping(cells[i],
                    false);
            if (view != null) {
                Font font = GraphConstants.getFont(view.getAllAttributes());
                AttributeMap attr = new AttributeMap();
                GraphConstants.setFont(attr, new Font(name, font.getStyle(),
                        font.getSize()));
                nested.put(cells[i], attr);
            }
        }
        graph.getGraphLayoutCache().edit(nested, null, null, null);
    }

    // -----------------------------------------------------------------
    // Component Listener
    // -----------------------------------------------------------------

    public void setScale(double scale) {
        scale = Math.max(Math.min(scale, 16), .01);
        graph.setScale(scale);
        componentResized(null);
    }

    // ----------------------------------------------------------------------
    // Printable
    // ----------------------------------------------------------------------

    /**
     * not from Printable interface, but related
     */
    public void updatePageFormat() {
        PageFormat f = getPageFormat();
        columnRule.setActiveOffset((int) (f.getImageableX()));
        rowRule.setActiveOffset((int) (f.getImageableY()));
        columnRule.setActiveLength((int) (f.getImageableWidth()));
        rowRule.setActiveLength((int) (f.getImageableHeight()));
        if (isPageVisible()) {
            int w = (int) (f.getWidth());
            int h = (int) (f.getHeight());
            graph.setMinimumSize(new Dimension(w + 5, h + 5));
        } else
            graph.setMinimumSize(null);
        docComponent.invalidate();
        // Execute fitAction...
        componentResized(null);
        graph.repaint();
    }

    public int print(Graphics g, PageFormat pF, int page) {
        int pw = (int) pF.getImageableWidth();
        int ph = (int) pF.getImageableHeight();
        int cols = (graph.getWidth() / pw) + 1;
        int rows = (graph.getHeight() / ph) + 1;
        int pageCount = cols * rows;
        if (page >= pageCount)
            return NO_SUCH_PAGE;
        int col = page % cols;
        int row = page % rows;
        g.translate(-col * pw, -row * ph);
        g.setClip(col * pw, row * ph, pw, ph);
        graph.paint(g);
        g.translate(col * pw, row * ph);
        return PAGE_EXISTS;
    }

    //
    // Listeners
    //
    
    // GraphSelectionListener
    public void valueChanged(GraphSelectionEvent e) {
        update();
    }

    // View Observer
    public void graphLayoutCacheChanged(GraphLayoutCacheEvent e) {
    	setModified( true);
    }

    // GraphModelListener
    public void graphChanged(GraphModelEvent e) {
    	setModified( true);
    }

    protected void update() {
        super.update();
        mdiContainer.getStatusBar().setScale(this.getDocumentScale());
    }
    
    /* Return the status of this document as a string. */
    protected String getDocumentStatus() {
        String s = null;
        int n = graph.getSelectionCount();
        if (n > 0)
            s = n + " " + Translator.getString("Selected");
        else {
            int c = graph.getModel().getRootCount();
            if (c == 0) {
                s = Translator.getString("Empty");
            } else {
                s = c + " ";
                if (c > 1)
                    s += Translator.getString("Cells");
                else
                    s += Translator.getString("Cell");
                c = graph.getSelectionCount();
                s = s + " / " + c + " ";
                if (c > 1)
                    s += Translator.getString("Components");
                else
                    s += Translator.getString("Component");
            }
        }
        return s;
    }

    /**
     * Returns the graphUndoManager.
     * 
     * @return GraphUndoManager
     */
    public GraphUndoManager getGraphUndoManager() {
        return graphUndoManager;
    }

    /**
     * Sets the graphUndoManager.
     * 
     * @param graphUndoManager
     *            The graphUndoManager to set
     */
    public void setGraphUndoManager(GraphUndoManager graphUndoManager) {
        this.graphUndoManager = graphUndoManager;
    }

    /**
     * Resets the Graph undo manager
     */
    public void resetGraphUndoManager() {
        graphUndoManager.discardAllEdits();
    }

    /**
     * Returns the scrollPane.
     * 
     * @return JScrollPane
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * Sets the scrollPane.
     * 
     * @param scrollPane
     *            The scrollPane to set
     */
    public void setScrollPane(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    /**
     * Returns the columnRule.
     * 
     * @return Rule
     */
    public Rule getColumnRule() {
        return columnRule;
    }

    /**
     * Returns the rowRule.
     * 
     * @return Rule
     */
    public Rule getRowRule() {
        return rowRule;
    }

    /**
     * Sets the columnRule.
     * 
     * @param columnRule
     *            The columnRule to set
     */
    public void setColumnRule(Rule columnRule) {
        this.columnRule = columnRule;
    }

    /**
     * Sets the rowRule.
     * 
     * @param rowRule
     *            The rowRule to set
     */
    public void setRowRule(Rule rowRule) {
        this.rowRule = rowRule;
    }

    /**
     * Sets the enableTooltips.
     * 
     * @param enableTooltips
     *            The enableTooltips to set
     */
    public void setEnableTooltips(boolean enableTooltips) {
        this.enableTooltips = enableTooltips;

        if (this.enableTooltips)
            ToolTipManager.sharedInstance().registerComponent(graph);
        else
            ToolTipManager.sharedInstance().unregisterComponent(graph);
    }

    public BasicMarqueeHandler getMarqueeHandler() {
        return graph.getMarqueeHandler();
    }

    public void setMarqueeHandler(BasicMarqueeHandler marqueeHandler) {
        graph.setMarqueeHandler(marqueeHandler);
    }

    public org.microplatform.io.IFile getFile() {
        GPGraphpadFile file = new GPGraphpadFile();
        file.setAttributes(graph.getAttributes(null));
        file.setConnectionSet(((DefaultGraphModel) graph.getModel()).getConnectionSet());
        file.setRoots(((DefaultGraphModel) graph.getModel()).getRoots());
        return file;
    }

    public ArrayList getEdgeCreators() {
        return edgeCreators;
    }

    public void setEdgeCreators(ArrayList edgeCreators) {
        this.edgeCreators = edgeCreators;
    }

    public ArrayList getVertexnPortsCreators() {
        return vertexnPortsCreators;
    }

    public void setVertexnPortsCreators(ArrayList vertexnPortsCreators) {
        this.vertexnPortsCreators = vertexnPortsCreators;
    }
    
    public void initCommand(ActionListener action) {
        mdiContainer.initCommand(action);
        if (action instanceof IVertexFactory) {
            vertexnPortsCreators.add(action);
          /* try {//TODO: this should work!
   			((IVertexFactory) action).getButton().setSelected(true);
			((GPMarqueeHandler) graph.getMarqueeHandler()).getButtonGroup().clearSelection();
			((GPMarqueeHandler) graph.getMarqueeHandler()).getButtonGroup().add(((IVertexFactory) action).getButton());
		} catch (RuntimeException e) {
			e.printStackTrace();
		}*/
        } else if (action instanceof IEdgeFactory) {
            edgeCreators.add(action);
        }
    }

	public JGraph getGraph() {
		return graph;
	}

	public void setGraph(JGraph graph) {
		this.graph = graph;
	}

	public boolean isEnableTooltips() {
		return enableTooltips;
	}
}
