/*
 * Copyright (C) 2001-2005 Gaudenz Alder
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jgraph.JGraph;
import org.jgraph.graph.GraphConstants;
import org.jgraph.pad.jgraphpad.util.ICellBuisnessObject;

/**
 * @author Gaudenz Alder
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/**
 * 
 * @author gaudenz alder
 * @version 1.0
 *
 */
public class GPUserObject
implements ICellBuisnessObject {
	
	/* defaultKey of the property used to return a value from the toString method
	 */
	public static final String keyValue = "value";
	
	/* defaultKey of the property used to return a value from the toString method
	 */
	public static final String keyURI = "url";
	
	/* Map that holds the attributes (key value pairs)
	 */
	protected Map properties;
	
	protected transient JDialog propertyDlg;
	
	protected transient JTable table;
	
	protected transient DefaultTableModel dataModel;
	
	/**
	 * public empty constructor required for instanciation by XML or by binary image
	 * DON'T USE IT DIRECTLY, IT MIGHT BREAK A POSSIBLE CUSTOMIZATION, USE INSTEAD:
	 */
	public GPUserObject() {
	}
	
	public void setValue(Object label) {
		putProperty(keyValue, label);
	}
	
	public Object getProperty(Object key) {
		return properties.get(key);
	}

	public Object putProperty(Object key, Object value) {
		if (value != null) {
			if (properties == null)
				properties = new Hashtable();
			return properties.put(key, value);
		}
		return properties.remove(key);
	}

	public Map getProperties() {
		return properties;
	}
	
	public void setProperties(Map map) {
		properties = map;
	}

	public void showPropertyDialog(final JGraph graph, final Object cell) {
		Frame frame = (Frame) SwingUtilities.windowForComponent(graph);
		if (frame != null && propertyDlg == null) {
			propertyDlg = new JDialog(frame, "", false);
			Container fContentPane = propertyDlg.getContentPane();
			fContentPane.setLayout(new BorderLayout());
			dataModel =
				new DefaultTableModel(new Object[] { "Key", "Value" }, 0);
			table = new JTable(dataModel);
			JScrollPane scrollpane = new JScrollPane(table);
			
			fContentPane.add(BorderLayout.CENTER, scrollpane);
			JButton okButton = new JButton("OK"); //Translator.getString("OK")
			JButton cancelButton = new JButton("Close"); //Translator.getString("Close")
			JButton applyButton =
				new JButton("Apply"); //Translator.getString("Apply")
			JButton addButton = new JButton("New"); //Translator.getString("New")
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);
			buttonPanel.add(applyButton);
			buttonPanel.add(addButton);
			fContentPane.add(BorderLayout.SOUTH, buttonPanel);
			applyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					apply(graph, cell, dataModel);
				}
			});
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					apply(graph, cell, dataModel);
					propertyDlg.dispose();
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					propertyDlg.dispose();
				}
			});
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dataModel.addRow(new Object[] { "Key", "Value" });
				}
			});
			propertyDlg.setSize(new Dimension(300, 300));
			propertyDlg.setLocationRelativeTo(frame);
		}
		dataModel = new DefaultTableModel(new Object[] { "Key", "Value" }, 0);
		Iterator it = properties.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			dataModel.addRow(new Object[] { entry.getKey(), entry.getValue()});
		}
		table.setModel(dataModel);
		propertyDlg.setTitle("Properties of " + toString());
		propertyDlg.setVisible(true);
	}
	
	protected void apply(JGraph graph, Object cell, TableModel model) {
		Map oldProperties = new Hashtable(properties);
		properties.clear();
		for (int i = 0; i < model.getRowCount(); i++) {
			properties.put(model.getValueAt(i, 0), model.getValueAt(i, 1));
		}
		if (!(oldProperties.equals(properties))) {
			Map nested = new Hashtable();
			Map transport = new Hashtable();
			Object value = getProperty(keyValue);
			if (value == null)
				putProperty(keyValue, "");
			GraphConstants.setValue(transport, this);
			nested.put(cell, transport);
			graph.getGraphLayoutCache().edit(nested, null, null, null);
		}
	}
	
	public Object clone() {
		GPUserObject other = new GPUserObject();
		other.setProperties(properties);//ask to clone?
		return other;
	}
		
	public String toString() {
		if (properties == null)
			return "";
		Object label = properties.get(keyValue);
		if (label != null)
			return label.toString();
		return "";
	}
	
}
