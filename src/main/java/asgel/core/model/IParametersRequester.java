package asgel.core.model;

import java.io.File;

import javax.swing.JFrame;

import asgel.app.App;

/**
 * @author Florent Guille
 **/

public interface IParametersRequester {

	public App getApp();

	public File getWorkingFile();

	public String[] getParameters(String... args);

	public int[] getParametersAsInt(String... args);

	public File getFile(String folder);

	public File getWorkingDir();

	public JFrame getJFrame();

}
