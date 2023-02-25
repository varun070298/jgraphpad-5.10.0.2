package org.jgraph.pad.jgraphpad;

/*
 * Created on 17 juin 2005
 * This is GNU GPL free software
 * Copyright (C) 2004 Rapha�l Valyi, See LICENSE file in distribution for license details
 */

import java.awt.geom.Point2D;

import javax.swing.JToggleButton;

import org.jgraph.graph.GraphCell;
import org.jgraph.graph.PortView;

/**
 * An interface specifying the minimum required methods for creating a cell
 * More method can also be subclassed form the AbstractDefaultVertexnPortsCreator
 * that already implements this interface with a convenient default behavior.
 * @author rvalyi, consider this as a LGPL contribution
 */
public interface IEdgeFactory {
  public GraphCell createCell();
  public JToggleButton getButton();
  public GraphCell addEdge(Point2D start, Point2D current, PortView firstPort,
		PortView port);
}

