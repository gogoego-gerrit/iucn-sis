package org.gogoego.util.getout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.atomic.AtomicInteger;

import org.gogoego.util.getout.GetOut;
import org.junit.Test;

public class TestGetOut {

	public static class SlowTestListener extends TestListener {
		public SlowTestListener(int ringBufferSize) {
			super(ringBufferSize);
		}

		public void handle(GetOut.Package pkg) {
			heard++;
			try {
				final File f = File.createTempFile("testlistener", "dat");
				final FileWriter fw = new FileWriter(f);
				for (int i = 0; i < 128; i++) {
					fw.write(pkg.message.getFormattedMessage());
					fw.flush();
				}
				fw.close();
				f.delete();
			} catch (final Exception x) {
			}
		}
	}

	public static class TestListener extends GetOut.Listener {
		public int heard;

		public TestListener(int ringBufferSize) {
			super(ringBufferSize);
		}

		public int getHeard() {
			return heard;
		}

		public void handle(GetOut.Package pkg) {
			heard++;
		}
	}

	public int multiThreadedListenerTest(TestListener listener) {
		GetOut.addListener(listener);
		GetOut.messagesLogged = 0;
		GetOut.production = true;
		GetOut.verbose = true;
		GetOut.resetFormatters();
		final AtomicInteger runningThreads = new AtomicInteger();
		final AtomicInteger totalCount = new AtomicInteger();
		for (int i = 0; i < 32; i++) {
			final int j = i;
			new Thread(new Runnable() {
				public void run() {
					runningThreads.incrementAndGet();
					Thread.currentThread().setName("TestThread-" + j);
					GetOut.set("host", "www." + j + ".com");
					for (int q = 0; q < 32; q++)
						GetOut.log("Multithreaded Listener Test "
								+ totalCount.incrementAndGet());
					runningThreads.decrementAndGet();
				}
			}).start();
		}
		int timer = 0;
		while (runningThreads.get() > 0 || listener.isProcessing == true) {
			timer++;
			try {
				Thread.sleep(10);
			} catch (final InterruptedException ignored) {
			}
			if (timer > 500)
				System.out.println("Waiting on " + runningThreads.get()
						+ " threads");
			if (timer > 6000)
				assertEquals("Takes less than 60 seconds",
						"Took more than 60 seconds");
		}
		;

		listener.stop();
		System.out.println("Total messages sent: " + totalCount.get());
		System.out.println("Total messages logged: " + GetOut.messagesLogged);
		System.out.println("Total messages into listener: "
				+ listener.getLocalMessageCount());
		assertEquals(totalCount.get(), listener.getLocalMessageCount());
		System.out.println("Total messages heard: " + listener.getHeard());
		assertTrue(listener.getHeard() > 0);
		return listener.getHeard();
	}
	
	@Test
	public void simpleTest() {
		assertTrue("Message should be printed.", GetOut.log("Simple Test")
				.wasPrinted());
	}

	@Test
	public void multiThreadedTest() {
		final AtomicInteger runningThreads = new AtomicInteger();
		for (int i = 0; i < 32; i++) {
			final int j = i;
			new Thread(new Runnable() {
				public void run() {
					runningThreads.incrementAndGet();
					try {
						Thread.currentThread().setName("TestThread-" + j);
						GetOut.set("host", "www." + j + ".com");
						for (int q = 0; q < 32; q++)
							assertTrue("Message should be printed.", GetOut
									.log("Multithreaded Test").wasPrinted());
						throw new UnsupportedOperationException("yuk",
								new NullPointerException("bad"));
					} catch (final Throwable t) {
						assertTrue("Exception should be printed.", GetOut.log(
								t, "A Thrown Exception").wasPrinted());
					} finally {
						runningThreads.decrementAndGet();
					}
				}
			}).start();
		}
		int timer = 0;
		while (runningThreads.get() > 0) {
			timer++;
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException ignored) {
			}
			if (timer > 5)
				GetOut.log("Waiting on " + runningThreads.get() + " threads");
			if (timer > 60)
				assertEquals("Takes less than 60 seconds",
						"Took more than 60 seconds");
		}
		;
	}

	@Test
	public void bigBuffListenerTest() {
		System.out.println("\nbuff=2048, fast listener");
		final long in = System.currentTimeMillis();
		assertEquals(1024, multiThreadedListenerTest(new TestListener(2048)));
		System.out.println("elapsed: " + (System.currentTimeMillis() - in));
	}

	@Test
	public void mediumBuffListenerTest() {
		System.out.println("\nbuff=256, fast listener");
		final long in = System.currentTimeMillis();
		multiThreadedListenerTest(new TestListener(256));
		System.out.println("elapsed: " + (System.currentTimeMillis() - in));
	}

	@Test
	public void smallBuffListenerTest() {
		System.out.println("\nbuff=16, fast listener");
		final long in = System.currentTimeMillis();
		multiThreadedListenerTest(new TestListener(16));
		System.out.println("elapsed: " + (System.currentTimeMillis() - in));
	}

	@Test
	public void bigBuffSlowListenerTest() {
		System.out.println("\nbuff=2048, slow listener");
		final long in = System.currentTimeMillis();
		assertEquals(1024,
				multiThreadedListenerTest(new SlowTestListener(2048)));
		System.out.println("elapsed: " + (System.currentTimeMillis() - in));
	}

	@Test
	public void smallBuffSlowListenerTest() {
		System.out.println("\nbuff=16, slow listener");
		final long in = System.currentTimeMillis();
		multiThreadedListenerTest(new SlowTestListener(16));
		System.out.println("elapsed: " + (System.currentTimeMillis() - in));
	}

}
