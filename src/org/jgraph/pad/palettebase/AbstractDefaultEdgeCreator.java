/*
 * Created on 17 juin 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jgraph.pad.palettebase;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.Edge;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.PortView;
import org.jgraph.pad.jgraphpad.IEdgeFactory;
import org.jgraph.pad.jgraphpad.util.JGraphParallelEdgeRouter;

/**
 * @author Raphael Valyi
 * 
 * This is the super class of various java beans containing their own data for the
 * default cell creation of a given graph cell EDGE type once a button is pressed. This class
 * contains generic methods to insert the cell in the graph model.
 */
public abstract class AbstractDefaultEdgeCreator extends AbstractCellCreator
		implements IEdgeFactory {

	public GraphCell addEdge(Point2D start, Point2D current,
			PortView firstPort, PortView port) {
		
		// creation of the cell:
		GraphCell cell = createCell();

		// view attributes creation:
		AttributeMap attributeMap = new AttributeMap();

		Point tempPoint = new Point((int) start.getX(), (int) start.getY());
		Point tempPoint2 = new Point((int) current.getX(), (int) current.getY());
		Point2D p = getCurrentGPDocument().getGraph().fromScreen(new Point(tempPoint));
		Point2D p2 = getCurrentGPDocument().getGraph().fromScreen(
				new Point(tempPoint2));
		ArrayList list = new ArrayList();
		list.add(p);
		list.add(p2);

		GraphConstants.setPoints(attributeMap, list);

		Map viewMap = new Hashtable();

		if (firstPort != null && port != null) {
			// Count directed parallel edges only
			Object[] edges = DefaultGraphModel.getEdgesBetween(
					getCurrentGPDocument().getGraph().getModel(), firstPort.getCell(), port
					.getCell(), true);
			GraphConstants.setLineColor(attributeMap, mdiContainer.getSessionParameters().getForegroundColor());
			if (edges != null && edges.length > 0) {
				Edge.Routing router = JGraphParallelEdgeRouter.sharedInstance;
				Map tmpMap = new Hashtable();
				GraphConstants.setRouting(tmpMap, router);
				GraphConstants
						.setLineStyle(tmpMap, GraphConstants.STYLE_BEZIER);
				for (int i = 0; i < edges.length; i++) {
					EdgeView view = (EdgeView) getCurrentGPDocument().getGraph()
							.getGraphLayoutCache().getMapping(edges[i], false);
					if (view != null
							&& GraphConstants.getRouting(view
									.getAllAttributes()) == null
							&& view.getPointCount() < 3)
						viewMap.put(edges[i], tmpMap);
				}
				GraphConstants.setRouting(attributeMap, router);
			}
		}

		adaptAttributeMap(cell, attributeMap);

		// insertion of the cell:
		viewMap.put(cell, attributeMap);

		viewMap.put(cell, attributeMap);
		Object[] insert = new Object[] { cell };
		ConnectionSet cs = new ConnectionSet();
		if (firstPort != null)
			cs.connect(cell, firstPort.getCell(), true);
		if (port != null)
			cs.connect(cell, port.getCell(), false);
		getCurrentGPDocument().getGraph().getGraphLayoutCache().insert(insert,
				viewMap, cs, null, null);

		return cell;
	}
}
