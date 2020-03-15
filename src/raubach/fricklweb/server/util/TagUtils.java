package raubach.fricklweb.server.util;

import com.icafe4j.image.meta.Metadata;
import com.icafe4j.image.meta.MetadataType;
import com.icafe4j.image.meta.iptc.IPTC;
import com.icafe4j.image.meta.iptc.IPTCApplicationTag;
import com.icafe4j.image.meta.iptc.IPTCDataSet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
				// Remove the ones that matches the given tags
				boolean toRemove = datasets.removeIf(ds -> tags.contains(ds.getDataAsString()));

				if (toRemove)
				{
					// Write the file to temp, then move to overwrite
					File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-temp");
					folder.mkdirs();
					File target = new File(folder, UUID.randomUUID().toString() + ".jpg");
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
			File target = new File(folder, UUID.randomUUID().toString() + ".jpg");
			try (InputStream in = new FileInputStream(file);
				 OutputStream out = new FileOutputStream(target))
			{
				Metadata.insertIPTC(in, out, datasets);
			}
			Files.move(target.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
