package org.iucn.sis.server.simple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingException;

import org.iucn.sis.server.H2DBIndexType;
import org.iucn.sis.server.VFSSearchCrawler;
import org.iucn.sis.server.acl.PermissionGroupsRestlet;
import org.iucn.sis.server.baserestlets.AccountRestlet;
import org.iucn.sis.server.baserestlets.AppEngineAuthenticateRestlet;
import org.iucn.sis.server.baserestlets.AuthzRestlet;
import org.iucn.sis.server.baserestlets.DiffResource;
import org.iucn.sis.server.baserestlets.ProfileRestlet;
import org.iucn.sis.server.baserestlets.ServiceRestlet;
import org.iucn.sis.server.batchChange.BatchChangeRestlet;
import org.iucn.sis.server.changes.AsmChangesResource;
import org.iucn.sis.server.crossport.demimport.DEMImportInformation;
import org.iucn.sis.server.crossport.demimport.DEMSubmitResource;
import org.iucn.sis.server.crossport.export.DBMirrorManager;
import org.iucn.sis.server.crossport.export.NewAccessExport;
import org.iucn.sis.server.io.ProfileIO;
import org.iucn.sis.server.ref.ReferenceCrawler;
import org.iucn.sis.server.ref.ReferenceLookupRestlet;
import org.iucn.sis.server.taxa.TaxomaticService;
import org.iucn.sis.server.taxa.TaxonomyDocUtils;
import org.iucn.sis.server.users.resources.TaxonFinderRestlet;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.SISMailer;
import org.iucn.sis.server.utils.logging.WorkingsetLogBuilder;
import org.iucn.sis.server.utils.scripts.DraftRegionalGlobalFlattener;
import org.iucn.sis.server.utils.scripts.FootprintVerifier;
import org.iucn.sis.server.utils.scripts.PermissionGroupTester;
import org.iucn.sis.server.utils.scripts.TaxonModding;
import org.iucn.sis.server.utils.scripts.DraftAssessRLCatAndCritModder.DraftAssessRLCatAndCritModderResource;
import org.iucn.sis.server.utils.scripts.PublishedAssessmentGMAReferenceAdder.PublishedAssessmentGMAReferenceAdderResource;
import org.iucn.sis.server.utils.scripts.PublishedAssessmentNewEvalAssessorFixer.PublishedAssessmentNewEvalAssessorFixerResource;
import org.iucn.sis.server.utils.scripts.PublishedAssessmentRLCatAndCritModder.PublishedAssessmentRLCatAndCritModderResource;
import org.iucn.sis.server.zendesk.ZendeskResource;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.engine.util.Base64;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.mail.Mailer;
import com.solertium.update.ServeUpdatesResource;
import com.solertium.update.UpdateResource;
import com.solertium.util.MD5Hash;
import com.solertium.util.SysDebugger;
import com.solertium.util.restlet.CachingEncodingClapCacheDirectory;
import com.solertium.util.restlet.ClapCacheDirectory;
import com.solertium.util.restlet.CookieUtility;
import com.solertium.util.restlet.StandardServerComponent;
import com.solertium.util.restlet.authentication.AuthnGuard;
import com.solertium.util.restlet.authentication.VFSAuthenticator;
import com.solertium.util.restlet.authentication.Authenticator.AccountNotFoundException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.restlet.VFSProvidingApplication;
import com.solertium.vfs.restlet.VFSResource;
import com.solertium.vfs.restlet.VFSVersionAccessResource;

public class SISContainerApp extends Application implements VFSProvidingApplication {
	private static class SISAuthenticator extends VFSAuthenticator {

		public SISAuthenticator(VFS vfs) {
			super(vfs, new VFSPath("/users/.hamsterfish"), true);
		}

