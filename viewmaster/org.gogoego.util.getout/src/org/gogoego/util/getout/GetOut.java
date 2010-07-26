package org.gogoego.util.getout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GetOut is a flyweight logging mechanism that is easy to use and trivial to
 * configure. Its core behavior is very predictable. It can be used as a simple
 * message bus, and extended with either local or remote listeners.
 * <p>
 * 
 * 
 * <h3>BASIC USAGE</h3>
 * 
 * Import this class, com.solertium.util.GetOut. Or graft it into your own
 * source tree; it's public domain.
 * <p>
 * 
 * For most intents and purposes, GetOut.log() is the same as saying
 * System.out.println() or throwable.printStackTrace(), but with useful side
 * effects.
 * <p>
 * 
 * <pre>
 * System.out.println("I am making tofu");
 *   becomes
 * GetOut.log("I am making tofu");
 *   
 * exception.printStackTrace();
 *   becomes
 * GetOut.log(exception);
 * </pre>
 * 
 * log() supports varargs. The first String argument is a printf template, and
 * subsequent arguments are used to satisfy it. If your printf template is
 * invalid, GetOut will try to print something useful anyway.
 * <p>
 * 
 * <pre>
 * System.out.println("My name is " + name)
 *   becomes
 * GetOut.log("My name is %s", name)
 * </pre>
 * 
 * Using templates and varargs is preferable to concatenating log strings,
 * because if GetOut is in production mode (see below) the concatenation
 * operation will be skipped.
 * <p>
 * 
 * You can use varargs when logging exceptions as well. The Throwable object
 * (Exception, etc.) always comes first in the call:
 * <p>
 * 
 * <pre>
 * GetOut.log(exception, &quot;The %s broke&quot;, thingThatBroke)
 * </pre>
 * 
 * 
 * <h3>CONFIGURATION</h3>
 * 
 * GetOut has minimal configuration settings -- sufficiently minimal that they
 * all can be set in a single system property, <b>GetOut</b>. If you don't set
 * this property, GetOut behaves exactly like System.out.println() and
 * Throwable.printStackTrace().
 * <p>
 * 
 * <pre>
 * production
 * </pre>
 * 
 * In production mode, messages are only written when there is an Exception
 * attached (to System.err) or when the message is wrapped by alwaysPrint().
 * <p>
 * 
 * <pre>
 * verbose
 * </pre>
 * 
 * This enables a verbose two-line form of the log. This form identifies the
 * caller, calling thread, and message sequence number. Stack traces are
 * abbreviated. It is probably more useful for a new application, but will be
 * noisy if converting an application already producing lots of System.out
 * output.
 * <p>
 * 
 * <pre>
 * server:[port]
 * </pre>
 * 
 * The selected port will be exposed for the network server.
 * 
 * Examples:
 * <p>
 * 
 * <pre>
 * GetOut=verbose,production
 * GetOut=verbose,server:22222
 * GetOut=server:10.0.0.1:22222
 * </pre>
 * 
 * <h3>USING THE SERVER</h3>
 * 
 * If the server is enabled (see CONFIGURATION below), you can telnet to the
 * designated port, and send a configuration line to tell GetOut what you are
 * interested in. Just hit enter if you want to receive all messages in simple
 * form.
 * <p>
 * 
 * The filtering format is similar to the GetOut.config setting, with some extra
 * options:
 * <p>
 * 
 * <pre>
 * [expr,expr...]\n
 * expr = (
 *      key:context-key=regex
 *   || class=regex
 *   || thread=regex
 *   || "production"
     || ("verbose"||"binary")
 * )
 * </pre>
 * 
 * Examples:
 * <p>
 * 
 * <pre>
 * verbose,production\n
 * key:site=mysite\n
 * class=com.solertium.*,key:site=mysite,format=binary\n
 * </pre>
 * 
 * You will receive a feed of the associated events in the selected format until
 * you or the server disconnects. The binary format, not available on standard
 * out, is a Java ObjectOutputStream of GetOut.Message objects.
 * <p>
 * 
 * The server feed is driven by a Listener and, as such, is potentially lossy.
 * Check message sequence numbers to determine if loss has occurred.
 * <p>
 * 
 * 
 * <h3>ADVANCED USAGE</h3>
 * 
 * log() returns a MessageFate which tells whether the message was, in fact,
 * emitted, and if so, where to look for it:
 * <p>
 * 
 * <pre>
 * MessageFate fate = log("This message is really trivial.")
 * assertTrue(fate.wasPrinted) // will fail in production
 * </pre>
 * 
 * GetOut keeps ThreadLocal maps to capture contextual information for each
 * thread. This information is part of verbose messages and binary LogRecords.
 * To manage:
 * <p>
 * 
 * <pre>
 * GetOut.set("site","www.solertium.com")
 * GetOut.remove("site")
 * </pre>
 * 
 * The hash for the current thread can be cleared (and all other behavior reset
 * to defaults) with:
 * <p>
 * 
 * <pre>
 * GetOut.reset()
 * </pre>
 * 
 * 
 * <h3>MANAGING DELIVERY</h3>
 * 
 * There are no log levels. This takes some getting used to. Take a while. Let
 * it sink in. Find your center. Breathe.
 * <p>
 * 
 * There are, however, attributes that can be set on log messages when you want
 * them to be handled specially. These attributes reflect specific desired
 * outcomes for the message delivery. To set these by hand, construct a
 * LogRecord yourself and append as many of these attributes as apply:
 * <p>
 * 
 * <b>GetOut.Delivery.ALWAYS_PRINT</b>
 * <p>
 * Regardless of the configuration settings in effect, always print this
 * message. If messages are sent over a lossy channel, try to ensure this one is
 * not lost.
 * <p>
 * 
 * <b>GetOut.Delivery.TELL_SOMEONE</b>
 * <p>
 * Make best efforts to send a notification to an operator when this message is
 * handled. GetOut itself does not send notifications, but will make a blocking
 * call to a notification listener when it sees this attribute.
 * <p>
 * 
 * <b>GetOut.Delivery.RETAIN</b>
 * <p>
 * This message should be retained in a persistent form, such as a file or
 * database. GetOut itself does not manage retention, but will make a blocking
 * call to a retention listener when it sees this attribute.
 * <p>
 * 
 * There are static methods as syntactic sugar for using each of these
 * attributes one at a time. They begin with "should" to help remind you that
 * they don't actually do anything on their own, they just modify the message
 * you wish to log.
 * <p>
 * 
 * <pre>
 * GetOut.log(GetOut.Delivery.shouldTellSomeone(&quot;The CPU is on fire&quot;))
 * </pre>
 * 
 * If you find yourself doing this, for more expressiveness (sorry, it's still
 * Java) you can "import static" the methods you plan to use:
 * 
 * <pre>
 * log(shouldTellSomeone(&quot;The CPU is on fire&quot;))
 * </pre>
 * 
 * This would be especially valuable if you need to combine delivery attributes:
 * <p>
 * 
 * <pre>
 * log(shouldRetain(shouldTellSomeone(&quot;host %s blocked for excess traffic&quot;,
 * 		badHost)))
 * </pre>
 * 
 * You can also add your own delivery preferences (and provide your own sugar)
 * by creating instances of GetOut.Delivery.Preference or its subclasses. This
 * information is made available to listeners.
 * <p>
 * 
 * 
 * <h3>LOGGING INTEGRATION</h3>
 * 
 * GetOut can act as a java.util.logging Handler. Just register GetOut.handler:
 * <p>
 * 
 * <pre>
 * logger.setHandler(GetOut.handler)
 * </pre>
 * 
 * To capture the root logger for java.util.logging, and disconnect other
 * loggers, use:
 * <p>
 * 
 * <pre>
 * GetOut.captureRoot()
 * </pre>
 * 
 * 
 * <h3>LISTENERS</h3>
 * 
 * Retention of messages, notification of messages, and any behaviors of
 * associated delivery classes do not happen in GetOut directly. Implement a
 * GetOut.Listener and register it:
 * <p>
 * 
 * <pre>
 * GetOut.addListener(myListener, &quot;key:site=mysite&quot;, GetOut.Delivery.RETAIN);
 * </pre>
 * 
 * The second argument here is a filter with the following grammar:
 * <p>
 * 
 * <pre>
 * [expr,expr...]\n
 *   expr = (
 *        key:context-key=regex
 *     || class=regex
 *     || thread=regex
 *   )
 * </pre>
 * 
 * Examples:
 * <p>
 * 
 * <pre>
 * key:site=mysite\n
 * class=com.solertium.*,key:site=mysite\n
 * </pre>
 * 
 * Each listener is run in its own thread, and has a ring buffer of messages,
 * whose size is determined in the Listener's constructor. When a matching
 * message arrives, the listener's thread will be interrupted, causing its
 * handle() method to be called. Messages can arrive faster than the listener
 * can process, in which case messages will be lost. A gap in message sequence
 * number reflects if this has happened.
 * <p>
 * 
 * <h3>PERFORMANCE TIPS</h3>
 * 
 * Without any configuration, GetOut will run with equivalent speed to
 * System.out.println and Exception.printStackTrace().  When verbose logging
 * is enabled, especially in development mode, or a listener is filtering by
 * class, the log operation can become a lot more expensive due to the need
 * to capture context and stack for every log message.<p>
 * 
 * The recommended configuration for production is to use:<p>
 * 
 * <pre>
 * -DGetOut=verbose,production,server:[port]
 * </pre>
 * 
 * Connect remotely and filter by context information:
 * 
 * <pre>
 * key:site=mysite
 * </pre>
 * 
 * @author rfc2616
 */
