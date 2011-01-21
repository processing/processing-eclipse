/**
 * Copyright (c) 2010 Ben Fry and Casey Reas
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Chris Lonnen - initial API and implementation
 */
package processing.plugin.ui;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import processing.plugin.core.ProcessingCore;
import processing.plugin.ui.editor.ProcessingPartitionScanner;
import processing.plugin.ui.editor.language.ProcessingCodeScanner;
import processing.plugin.ui.editor.util.ProcessingColorProvider;

/**
 * Controls Processing Plugin user interface elements.
 * <p>
 * It provides convenience methods and controls elements shared by editors and other UI elements
 * such as the document provider or the partitioning scanner.
 */
public class ProcessingPlugin extends AbstractUIPlugin {

	/** The ID of the Processing UI Plugin */
	public static final String PLUGIN_ID = "processing.plugin.ui"; //$NON-NLS-1$

	/** The ID of the Processing Perspective */
	public static final String ID_PERSPECTIVE = PLUGIN_ID + ".sketchingPerspecitve";

	/** The ID of the processing partioning scheme */
	public static final String PROCESSING_PARTITIONING = "__processing_partitioning";

	/** The shared plugin instance */
	private static ProcessingPlugin plugin;

	// Supporting Objects
	private ProcessingPartitionScanner fPartitionScanner;
	private ProcessingColorProvider fColorProvider;
	private ProcessingCodeScanner fCodeScanner;

	/** Initialized the shared instance. */
	public ProcessingPlugin() {
		super();
		plugin = this;
	}

	/* Method declared in plug-in */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		// Other init stuff
		// Don't forget to shut it down in stop()!
	}

	/* Method declared in plug-in */
	public void stop(BundleContext context) throws Exception {
		try{
			plugin = null;
			if(fPartitionScanner != null)
				fPartitionScanner = null;
			if(fColorProvider != null) {
				fColorProvider.dispose();
				fColorProvider = null;
			}
			if(fCodeScanner != null)
				fCodeScanner = null;
		} finally {
			super.stop(context);
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ProcessingPlugin getDefault() {
		return plugin;
	}

	/**
	 * Return a scanner for creating Processing partitions.
	 * Processing uses Java's commenting scheme, so our partitioner is almost identical. Unlike
	 * the Java partitioner, however, the Processing scanner treats the JavaDoc comments as 
	 * simple multiline comments. 
	 * 
	 * @return a scanner for creating Processing partitions
	 */
	public ProcessingPartitionScanner getProcessingPartitionScanner() {
		return (fPartitionScanner == null) ? new ProcessingPartitionScanner() : fPartitionScanner;
	}

	/**
	 * Returns the shared code scanner.
	 * 
	 * @return the singleton Processing code scanner
	 */
	public RuleBasedScanner getProcessingCodeScanner() {
		return (fCodeScanner == null) ? new ProcessingCodeScanner(getProcessingColorProvider()) : fCodeScanner;
	}

	/**
	 * Returns the shared color provider.
	 * 
	 * @return the singleton Processing color provider
	 */
	public ProcessingColorProvider getProcessingColorProvider() {
		return (fColorProvider == null) ? new ProcessingColorProvider() : fColorProvider;
	}

	///// PLUGIN RESOURCES
	
	/** 
	 * Gets a URL to a file or folder in the plug-in's Resources folder.
	 * Returns null if the path results in a bad URL.
	 * 
	 * @param path relative path from the Resources folder
	 */
	public URL getPluginResource(String path) {
		try{
			return new URL(this.getBundle().getEntry("/"), "Resources/" + path);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	/**
	 * Returns a URL to a file or folder in the plug-in's Resources folder.
	 * Returns null if the path results in a bad URL.
	 * 
	 * @param path relative from the Resources folder
	 */
	public URL getPluginResource(IPath path) {
		return getPluginResource(path.toOSString());
	}
	
	/**
	 * Resolves the plug-in resources folder to a File and returns it. This will include the
	 * Processing libraries and the core libraries folder.
	 * 
	 * @return File reference to the core resources
	 */
	public File getPluginResourceFolder() {
		URL fileLocation = getPluginResource("");
		try {
			File folder = new File(FileLocator.toFileURL(fileLocation).getPath());
			if (folder.exists())
				return folder;
		} catch (Exception e) {
			ProcessingCore.logError(e);
		}
		return null;
	}
	
	///// LOGGING STUFF
	
	/**
	 * Creates a status object to log
	 * 
	 * @param severity integer code indicating the type of message
	 * @param code plug-in-specific status code
	 * @param message a human readable message
	 * @param a low-level exception, or null
	 * @return status object
	 */
	public static IStatus createStatus(int severity, int code, String message, Throwable exception) {
		return new Status(severity, PLUGIN_ID, code, message, exception);
	}

	/**
	 * Write a status to the log
	 * 
	 * @param status
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * Convenience method for appending a string to the log file.
	 * Don't use this if there is an error.
	 * 
	 * @param message something to append to the log file 
	 */
	public static void logInfo(String message) {
		log(IStatus.INFO, IStatus.OK, message, null);
	}

	/**
	 * Convenience method for appending an unexpected exception to the log file
	 * 
	 * @param exception some problem
	 */
	public static void logError(Throwable exception) {
		logError("Unexpected Exception", exception);
	}

	/**
	 * Convenience method for appending an exception with a message
	 * 
	 * @param message a message, preferably something about the problem
	 * @param exception the problem
	 */
	public static void logError(String message, Throwable exception) {
		log(IStatus.ERROR, IStatus.OK, message, exception);
	}

	/**
	 * Adapter method that creates the appropriate status to be logged
	 * 
	 * @param severity integer code indicating the type of message
	 * @param code plug-in-specific status code
	 * @param message a human readable message
	 */
	public static void log(int severity, int code, String message, Throwable exception) {
		log(createStatus(severity, code, message, exception));
	}

}
