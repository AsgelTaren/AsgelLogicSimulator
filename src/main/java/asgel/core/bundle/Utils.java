package asgel.core.bundle;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Utils {

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

}