public class GetOut {

	static {
		configure();
	}
	
	private static abstract class BaseFormatter implements Formatter {

		protected OutputStreamWriter eventualErr = new OutputStreamWriter(System.err);
		protected OutputStreamWriter eventualOut = new OutputStreamWriter(System.out);
		protected PrintWriter stackPrinter = new PrintWriter(eventualErr);
		
		private boolean fProduction = false;

		protected boolean isProduction() {
			return fProduction;
		}

		protected void setProduction(boolean p) {
			fProduction = p;
		}

		protected boolean shouldGoOut(Message message) {
			if (!isProduction())
				return true;
			if (message.preferences != null
					&& message.preferences.contains(Delivery.ALWAYS_PRINT))
				return true;
			return false;
		}

	}

	public final static class Delivery {
		public static class AlwaysPrint extends Preference {
		}

		public abstract static class Preference {
		};

		public static class Retain extends Preference {
		};

		public static class TellSomeone extends Preference {
		};

		public static final Preference ALWAYS_PRINT = new AlwaysPrint();
		public static final Preference RETAIN = new Retain();
		public static final Preference TELL_SOMEONE = new TellSomeone();

		public static Message alwaysPrint(Message message) {
			return message.addPreference(ALWAYS_PRINT);
		}

		public static Message alwaysPrint(String s) {
			return new Message(null, s).addPreference(ALWAYS_PRINT);
		}