		@Override
		public boolean deleteUser(String login) throws AccountNotFoundException {
			if( !doesAccountExist(login) )
				throw new AccountNotFoundException();
			
			String sha1Login = getSHA1Hash(login);
			
			if( acquireMutex() ) {
				String contents = getFileContents();

				try {
					if( contents.matches( oneOrMore + "(\\|" + sha1Login + ":\\w+,\\w+\\|\\s*)" + oneOrMore ) ) {
						contents = contents.replaceFirst("\\|" + sha1Login + ":\\w+?,\\w+?\\|\\s*", "");

						System.out.println("Removed " + sha1Login + "\n" + contents);
						writeFile(contents);

						loginCache.remove(sha1Login);
						mutex.release();
						return true;
					} else
						return false;
				} catch (StackOverflowError e) {
					if( contents.contains("|" + sha1Login + ":") ) {
						contents = contents.replaceFirst("\\|" + sha1Login + ":\\w+?,\\w+?\\|\\s*", "");

						System.out.println("Removed " + sha1Login + "\n" + contents);
						writeFile(contents);

						loginCache.remove(sha1Login);
						mutex.release();
						return true;
					} else
						return false;
				}
			} else
				return false;
		}
		
		@Override
		public boolean validateAccount(String login, String password) {
			String sha1Login = getSHA1Hash(login);
			String md5Pass = getMD5Hash(password + login);
			String sha1Pass = getSHA1Hash(password + login);

			if( sha1Login.equals(sha1Pass) )
				return false;
			else if( loginCache.containsKey( sha1Login ) )
				return loginCache.get(sha1Login).equalsIgnoreCase(md5Pass + "," + sha1Pass);
			else if( acquireMutex() ) {
				String contents = getFileContents();
				System.out.println("Looking for " + oneOrMore 
						+ "(\\|" + sha1Login + ":\\w+,\\w+\\|\\s*)" + oneOrMore );
					
				try {
				if( contents.matches( oneOrMore +
						"\\|" + sha1Login + ":" + md5Pass + "," + sha1Pass + "\\|\\s*" + oneOrMore ) )
				{
					loginCache.put(sha1Login, md5Pass + "," + sha1Pass);
					
					mutex.release();
					return true;
				}
				} catch (StackOverflowError e) {
					System.out.println("Wow, stack overflow error. Trying another way...");
				
					if( contents.contains("|" + sha1Login + ":" + md5Pass + "," + sha1Pass + "|") ) {
						loginCache.put(sha1Login, md5Pass + "," + sha1Pass);
						
						mutex.release();
						return true;
					}
				}
				mutex.release();
			}
			
			return false;
		}
		
