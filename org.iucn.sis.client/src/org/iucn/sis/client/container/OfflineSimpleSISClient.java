package org.iucn.sis.client.container;

import org.iucn.sis.client.api.container.SISClientBase;

public class OfflineSimpleSISClient extends SimpleSISClient {
	
	@Override
	public void loadModule() {
		SISClientBase.iAmOnline = false;
		super.loadModule();
	}

}