/*
 * TEIToolPanel.java
 * part of the TEI plugin for the jEdit text editor
 */

package org.tei_c.jedit.teiplugin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.ImageIcon;

import java.util.HashMap;

import eclipseicons.EclipseIconsPlugin;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.RolloverButton;
import org.gjt.sp.jedit.EditAction;


public class TEIToolPanel extends JPanel {
	private TEIPanel dockablePanel;

	private JLabel label;
	private HashMap<String, Component> components = new HashMap<String, Component>();

	public TEIToolPanel(TEIPanel dockablePanel) {
		org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG, this, "TEI toolbar initializing...");
		TEI tei = TEI.getInstance();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.dockablePanel = dockablePanel;

		add(Box.createGlue());

		add(
			makeCustomButton(
				"tei.update-tei-package", 
				new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						TEI.getInstance().updateTEIPackage(true);
					}
				}
			)
		);
		add(
			makeCustomButton(
				"tei.new-buffer-from-template", 
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG, this, "new TEI file button clicked");
						//TEI.newBufferFromTemplate(dockablePanel.getView());
						JPopupMenu popup = new JPopupMenu();
						TEI.getInstance().getTemplateMenuProvider().addTemplatesFolderContents(popup);
						AbstractButton button = (AbstractButton) event.getSource();
						TEI.debug("Popping up menu from button", button);
						popup.show(
							button.getParent(),
							button.getX() + button.getWidth(), 
							button.getY()
						);
					}
				}
			)
		);
		addExternalActionButton("sidekick-parse");
		addExternalActionButton("xml-insert");
		addExternalActionButton("xml-match-tag");
		addExternalActionButton("xml-select-between-tags");
		addExternalActionButton("xml-split-tag");
		addExternalActionButton("xmlindenter.indent");
/*
can we look up an action by its code and invoke it?
*/
/*
sidekick-parse.shortcut=CS+w
xml-insert.shortcut=C+e
xml-match-tag.shortcut=CS+m
xml-select-between-tags.shortcut=CS+t
xml-split-tag.shortcut=CS+d
xml-split-tag.shortcut2=AS+d
xmlindenter.indent.shortcut=CS+p
*/
		org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG, this, "TEI toolbar initialized");
	}
	
	public Component getComponent(String name) {
		return components.get(name);
	}

	/**
	* make a button for calling an existing action defined externally to this plugin
	* @param actionName the name of the external action
	*/
	private void addExternalActionButton(String actionName) {
		add(
			makeCustomButton(
				actionName, 
				new ActionListener(){
					public void actionPerformed(ActionEvent event) {
						AbstractButton button = (AbstractButton) event.getSource();
						String actionName = button.getName();
						org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG, this, actionName + " button clicked");
						EditAction action = jEdit.getAction(actionName);
						action.invoke(jEdit.getActiveView());
					}
				}
			)
		);
	}
	
	// index all the buttons in this panel by their name
	public Component add(Component component) {
		super.add(component);
		String name = component.getName();
		if (name != null) {
			components.put(name, component);
		}
		return component;
	}
	
	/*

	void propertiesChanged() {
		
	}
	*/

	private AbstractButton makeCustomButton(String name, ActionListener listener) {
		org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG, this, "TEI toolbar adding button " + name);

		String toolTip = jEdit.getProperty(name.concat(".label"));
		String iconName = jEdit.getProperty(name.concat(".icon"));
		AbstractButton button = new RolloverButton();
		button.setName(name);
		button.setText(toolTip);
		try {
			ImageIcon icon = EclipseIconsPlugin.getIcon(iconName); 
			button.setIcon(icon);
		} catch (NullPointerException e) {
			org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.ERROR, this, "No icon named " + iconName);
		}
		if (listener != null) {
			button.addActionListener(listener);
			button.setEnabled(true);
		} else {
			button.setEnabled(false);
		}
		button.setToolTipText(toolTip);
		org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG, this, "TEI toolbar added button " + name);
		return button;
	}

}
