package org.jgraph.plugins.library;

import org.jgraph.pad.jgraphpad.GPAction;


public abstract class LibraryAction extends GPAction {

	public LibraryDecorator getCurrentLibraryDocument() {
		try {
			return (LibraryDecorator) getCurrentGPDocument().getPluginsMap().get(LibraryDecorator.LIBRARY_PLUGIN);
		} catch (Exception ex) {
			System.err.print("Your graph base class isn't a LibraryDecorator!");
			ex.printStackTrace();
			return null;
		}
	}

}

