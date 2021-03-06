package de.zahlii.youtube.download.basic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.StandardArtwork;

import de.zahlii.youtube.download.QueueEntry;

/**
 * Wrapper around the jAudioTagger library for easier handling of necessary information in the audio tags.
 * 
 * @author Zahlii
 * 
 */
public class TagEditor {
	private final QueueEntry entry;
	private final File musicFile;
	private AudioFile song;
	private Tag tag;

	/**
	 * Opens a music(video) file for read/write access on the tag information.
	 * 
	 * @param file
	 * @param entry
	 */
	public TagEditor(final File file, final QueueEntry entry) {
		musicFile = file;
		this.entry = entry;

		try {
			song = AudioFileIO.read(musicFile);
			tag = song.getTag();
		} catch (IOException | InvalidAudioFrameException | CannotReadException | TagException | ReadOnlyFileException e) {
			Logging.log("failed loading audio file", e);
			tag = null;
			song = null;
		}
	}

	/**
	 * Saves all changes done to the tags
	 */
	public void commit() {
		try {
			AudioFileIO.write(song);
		} catch (final CannotWriteException e) {
			Logging.log("failed committing audio data", e);
		}
	}

	public File getFile() {
		return musicFile;
	}

	/**
	 * Construct a BufferedImage out of embedded album art.
	 * 
	 * @return
	 */
	public BufferedImage readArtwork() {
		int s;
		try {
			s = tag.getArtworkList().size();
		} catch (final NullPointerException e) {
			s = 0;
		}

		if (s == 0)
			return null;

		final Artwork a = tag.getFirstArtwork();
		final byte[] data = a.getBinaryData();
		BufferedImage img;
		try {
			img = ImageIO.read(new ByteArrayInputStream(data));
			return img;
		} catch (final IOException e) {
			Logging.log("failed to extract artwork", e);
			return null;
		}
	}

	/**
	 * Reads a specific field out of the tag information.
	 * 
	 * @param f
	 * @return Information or ""
	 */
	public String readField(final FieldKey f) {
		try {
			final String s = tag.getFirst(f);
			return s == null ? "" : s;
		} catch (final KeyNotFoundException e) {
			return "";
		}
	}

	public void writeAllFields(final Map<FieldKey, String> fields) {
		try {
			for (final Entry<FieldKey, String> e : fields.entrySet()) {
				tag.setField(e.getKey(), e.getValue().replace("\\r", "").trim());
			}
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			Logging.log("failed writing fields", e);
		}
	}

	public void writeArtwork(final BufferedImage img) {
		if (img == null)
			return;
		try {
			final StandardArtwork s = new StandardArtwork();

			ImageIO.write(img, "png", entry.getCoverTempFile());
			s.setFromFile(entry.getCoverTempFile());
			tag.deleteArtworkField();
			tag.addField(s);
		} catch (FieldDataInvalidException | IOException e) {
			Logging.log("failed to save artwork", e);
		}
	}

	public void writeField(final FieldKey f, final String v) {
		try {
			tag.setField(f, v.replace("\\r", "").trim());
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			Logging.log("failed writing field " + f, e);
		}
	}
}
