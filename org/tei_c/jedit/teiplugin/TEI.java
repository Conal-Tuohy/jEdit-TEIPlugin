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

import java.net.HttpURLConnection;
import java.net.URLConnection;

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

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.StandardUtilities;

/**
 * The main class of the TEI plugin
 *
 */
public class TEI {
	private static TEI singleton = null;
	private TemplateMenuProvider templateMenuProvider;
	private View view;

	/**
	 * 
	 * @param view the current jedit window
	 * @param position a variable passed in from the script in actions.xml,
	 * 	which can be DockableWindowManager.FLOATING, TOP, BOTTOM, LEFT, RIGHT, etc.
	 * 	see @ref DockableWindowManager for possible values.
	 */
	public TEI() {
		super();
		templateMenuProvider = new TemplateMenuProvider(
			new File(
				getPlugin().getPluginHome(),
				"tei/templates/"
			)
		);
    		processor = new Processor(false); // commercial license key not required
    		xpathCompiler = processor.newXPathCompiler();

		/* retrieve settings */
		/*
		if (jEdit.getSettingsDirectory() != null) {
			this.filename = jEdit.getProperty(TEIPlugin.OPTION_PREFIX
					+ "filepath");
			if (this.filename == null || this.filename.length() == 0) {
				this.filename = new String(jEdit.getSettingsDirectory()
						+ File.separator + "qn.txt");
				jEdit.setProperty(
						TEIPlugin.OPTION_PREFIX + "filepath",
						this.filename);
			}
			this.defaultFilename = this.filename;
		}
		*/
	}


	/**
	* Return the single instance of this class
	*/
	public static TEI getInstance() {
		if (singleton == null) {
			singleton = new TEI();
		}
		return singleton;
	}
    
	private void propertiesChanged() {
	}
    
	// TEIActions implementation

	
	/**
	* Provides an interface for jEdit to ask the TEI Plugin to populate a menu
	*/
	public TemplateMenuProvider getTemplateMenuProvider() {
		return templateMenuProvider;
	};
    
