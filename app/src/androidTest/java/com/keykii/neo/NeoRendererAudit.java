package com.keykii.neo;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.io.*;

/** Dependency-free device audit. Failure returns a nonzero instrumentation result.
 * Captures the real Android Canvas preview and validates real adapted view bounds.
 */
public final class NeoRendererAudit extends Instrumentation {
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);start();}
    private static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    @Override public void onStart(){
        Bundle results=new Bundle();
        try{
            Context c=getTargetContext();NeoArt.preload(c);
            long until=System.currentTimeMillis()+15000;
            while((NeoArt.atlas(0)==null||NeoArt.atlas(16)==null)&&System.currentTimeMillis()<until)Thread.sleep(25);
            check(NeoArt.atlas(0)!=null&&NeoArt.atlas(16)!=null,"Original artwork did not load");
            File dir=new File(c.getExternalFilesDir(null),"theme-v2-audit");dir.mkdirs();
            final Throwable[] error={null};
            runOnMainSync(()->{
                try{
                    Paint text=new Paint(Paint.ANTI_ALIAS_FLAG);text.setColor(0xff302b32);text.setTextSize(17);
                    // Six sheets show all 48 real renderer families. Source is the same
                    // NeoThemePreview class used in the shop, with real Android fonts.
                    for(int sheet=0;sheet<6;sheet++){
                        Bitmap bm=Bitmap.createBitmap(1440,1700,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bm);canvas.drawColor(0xfff5f1f5);
                        for(int n=0;n<8;n++){
                            int pack=1000+sheet*8+n,col=n%2,row=n/2;
                            NeoThemePreview preview=new NeoThemePreview(c,pack,Typeface.DEFAULT,false);
                            preview.measure(View.MeasureSpec.makeMeasureSpec(700,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(390,View.MeasureSpec.EXACTLY));
                            preview.layout(0,0,700,390);
                            canvas.save();canvas.translate(col*720+10,row*425+28);preview.draw(canvas);canvas.restore();
                            canvas.drawText(NeoThemeCatalog.get(pack).name,col*720+12,row*425+20,text);
                        }
                        try(FileOutputStream out=new FileOutputStream(new File(dir,"families-"+sheet+".png"))){bm.compress(Bitmap.CompressFormat.PNG,100,out);}bm.recycle();
                    }
                    // Duplicate guard: every structural family is exclusively FREE or PRO,
                    // and its 11 compositions must not recycle the same artwork.
                    for(int family=0;family<48;family++){
                        Boolean tier=null;java.util.HashSet<Integer> artSeen=new java.util.HashSet<>();
                        for(int composition=0;composition<11;composition++){
                            int pack=1000+composition*48+family;
                            if(pack>=1512)continue;
                            NeoThemeCatalog.Entry entry=NeoThemeCatalog.get(pack);
                            if(tier==null)tier=entry.pro;else check(tier==entry.pro,"FREE/PRO family duplicate "+family);
                            check(artSeen.add(entry.art),"Repeated scene in family "+family+" composition "+composition);
                        }
                    }
                    for(int pack=1000;pack<1512;pack++){
                        LinearLayout body=new LinearLayout(c);body.setOrientation(LinearLayout.VERTICAL);
                        String[][] actions={{"q","w","e","r","t","y","u","i","o","p"},{"a","s","d","f","g","h","j","k","l"},{"SHIFT","z","x","c","v","b","n","m","BACK"},{"123",",","LANG","SPACE",".","ENTER"}};
                        final int[] taps={0};int count=0;
                        for(int row=0;row<actions.length;row++){
                            LinearLayout line=new LinearLayout(c);
                            for(String action:actions[row]){
                                FrameLayout key=new FrameLayout(c);boolean last=row==actions.length-1;
                                float weight=last?(action.equals("SPACE")?2.8f:action.equals("LANG")?.68f:action.equals(",")||action.equals(".")?.58f:1.12f):(action.length()>1?1.05f:1);
                                key.setTag(new NeoKeyboardLayout.Tag(action,weight,action.length()>1&&!action.equals("SPACE")));
                                key.setOnClickListener(v->taps[0]++);key.setOnLongClickListener(v->true);
                                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,42,weight);p.setMargins(1,2,1,2);line.addView(key,p);count++;
                            }
                            body.addView(line,new LinearLayout.LayoutParams(-1,-2));
                        }
                        NeoKeyboardLayout.adopt(body,0,pack);check(body.getChildCount()==1,"Rows were not adapted");
                        NeoKeyboardLayout board=(NeoKeyboardLayout)body.getChildAt(0);check(board.getChildCount()==count,"Key count changed");
                        board.measure(View.MeasureSpec.makeMeasureSpec(360,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(184,View.MeasureSpec.EXACTLY));board.layout(0,0,360,184);
                        check(board.getMeasuredHeight()==184,"Keyboard height changed");
                        for(int i=0;i<board.getChildCount();i++){
                            View key=board.getChildAt(i);check(key.getWidth()>0&&key.getHeight()>0,"Empty live target");key.performClick();check(key.performLongClick(),"Long-press handler lost");
                            for(int j=0;j<i;j++){View old=board.getChildAt(j);check(Math.min(old.getRight(),key.getRight())<=Math.max(old.getLeft(),key.getLeft())||Math.min(old.getBottom(),key.getBottom())<=Math.max(old.getTop(),key.getTop()),"Live hit rectangles overlap");}
                        }
                        check(taps[0]==count,"Click handlers lost");
                    }
                }catch(Throwable t){error[0]=t;}
            });
            if(error[0]!=null)throw new AssertionError(error[0]);
            try(FileWriter out=new FileWriter(new File(dir,"result.txt"))){out.write("PASS: artwork decoded; 48 family previews captured; FREE/PRO families are structurally exclusive; family scenes do not repeat; 512 live layouts preserve all views, click/long-press handlers, height and non-overlapping hit bounds.\n");}
            results.putString("stream","\nTHEME_V2_AUDIT_PASS\n");finish(-1,results);
        }catch(Throwable t){results.putString("stream","\nTHEME_V2_AUDIT_FAIL: "+t+"\n");finish(1,results);}
    }
}
