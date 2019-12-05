/*
 * TEIToolPanel.java
 * part of the TEI plugin for the jEdit text editor
 */

package org.tei_c.jedit.teiplugin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ImageIcon;

import eclipseicons.EclipseIconsPlugin;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.RolloverButton;

public class TEIToolPanel extends JPanel {
	private TEIPanel dockablePanel;

	private JLabel label;

	public TEIToolPanel(TEIPanel dockablePanel) {
		org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG, this, "TEI toolbar initializing...");
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.dockablePanel = dockablePanel;

		add(Box.createGlue());

		add(
			makeCustomButton(
				"tei.update-tei-package", 
				new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						TEI.getInstance().updateTEIPackage();
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
		org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG, this, "TEI toolbar initialized");
	}
	
	/*

	void propertiesChanged() {
		
	}
	*/

	private AbstractButton makeCustomButton(String name, ActionListener listener) {
		org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG, this, "TEI toolbar adding button " + name);

		String toolTip = jEdit.getProperty(name.concat(".label"));
		String iconName = jEdit.getProperty(name.concat(".icon"));
		ImageIcon icon = EclipseIconsPlugin.getIcon(iconName); 
		AbstractButton b = new RolloverButton(icon);
		if (listener != null) {
			b.addActionListener(listener);
			b.setEnabled(true);
		} else {
			b.setEnabled(false);
		}
		b.setToolTipText(toolTip);
		org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG, this, "TEI toolbar added button " + name);
		return b;
	}

}
