/*
 * @(#)FileExportGIF.java	1.2 01.02.2003
 *
 * Copyright (C) 2003 gaudenz alder
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
package org.jgraph.plugins.codecs;

import java.awt.event.ActionEvent;

import org.jgraph.pad.actionsbase.lazy.FileExportPNG;

//import Acme.JPM.Encoders.GifEncoder;
//import org.shetline.io.*;

//import com.eteks.filter.Web216ColorsFilter;

public class FileExportGIF extends FileExportPNG {
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		encode("gif");
	}
}
