package org.iucn.sis.client.api.caches;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.client.api.container.SISClientBase;
import org.iucn.sis.client.api.utils.UriBase;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.PublicationData;
import org.iucn.sis.shared.api.models.PublicationTarget;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.solertium.lwxml.shared.GWTConflictException;
import com.solertium.lwxml.shared.GWTResponseException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.events.ComplexListener;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.portable.XMLWritingUtils;

public class PublicationCache {
	
	public static final PublicationCache impl = new PublicationCache();

	private final Map<Integer, PublicationTarget> targets;
	private final Map<Integer, PublicationData> data;
	
	private PublicationCache() {
		data = new HashMap<Integer, PublicationData>();
		targets = new HashMap<Integer, PublicationTarget>();
	}
	
	public void listTargets(final ComplexListener<List<PublicationTarget>> callback) {
		if (targets.isEmpty()) {
			final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
			document.get(UriBase.getInstance().getSISBase() + "/publication/targets", new GenericCallback<String>() {
				public void onSuccess(String result) {
					final NativeNodeList nodes = document.getDocumentElement().
						getElementsByTagName(PublicationTarget.ROOT_TAG);
					for (int i = 0; i < nodes.getLength(); i++)
						cacheTarget(PublicationTarget.fromXML(nodes.elementAt(i)));
					
					callback.handleEvent(listTargetsFromCache());
				}
				public void onFailure(Throwable caught) {
					callback.handleEvent(new ArrayList<PublicationTarget>());
				}
			});
		}
		else
			callback.handleEvent(listTargetsFromCache());
	}
	
	public List<PublicationTarget> listTargetsFromCache() {
		List<PublicationTarget> list = new ArrayList<PublicationTarget>(targets.values());
		Collections.sort(list);
		
		return list;
	}
	
	public List<PublicationData> listDataFromCache() {
		return new ArrayList<PublicationData>(data.values());
	}
	
	public void listData(final ComplexListener<List<PublicationData>> callback) {
		if (data.isEmpty()) {
			final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
			document.get(UriBase.getInstance().getSISBase() + "/publication/data", new GenericCallback<String>() {
				public void onSuccess(String result) {
					final NativeNodeList nodes = document.getDocumentElement().
						getElementsByTagName(PublicationData.ROOT_TAG);
					for (int i = 0; i < nodes.getLength(); i++)
						cacheData(PublicationData.fromXML(nodes.elementAt(i)));
					
					callback.handleEvent(listDataFromCache());
				}
				public void onFailure(Throwable caught) {
					callback.handleEvent(listDataFromCache());
				}
			});
		}
		else
			callback.handleEvent(listDataFromCache());
	}
	
	public void updateData(final String status, final Integer targetGoal, final Integer targetApproved, 
			final List<Integer> ids, final GenericCallback<Object> callback) {
		final StringBuilder out = new StringBuilder();
		out.append("<root>");
		out.append(XMLWritingUtils.writeCDATATag("status", status, true));
		out.append(XMLWritingUtils.writeTag("goal", targetGoal == null ? null : targetGoal.toString(), true));
		out.append(XMLWritingUtils.writeTag("approved", targetApproved == null ? null : targetApproved.toString(), true));
		for (Integer id : ids)
			out.append(XMLWritingUtils.writeTag("data", id.toString()));
		out.append("</root>");
		
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.post(UriBase.getInstance().getSISBase() + "/publication/data/update", out.toString(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				for (Integer id : ids) {
					PublicationData model = data.get(id);
					if (model != null) {
						if (status != null) {
							model.getAssessment().setType(status);
							Assessment cached = AssessmentCache.impl.getAssessment(model.getAssessment().getId());
							if (cached != null)
								cached.setType(status);
							AssessmentCache.impl.evictTaxonToAssessment(model.getAssessment().getTaxon().getId());
						}
						if (targetGoal != null)
							model.setTargetGoal(targets.get(targetGoal));
						if (targetApproved != null)
							model.setTargetApproved(targets.get(targetApproved));
					}
					
					if (model.getAssessment().isDraft())
						data.remove(id);
				}
				
				callback.onSuccess(null);
			}
			public void onFailure(Throwable caught) {
				/*WindowUtils.errorAlert("Could not make changes, please try again later: <br/>" + 
						ClientDocumentUtils.parseStatus(document));*/
				onSuccess(null);
			}
		});
	}
	
	public void createTarget(final PublicationTarget target, final GenericCallback<Object> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.put(UriBase.getInstance().getSISBase() + "/publication/targets", target.toXML(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				PublicationTarget source = PublicationTarget.fromXML(document.getDocumentElement());
				target.setId(source.getId());
				
				cacheTarget(target);
				
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void updateTarget(final PublicationTarget target, final GenericCallback<Object> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.post(UriBase.getInstance().getSISBase() + "/publication/targets/" + target.getId(), target.toXML(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void deleteTarget(final PublicationTarget target, final GenericCallback<Object> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.delete(UriBase.getInstance().getSISBase() + "/publication/targets/" + target.getId(), new GenericCallback<String>() {
			public void onSuccess(String result) {
				targets.remove(target.getId());
				
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				if (caught instanceof GWTConflictException)
					WindowUtils.errorAlert("Could not delete as this target is still in use.");
				else
					WindowUtils.errorAlert("Could not delete, please try again later.");
				
				callback.onFailure(caught);
			}
		});
	}
	
	public void submit(final Assessment assessment, final GenericCallback<Object> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.put(UriBase.getInstance().getSISBase() + "/publication/submit/assessment/" + assessment.getId(), "<xml/>", new GenericCallback<String>() {
			public void onSuccess(String result) {
				PublicationData data = PublicationData.fromXML(document.getDocumentElement());
				cacheData(data);
				
				assessment.setType(AssessmentType.SUBMITTED_ASSESSMENT_TYPE);
				assessment.setPublicationData(data);
				AssessmentCache.impl.evictTaxonToAssessment(assessment.getTaxon().getId());
				
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				if (!(caught instanceof GWTResponseException))
					WindowUtils.errorAlert("Failed to submit assessment, please try again later.");
				else {
					int status = ((GWTResponseException)caught).getCode();
					if (status >= 500)
						WindowUtils.errorAlert("Failed to submit assessment, please try again later.");
					else if (status == 423)
						WindowUtils.errorAlert("Only draft assessments can be submitted.");
					else {
						//TODO: process exception / xml
						WindowUtils.errorAlert("Failed to submit assessment due to user error, please try again later.");
					}
				}
				
				callback.onFailure(caught);
			}
		});
	}
	
	public void submit(final WorkingSet workingSet, final GenericCallback<Object> callback) {
		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
		document.put(UriBase.getInstance().getSISBase() + "/publication/submit/workingSet/" + workingSet.getId(), "<xml/>", new GenericCallback<String>() {
			public void onSuccess(String result) {
				callback.onSuccess(result);
			}
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	private void cacheTarget(PublicationTarget target) {
		targets.put(target.getId(), target);
	}
	
	private void cacheData(PublicationData data) {
		this.data.put(data.getId(), data);
	}
	
}
