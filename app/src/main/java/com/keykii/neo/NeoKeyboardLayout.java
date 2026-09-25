package com.keykii.neo;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import java.util.ArrayList;

/** Positions the existing IME key views: their click, long-press, glide and cursor
 * handlers survive unchanged. Visual bounds ARE touch bounds; no invisible offsets.
 */
final class NeoKeyboardLayout extends ViewGroup {
    static final class Tag {
        final NeoGeometry.Key key;
        Tag(String action,float weight,boolean special){key=new NeoGeometry.Key(action,weight,special);}
    }
    private final NeoThemeCatalog.Entry entry;
    private final NeoThemeRenderer renderer;
    private final NeoGeometry.Key[][] rows;
    private final NeoGeometry.Box[] boxes;
    private final int naturalHeight;
    private NeoKeyboardLayout(Context context,int pack,NeoGeometry.Key[][] rows,int height){
        super(context);entry=NeoThemeCatalog.get(pack);renderer=new NeoThemeRenderer(pack);
        this.rows=rows;boxes=NeoGeometry.layout(rows,entry.architecture,entry.composition);naturalHeight=height;
        setWillNotDraw(false);setClipChildren(false);setClipToPadding(false);NeoArt.watch(this);
    }
    static void adopt(LinearLayout body,int first,int pack){
        if(!NeoThemeCatalog.isNeoPack(pack))return;
        ArrayList<ArrayList<View>> views=new ArrayList<>();ArrayList<NeoGeometry.Key[]> keys=new ArrayList<>();
        int height=0;
        for(int i=first;i<body.getChildCount();i++){
            View child=body.getChildAt(i);if(!(child instanceof LinearLayout))return;
            LinearLayout row=(LinearLayout)child;ArrayList<View> vr=new ArrayList<>();ArrayList<NeoGeometry.Key> kr=new ArrayList<>();int rh=0;
            for(int k=0;k<row.getChildCount();k++){
                View key=row.getChildAt(k);if(!(key.getTag() instanceof Tag))continue;
                vr.add(key);kr.add(((Tag)key.getTag()).key);
                LinearLayout.LayoutParams p=(LinearLayout.LayoutParams)key.getLayoutParams();rh=Math.max(rh,p.height+p.topMargin+p.bottomMargin);
            }
            if(kr.isEmpty())return;
            ViewGroup.LayoutParams lp=row.getLayoutParams();if(lp instanceof MarginLayoutParams)rh+=((MarginLayoutParams)lp).topMargin+((MarginLayoutParams)lp).bottomMargin;
            height+=rh;views.add(vr);keys.add(kr.toArray(new NeoGeometry.Key[0]));
        }
        if(keys.isEmpty())return;
        NeoKeyboardLayout board=new NeoKeyboardLayout(body.getContext(),pack,keys.toArray(new NeoGeometry.Key[0][]),height);
        int index=0;
        for(ArrayList<View> row:views)for(View key:row){
            ((ViewGroup)key.getParent()).removeView(key);
            Tag tag=(Tag)key.getTag();
            key.setBackground(NeoThemeKeyDrawable.state(body.getContext(),pack,tag.key.action,tag.key.special,board.boxes[index++]));
            key.setContentDescription(tag.key.action.equals("SPACE")?"Space":tag.key.action);
            board.addView(key,new LayoutParams(1,1));
        }
        body.removeViews(first,body.getChildCount()-first);
        body.addView(board,new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,height));
    }
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        int w=MeasureSpec.getSize(widthSpec),h=resolveSize(naturalHeight,heightSpec);setMeasuredDimension(w,h);
        for(int i=0;i<getChildCount();i++){
            NeoGeometry.Box b=boxes[i];int kw=Math.max(1,Math.round(b.right*w)-Math.round(b.left*w)),kh=Math.max(1,Math.round(b.bottom*h)-Math.round(b.top*h));
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec(kw,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(kh,MeasureSpec.EXACTLY));
        }
    }
    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){
        int w=r-l,h=b-t;for(int i=0;i<getChildCount();i++){NeoGeometry.Box box=boxes[i];getChildAt(i).layout(Math.round(box.left*w),Math.round(box.top*h),Math.round(box.right*w),Math.round(box.bottom*h));}
    }
    @Override protected void onDraw(Canvas c){renderer.background(c,getWidth(),getHeight());renderer.structure(c,getWidth(),getHeight(),boxes);}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();NeoArt.watch(this);}
}