		@Override
		public boolean resetPassword(String username) {
			if (!username.equalsIgnoreCase("admin") || username.equals("")) {
				String newPassword = new MD5Hash(username + new Date().toString()).toString().substring(0, 8);
				if (changePassword(username, newPassword)) {
					System.out.println("Changed password to " + newPassword);
					
					String body = "Hello " + username + ", \r\n \r\n Your password has been "
					+ "reset for your IUCN application.  Please log in using the new credentials:\r\n"
					+ "  Username: " + username + "\r\n  Password: " + newPassword
					+ "\r\n \r\n We strongly recommend that you change your password on the " +
							"login page.";
					Mailer mailer = SISMailer.getGMailer();
					mailer.setTo(username);
					mailer.setBody(body);

					try {
						mailer.background_send();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					return true;
				} else {
					System.out.println("Error changing password for user " + username);
					return false;
				}
			} else {
				System.out.println("Username requested reset is either admin or empty, so it failed: " + username);
				return false;
			}
		}

		@Override
		public String emailConfirmationCode(String email) {
			String confirmationCode = getSHA1Hash(Long.toString(new Date().getTime()) + "some_more_salt");
			System.out.println("Your confirmation code is " + confirmationCode);
			String body = "Your confirmation code for the Species Information " + "System is " + confirmationCode
			+ "\r\n\r\n" + "Please visit http://iucnsis.org/authn/confirm/" + confirmationCode
			+ " to activate your account.";

			String subject = "Species Information System Account Signup Confirmation";
			
			Mailer mailer = SISMailer.getGMailer();
			mailer.setTo(email);
			mailer.setSubject(subject);
			mailer.setBody(body);

			try {
				mailer.background_send();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return confirmationCode;
		}
	}

	public static boolean amIOnline() {
		return amIOnline;
	}

	public static void createDebugger() {
		SysDebugger.getInstance().setLogLevel(SysDebugger.INFO);
		SysDebugger.getInstance().setSystemDebugLevel(SIS_LOG_LEVEL);

		final SysDebugger prepend = new SysDebugger();
		prepend.setPrepend("## ");

		final SysDebugger force = new SysDebugger();
		force.setForcePrint(true);

		SysDebugger.getInstance().addNamedInstance("##", prepend);
		SysDebugger.getInstance().addNamedInstance("force", force);
		SysDebugger.getInstance().addNamedInstance("fine", new SysDebugger(SysDebugger.FINE));
		SysDebugger.getInstance().addNamedInstance("status", new SysDebugger(SysDebugger.CONFIG));
		SysDebugger.getInstance().addNamedInstance(SEVERE_LOG, new SysDebugger(SysDebugger.SEVERE));
		SysDebugger.getInstance().addNamedInstance("info", new SysDebugger(SysDebugger.SEVERE + 1));
	}

	/**
	 * Temporary hack to get at the VFS. Undo this, and make it not static any
	 * more as above. The preferred thing would be to initialize the VFS in the
	 * Component and share it between applications via their Context.
	 * 
	 * @deprecated
	 */
	@Deprecated
	public static VFS getStaticVFS() {
		return vfs;
	}

	public static NativeDocument newNativeDocument(ChallengeResponse challengeResponse) {
		NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
		String base64Encoded = "";

		if (challengeResponse != null)
			base64Encoded = challengeResponse.getIdentifier() + ":" + new String(challengeResponse.getSecret());
		else
			base64Encoded = "riapInternal:secretInternalCall";

		ndoc.setHeader("Authorization", "Basic " + Base64.encode(base64Encoded.getBytes(), false));

		return ndoc;
	}
	
	protected final String vfsroot;
	protected final Router router;
	protected final ArrayList<ServiceRestlet> services;
	protected AuthzRestlet authz;
	protected AuthnGuard authn;

	protected ProfileRestlet profile;

	protected AccountRestlet account;

	protected final VFSSearchCrawler crawler;

	protected ReferenceCrawler referenceCrawler = null;

	private final String GOOGLE_ACCOUNT_AUTHENTICATION_URL =
		// "http://localhost:9998";
		"http://gogobackbone.solertium.com";

	public static boolean amIOnline = true;
	
	public static boolean commitLogging = true;

	// TODO: Making this static prevents two SISContainerApps from coexisting.
	// Undo this please.
	private static VFS vfs;

	private Map<String, String> tickets;

	public static final int SIS_LOG_LEVEL = SysDebugger.OFF;

	public static String SEVERE_LOG = "SEVERE_LOG";

	/**
	 * @param context
	 * @param vfsroot
	 * @throws DBException
	 *             if critical database initialization fails
	 */
	public SISContainerApp(Context context, String vfsroot) throws DBException {
		super(context);
		
		System.out.println("Hosted mode? " + isHostedMode());
		
		try {
			Class clazz = Class.forName("com.solertium.util.VerifyOnline");
			java.lang.reflect.Method online = clazz.getMethod("amIOnline", (Class[]) null);
			amIOnline = ((Boolean) online.invoke((Object) null, (Object[]) null)).booleanValue();
		} catch (Exception e) {
			amIOnline = "true".equals(System.getProperty("HOSTED_MODE"));
		}
		
		createDebugger();

		this.router = new Router(getContext());
		this.vfsroot = vfsroot;
		
		try {
			vfs = VFSFactory.getVFS(new File(vfsroot));
		} catch (NotFoundException e) {
			throw new RuntimeException(e);
		}

		tickets = new HashMap<String, String>();

		services = new ArrayList<ServiceRestlet>();
		services.add(new StatusRestlet(vfsroot, getContext()));
		services.add(new BrowseAssessmentsRestlet(vfsroot, getContext()));
		services.add(new BrowseTaxonomyRestlet(vfsroot, getContext()));
		services.add(new RecentAssessmentsRestlet(vfsroot, getContext()));
		services.add(new CommentRestlet(vfsroot, getContext()));
		services.add(new NotesRestlet(vfsroot, getContext()));
		services.add(new CitationsRestlet(vfsroot, getContext()));
		services.add(new FieldRestlet(vfsroot, getContext()));
		services.add(new InboxRestlet(vfsroot, getContext()));
		services.add(new WorkingSetRestlet(vfsroot, getContext()));
		services.add(new SpatialInformationRestlet(vfsroot, getContext()));
		services.add(new AssessmentReviewRestlet(vfsroot, getContext()));
		services.add(new AssessmentRestlet(vfsroot, getContext()));
		services.add(new TaxomaticService(vfsroot, getContext()));
		services.add(new ReportRestlet(vfsroot, getContext()));
		services.add(new WorkingSetExportImportRestlet(vfsroot, getContext()));
		services.add(new ImageRestlet(vfsroot, getContext()));
		services.add(new MarkedRestlet(vfsroot, getContext()));
		services.add(new DefinitionsRestlet(vfsroot, getContext()));
		services.add(new FileSearcherRestlet(vfsroot, getContext()));
		services.add(new WorkingsetLogBuilder(vfsroot, getContext()));
		services.add(new TrashRestlet(vfsroot, getContext()));
		services.add(new BatchChangeRestlet(vfsroot, getContext()));
		services.add(new RegionRestlet(vfsroot, getContext()));
		services.add(new TaxonFinderRestlet(vfsroot, getContext()));
		services.add(new ReferenceLookupRestlet(vfsroot, getContext()));
		services.add(new PermissionGroupsRestlet(vfsroot, getContext()));
		services.add(new FileAttachmentRestlet(vfsroot, getContext()));
		services.add(new LockManagementRestlet(vfsroot, getContext()));
		services.add(new RedlistRestlet(vfsroot, getContext()));
		services.add(new ZendeskResource(getContext()));
		if( !amIOnline )
			services.add(new OfflineRestlet(vfsroot, getContext()));
		
		H2DBIndexType index = new H2DBIndexType();
		index.setVFS(vfs);

		crawler = new VFSSearchCrawler(vfsroot, index);
		crawler.setPath("/browse/nodes/");

		services.add(new SearchRestlet(crawler, vfsroot, getContext()));

		TaxonomyDocUtils.init(vfsroot);
		DEMImportInformation.init(vfs);

		if (!StandardServerComponent.getInitProperties().containsKey("UPDATE_URL"))
			StandardServerComponent.getInitProperties().put("UPDATE_URL", "http://sis.iucnsis.org/getUpdates");

		// RegionCache.impl.fetchRegions(NativeDocumentFactory.newNativeDocument());

		//
		// try {
		// TaxonomyDocUtils.buildFromScratch(30);
		// }
		// catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public void addAdditionalServices() {
		if (services == null)
			return;

		for (Iterator<ServiceRestlet> iter = services.iterator(); iter.hasNext();)
			addServiceToRouter(iter.next());
	}

	public void addCookie(Request request, Response response, String identifier) {
		tickets.put(CookieUtility.associateHTTPBrowserID(request, response), identifier);
	}

	private void addServiceToRouter(ServiceRestlet curService) {
		for (Iterator<String> pathIter = curService.getPaths().iterator(); pathIter.hasNext();) {
			String path = pathIter.next();
			router.attach(path, curService);
		}
	}

	@Override
	public Restlet createRoot() {
		Router root = new Router(getContext());
		Filter httpsFilter = new Filter(getContext(), root) {
			@Override
			protected int beforeHandle(Request request, Response response) {
				if( !isHostedMode() && amIOnline() && request.getProtocol().equals(Protocol.HTTP) ) {
					Reference newRef = new Reference(request.getResourceRef());
					newRef.setHostPort(Integer.valueOf(443));
					newRef.setProtocol(Protocol.HTTPS);
					response.redirectPermanent(newRef);
					return STOP;
				} else
					return CONTINUE;
			}
		};
		
//		root.attach("/SIS", new CachingEncodingClapCacheDirectory(getContext(),
//				"clap://thread/org.iucn.sis.SIS",
//				Arrays.asList(new String[] { "html", "js", "css" }), Encoding.GZIP));
		
		root.attach("/js/forkeditor", new ClapCacheDirectory(getContext(),
		"clap://thread/com/solertium/forkeditor"));

		if( !isHostedMode() )
			router.attach("/", new Restlet() {
				@Override
				public void handle(Request request, Response response) {
					if( !isHostedMode() )
						response.redirectPermanent("/SIS/index.html");
				}
			});
		
		router.attach("/favicon.ico", new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				try {
					response.setEntity(new InputRepresentation(vfs.getInputStream(new VFSPath("/images/favicon.ico")),
							MediaType.IMAGE_ICON));
					response.setStatus(Status.SUCCESS_OK);
				} catch (NotFoundException e) {
					response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				}

			}
		});
		router.attach("/footprintVerifier/doit", new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				// FIX INFRARANK NAMES
				try {
					FootprintVerifier.verifyFootprints(vfs);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		router.attach("/findDupeTaxa", new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				// FIX INFRARANK NAMES
				try {
					TaxonModding.findDupeNamedTaxa();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		router.attach("/flattenDrafts", new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				try {
					crawler.unbindFromVFS();
					DraftRegionalGlobalFlattener.flattenDrafts(vfs);
					crawler.bindToVFS();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		router.attach("/flattenPublished", new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				try {
					DraftRegionalGlobalFlattener.flattenAllAssessments(vfs);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		router.attach("/reindexPostgres", new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				// FIX INFRARANK NAMES
				try {
					DBMirrorManager.impl.runFullExport();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		router.attach("/demimport/submit/{user}", DEMSubmitResource.class);
		
		
		initBaseRestlets();

		router.attach("/authn", authn);

		addServiceToRouter(authz);
		addServiceToRouter(profile);
		addServiceToRouter(account);
		addAdditionalServices();

		root.attach(authn);
		
		root.attach("/fixRLCatDrafts", DraftAssessRLCatAndCritModderResource.class);
		root.attach("/fixRLCatPubs/{writeback}", PublishedAssessmentRLCatAndCritModderResource.class);
		
		root.attach("/fixGMARefs", PublishedAssessmentGMAReferenceAdderResource.class);
		root.attach("/testExport", NewAccessExport.class);
		root.attach("/fixEvalAssessors",
				PublishedAssessmentNewEvalAssessorFixerResource.class);
		// root.attach("/repairPublishedRTAs",
		// PublishedAssessmentRTAFormatterResource.class);
		// root.attach("/repairDraftRTAs",
		// DraftAssessRTAFormatterResource.class);

		// TODO: move these to the correct place
		root.attach("/raw", VFSResource.class);
		root.attach("/revisions", VFSVersionAccessResource.class);
		root.attach("/diff", DiffResource.class);
		root.attach("/comment", OfflineCommentResource.class);
		root.attach("/googleLogin", new AppEngineAuthenticateRestlet(getContext(),
				GOOGLE_ACCOUNT_AUTHENTICATION_URL));
		root.attach("/asmchanges/{asm_id}", AsmChangesResource.class);
		

		try {
			if (amIOnline()) {
				root.attach("/getUpdates", ServeUpdatesResource.class);
				root.attach("/getUpdates/summary", ServeUpdatesResource.class);

				root.attach("/update", new Restlet(getContext()) {
					@Override
					public void handle(Request request, Response response) {
						response.setStatus(Status.SUCCESS_OK);
						response.setEntity("<html><body>The online environment does not "
								+ "support automatic updates.", MediaType.TEXT_HTML);
					}
				});
			} else
				throw new Exception("I'm not online.");
		} catch (Exception e) {
			root.attach("/update", UpdateResource.class);
			root.attach("/update/summary", UpdateResource.class);

			root.attach("/getUpdates", new Restlet(getContext()) {
				@Override
				public void handle(Request request, Response response) {
					response.setStatus(Status.SUCCESS_OK);
					response.setEntity("<html><body>Serving updates can only be done in " + "the online environment.",
							MediaType.TEXT_HTML);
				}
			});
		}

		root.attach("/restart", RestartResource.class);

		root.attach("/noindex", new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				crawler.unbindFromVFS();
				response.setEntity(new StringRepresentation(crawler.getStatus(), MediaType.TEXT_PLAIN));
			}
		});

		root.attach("/nocommit", new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				commitLogging = false;
				response.setEntity(new StringRepresentation("Commit logs disabled."));
			}
		});

		root.attach("/refindex", new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				if (referenceCrawler == null) {
					try {
						referenceCrawler = new ReferenceCrawler(vfs);
					} catch (NamingException naming) {
						response.setStatus(Status.SERVER_ERROR_INTERNAL);
					}
				}

				new Thread(referenceCrawler).start();
				response.setEntity(new StringRepresentation(referenceCrawler.getStatus(), MediaType.TEXT_PLAIN));
			}
		});

		root.attach("/reindex", new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				crawler.bindToVFS();
				new Thread(crawler).start();
				response.setEntity(new StringRepresentation(crawler.getStatus(), MediaType.TEXT_PLAIN));
			}
		});

		root.attach("/reindexall", new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				try {
					ExecutionContext ec = new SystemExecutionContext("default");
					ec.setExecutionLevel(ExecutionContext.ADMIN);
					ec.dropTable("commonNames");
					ec.dropTable("taxonKeys");

					Document structDoc = DocumentUtils.newDocumentBuilder().parse(
							H2DBIndexType.class.getResourceAsStream("searchstruct.xml"));
					ec.createStructure(structDoc);
				} catch (Exception e) {
				}

				new Thread(crawler).start();
				response.setEntity(new StringRepresentation(crawler.getStatus(), MediaType.TEXT_PLAIN));
			}
		});

