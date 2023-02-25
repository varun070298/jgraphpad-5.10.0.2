package org.jgraph.plugins.gpgraph;

import org.jgraph.pad.jgraphpad.GPAction;


public abstract class GPGraphAction extends GPAction {

	public GPGraph getCurrentGPGraph() {
		try {
			return (GPGraph) getCurrentGraph();
		} catch (Exception ex) {
			System.err.print("Your graph base class isn't a GPGraph!");
			ex.printStackTrace();
			return null;
		}
	}

}
