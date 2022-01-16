package raubach.fricklweb.server.util;

import com.icafe4j.image.meta.*;
import com.icafe4j.image.meta.iptc.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Sebastian Raubach
 */
public class TagUtils
{
	public static synchronized void deleteTagFromImage(File file, List<String> tags)
		throws IOException
	{
		Map<MetadataType, Metadata> metadataMap = Metadata.readMetadata(file);
		IPTC iptc = (IPTC) metadataMap.get(MetadataType.IPTC);

		// Check if IPTC even exists
		if (iptc != null)
		{
			// Get all information
			Map<String, List<IPTCDataSet>> datasetMap = iptc.getDataSets();

			// Get keywords
			List<IPTCDataSet> datasets = datasetMap.get(IPTCApplicationTag.KEY_WORDS.getName());

			// If any exist
			if (datasets != null)
			{
				Logger.getLogger("").info("DELETING TAGS: " + tags + " FROM " + datasets.stream().map(IPTCDataSet::getDataAsString).collect(Collectors.joining(", ")));

				// Remove the ones that matches the given tags
				boolean toRemove = datasets.removeIf(ds -> tags.contains(ds.getDataAsString()));

				Logger.getLogger("").info("REMAINING TAGS: " + datasets.stream().map(IPTCDataSet::getDataAsString).collect(Collectors.joining(", ")));

				if (toRemove)
				{
					// Write the file to temp, then move to overwrite
					File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-temp");
					folder.mkdirs();
					File target = new File(folder, UUID.randomUUID() + ".jpg");
					try (InputStream in = new FileInputStream(file);
						 OutputStream out = new FileOutputStream(target))
					{
						Metadata.insertIPTC(in, out, datasets);
					}
					Files.move(target.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	public static void main(String[] args)
		throws IOException
	{
		File file = new File("C:/Users/sr41756/Photos/wallpapers/03832_blandfordroad_3440x1440.jpg");

		addTagToImage(file, new ArrayList<>(Arrays.asList("testing", "a", "b")));

		deleteTagFromImage(file, new ArrayList<>(Arrays.asList("testing", "a", "b")));
	}

	public static synchronized void addTagToImage(File file, List<String> tags)
		throws IOException
	{
		Map<MetadataType, Metadata> metadataMap = Metadata.readMetadata(file);
		IPTC iptc = (IPTC) metadataMap.get(MetadataType.IPTC);

		// If no IPTC exists, create it
		if (iptc == null)
			iptc = new IPTC();

		// Get all information from it
		Map<String, List<IPTCDataSet>> datasetMap = iptc.getDataSets();

		// Get keywords
		List<IPTCDataSet> datasets = datasetMap.get(IPTCApplicationTag.KEY_WORDS.getName());

		// If none exist, create a new list
		if (datasets == null)
			datasets = new ArrayList<>();

		// Remove the ones that are already there
		List<String> tagStrings = datasets.stream()
										  .map(IPTCDataSet::getDataAsString)
										  .collect(Collectors.toList());
		tags.removeAll(tagStrings);

		if (tags.size() > 0)
		{
			// Add it if not present
			for (String tag : tags)
				datasets.add(new IPTCDataSet(IPTCApplicationTag.KEY_WORDS, tag));

			// Write the file to temp, then move to overwrite
			File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-temp");
			folder.mkdirs();
			File target = new File(folder, UUID.randomUUID() + ".jpg");
			try (InputStream in = new FileInputStream(file);
				 OutputStream out = new FileOutputStream(target))
			{
				Metadata.insertIPTC(in, out, datasets);
			}
			Files.move(target.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
