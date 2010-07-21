package com.solertium.util.querybuilder.query;

import com.solertium.lwxml.shared.NativeElement;


public interface QBConstraint {

    public final static int CT_EQUALS=1;
    public final static int CT_GT=2;
    public final static int CT_LT=3;
    public final static int CT_CONTAINS=10;
    public final static int CT_STARTS_WITH=11;
    public final static int CT_ENDS_WITH=12;
    public final static int CT_NOT=90;
    public final static int CG_AND=1;
    public final static int CG_OR=2;
    
    public String saveConfig();
    
    public void loadConfig(NativeElement config);
    
    public String getID();

}
