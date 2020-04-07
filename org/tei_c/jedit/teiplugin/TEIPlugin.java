/*
 * TEIPlugin.java
 * The TEI plugin for the jEdit text editor
 * This class represents the plugin to jEdit, allowing jEdit to initialize the plugin and send it messages.
 * $Id$
 */

package org.tei_c.jedit.teiplugin;
import org.gjt.sp.jedit.EBPlugin;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.msg.PropertiesChanged;

/**
 * The TEI plugin
 * 
 * @author Conal Tuohy
 */
public class TEIPlugin extends EBPlugin {
	public static final String NAME = "tei";
	public static final String OPTION_PREFIX = "options.tei.";
	//private static final TEI tei = new TEI();
	
	public void start() {
		super.start();
		TEI.getInstance();
	}
	//public static TEI getTEI() {
	///	return tei;
	//}
		// EBComponent implementation
	
    // {{{ handleMessage
	public void handleMessage(EBMessage message) {
		if (message instanceof PropertiesChanged) {
			propertiesChanged();
		}
	}
    // }}}
    // {{{ propertiesChanged
	private void propertiesChanged() {
	}
    // }}}
}
