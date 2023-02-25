package org.jgraph.pad.actionsbase.eager;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JToggleButton;

import org.jgraph.pad.jgraphpad.GPAction;
import org.microplatform.BarFactory;

public class ToolBoxZoomArea extends GPAction {
    
    JToggleButton button;

    public void actionPerformed(ActionEvent e) {
    }

    public Component getToolComponent(final String actionCommand) {
        button = new JToggleButton() {
            public float getAlignmentY() {
                return 0.5f;
            }
        };
        BarFactory.fillToolbarButton(button, getName(), actionCommand);
        return button;
    }

    public void update() {
        this.setEnabled(this.isEnabled());
        super.update();
    }

}