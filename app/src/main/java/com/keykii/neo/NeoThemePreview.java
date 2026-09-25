package com.keykii.neo;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** One lightweight canvas per card, with the same geometry and renderer as live IME.
 * No nested rows/TextViews and no full-catalog bitmap allocation in the Theme Shop.
 */
final class NeoThemePreview extends View {
    private final NeoThemeCatalog.Entry entry;
    private final NeoThemeRenderer renderer;
    private final NeoGeometry.Key[][] rows;
    private final NeoGeometry.Box[] boxes;
    private final Paint text=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Typeface typeface;
    private final int fontStyle;
    private final String language;
    NeoThemePreview(Context c,int pack,Typeface font,boolean numberRow){
        super(c);entry=NeoThemeCatalog.get(pack);renderer=new NeoThemeRenderer(pack);typeface=font;
        fontStyle=c.getSharedPreferences("keykii_prefs",Context.MODE_PRIVATE).getInt("keyboard_font_style",0);
        language=c.getSharedPreferences("keykii_prefs",Context.MODE_PRIVATE).getString("keyboard_language_active","en-US");
        java.util.ArrayList<String[]> tokenRows=new java.util.ArrayList<>();
        if(numberRow)tokenRows.add(new String[]{"1","2","3","4","5","6","7","8","9","0"});
        tokenRows.add(NeoKeyboardLanguage.top(language));
        tokenRows.add(NeoKeyboardLanguage.middle(language));
        String[] letters=NeoKeyboardLanguage.bottom(language),third=new String[letters.length+2];
        third[0]="SHIFT";System.arraycopy(letters,0,third,1,letters.length);third[third.length-1]="BACK";
        tokenRows.add(third);tokenRows.add(new String[]{"123",",","LANG","SPACE",".","ENTER"});
        String[][] tokens=tokenRows.toArray(new String[0][]);
        rows=new NeoGeometry.Key[tokens.length][];
        for(int r=0;r<tokens.length;r++){
            rows[r]=new NeoGeometry.Key[tokens[r].length];
            for(int k=0;k<tokens[r].length;k++){
                String a=tokens[r][k];boolean last=r==tokens.length-1;
                float wt=last?(a.equals("SPACE")?2.8f:a.equals(",")||a.equals(".")?.58f:a.equals("LANG")?.68f:1.12f):(a.equals("SHIFT")||a.equals("BACK")?1.05f:1f);
                rows[r][k]=new NeoGeometry.Key(a,wt,a.length()>1&&!a.equals("SPACE"));
            }
        }
        boxes=NeoGeometry.layout(rows,entry.architecture,entry.composition);NeoArt.watch(this);
        setContentDescription(entry.name+" keyboard preview");
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float w=getWidth(),h=getHeight();renderer.background(c,w,h);renderer.structure(c,w,h,boxes);
        text.setTypeface(typeface);text.setTextAlign(Paint.Align.CENTER);
        for(NeoGeometry.Box b:boxes){
            NeoGeometry.Key k=rows[b.row][b.col];float l=b.left*w,t=b.top*h,kw=(b.right-b.left)*w,kh=(b.bottom-b.top)*h;
            c.save();c.translate(l,t);renderer.key(c,kw,kh,b,k.action,k.special,false);
            String label=k.action.equals("SPACE")?NeoKeyboardLanguage.label(language):k.action.equals("SHIFT")?"⇧":k.action.equals("BACK")?"⌫":k.action.equals("LANG")?"◎":k.action.equals("ENTER")?"↵":k.action;
            float size=Math.min(h/rows.length*.38f,w*.045f);if(label.length()>2)size*=.68f;
            text.setTextSize(size);text.setColor(ColorFontCatalog.colorFor(fontStyle,label,NeoThemeRenderer.textColor(entry)));
            float baseline=(kh-text.ascent()-text.descent())*.5f;c.drawText(label,kw*.5f,baseline,text);
            String hint=NeoKeyboardLanguage.hint(k.action);
            if(!hint.isEmpty()){
                text.setTextSize(size*.45f);text.setAlpha(140);
                c.drawText(hint,kw*.5f,kh*.24f,text);text.setAlpha(255);
            }
            c.restore();
        }
    }
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();NeoArt.watch(this);}
}