		public static Message alwaysPrint(String s, Object... args) {
			return new Message(null, s, args).addPreference(ALWAYS_PRINT);
		}

		public static Message alwaysPrint(Throwable t) {
			return new Message(t, null).addPreference(ALWAYS_PRINT);
		}

		public static Message alwaysPrint(Throwable t, String s) {
			return new Message(t, s).addPreference(ALWAYS_PRINT);
		}

		public static Message alwaysPrint(Throwable t, String s, Object... args) {
			return new Message(t, s, args).addPreference(ALWAYS_PRINT);
		}

		public static Message retain(Message message) {
			return message.addPreference(RETAIN);
		}

		public static Message retain(String s) {
			return new Message(null, s).addPreference(RETAIN);
		}

		public static Message retain(String s, Object... args) {
			return new Message(null, s, args).addPreference(RETAIN);
		}

		public static Message retain(Throwable t) {
			return new Message(t, null).addPreference(RETAIN);
		}

		public static Message retain(Throwable t, String s) {
			return new Message(t, s).addPreference(RETAIN);
		}

		public static Message retain(Throwable t, String s, Object... args) {
			return new Message(t, s, args).addPreference(RETAIN);
		}

		public static Message tellSomeone(Message message) {
			return message.addPreference(TELL_SOMEONE);
		}

