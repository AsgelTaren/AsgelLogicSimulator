package asgel.app;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * @author Florent Guille
 **/

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

	public static InputStream openStreamToJar(File jar) {
		try {
			URL url = new URL("jar:file:/" + jar.getAbsolutePath());
			JarURLConnection con = (JarURLConnection) url.openConnection();
			return con.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String loadFileAsString(InputStream in) throws IOException {
		StringBuilder builder = new StringBuilder();
		InputStreamReader reader = new InputStreamReader(in, "UTF-8");
		BufferedReader br = new BufferedReader(reader);
		String line;
		while ((line = br.readLine()) != null) {
			builder.append(line + "\n");
		}
		br.close();
		return builder.toString();
	}

	public static void write(File f, String data) {
		try {
			Files.writeString(f.toPath(), data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ImageIcon loadIcon(InputStream in, int size) {
		try {
			return new ImageIcon(resize(ImageIO.read(in), size, size));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static BufferedImage resize(BufferedImage in, int width, int height) {
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		result.getGraphics().drawImage(in, 0, 0, width, height, null);
		return result;
	}

	public static BufferedImage loadImage(InputStream in) {
		try {
			return ImageIO.read(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static File resolvePath(File from, File workingDir, String path) {
		String[] split = path.split(":");
		return new File(
				(split[0].equals("relative") ? from.getParentFile().getAbsolutePath() : workingDir.getAbsolutePath())
						+ "/" + split[1]);
	}

}