    	static void debug(String label, Object object) {
    		debug(label + ": " + object);
    	}
    	static void debug(String message) {
    		org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG, singleton, message);
    	}
     	static void error(String label, Object object) {
    		error(label + ": " + object);
    	}
    	static void error(String message) {
    		org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.ERROR, singleton, message);
    	}   	

   	/**
	 * 
	 * Make a connection to an HTTP URL, following any redirects
	 *
	 */
	 private HttpURLConnection getConnection(String url)  throws java.net.MalformedURLException, java.io.IOException {
	 	 debug("Connecting to URL", url);
    		HttpURLConnection connection = (HttpURLConnection) new java.net.URL(url).openConnection();
		// may need to follow redirects   			
		java.net.HttpURLConnection httpConnection = (java.net.HttpURLConnection) connection;
		httpConnection.setInstanceFollowRedirects(true);
		// TEI web server is configured to require the following request headers; otherwise it returns a 404! */
		//httpConnection.setRequestProperty("Host", "www.tei-c.org");
		//httpConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		//httpConnection.setRequestProperty("User-Agent", "jEdit TEI Plugin");
		int status = httpConnection.getResponseCode();
		debug("Retrieved HTTP status code", status);
		if (status == java.net.HttpURLConnection.HTTP_MOVED_TEMP | status == java.net.HttpURLConnection.HTTP_MOVED_PERM) {
			url = connection.getHeaderField("Location");
			return getConnection(url);
		} else return connection;
    	}
    
   	/**
	 * 
	 * Download from an HTTP URL into a File
	 *
	 */
	 private File downloadResource(String url, String filename) throws java.net.MalformedURLException, java.io.IOException, java.io.FileNotFoundException {
    		// get folder to download into
    		debug("Downloading", url);
		File pluginHome = getPlugin().getPluginHome();
		pluginHome.mkdir();
		File downloadTarget = new File(pluginHome, filename);
		if (!downloadTarget.exists()) {
	    		 URLConnection connection = getConnection(url);
	    		 InputStream inputStream = connection.getInputStream();
	    		 streamToFile(inputStream, downloadTarget);
	    	}
    		debug("Download complete");
    		return downloadTarget;
    	}
    	
    	
   	/**
	 * 
	 * Writes the content of an InputStream to a File
	 *
	 */
	 private void streamToFile(InputStream inputStream, File file) throws IOException {
    		OutputStream outputStream = new FileOutputStream(file);
    		byte[] buffer = new byte[1024];
    		int bytesRead;
		while ((bytesRead = inputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.close();
    	}
    	
    	private Processor processor = new Processor(false); // commercial license key not required
    	private XPathCompiler xpathCompiler = processor.newXPathCompiler();

    	
   	/**
	 * 
	 * Read a Document from a File
	 *
	 */
	 private XdmNode getDocument(File file) throws SaxonApiException {
    		DocumentBuilder builder = processor.newDocumentBuilder();
    		return builder.build(file);
    	}

   	/**
	 * 
	 * Checks the TEI website for updated package, which it downloads and installs
	 *
	 */
    	public void updateTEIPackage() {
    		// download and install the Oxygen TEI package
    		org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG, singleton, "Checking for updated package...");
    		try {
    			XdmNode packageMetadataDocument = getDocument(
    				downloadResource("https://www.tei-c.org/release/oxygen/updateSite.oxygen", "package-metadata.xml")
			);
			// declare namespace of Oxygen's "extension" vocabulary
			xpathCompiler.declareNamespace("xt", "http://www.oxygenxml.com/ns/extension");
			// compile XPath expression identifying the location of the last extension in the document
			XPathExecutable executable = xpathCompiler.compile("/xt:extensions/xt:extension[last()]/xt:location/@href");
			// prepare XPath for execution
			XPathSelector selector = executable.load();
			// specify the package metadata documents as the context in which to evaluate the XPath
			selector.setContextItem(packageMetadataDocument);
			/// evaluate the XPath as a String value
			String packageLocation = selector.evaluateSingle().getStringValue();
			debug("Location of latest package", packageLocation);
			// Download the actual package as package.zip
			File teiPackage = downloadResource(packageLocation, "package.zip");
			unpackPackage(teiPackage);
    		} catch (Exception e) {
    			error("Failed to update TEI package", e);
    			StackTraceElement[] stack = e.getStackTrace();
    			for (int i = 0; i < stack.length; i++) {
    				error(stack[i].toString());
    			}
    		}
    		debug("Download package action ends");
    	}
    	
    	private TEIPlugin getPlugin() {
    		return (TEIPlugin)  jEdit.getPlugin(TEIPlugin.class.getName());
    	}
    	
    	private static String TEI_PACKAGE_TEMPLATES_FOLDER = "tei/templates/";
    	
    	private boolean shouldUnpackEntry(ZipEntry entry) {
    		// Controls which zip file entries are unpacked from the TEI package
		String entryName = entry.getName();
		
		// ignore folders and files which Oxygen wants but the jEdit TEI plugin doesn't currently use
		boolean ignorableFoldersAndFiles = 
			entryName.startsWith(TEI_PACKAGE_TEMPLATES_FOLDER + "icons/") ||
			entryName.startsWith("tei/i18n/") ||
			entryName.startsWith("tei/resources/") ||
			entryName.startsWith("tei/styleguide/") ||
			entryName.startsWith("tei/web/") ||
			entryName.startsWith("tei/xml/tei/css/") ||
			entryName.startsWith("tei/xml/tei/jtei_aux/") ||
			entryName.startsWith("tei/xml/tei/odd/") ||
			entryName.endsWith("/catalog.xml") ||
			entryName.equals("tei/tei.jar") ||
			entryName.equals("tei/tei-node-customizer.jar") ||
			entryName.equals("tei/LICENSE.txt") ||
			entryName.equals("tei/README.txt") ||
			entryName.endsWith(".framework");
			
		// ignore anything in the templates folder which is neither a directory nor an XML file
		boolean ignorableTemplates = 
			entryName.startsWith(TEI_PACKAGE_TEMPLATES_FOLDER) && 
			! (entry.isDirectory() || entryName.endsWith(".xml"));
			
		// unpack anything which is not ignorable
		return ! (ignorableFoldersAndFiles || ignorableTemplates);
    	}
    	
	private void unpackPackage(File teiPackage) throws FileNotFoundException, IOException {
		// extract templates from the package
		// get "Templates" folder
		// unzip templates subfolders (exclude the icons folder)
		debug("Unpacking package...");
		ZipInputStream zip = new ZipInputStream(new FileInputStream(teiPackage));
		ZipEntry entry = zip.getNextEntry();
		File pluginHomeFolder = getPlugin().getPluginHome();
		while (entry != null) {
			String entryName = entry.getName();
			debug("Resource found in package", entryName);
			if (shouldUnpackEntry(entry)) {
				if (entry. isDirectory()) {
					File directory = new File(pluginHomeFolder, entryName);
					if (!directory.exists()) {
						debug("Creating directory", directory.getPath());
						directory.mkdir();
					}
				} else {
					File file = new File(pluginHomeFolder, entryName);
					debug("Extracting file", file.getPath());
					streamToFile(zip, file);
				}
			}
			zip.closeEntry();
			entry = zip.getNextEntry();
		}
		zip.close();
	}
    	
	/**
	 * Read a template file into a String
	 */
	private String readTemplate(String filename) {
		if (filename == null || filename.length() == 0)
			return "";

		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(filename));
			StringBuffer sb = new StringBuffer(2048);
			String str;
			while ((str = bf.readLine()) != null) {
				sb.append(str).append('\n');
			}
			bf.close();
			return sb.toString();
		} catch (FileNotFoundException fnf) {
			Log.log(Log.ERROR, TEI.class, "Template file " + filename + " does not exist");
		} catch (IOException ioe) {
			Log.log(Log.ERROR, TEI.class, "Could not read template file " + filename);
		} finally {
			return "";
		}
	}
}
