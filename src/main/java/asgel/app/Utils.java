package asgel.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;

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

	public static void write(File f, String data) {
		try {
			Files.writeString(f.toPath(), data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}