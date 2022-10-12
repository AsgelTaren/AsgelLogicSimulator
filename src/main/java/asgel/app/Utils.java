package asgel.app;

import java.io.File;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;

public class Utils {

	public static InputStream openStreamInJar(File jar, String pathInjar) {
		try {
			URL url = new URL("jar:file:/" + jar.getAbsolutePath() + "!/" + pathInjar);
			JarURLConnection con = (JarURLConnection) url.openConnection();
			return con.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}