		public static Message tellSomeone(String s) {
			return new Message(null, s).addPreference(TELL_SOMEONE);
		}

		public static Message tellSomeone(String s, Object... args) {
			return new Message(null, s, args).addPreference(TELL_SOMEONE);
		}

		public static Message tellSomeone(Throwable t) {
			return new Message(t, null).addPreference(TELL_SOMEONE);
		}

		public static Message tellSomeone(Throwable t, String s) {
			return new Message(t, s).addPreference(TELL_SOMEONE);
		}

		public static Message tellSomeone(Throwable t, String s, Object... args) {
			return new Message(t, s, args).addPreference(TELL_SOMEONE);
		}

	}

	public static interface Formatter {
		public MessageFate format(Package pkg) throws IOException;
	}

	public static abstract class Listener implements Runnable {
		private final PackageRingBuffer buffer;
		private final AtomicLong highMessageNumber = new AtomicLong();
		public boolean isProcessing = false;
		private long localMessageCount;
		private final int ringBufferSize;
		boolean stopped = false;
		private WeakReference<Thread> threadRef;
		private String classFilter = null;
		private String contextFilterKey = null;
		private String contextFilterValue = null;

		protected Listener() {
			this(16);
		}
		
		protected void setClassFilter(String classFilter){
			this.classFilter = classFilter;
		}

		protected void setContextFilter(String key, String value){
			this.contextFilterKey = key;
			this.contextFilterValue = value;
		}

		protected Listener(int ringBufferSize) {
			this.ringBufferSize = ringBufferSize;
			buffer = new PackageRingBuffer(ringBufferSize);
		}

		public synchronized long getLocalMessageCount() {
			return localMessageCount;
		}

		/**
		 * Implementers override this method to determine what the
		 * Listener does to handle each package; e.g. write it out
		 * to a file or a network stream.
		 * 
		 * @param pkg A package containing message, context, and stack
		 */
		public abstract void handle(Package pkg);

		/**
		 * Implementers override this method to do any operations
		 * that are necessary to prepare a package when it is
		 * written (blocking in-thread with the caller).  This
		 * method must return as quickly as possible so as not to
		 * impact application liveness.  Messages with attributes
		 * like AlwaysPrint or Retain might be handled here.<p>
		 * 
		 * To enable verbose formatting, you need this at least:<p>
		 * 
		 * <pre>
		 * pkg.getContext();
		 * pkg.getCallSite();
		 * </pre>
		 * 
		 * Otherwise, this information will not be saved in-thread
		 * and will not be available when you want it.  By default,
		 * this is not included in the implementation of receive,
		 * as it causes a significant performance drain.
		 * 
		 * @param pkg
		 */
		public void receive(Package pkg){}

		public final boolean isStopped() {
			return stopped;
		}

		protected synchronized final void postPackage(Package pkg) {
			if(contextFilterKey!=null){
				String match = pkg.getContext().get(contextFilterKey);
				if(match==null) return;
				if(!match.equals(contextFilterValue)) return;
			}
			if(classFilter!=null)
				if(!pkg.getCallSite().startsWith(classFilter)) return;
			receive(pkg);
			localMessageCount++;
			buffer.push(pkg);
			if (threadRef != null) { // um. Maybe not running yet?
				final Thread t = threadRef.get();
				if (t != null)
					t.interrupt();
			}
		}

		private void processQueue() {
			isProcessing = true;
			int count = ringBufferSize;
			while (count > 0) {
				count--;
				final Package pkg = buffer.pop();
				if (pkg == null)
					break;
				highMessageNumber.set(pkg.number);
				handle(pkg);
			}
			isProcessing = false;
		}

		public void run() {
			try {
				threadRef = new WeakReference<Thread>(Thread.currentThread());
				while (!stopped) {
					// sleep for 10 minutes
					try {
						Thread.sleep(600000);
					} catch (final InterruptedException interrupted) {
						// carry on
					}
					processQueue();
				}
			} catch (final Throwable t) {
				GetOut.log(t);
			} finally {
				stopped = true;
			}
		}

		protected final void stop() {
			if(stopped) return;
			stopped = true;
			final Thread thread = threadRef.get();
			if (thread != null)
				thread.interrupt();
		}
	}

