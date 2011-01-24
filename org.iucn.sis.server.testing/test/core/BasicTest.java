package core;

import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.debug.Debugger;

import com.solertium.util.SysDebugger;

public class BasicTest implements Debugger {
	
	public BasicTest() {
		Debug.setInstance(this);
	}
	
	@Override
	public void println(Throwable e) {
		SysDebugger.out.println("{0}", e);
	}
	
	@Override
	public void println(Object obj) {
		SysDebugger.out.println(obj);
	}
	
	@Override
	public void println(String template, Object... args) {
		SysDebugger.out.println(template, args);
	}

}
