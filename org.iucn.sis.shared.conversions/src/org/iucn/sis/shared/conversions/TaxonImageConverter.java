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
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

public class TaxonImageConverter extends GenericConverter<VFSInfo> {
	
	/*
	 * Set to false if you plan to manually copy the 
	 * images to the appropriate destination, or 
	 * set to true to let the program handle this.
	 */
	private static final boolean COPY_FROM_BIN = false;

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
		
		for (VFSPathToken token : path.getTokens()) {
			final VFSPath uri = path.child(token);
			if (data.getOldVFS().isCollection(uri)) {
				copyXML(path, batchSize);
			}
			else if (token.toString().endsWith(".xml")) {
				try {
					int index = token.toString().lastIndexOf('.');
					Integer taxonID = Integer.valueOf(token.toString().substring(0, index));
					
					final TaxonIO io = new TaxonIO(session);
					final Taxon taxon = io.getTaxon(taxonID);
					if (taxon == null) {
						printf("No taxon saved with the ID %s, skipping...", taxonID);
						continue;
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
				} catch (Exception e) {
					printf("Skipping images for {0} due to exception: {1}", token, e.getMessage());
					continue;
				}
			}
		}
	}
	
	private void batchIfNeeded(AtomicInteger batchSize) {
		if (batchSize.incrementAndGet() >= 25)
			commitAndStartTransaction();
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
