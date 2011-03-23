package org.iucn.sis.shared.conversions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.shared.api.models.User;

import com.solertium.util.BaseTagListener;
import com.solertium.util.TagFilter;
import com.solertium.util.TagFilter.Tag;
import com.solertium.vfs.VFSPath;

public class UserWorkingSetConverter extends GenericConverter<VFSInfo> {
	
	/**
	 * Assumes that:
	 *  - Users have already been converted
	 *  - Working Sets have already been converted
	 *  - Working Set IDs have remained the same.
	 */
	protected void run() throws Exception {
		final AtomicInteger count = new AtomicInteger(0);
		final UserIO userIO = new UserIO(session);
		final WorkingSetIO wsIO = new WorkingSetIO(session);
		
		for (final User user : userIO.getAllUsers()) {
			final VFSPath workingSetPath = new VFSPath("/users/" + user.getUsername() + "/workingSet.xml");
			
			if (data.getOldVFS().exists(workingSetPath)) {
				final TagFilter tf = new TagFilter(data.getOldVFS().getReader(workingSetPath));
				tf.shortCircuitClosingTags = true;
				tf.registerListener(new BaseTagListener() {
					public void process(Tag t) throws IOException {
						if (!user.getUsername().equals(t.getAttribute("creator"))) {
							printf("Subscribing %s to ws %s", user.getUsername(), t.getAttribute("id"));
							boolean success = wsIO.subscribeToWorkingSet(Integer.valueOf(t.getAttribute("id")), user);
							if (!success)
								printf("# Failed to subscribe %s to ws %s", user.getUsername(), t.getAttribute("id"));
							else {
								printf("Subscribed %s to ws %s", user.getUsername(), t.getAttribute("id"));
								if (count.incrementAndGet() % 50 == 0) {
									printf("Converted %s working set subscriptions...", count.get());
									commitAndStartTransaction();
								}
							}
						}
					}
					public List<String> interestingTagNames() {
						List<String> list = new ArrayList<String>();
						list.add("workingSet");
						return list;
					}
				});
				tf.parse();
			}
		}
	}
	
	

}
