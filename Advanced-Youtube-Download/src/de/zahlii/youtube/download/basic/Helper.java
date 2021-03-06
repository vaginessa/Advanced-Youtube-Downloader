package de.zahlii.youtube.download.basic;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

import de.zahlii.youtube.download.basic.net.WebNavigator;

/**
 * Some basic support functions.
 * 
 * @author Zahlii
 * 
 */
public class Helper {
	/**
	 * Downloads an image from the Web, using WebNavigator
	 * 
	 * @param url
	 * @return
	 */
	public static BufferedImage downloadImage(final String url) {
		final InputStream iSReader = WebNavigator.getInstance().navigateStream(url);
		try {
			return ImageIO.read(iSReader);
		} catch (final IOException e) {
			Logging.log("failed to download artwork", e);
			return new BufferedImage(500, 500, BufferedImage.TYPE_4BYTE_ABGR);
		}
	}

	/**
	 * Might be used for construction of temp files.
	 * 
	 * @param webURL
	 * @return
	 */
	public static String getFileID(final String webURL) {
		try {
			final byte[] bytesOfMessage = webURL.getBytes("UTF-8");

			final MessageDigest md = MessageDigest.getInstance("MD5");

			final byte[] thedigest = md.digest(bytesOfMessage);
			return new String(thedigest, "UTF-8");
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			return sanitize(webURL);
		}
	}

	/**
	 * Removes all chars not accepted by the file system naming scheme.
	 * 
	 * @param main
	 * @return
	 */
	public static String sanitize(final String main) {
		return main.replace("<", "").replace(">", "").replace(":", "").replace("/", "").replace("\\", "").replace("|", "").replace("?", "").replace("*", "").replace("\"", "").replace("\r", "")
				.replace("\n", "");
	}
}
