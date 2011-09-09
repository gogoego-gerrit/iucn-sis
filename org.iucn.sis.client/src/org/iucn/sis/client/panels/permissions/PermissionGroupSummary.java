package org.iucn.sis.client.panels.permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.caches.AuthorizationCache;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.Permission;
import org.iucn.sis.shared.api.models.PermissionGroup;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.solertium.util.extjs.client.WindowUtils;

public class PermissionGroupSummary {
	
	public static void summarize(PermissionGroup group) {
		Window window = WindowUtils.newWindow("Permission Group Summary for " + group.getName() + " (Beta)");
		window.setSize(500, 300);
		window.setScrollMode(Scroll.AUTO);
		window.setLayout(new FlowLayout());
		
		//Ensure the model is the full model & not just a shim...
		List<String> parents = new ArrayList<String>();
		PermissionGroup current = group;
		do {
			if (current.getParent() != null) {
				current.setParent(AuthorizationCache.impl.getGroup(current.getParent().getId()));
				if (current.getParent() != null)
					parents.add(current.getParent().getName());
			}
			
			current = current.getParent();
		} while (current != null);
		
		Map<String, Permission> map = group.getResourceToPermission();
		Map<String, Boolean> features = new HashMap<String, Boolean>();
		Map<String, List<String>> allow = new HashMap<String, List<String>>();
		Map<String, List<String>> deny = new HashMap<String, List<String>>();
		for (Permission perm : map.values()) {
			if (perm.getUrl().equals(PermissionGroup.DEFAULT_PERMISSION_URI))
				continue;
			else if (perm.getUrl().startsWith("feature")) {
				features.put(perm.getUrl(), perm.isUse());
			}
			else {
				List<String> yes = new ArrayList<String>();
				List<String> no = new ArrayList<String>();
				for (String operation : new String[] { AuthorizableObject.READ, AuthorizableObject.WRITE, AuthorizableObject.CREATE, 
					AuthorizableObject.DELETE, AuthorizableObject.GRANT }) {
					if (perm.check(operation))
						yes.add(operation);
					else
						no.add(operation);
					
					if (!yes.isEmpty())
						allow.put(perm.getUrl(), yes);
					if (!no.isEmpty())
						deny.put(perm.getUrl(), no);
				}
			}
		}
		
		if (!parents.isEmpty()) {
			window.add(println("<b>NOTE: This permission group inherits from " + showChain(parents) + ".</b>"));
		}
		
		window.add(println("For taxa and assessments in the scope of " + showScope(group.getScopeURI()) + "..."));
		if (allow.isEmpty())
			window.add(println("Users are not granted permissions to any resources."));
		else  {
			window.add(println("Users are granted permissions to the following resources:"));
			for (Map.Entry<String, List<String>> entry : allow.entrySet()) {
				window.add(println(" - " + showChecks(entry.getValue()) + " " + showResourceName(entry.getKey())));
			}
		}
		if (deny.isEmpty())
			window.add(println("Users are not denied permissions to any resources."));
		else {
			window.add(println("Users are denied permissions to the following resources:"));
			for (Map.Entry<String, List<String>> entry : deny.entrySet()) {
				window.add(println(" - " + showChecks(entry.getValue()) + " " + showResourceName(entry.getKey())));
			}
		}
		if (!features.isEmpty()) {
			StringBuilder allowFeature = new StringBuilder();
			for (Map.Entry<String, Boolean> entry : features.entrySet()) {
				if (entry.getValue())
					allowFeature.append(" - " + showFeatureName(entry.getKey()) + "<br/>");
			}
			if (!"".equals(allowFeature.toString())) {
				window.add(println("These users are allowed access to the following features:"));
				window.add(println(allowFeature.toString()));
			}
			
			StringBuilder denyFeature = new StringBuilder();
			for (Map.Entry<String, Boolean> entry : features.entrySet()) {
				if (!entry.getValue())
					denyFeature.append(" - " + showFeatureName(entry.getKey()) + "<br/>");
			}
			if (!"".equals(denyFeature.toString())) {
				window.add(println("These users are denied access to the following features:"));
				window.add(println(denyFeature.toString()));
			}
		}
		Permission defaultPerm = map.get(PermissionGroup.DEFAULT_PERMISSION_URI);
		if (defaultPerm == null)
			defaultPerm = new Permission();
		window.add(println("For all other actions, the default permissions are used, which are:"));
		for (String operation : new String[] { AuthorizableObject.READ, AuthorizableObject.WRITE, AuthorizableObject.CREATE, 
				AuthorizableObject.GRANT, AuthorizableObject.DELETE, AuthorizableObject.USE_FEATURE }) {
			window.add(println(" - " + operation + " -> " + defaultPerm.check(operation)));
		}
		
		window.show();
	}
	
	private static String showFeatureName(String url) {
		//TODO: show a friendly name for this feature
		return url;
	}
	
	private static String showResourceName(String url) {
		//TODO: show a friendly name for this resource
		return url;
	}
	
	private static String showChain(List<String> parents) {
		final StringBuilder out = new StringBuilder();
		for (Iterator<String> iter = parents.iterator(); iter.hasNext(); ) {
			out.append(iter.next() + (iter.hasNext() ? ", which inherits from " : ""));
		}
		return out.toString();
	}
	
	private static String showChecks(List<String> checks) {
		final StringBuilder out = new StringBuilder();
		for (Iterator<String> iter = checks.listIterator(); iter.hasNext(); ) {
			out.append(iter.next() + (iter.hasNext() ? ", " : ""));
		}
		return out.toString();
	}
	
	private static String showScope(String scope) {
		if ("".equals(scope))
			return "all taxonomy";
		else if ("workingSets".equals(scope))
			return "all their working sets";
		else if (scope.startsWith("workingSet"))
			return "the selected working set";
		else if (scope.startsWith("taxon"))
			return "the selected taxon";
		else
			return "all taxonomy";
	}
	
	private static Html println(String value) {
		Html html = new Html(value);
		html.addStyleName("fontSize12");
		return html;
	}

}
