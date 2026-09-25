package com.keykii.neo;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

/** Toolbar/panel backdrop. Key-area rendering is owned by NeoKeyboardLayout. */
final class NeoThemeDrawable extends Drawable {
    private final NeoThemeRenderer renderer;
    NeoThemeDrawable(Context c,int pack){renderer=new NeoThemeRenderer(pack);NeoArt.preload(c);}
    @Override public void draw(Canvas c){Rect b=getBounds();c.save();c.translate(b.left,b.top);renderer.background(c,b.width(),b.height());c.restore();}
    @Override public void setAlpha(int a){}
    @Override public void setColorFilter(ColorFilter f){}
    @Override public int getOpacity(){return PixelFormat.OPAQUE;}
}

final class NeoThemeKeyDrawable extends Drawable {
    private final NeoThemeRenderer renderer;
    private final String action;
    private final boolean special,pressed;
    private NeoGeometry.Box geometry;
    private NeoThemeKeyDrawable(int pack,String action,boolean special,boolean pressed){renderer=new NeoThemeRenderer(pack);this.action=action;this.special=special;this.pressed=pressed;}
    static StateListDrawable state(Context c,int pack,boolean special,boolean space){return state(c,pack,space?"SPACE":"",special,null);}
    static StateListDrawable state(Context c,int pack,String action,boolean special,NeoGeometry.Box box){
        StateListDrawable state=new StateListDrawable();
        NeoThemeKeyDrawable down=new NeoThemeKeyDrawable(pack,action,special,true);down.geometry=box;
        NeoThemeKeyDrawable normal=new NeoThemeKeyDrawable(pack,action,special,false);normal.geometry=box;
        state.addState(new int[]{android.R.attr.state_pressed},down);state.addState(new int[]{},normal);
        return state;
    }
    @Override public void draw(Canvas c){Rect b=getBounds();c.save();c.translate(b.left,b.top);renderer.key(c,b.width(),b.height(),geometry,action,special,pressed);c.restore();}
    @Override public void setAlpha(int a){}
    @Override public void setColorFilter(ColorFilter f){}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
