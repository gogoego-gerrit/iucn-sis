package org.iucn.sis.shared.conversions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonImage;

import com.solertium.util.BaseTagListener;
import com.solertium.util.TagFilter;
import com.solertium.util.TagFilter.Tag;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class TaxonImageConverter extends GenericConverter<VFSInfo> {
	
	/*
	 * Set to false if you plan to manually copy the 
	 * images to the appropriate destination, or 
	 * set to true to let the program handle this.
	 */
	private static final boolean COPY_FROM_BIN = false;
	
	public TaxonImageConverter() {
		super();
		setClearSessionAfterTransaction(true);
	}

	@Override
	protected void run() throws Exception {
		final VFSPath imageRoot = new VFSPath("/images");
		if (!data.getOldVFS().exists(imageRoot))
			return;
		
		VFSPathToken[] tokens = data.getOldVFS().list(imageRoot);
		for (VFSPathToken token : tokens) {
			if ("bin".equals(token.toString())) {
				if (COPY_FROM_BIN)
					copyImages(imageRoot.child(token));
			}
			else {
				copyXML(imageRoot.child(token), new AtomicInteger(0));
			}
		}
		
		commitAndStartTransaction();
	}
	
	private void copyXML(VFSPath path, final AtomicInteger batchSize) throws Exception {
		batchIfNeeded(batchSize);
		
		if (data.getOldVFS().isCollection(path)) {
			VFSPathToken[] tokens = data.getOldVFS().list(path);
			for (VFSPathToken token : tokens)
				copyXML(path.child(token), batchSize);
		}
		else if (path.getName().endsWith(".xml"))
			convert(path, batchSize);
	}
	
	private void convert(VFSPath uri, final AtomicInteger batchSize) throws Exception {		
		try {
			int index = uri.getName().lastIndexOf('.');
			Integer taxonID = Integer.valueOf(uri.getName().substring(0, index));
			
			final TaxonIO io = new TaxonIO(session);
			final Taxon taxon = io.getTaxon(taxonID);
			if (taxon == null) {
				printf("No taxon saved with the ID %s, skipping...", taxonID);
				return;
			}
			
			final TagFilter tf = new TagFilter(data.getOldVFS().getReader(uri));
			tf.shortCircuitClosingTags = true;
			tf.registerListener(new BaseTagListener() {
				public void process(Tag t) throws IOException {
					TaxonImage image = new TaxonImage();
					image.setCaption(t.getAttribute("caption"));
					image.setCredit(t.getAttribute("credit"));
					image.setEncoding(t.getAttribute("encoding"));
					image.setIdentifier(t.getAttribute("id"));
					image.setPrimary("true".equalsIgnoreCase(t.getAttribute("primary")));
					try {
						image.setRating(Float.valueOf(t.getAttribute("rating")));
					} catch (Exception e) {
						image.setRating(0.0F);
					}
					image.setShowRedList("true".equalsIgnoreCase(t.getAttribute("showRedlist")));
					image.setShowSIS("true".equalsIgnoreCase(t.getAttribute("showSIS")));
					image.setSource(t.getAttribute("source"));
					image.setTaxon(taxon);
					try {
						image.setWeight(Integer.valueOf(t.getAttribute("weight")));
					} catch (Exception e) {
						image.setWeight(0);
					}

					session.save(image);
					batchIfNeeded(batchSize);
				}
				public List<String> interestingTagNames() {
					List<String> l = new ArrayList<String>();
					l.add("image");
					return l;
				}
			});
			tf.parse();
		} catch (NotFoundException e) {
			printf("Could not find %s", uri);
		} catch (Exception e) {
			printf("Skipping images for %s due to exception: %s", uri, e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void batchIfNeeded(AtomicInteger batchSize) {
		if (batchSize.incrementAndGet() >= 25) {
			printf("Created %s images...", batchSize.get());
			commitAndStartTransaction();
		}
	}
	
	private void copyImages(VFSPath path) throws Exception {
		data.getNewVFS().makeCollections(path);
		
		for (VFSPathToken token : path.getTokens()) {
			final VFSPath uri = path.child(token);
			
			copyStream(data.getOldVFS().getInputStream(uri), data.getNewVFS().getOutputStream(uri));
		}
	}
	
	private void copyStream(final InputStream is, final OutputStream os) throws IOException {
		final byte[] buf = new byte[65536];
		int i = 0;
		while ((i = is.read(buf)) != -1)
			os.write(buf, 0, i);
		}
	}
