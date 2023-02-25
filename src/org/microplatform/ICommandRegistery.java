package org.microplatform;

import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.ActionMap;

public interface ICommandRegistery {
	public Action getCommand(String key);
    public void initCommand(ActionListener action);
    public ActionMap getActionMap();
}
