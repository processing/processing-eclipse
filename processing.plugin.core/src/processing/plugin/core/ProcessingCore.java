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
package processing.plugin.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

import processing.plugin.core.model.LibraryModel;
 
/**
 * The plug-in activator enabling the core (UI-free) support for Processing sketches.
 * <p>
 * The single instance of this class can be accessed from any plug-in declaring the 
 * Processing core plug-in as a prerequisite via 
 * <code>ProcessingCore.getProcessingCore()</code>. The Processing core plug-in 
 * will be activated and instantiated automatically if it is not already active.
 * </p>
 */
public final class ProcessingCore extends Plugin {

	/** The plug-in identifier of the Processing core support */
	public static final String PLUGIN_ID = "processing.plugin.core";

	/** shared plugin object */
	private static ProcessingCore plugin;

	// shared objects
	private ResourceBundle resourceBundle;
	private LibraryModel pLibs;

	/**
	 * Creates the Processing core plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the eclipse platform. 
	 * Clients must not call.
	 * </p>
	 * <p>
	 * Patterned after the JDT, this is public, but I'm not sure why. I thought
	 * singletons were supposed to have private constructors. [lonnen 09 09 2010]
	 */
	public ProcessingCore() {
		super();
		plugin = this;
		try{
			this.resourceBundle = ResourceBundle.getBundle(PLUGIN_ID + ".CorePluginResources");
		} catch (MissingResourceException x) {
			this.resourceBundle = null;
		}
	}

	/** Returns the single model instance providing access to Processing libraries */
	public LibraryModel getLibraryModel() {
		return (pLibs == null) ? new LibraryModel() : pLibs;
	}
	
	// any special initialization and shutdown goes here
	/* 	public void start(BundleContext context) throws Exception {} */
	/* 	public void stop(BundleContext context) throws Exception {} */

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

	/**
	 * Finds and retrieves core.jar in the resource bundle.
	 * 
	 * @return File core.jar
	 */
	public File getCoreJarFile() {
		URL coreLoc = getPluginResource("lib");
		try{
			File folder = new File(FileLocator.toFileURL(coreLoc).getPath());
			if (folder.exists()) {
				File core = new File(folder, "core.jar");
				if (core.exists())
					return core;
			}
		}catch (Exception e) {
			ProcessingCore.logError(e);
		}
		ProcessingCore.logInfo("Something went wrong getting the core.jar file from the Processing plug-in. All sketches will break without this. Please reinstall the plug-in.");
		return null;
	}

	
	/** Returns a file handle to the plug-in's local cache folder. */
	public File getBuiltInCacheFolder() {
		return new File(this.getStateLocation().toString());
	}

	/** Returns a file handle to the temp folder in the plug-in's local cache*/
	public File getPluginTempFolder() {
		return new File(getBuiltInCacheFolder(), "temp");
	}

	/** Returns the plug-in's resource bundle */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/** 
	 * Get the workspace the platform workspace.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/** Returns the single instance of the Processing core plug-in runtime class. */
	public static ProcessingCore getCore() {
		return plugin;
	}
	
	/** Returns true if the resource is a Processing file */	
	public static boolean isProcessingFile(IResource resource) {
		if (resource.getType() == IResource.FILE)
			return isProcessingFile(resource.getName());
		return false;
	}

	/** Returns true if the file is a Processing file  */
	public static boolean isProcessingFile(IFile resource) {
		return isProcessingFile(resource.getName());
	}

	/** Returns true if the file has a Processing extension */
	public static boolean isProcessingFile(String filename) {
		return filename.endsWith(".pde");
	}
	
	//////// LOGGING STUFF
	
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
	 * Adapter method that creates the appropriate status to be logged
	 * 
	 * @param severity integer code indicating the type of message
	 * @param code plug-in-specific status code
	 * @param message a human readable message
	 */
	public static void log(int severity, int code, String message, Throwable exception) {
		log(ProcessingCore.createStatus(severity, code, message, exception));
	}

	/**
	 * Convenience method for appending an exception with a message
	 * 
	 * @param message a message, preferably something about the problem
	 * @param exception the problem
	 */
	public static void logError(String message, Throwable exception) {
		ProcessingCore.log(IStatus.ERROR, IStatus.OK, message, exception);
	}

	/**
	 * Convenience method for appending an unexpected exception to the log file
	 * 
	 * @param exception some problem
	 */
	public static void logError(Throwable exception) {
		ProcessingCore.logError("Unexpected Exception", exception);
	}

	/**
	 * Convenience method for appending a string to the log file.
	 * Don't use this if there is an error.
	 * 
	 * @param message something to append to the log file 
	 */
	public static void logInfo(String message) {
		ProcessingCore.log(IStatus.INFO, IStatus.OK, message, null);
	}

	/**
	 * Write a status to the log
	 * 
	 * @param status
	 */
	public static void log(IStatus status) {
		ProcessingCore.getCore().getLog().log(status);
	}

}
