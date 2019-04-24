package raubach.fricklweb.server.util;

import com.icafe4j.image.meta.*;
import com.icafe4j.image.meta.iptc.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * @author Sebastian Raubach
 */
public class TagUtils
{
	public static synchronized void deleteTagFromImage(File file, String tag)
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
				// Remove the ones that matches the given tag
				datasets.removeIf(ds -> Objects.equals(ds.getDataAsString(), tag));

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

	public static synchronized void addTagToImage(File file, String tag)
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

		// Check if the one in question is already present
		boolean exists = datasets.stream()
								 .anyMatch(ds -> Objects.equals(ds.getDataAsString(), tag));

		if (!exists)
		{
			// Add it if not present
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