	public final static class Message {
		protected Object[] args;
		protected String format;
		protected ArrayList<Delivery.Preference> preferences;
		protected Throwable throwable;

		public Message(Throwable throwable, String format) {
			this(throwable, format, (Object[]) null);
		}

		public Message(Throwable throwable, String format, Object... args) {
			this.throwable = throwable;
			this.format = format;
			this.args = args;
		}

		public Message addPreference(Delivery.Preference preference) {
			if (preferences == null)
				preferences = new ArrayList<Delivery.Preference>();
			if (!preferences.contains(preference))
				preferences.add(preference);
			return this;
		}

		public String getFormattedMessage() {
			if (format == null)
				return "";
			if (args == null || args.length == 0 || args[0] == null)
				return format;
			try {
				return String.format(format, args);
			} catch (final Exception badFormat) {
				return "[?] " + format + " + " + args[0];
			}
		}
	}

	public static interface MessageFate {
		public boolean wasPrinted();
	}

	public static class NetworkDaemon implements Runnable {
		private final int port;

		protected NetworkDaemon(int port) {
			this.port = port;
		}

		public void run() {
			daemonLaunched = true;
			GetOut.log("GetOut network daemon started on port " + port);
			Thread.currentThread().setName("GetOut Network Daemon");
			try {
				final ServerSocket ss = new ServerSocket(port);
				while (true) {
					final Socket socket = ss.accept();
					GetOut.log("GetOut remote connection accepted on port "
							+ port);
					addListener(new NetworkWorker(socket));
				}
			} catch (final IOException iox) {
				GetOut.log(iox, "Problem binding to server socket");
			} finally {
				daemonLaunched = false;
			}
		}
	}

	public static class NetworkWorker extends Listener {
		private BaseFormatter formatter;
		private final String host;
		private OutputStreamWriter out;
		private final Socket socket;
		private boolean verbose = false;

		protected NetworkWorker(Socket socket) {
			super(256);
			this.socket = socket;
			try{
				socket.setSoTimeout(30000);
			} catch (SocketException x) {
				x.printStackTrace();
			}
			host = socket.getRemoteSocketAddress().toString();
			try {
				out = new OutputStreamWriter(socket.getOutputStream());
			} catch (final IOException iox) {
				GetOut.log(iox);
				stop();
			}
		}
		
		public void receive(Package pkg) {
			if(verbose){
				pkg.getCallSite();
				pkg.getContext();
			}
		}

		public void handle(Package pkg) {
			if (formatter != null)
				try{
					formatter.format(pkg);
				} catch (IOException iox) {
					GetOut.log("Network worker IO exception, stopping");
					stop();
				}
		}

		public void run() {
			try {
				final BufferedReader lineReader = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				String config = lineReader.readLine();
				GetOut.log("Configuration request from %s: %s", host, config);
				if (config.contains("verbose")){
					verbose = true;
					formatter = new VerboseFormatter();
				} else
					formatter = new StandardFormatter();
				formatter.eventualOut = out;
				formatter.eventualErr = out;
				formatter.stackPrinter = new PrintWriter(out);
				if (config.contains("production"))
					formatter.setProduction(true);
				config = config+",";
				if(config.contains("key:")){
					Pattern p = Pattern.compile(".*key:(.*),");
					Matcher m = p.matcher(config);
					try{
						if(m.matches()){
							String[] elements = m.group(1).split("=");
							setContextFilter(elements[0],elements[1]);
							GetOut.log("Context filter is %s=%s for %s",elements[0],elements[1],host);
						}
					} catch (Exception x) {
						GetOut.log(x);
					}
				}
				if(config.contains("class:")){
					Pattern p = Pattern.compile(".*class:(.*),");
					Matcher m = p.matcher(config);
					try{
						if(m.matches()){
							setClassFilter(m.group(1));
							GetOut.log("Class filter is %s for %s",m.group(1),host);
						}
					} catch (Exception x) {
						GetOut.log(x);
					}
				}
			} catch (final IOException iox) {
				GetOut.log(iox);
				stop();
				return;
			}
			Thread.currentThread().setName("GetOut Listener " + host);
			super.run();
			GetOut.log("GetOut listener for %s finished running",host);
		}
	}

