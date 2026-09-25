package com.keykii.neo;

import java.util.*;

/** Host regression gate: every recipe, ten languages, symbols, number-row modes. */
public final class NeoGeometryAudit {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static NeoGeometry.Key[][] keys(String language,boolean numbers,boolean symbols){
        List<String[]> rows=new ArrayList<>();
        if(numbers||symbols)rows.add(new String[]{"1","2","3","4","5","6","7","8","9","0"});
        if(symbols){rows.add(new String[]{"@","#","$","_","&","-","+","(",")","/"});rows.add(new String[]{"SYM2","*","\"","'",":",";","!","?","BACK"});}
        else{
            rows.add(NeoKeyboardLanguage.top(language));rows.add(NeoKeyboardLanguage.middle(language));
            String[] letters=NeoKeyboardLanguage.bottom(language),third=new String[letters.length+2];
            third[0]="SHIFT";System.arraycopy(letters,0,third,1,letters.length);third[third.length-1]="BACK";rows.add(third);
        }
        rows.add(symbols?new String[]{"ABC",",","SPACE",".","ENTER"}:new String[]{"123",",","LANG","SPACE",".","ENTER"});
        NeoGeometry.Key[][] out=new NeoGeometry.Key[rows.size()][];
        for(int r=0;r<rows.size();r++){
            String[] rr=rows.get(r);out[r]=new NeoGeometry.Key[rr.length];
            for(int k=0;k<rr.length;k++){String a=rr[k];float wt=r==rows.size()-1?(a.equals("SPACE")?2.8f:a.equals(",")||a.equals(".")?.58f:a.equals("LANG")?.68f:1.12f):(a.length()>1?1.05f:1f);out[r][k]=new NeoGeometry.Key(a,wt,a.length()>1&&!a.equals("SPACE"));}
        }
        return out;
    }
    public static void main(String[] args){
        int checks=0;Set<String> layouts=new HashSet<>();
        String[] langs={"en-US","en-GB","fil","ceb","es","fr","de","tr","pt","it"};
        for(int pack=0;pack<512;pack++)for(String language:langs)for(int mode=0;mode<3;mode++){
            NeoGeometry.Key[][] keys=keys(language,mode==1,mode==2);
            NeoGeometry.Box[] boxes=NeoGeometry.layout(keys,pack%48,pack/48);
            int count=0,spaces=0;for(NeoGeometry.Key[] row:keys)count+=row.length;
            check(boxes.length==count,"Missing key");StringBuilder signature=new StringBuilder();
            for(int i=0;i<boxes.length;i++){
                NeoGeometry.Box b=boxes[i];
                check(Float.isFinite(b.left)&&Float.isFinite(b.top)&&Float.isFinite(b.right)&&Float.isFinite(b.bottom),"Nonfinite bounds");
                check(b.left>=0&&b.top>=0&&b.right<=1.00001f&&b.bottom<=1.00001f,"Out of bounds "+pack);
                check((b.right-b.left)*320>=12,"Unusable narrow target "+pack+" "+language);
                check((b.bottom-b.top)*180>=24,"Unusable short target "+pack);
                if(keys[b.row][b.col].action.equals("SPACE")){spaces++;check((b.right-b.left)*320>65,"Spacebar too small");}
                signature.append(Math.round(b.left*10000)).append(',').append(Math.round(b.top*10000)).append(',').append(Math.round(b.right*10000)).append(',').append(Math.round(b.bottom*10000)).append(';');
                for(int j=0;j<i;j++){
                    NeoGeometry.Box a=boxes[j];float iw=Math.min(a.right,b.right)-Math.max(a.left,b.left),ih=Math.min(a.bottom,b.bottom)-Math.max(a.top,b.top);
                    check(iw<.00001f||ih<.00001f,"Overlapping touch targets "+pack);
                }
            }
            check(spaces==1,"Space action lost or duplicated");
            if(language.equals("en-US")&&mode==0)check(layouts.add(signature.toString()),"Duplicate geometry "+pack);
            checks++;
        }
        System.out.println("PASS: "+checks+" keyboard configurations; 512 distinct geometry signatures; all keys retained; no overlaps; usable spacebars.");
    }
}
