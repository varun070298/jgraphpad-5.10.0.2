/*
 * @(#)FilePrint.java	1.2 30.01.2003
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
package org.jgraph.pad.actionsbase.lazy;

import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import org.jgraph.pad.jgraphpad.GPAction;

/**
 * Prints the current graph.
 */
public class FilePrint extends GPAction {

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
			PrinterJob printJob = PrinterJob.getPrinterJob();
			PageFormat pageFormat = getCurrentGPDocument().getPageFormat();
			printJob.setPrintable(mdiContainer.getCurrentDocument(),pageFormat); 
			if (printJob.printDialog()) {
				try {
					boolean oldvalue = false;
					double oldscale = getCurrentGraph().getScale();
					getCurrentGPDocument().setScale(1);
					oldvalue = getCurrentGPDocument().isPageVisible();
					printJob.print();
					getCurrentGPDocument() .setScale(oldscale);
					getCurrentGPDocument().setPageVisible(oldvalue);
				} catch (Exception printException) {
					printException.printStackTrace();
				}
			}
	}

}
