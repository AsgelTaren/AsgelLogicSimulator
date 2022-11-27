package asgel.core.model;

import java.io.File;

import javax.swing.JFrame;

/**
 * @author Florent Guille
 **/

public interface IParametersRequester {

	public String[] getParameters(String... args);

	public int[] getParametersAsInt(String... args);

	public File getFile(String folder);

	public File getWorkingDir();

	public JFrame getJFrame();

}
