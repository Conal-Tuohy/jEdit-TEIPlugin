/*
 * TEI.java
 * part of the TEI plugin for the jEdit text editor
 */

package org.tei_c.jedit.teiplugin;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.Processor; 
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
 
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.gjt.sp.jedit.browser.VFSBrowser;
/*
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EditBus;
*/
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.StandardUtilities;

/**
 * 
 * TEIPanel - a dockable JPanel
 *
 */
 
public class TEIPanel extends JPanel
    implements DefaultFocusComponent {

	private View view;

	private boolean floating;

	private TEIToolPanel toolPanel;
	
	/**
	 * 
	 * @param view the current jedit window
	 * @param position a variable passed in from the script in actions.xml,
	 * 	which can be DockableWindowManager.FLOATING, TOP, BOTTOM, LEFT, RIGHT, etc.
	 * 	see @ref DockableWindowManager for possible values.
	 */
	public TEIPanel(View view, String position) {
		super(new BorderLayout());
		this.view = view;
		this.floating = position.equals(DockableWindowManager.FLOATING);

		this.toolPanel = new TEIToolPanel(this);
		add(BorderLayout.NORTH, this.toolPanel);

		if (floating)
			this.setPreferredSize(new Dimension(500, 250));

	}
	
	View getView() {
		return view;
	}
	
	public TEIToolPanel getToolPanel() {
		return toolPanel;
	}
	
	public void focusOnDefaultComponent() {
		
	}

}