	public static class Package {
		Message message;
		long number;
		private String thread;
		private String pcallSite;
		private String firstcallSite;
		private Map<String, String> pcontext;
		public String getCallSite(){
			for (final StackTraceElement element : Thread.currentThread().getStackTrace()) {
				final String n = element.getClassName();
				if(firstcallSite == null && n.startsWith("com.solertium.util.GetOut")) firstcallSite = n+":"+element.getLineNumber();
				if (!n.startsWith("com.solertium.util.GetOut")
						&& !n.startsWith("java.")) {
					pcallSite = n+":"+element.getLineNumber();
					break;
				}
			}
			if(pcallSite==null) pcallSite=firstcallSite;
			if(pcallSite==null) pcallSite="unknown";
			return pcallSite;
		}
		public Map<String, String> getContext(){
			if(pcontext!=null) return pcontext;
			pcontext = new TreeMap<String, String>();
			final Map<String, String> context = GetOut.getContext();
			if (context != null)
				for (final Map.Entry<String, String> entry : context.entrySet())
					pcontext.put(entry.getKey(), entry.getValue());
			return context;
		}
	}

	private static class PackageRingBuffer {
		private final Package[] array;
		private final int max;
		int readPtr = 0;
		int writePtr = -1;

		PackageRingBuffer(int size) {
			array = new Package[size];
			max = size - 1;
		}

		public synchronized Package pop() {
			if (readPtr == max + 1)
				readPtr = 0;
			final Package got = array[readPtr];
			if (got != null) {
				array[readPtr] = null;
				readPtr++;
			}
			return got;
		}

		public synchronized void push(Package pkg) {
			if (writePtr == max)
				writePtr = -1;
			writePtr++;
			if (array[writePtr] != null)
				// move the read pointer to track the write
				// pointer until this issue stops
				readPtr = writePtr;
			array[writePtr] = pkg;
		}
	}

	private static class StandardFormatter extends BaseFormatter {

		public MessageFate format(Package pkg) throws IOException {
			final String s = pkg.message.getFormattedMessage();
			if (pkg.message.throwable != null){
				if (!"".equals(s)){
					eventualErr.write(s);
					eventualErr.write("\n");
				}
				pkg.message.throwable.printStackTrace(stackPrinter);
				eventualErr.flush();
			} else if (shouldGoOut(pkg.message)) {
				eventualOut.write(s);
				eventualOut.write("\n");
				eventualOut.flush();
			} else
				return nullFate;
			return fate;
		}

	}

	private static class VerboseFormatter extends BaseFormatter {

		private final AtomicLong number = new AtomicLong();

		public MessageFate format(Package pkg) throws IOException {

			if (pkg.message.throwable == null && !shouldGoOut(pkg.message))
				return nullFate;

			final StringWriter sw = new StringWriter(1024);
			final PrintWriter out = new PrintWriter(sw);
			int line = 0;
			out.printf("#%d [%s] %s:%d\n", number.incrementAndGet(),
					pkg.thread, pkg.getCallSite(), line);
			for (final Map.Entry<String, String> entry : pkg.getContext().entrySet())
				out.printf("  %s: %s\n", entry.getKey(), entry.getValue());
			final String s = pkg.message.getFormattedMessage();
			if (!"".equals(s)) {
				out.print("    ");
				out.println(s);
			}
			sw.append("\n");
			if (pkg.message.throwable != null) {
				recurse(out, 0, pkg.message.throwable);
				eventualErr.write(sw.toString());
			} else
				eventualOut.write(sw.toString());
			eventualOut.flush();
			return fate;
		}

		private void recurse(PrintWriter out, int depth, Throwable t) {
			out.printf("    %s (%s)\n", t.getMessage(), t.getClass());
			if (depth > 4)
				return;
			int i = 4;
			for (final StackTraceElement element : t.getStackTrace()) {
				i--;
				if (i == 0)
					break;
				out.printf("      at %s\n", element.toString());
			}
			final Throwable cause = t.getCause();
			if (cause != null)
				recurse(out, depth + 1, cause);
		}

	}

	private static boolean daemonLaunched = false;

