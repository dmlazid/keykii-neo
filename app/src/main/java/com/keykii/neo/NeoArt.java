package com.keykii.neo;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/** Two original atlases, decoded once off the UI thread. Never decodes in draw(). */
final class NeoArt {
    private static final Bitmap[] atlases=new Bitmap[2];
    private static boolean loading;
    private static final ArrayList<WeakReference<View>> waiting=new ArrayList<>();
    static synchronized void preload(Context context){
        if(loading || atlases[0]!=null)return;
        loading=true; final Context app=context.getApplicationContext();
        new Thread(()->{
            for(int i=0;i<2;i++){
                Bitmap bitmap=null;
                try(InputStream in=app.getAssets().open("themes/v2/scenes-"+(i==0?"a":"b")+".png")){
                    BitmapFactory.Options o=new BitmapFactory.Options();
                    o.inPreferredConfig=Bitmap.Config.RGB_565;
                    bitmap=BitmapFactory.decodeStream(in,null,o);
                }catch(Exception ignored){}
                synchronized(NeoArt.class){atlases[i]=bitmap;}
            }
            synchronized(NeoArt.class){
                loading=false;
                for(WeakReference<View> ref:waiting){View v=ref.get();if(v!=null)v.post(()->invalidateTree(v));}
                waiting.clear();
            }
        },"KeyKii-Theme-Art").start();
    }
    static synchronized void watch(View view){
        if(atlases[0]!=null&&atlases[1]!=null)return;
        for(WeakReference<View> r:waiting)if(r.get()==view)return;
        waiting.add(new WeakReference<>(view));preload(view.getContext());
    }
    private static void invalidateTree(View view){
        view.invalidate();
        if(view instanceof android.view.ViewGroup){
            android.view.ViewGroup group=(android.view.ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++)invalidateTree(group.getChildAt(i));
        }
        android.view.ViewParent parent=view.getParent();
        for(int i=0;i<2&&parent instanceof View;i++){
            ((View)parent).invalidate();parent=parent.getParent();
        }
    }
    static synchronized Bitmap atlas(int scene){return atlases[Math.floorMod(scene,32)/16];}
    static void draw(Canvas c,int scene,RectF dest,Paint p){
        drawSlice(c,scene,dest,p,0,0,1,1);
    }
    static void drawSlice(Canvas c,int scene,RectF dest,Paint p,float l,float t,float r,float b){
        Bitmap bitmap=atlas(scene);if(bitmap==null)return;
        int cell=Math.floorMod(scene,16);float cw=bitmap.getWidth()/4f,ch=bitmap.getHeight()/4f;
        int x=cell%4,y=cell/4;
        Rect src=new Rect((int)((x+l)*cw),(int)((y+t)*ch),(int)((x+r)*cw),(int)((y+b)*ch));
        c.drawBitmap(bitmap,src,dest,p);
    }
    private NeoArt(){}
}
