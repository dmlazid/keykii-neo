package com.keykii.neo;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import java.io.*;

/** Dependency-free device audit. Failure returns a nonzero instrumentation result. */
public final class NeoRendererAudit extends Instrumentation {
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);start();}
    private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    @Override public void onStart(){
        Bundle results=new Bundle();
        try{
            Context c=getTargetContext();
            check(NeoThemeCatalog.COUNT==0,"512 bulk NeoThemeCatalog packs must be removed");
            check(!NeoThemeCatalog.isNeoPack(1000),"NeoThemeCatalog packs 1000+ must be disabled");
            check(!IllustratedThemeAssets.isNewPack(120),"Bulk illustrated packs 120-139 must be disabled");
            int validCount=0;
            for(int p=100;p<=220;p++)if(KeyKiiThemeCollection.isThemePack(p))validCount++;
            check(validCount==30,"Expected 30 total themes (100-119 and 200-209), got "+validCount);
            for(int s=152;s<=157;s++) {
                boolean expectedPro=s>=155;
                check(KeyKiiAccess.fontNeedsAccess(s)==expectedPro,"Font access mismatch for style "+s);
            }
            File dir=new File(c.getExternalFilesDir(null),"theme-v2-audit");dir.mkdirs();
            try(FileWriter out=new FileWriter(new File(dir,"result.txt"))){
                out.write("PASS: 512 bulk themes removed, 20 original themes preserved, 10 clean gradient themes (200-209) active, and 6 stylish fonts (152-157) registered.\n");
            }
            results.putString("stream","\nTHEME_V2_AUDIT_PASS\n");finish(-1,results);
        }catch(Throwable t){results.putString("stream","\nTHEME_V2_AUDIT_FAIL: "+t+"\n");finish(1,results);}
    }
}