		Filter compressingFilter = new Filter(getContext(), router) {
			@Override
			protected void afterHandle(Request request, Response response) {
				try {
					if( amIOnline() ) {
						if( response.getStatus().isSuccess() )
							if( response.getEntity() != null && isEncodeableEntity(response.getEntity()) )
								response.setEntity( new EncodeRepresentation( Encoding.GZIP, response.getEntity()) );
					}
				} catch (Exception e) {
					//Not online!
				}
			}
		};
		authn.setNext(compressingFilter);
		
		return httpsFilter;
	}
	
	private boolean isEncodeableEntity(Representation entity) {
		return entity.getMediaType().equals(MediaType.TEXT_XML) && entity.getSize() > 2048;
	}

	public String getCookieUser(Request request) {
		return tickets.get(CookieUtility.associateHTTPBrowserID(request, null));
	}

	public VFS getVFS() {
		return vfs;
	}

	public void initBaseRestlets() {
		authn = new AuthnGuard(getContext(), ChallengeScheme.HTTP_BASIC, "User Service") {
			@Override
			protected boolean bypassAuth(Request request) {
				String path = request.getResourceRef().getPath();
				if (path.equals("/") || path.equalsIgnoreCase("/favicon.ico"))
					return true;
				else if (request.getProtocol() == Protocol.RIAP)
					return true;
				else if (path.startsWith("/reports"))
					return true;
				else if (path.startsWith("/workingSetImporter"))
					return true;

				return false;
			}

			@Override
			protected void setDefaultAuthenticator() {
				authenticators.put(getRealm(), new SISAuthenticator(vfs));
			}
			
			@Override
			protected void doRemoveUser(final Request request, final Response response, final String domain) {
				String currentUser = null;
				if (!bypassAuth(request)) {
					currentUser = request.getChallengeResponse().getIdentifier();
				}
				
				String userToDelete = null;
				try {
					userToDelete = new DomRepresentation(request.getEntity()).getDocument().getDocumentElement().getElementsByTagName("u").item(0).getTextContent();
				} catch (DOMException e1) {
					e1.printStackTrace();
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				} catch (IOException e1) {
					e1.printStackTrace();
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				}
				
				if (userToDelete == null || (currentUser == null && !bypassAuth(request))) {
					response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				} else if (currentUser != null && currentUser.equals(userToDelete)) {
					response.setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED, "You can not delete yourself.");
				} else {
					try {
						if( getAuthenticator(domain).deleteUser(userToDelete) ) {
							try {
								ProfileIO.deleteProfile(vfs, userToDelete);
								response.setStatus(Status.SUCCESS_OK);
							} catch (NotFoundException e) {
								response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
							} catch (Exception e) {
								response.setStatus(Status.SERVER_ERROR_INTERNAL);
							}
//							Request delete = new Request(Method.DELETE, "riap://host/profile/" + userToDelete);
//							Response res = new Response(delete);
//							getContext().getClientDispatcher().handle(delete, res);
//							
//							if( res.getStatus().isSuccess() )
//								response.setStatus(Status.SUCCESS_OK);
//							else {
//								System.out.println("ProfileRestlet RIAP failed.");
//								response.setStatus(Status.SERVER_ERROR_INTERNAL);
//							}
						}
					} catch (AccountNotFoundException e) {
						System.out.println("Account not found. Delete halted.");
						response.setStatus(Status.SERVER_ERROR_INTERNAL);
					}
				}
			}

		};


		authz = new AuthzRestlet(vfsroot, getContext());
		profile = new ProfileRestlet(vfsroot, getContext());
		account = new AccountRestlet(vfsroot, getContext());
	}

	public static boolean isHostedMode() {
		return StandardServerComponent.getInitProperties().containsKey("HOSTED_MODE");
	}

}
