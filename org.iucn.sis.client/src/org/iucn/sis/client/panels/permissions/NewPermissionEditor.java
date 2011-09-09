package org.iucn.sis.client.panels.permissions;

import org.iucn.sis.client.api.caches.RegionCache;
import org.iucn.sis.client.api.caches.SchemaCache;
import org.iucn.sis.client.api.caches.SchemaCache.AssessmentSchema;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.models.Permission;
import org.iucn.sis.shared.api.models.PermissionResourceAttribute;
import org.iucn.sis.shared.api.models.Region;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.solertium.util.extjs.client.WindowUtils;

public class NewPermissionEditor extends LayoutContainer {
	
	/*
	 * Add any additional types here
	 */
	public static enum PermissionAttributeOptions {
		Assessment
	}
	
	private final boolean editable;
	
	private EditablePermission current;
	
	private CheckBox read;
	private CheckBox write;
	private CheckBox create;
	private CheckBox delete;
	private CheckBox grant;
	private CheckBox use;
	
	private ComboBox<TextValueModelData> region, schema;
	
	private Html displayName;
	
	private String url;
	private String type;
	
	public NewPermissionEditor(boolean editable) {
		this(editable, null);
	}
	
	public NewPermissionEditor(boolean editable, PermissionAttributeOptions options) {
		super();
		
		this.editable = editable;
		
		displayName = new Html();
		displayName.addStyleName("bold");
		
		read = getBox();
		write = getBox();
		create = getBox();
		delete = getBox();
		grant = getBox();
		use = getBox();
	
		HorizontalPanel panel = new HorizontalPanel();
		panel.setVerticalAlign(VerticalAlignment.MIDDLE);
		panel.setSpacing(5);
		
		panel.add(new Html("Read: "));
		panel.add(read);
		
		panel.add(new Html("Write: "));
		panel.add(write);
		
		panel.add(new Html("Create: "));
		panel.add(create);
		
		panel.add(new Html("Delete: "));
		panel.add(delete);
		
		panel.add(new Html("Grant: "));
		panel.add(grant);
		
		panel.add(new Html("Use: "));
		panel.add(use);
		
		HorizontalPanel attributes = new HorizontalPanel();
		attributes.setVerticalAlign(VerticalAlignment.MIDDLE);
		attributes.setSpacing(5);
		
		if (PermissionAttributeOptions.Assessment.equals(options)) {
			region = new ComboBox<TextValueModelData>();
			region.setAllowBlank(true);
			region.setForceSelection(true);
			region.setEditable(false);
			region.setTriggerAction(TriggerAction.ALL);
			region.setName("region");
			region.setData("id", 0);
			
			final ListStore<TextValueModelData> regionStore = new ListStore<TextValueModelData>();
			regionStore.setKeyProvider(new ModelKeyProvider<TextValueModelData>() {
				public String getKey(TextValueModelData model) {
					return model.getValue();
				}
			});
			regionStore.add(new TextValueModelData("Regional/Global", ""));
			regionStore.add(new TextValueModelData("Global", "global"));
			regionStore.add(new TextValueModelData("Any Region", "(\\d+,?)+"));
			for (Region curRegion : RegionCache.impl.getRegions())
				if (curRegion.getId() != Region.GLOBAL_ID)
					regionStore.add(new TextValueModelData(curRegion.getRegionName(), curRegion.getId()+""));
			regionStore.sort("text", SortDir.ASC);
			
			region.setStore(regionStore);
			
			attributes.add(new Html("Region: "));
			attributes.add(region);
			
			schema = new ComboBox<TextValueModelData>();
			schema.setAllowBlank(true);
			schema.setForceSelection(true);
			schema.setEditable(false);
			schema.setTriggerAction(TriggerAction.ALL);
			schema.setName("schema");
			schema.setData("id", 0);
			
			final ListStore<TextValueModelData> schemaStore = new ListStore<TextValueModelData>();
			schemaStore.setKeyProvider(new ModelKeyProvider<TextValueModelData>() {
				public String getKey(TextValueModelData model) {
					return model.getValue();
				}
			});
			schemaStore.add(new TextValueModelData("All Assessments", ".*"));
			for (AssessmentSchema current : SchemaCache.impl.listFromCache())
				schemaStore.add(new TextValueModelData(current.getName(), current.getId()));
			schemaStore.sort("text", SortDir.ASC);
			
			schema.setStore(schemaStore);
			
			attributes.add(new Html("Schema: "));
			attributes.add(schema);
		}
		
		if (editable) {
			VerticalPanel right = new VerticalPanel();
			right.add(displayName);
			right.add(panel);
			if (PermissionAttributeOptions.Assessment.equals(options))
				right.add(attributes);
			
			IconButton menuIcon = new IconButton("icon-remove", new SelectionListener<IconButtonEvent>() {
				public void componentSelected(IconButtonEvent ce) {
					WindowUtils.confirmAlert("Confirm", "Are you sure you want to remove this permission?", new WindowUtils.SimpleMessageBoxListener() {
						public void onYes() {
							removePermission();
						}
					});
				}
			});
			
			HorizontalPanel container = new HorizontalPanel();
			container.setVerticalAlign(VerticalAlignment.TOP);
			container.add(menuIcon);
			container.add(right);
			
			add(container);
		}
		else
			add(panel);
		
	}
	