	protected static MessageFate fate = new MessageFate() {
		public boolean wasPrinted() {
			return true;
		}
	};

	public static Handler handler = new Handler() {
		@Override
		public void close() throws SecurityException {
			// has no effect
		}

		@Override
		public void flush() {
			// has no effect
		}

		@Override
		public void publish(LogRecord record) {
			GetOut.publish(record);
		}

	};

	private static List<Listener> listeners = new ArrayList<Listener>();

	private static List<Listener> listenersToRemove = new ArrayList<Listener>();

	private static ThreadLocal<Map<String, String>> localContext = new ThreadLocal<Map<String, String>>();

	public static int messagesLogged = 0;

	private static MessageFate nullFate = new MessageFate() {
		public boolean wasPrinted() {
			return false;
		}
	};

	public static final AtomicLong packageCount = new AtomicLong();

	static boolean production;

	private static StandardFormatter standardFormatter;

	static boolean verbose;

	private static VerboseFormatter verboseFormatter;

	public static void addListener(Listener listener) {
		new Thread(listener).start();
		listeners.add(listener);
	}

	public static void captureRoot() {
		final Logger rootLogger = Logger.getLogger("");
		for (final Handler handler : rootLogger.getHandlers())
			rootLogger.removeHandler(handler);
		rootLogger.addHandler(handler);
	}

	private static Map<String, String> getContext() {
		synchronized (localContext) {
			if (localContext.get() == null)
				localContext.set(new TreeMap<String, String>());
			return localContext.get();
		}
	}

	private static Formatter getCurrentFormatter() {
		if (verbose)
			return verboseFormatter;
		else
			return standardFormatter;
	}

	public static void launchNetworkDaemon(int port) {
		if (daemonLaunched)
			return;
		new Thread(new NetworkDaemon(port)).start();
	}

	public static MessageFate log(Message message) {
		try {
			final Package pkg = new Package();
			pkg.message = message;
			pkg.number = packageCount.incrementAndGet();
			pkg.thread = Thread.currentThread().getName();
			final MessageFate mf = getCurrentFormatter().format(pkg);
			notifyListeners(pkg);
			return mf;
		} catch (final Throwable thrown) {
			thrown.printStackTrace();
			return nullFate;
		}
	}

	public static MessageFate log(String format, Object... args) {
		return log(new Message(null, format, args));
	}

	public static MessageFate log(Throwable t) {
		return log(new Message(t, null));
	}

	public static MessageFate log(Throwable t, String format, Object... args) {
		return log(new Message(t, format, args));
	}

	private static void notifyListeners(Package pkg) {
		synchronized (listeners) {
			messagesLogged++;
			final boolean removeSomething = false;
			for (final Listener listener : listeners)
				if (listener.isStopped())
					listenersToRemove.add(listener);
				else
					listener.postPackage(pkg);
			if (removeSomething)
				for (final Listener listener : listenersToRemove)
					if (listeners.contains(listener))
						listeners.remove(listener);
		}
	}

	public static MessageFate publish(LogRecord record) {
		return log(record.getThrown(), record.getMessage());
	}

	public static void remove(String key) {
		getContext().remove(key);
	}

	public static void removeListener(Listener listener) {
		listener.stop();
		listeners.remove(listener);
	}

	private static void configure() {
		try{
			String config = System.getProperty("GetOut");
			if(config!=null){
				if(config.contains("production")){
					production = true;
				}
				if(config.contains("verbose")){
					verbose = true;
				}
				if(config.contains("server:")){
					Pattern p = Pattern.compile(".*server:(\\d+).*");
					Matcher m = p.matcher(config);
					String sport = null;
					if(m.matches()){
						sport = m.group(1);
					}
					if (sport != null){
						final int port = Integer.parseInt(sport);
						launchNetworkDaemon(port);
					}
				}
			}
			resetFormatters();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	static void reset(){
		getContext().clear();
	}
	
	static void resetFormatters(){
		verboseFormatter = new VerboseFormatter();
		verboseFormatter.setProduction(production);
		standardFormatter = new StandardFormatter();
		standardFormatter.setProduction(production);
	}

	public static void set(String key, String value) {
		getContext().put(key, value);
	}

}
