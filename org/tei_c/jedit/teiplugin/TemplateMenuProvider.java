/*
 * TemplateMenuProvider.java - provides a hierarchical menu of the TEI template files
 *
 * Copyright (C) 2019 Conal Tuohy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.tei_c.jedit.teiplugin;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;

import org.gjt.sp.jedit.browser.*;
import org.gjt.sp.jedit.io.FileVFS;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.StandardUtilities;
import org.gjt.sp.jedit.menu.DynamicMenuProvider;

/**
 * @author Conal Tuohy
 */
public class TemplateMenuProvider implements DynamicMenuProvider {
	private File templatesFolder;
	
	public TemplateMenuProvider(File templatesFolder)
	{
		this.templatesFolder = templatesFolder;
	}
	
	public boolean updateEveryTime()
	{
		return true;
	}

	public void update(JMenu menu)
	{
		addTemplatesFolderContents(menu);
	}
	
	/*
	* Add the contents of the templates folder to a JComponent (either a JMenu or JPopupMenu)
	*/
	void addTemplatesFolderContents(JComponent component) {
		final View view = jEdit.getActiveView();

		ActionListener templateListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				// create new file
				String templateFileName = event.getActionCommand();
				TEI.debug("Creating from template file", "'" + templateFileName + "'");
				TEI.debug("View", view);
				Buffer buffer = jEdit.newFile(view.getEditPane(), System.getProperty("user.home"));
				view.showBuffer(buffer);
				buffer.setMode("xml");
				boolean inserted = buffer.insertFile(view, templateFileName);
				if (!inserted) {
					TEI.error("Failed to insert template file", templateFileName);
				}
				// neither of these lines seems to do anything
				view.getEditPane().getTextArea().goToBufferStart(false); 
				view.getEditPane().getTextArea().setCaretPosition(0);
			}
		};
		
		File[] templatesFolderContents = templatesFolder.listFiles();
		Arrays.sort(templatesFolderContents);
		for (File item : templatesFolderContents) {
			addItemToMenu(item, component, templateListener);
		}
	}
	
	/*
	* Add the item (a subfolder, or a file which might be a template file) to the menu
	*/
	private void addItemToMenu(File item, JComponent menu, ActionListener templateListener) {
		String name = item.getName();
		if (item.isDirectory()) {
			// item is a subfolder within the template folder; it may or may not contain template files
			TEI.debug("Subfolder found in template folder", name);
			JMenu submenu = new JMenu(name);
			File[] subFolderContents = item.listFiles();
			Arrays.sort(subFolderContents);
			for (File subItem : subFolderContents) {
				addItemToMenu(subItem, submenu, templateListener);
			}
			// if the subfolder contained at least one template file, the submenu will be non-empty
			// and should be added to the menu
			if (submenu.getItemCount() > 0) {
				TEI.debug("Adding submenu", name);
				menu.add(submenu);
			} else {
				TEI.debug("No templates found in folder", name);
			}
		} else {
			// Item is a file, which may or may not be a template file.
			// Ignore non-XML files and any XML catalog files.
			if (name.endsWith(".xml") && !name.equals("catalog.xml")) {
				// item is a template file, so add it to the menu
				TEI.debug("Adding template file to menu", name);
				JMenuItem subMenuItem = new JMenuItem(name.substring(0, name.length() - ".xml".length()));
				subMenuItem.setActionCommand(item.getPath());
				subMenuItem.addActionListener(templateListener);
				menu.add(subMenuItem);
			} else {
				TEI.debug("Ignoring non-template file", name);
			}
		}
	}
}