	private void removePermission() {
		removeFromParent();
	}
	
	private CheckBox getBox() {
		CheckBox l = new CheckBox();
		return l;
	}
	
	public void disableForFeatures() {
		disableBoxes("rwcdg");
	}
	
	public void disableForResource() {
		disableBoxes("u");
	}
	
	public void disableBoxes(String keyCodes) {
		for (char c : keyCodes.toCharArray()) {
			switch (c) {
			case 'r': read.setEnabled(false); break;
			case 'w': write.setEnabled(false); break;
			case 'c': create.setEnabled(false); break;
			case 'd': delete.setEnabled(false); break;
			case 'g': grant.setEnabled(false); break;
			case 'u': use.setEnabled(false);
			}
		}
	}
	
	public void setValue(Permission permission) {
		this.current = new EditablePermission(permission);
		
		if (editable)
			displayName.setHtml(current.getUrl() + ": ");
		
		this.url = current.getUrl();
		
		setChecked(read, AuthorizableObject.READ);
		setChecked(write, AuthorizableObject.WRITE);
		setChecked(create, AuthorizableObject.CREATE);
		setChecked(delete, AuthorizableObject.DELETE);
		setChecked(grant, AuthorizableObject.GRANT);
		setChecked(use, AuthorizableObject.USE_FEATURE);
		
		setItemId(url);
		
		for (PermissionResourceAttribute attribute : current.getAttributes()) {
			if ("region".equals(attribute.getName()) && region != null) {
				region.setValue(region.getStore().findModel(attribute.getRegex()));
				region.setData("id", attribute.getId());
			}
			if ("schema".equals(attribute.getName()) && schema != null) {
				schema.setValue(schema.getStore().findModel(attribute.getRegex()));
				schema.setData("id", attribute.getId());
			}
		}
		
		if (url.startsWith("feature")) {
			type = "feature";
			disableForFeatures();
		}
		else if (url.startsWith("resource")) {
			type = "resource";
			//TODO: this can be more fine-tuned to particular resources
			disableForResource();
		}
	}
	
	private void setChecked(CheckBox box, String operation) {
		box.setValue(current.check(operation));
	}
	
	public void stageChanges() {
		current.sink(new Permission(), current);
		
		if (region != null && region.getValue() != null) {
			PermissionResourceAttribute attr = new PermissionResourceAttribute(region.getName(), region.getValue().getValue(), current);
			attr.setId((Integer)region.getData("id"));
			current.getAttributes().add(attr);
		}
		
		if (schema != null && schema.getValue() != null) {
			PermissionResourceAttribute attr = new PermissionResourceAttribute(schema.getName(), schema.getValue().getValue(), current);
			attr.setId((Integer)schema.getData("id"));
			current.getAttributes().add(attr);
		}
		
		current.setCreate(create.getValue());
		current.setDelete(delete.getValue());
		current.setGrant(grant.getValue());
		current.setRead(read.getValue());
		current.setUrl(url);
		current.setType(type);
		current.setUse(use.getValue());
		current.setWrite(write.getValue());
	}
	
	public Permission getValue() {
		return current;
	}
	
	
	
	public static class PermissionResource {
		private String url;
		private PermissionAttributeOptions option;
		public PermissionResource(String url, PermissionAttributeOptions option) {
			this.url = url;
			this.option = option;
		}
		public PermissionAttributeOptions getOption() {
			return option;
		}
		public String getUrl() {
			return url;
		}
	}
	
	private static class TextValueModelData extends BaseModelData {
		private static final long serialVersionUID = 1L;
		
		public TextValueModelData(String text, String value) {
			super();
			set("text", text);
			set("value", value);
		}
		
		public String getValue() {
			return get("value");
		}
	}
	
	@SuppressWarnings("unused")
	private static class EditablePermission extends Permission {

		private static final long serialVersionUID = 1L;
		
		private final Permission permission;
		
		public EditablePermission(Permission permission) {
			this.permission = permission;
			if (permission.getId() != 0)
				setId(permission.getId());
			init();
		}
		
		public Permission getModel() {
			return permission;
		}
		
		public void rejectChanges() {
			init();
		}
		
		public void acceptChanges() {
			sink(this, permission);
		}
		
		private void init() {
			sink(permission, this);
		}
		
		public void sink(Permission source, Permission target) {
			target.setAttributes(source.getAttributes());
			target.setCreate(source.isCreate());
			target.setDelete(source.isDelete());
			target.setGrant(source.isGrant());
			target.setRead(source.isRead());
			target.setType(source.getType());
			target.setUrl(source.getUrl());
			target.setUse(source.isUse());
			target.setWrite(source.isWrite());
		}	
	}	

}
