/*
 * TEIOptionPane.java
 * part of the TEI plugin for the jEdit text editor
 */
package org.tei_c.jedit.teiplugin;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.gui.HistoryTextField;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.FontSelector;


public class TEIOptionPane extends AbstractOptionPane {
	private JCheckBox autoUpdate;

	private HistoryTextField packageURL;


	public TEIOptionPane() {
		super(TEIPlugin.NAME);
	}

	public void _init() {
		autoUpdate = new JCheckBox(
			jEdit.getProperty(TEIPlugin.OPTION_PREFIX+ "auto-update.title"),
			jEdit.getProperty(TEIPlugin.OPTION_PREFIX + "auto-update").equals("true")
		);
		addComponent(autoUpdate);

		packageURL = new HistoryTextField("package-url");
		packageURL.setText(
			jEdit.getProperty(TEIPlugin.OPTION_PREFIX + "package-url")
		);

		addComponent(
			jEdit.getProperty(TEIPlugin.OPTION_PREFIX + "package-url.title"), 
			packageURL
		);

	}

	public void _save() {
		jEdit.setProperty(TEIPlugin.OPTION_PREFIX + "package-url", packageURL.getText());
		jEdit.setProperty(TEIPlugin.OPTION_PREFIX + "auto-update", String.valueOf(autoUpdate.isSelected()));
	}

	// end AbstractOptionPane implementation
}
