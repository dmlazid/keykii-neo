package com.keykii.neo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

final class NeoThemeDrawable extends Drawable {

    private final NeoThemeCatalog.Entry e;
    private final float d;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private int alpha=255;

    NeoThemeDrawable(Context context,int pack) {
        e=NeoThemeCatalog.get(pack);
        d=context.getResources().getDisplayMetrics().density;
    }

    @Override public void draw(Canvas c) {
        if(e==null) return;

        Rect b=getBounds();
        float w=b.width(),h=b.height();

        if(w<=0 || h<=0) return;

        c.save();
        c.translate(b.left,b.top);

        drawArchitecture(c,w,h,e.architecture);
        drawWorld(c,w,h,e.world);

        c.restore();
    }

    private void drawArchitecture(Canvas c,float w,float h,int architecture) {
        int base=architecture & 15;
        int soft=NeoThemeCatalog.mix(e.accent,Color.WHITE,e.dark?24:70);
        int deep=NeoThemeCatalog.mix(e.accent,Color.BLACK,e.dark?12:38);

        p.setShader(null);
        p.setStyle(Paint.Style.FILL);

        switch(base) {
            case 0: capsuleGrid(c,w,h,soft,e.accent); break;
            case 1: floatingTiles(c,w,h,soft,e.accent); break;
            case 2: splitDeck(c,w,h,e.accent,deep); break;
            case 3: ribbonRows(c,w,h,soft,e.accent); break;
            case 4: haloField(c,w,h,soft,e.accent); break;
            case 5: mosaic(c,w,h,soft,e.accent); break;
            case 6: orbit(c,w,h,soft,e.accent); break;
            case 7: bento(c,w,h,soft,e.accent); break;
            case 8: prisms(c,w,h,soft,e.accent); break;
            case 9: cloudDeck(c,w,h,soft,e.accent); break;
            case 10: glassDock(c,w,h,soft,e.accent); break;
            case 11: arcadeMatrix(c,w,h,soft,e.accent); break;
            case 12: decoSteps(c,w,h,soft,e.accent); break;
            case 13: waveBoard(c,w,h,soft,e.accent); break;
            case 14: petalRows(c,w,h,soft,e.accent); break;
            default: jewelGrid(c,w,h,soft,e.accent); break;
        }

        if(architecture>=16)
            drawArchitectureOverlay(c,w,h,architecture-16,soft,e.accent);
    }

    private void drawArchitectureOverlay(Canvas c,float w,float h,int mode,int soft,int accent) {
        p.setShader(null);

        switch(mode) {
            case 0:
                dottedRails(c,w,h,soft,accent);
                break;
            case 1:
                cornerFrames(c,w,h,soft,accent);
                break;
            case 2:
                portalDots(c,w,h,soft,accent);
                break;
            case 3:
                diagonalGlass(c,w,h,soft,accent);
                break;
            case 4:
                metroLine(c,w,h,soft,accent);
                break;
            case 5:
                lanternLine(c,w,h,soft,accent);
                break;
            case 6:
                pebbleBand(c,w,h,soft,accent);
                break;
            case 7:
                circuitRail(c,w,h,soft,accent);
                break;
            case 8:
                gardenFrame(c,w,h,soft,accent);
                break;
            case 9:
                velvetBands(c,w,h,soft,accent);
                break;
            case 10:
                sunsetDisc(c,w,h,soft,accent);
                break;
            case 11:
                auroraSweep(c,w,h,soft,accent);
                break;
            case 12:
                quiltDiamonds(c,w,h,soft,accent);
                break;
            case 13:
                skyline(c,w,h,soft,accent);
                break;
            case 14:
                facets(c,w,h,soft,accent);
                break;
            default:
                zenFrame(c,w,h,soft,accent);
                break;
        }
    }

    private void drawWorld(Canvas c,float w,float h,int world) {
        int soft=NeoThemeCatalog.mix(e.accent,Color.WHITE,e.dark?34:76);
        int deep=NeoThemeCatalog.mix(e.accent,Color.BLACK,e.dark?10:42);

        switch(world) {
            case 0: petals(c,w,h,e.accent,soft); break;
            case 1: moonStars(c,w,h,soft,e.accent); break;
            case 2: bubbles(c,w,h,soft,e.accent); break;
            case 3: leaves(c,w,h,deep,soft); break;
            case 4: dunes(c,w,h,soft,e.accent); break;
            case 5: snow(c,w,h,soft,e.accent); break;
            case 6: candy(c,w,h,soft,e.accent); break;
            case 7: circuitWorld(c,w,h,soft,e.accent); break;
            case 8: cosmos(c,w,h,soft,e.accent); break;
            case 9: coffee(c,w,h,soft,deep); break;
            case 10: botanical(c,w,h,soft,deep); break;
            case 11: crystal(c,w,h,soft,e.accent); break;
            case 12: lava(c,w,h,soft,e.accent); break;
            case 13: rain(c,w,h,soft,e.accent); break;
            case 14: aurora(c,w,h,soft,e.accent); break;
            case 15: sunset(c,w,h,soft,e.accent); break;
            case 16: city(c,w,h,soft,e.accent); break;
            case 17: pixel(c,w,h,soft,e.accent); break;
            case 18: paper(c,w,h,soft,e.accent); break;
            case 19: velvet(c,w,h,soft,e.accent); break;
            case 20: marble(c,w,h,soft,e.accent); break;
            case 21: glass(c,w,h,soft,e.accent); break;
            case 22: neon(c,w,h,soft,e.accent); break;
            case 23: ink(c,w,h,soft,e.accent); break;
            case 24: meadow(c,w,h,soft,e.accent); break;
            case 25: coral(c,w,h,soft,e.accent); break;
            case 26: storm(c,w,h,soft,e.accent); break;
            case 27: pumpkin(c,w,h,soft,e.accent); break;
            case 28: winter(c,w,h,soft,e.accent); break;
            case 29: festival(c,w,h,soft,e.accent); break;
            case 30: galaxy(c,w,h,soft,e.accent); break;
            default: orchard(c,w,h,soft,e.accent); break;
        }
    }

    private void capsuleGrid(Canvas c,float w,float h,int soft,int accent) {
        float rw=w*.135f,rh=h*.14f;
        for(int y=0;y<4;y++) for(int x=0;x<6;x++) {
            float l=w*.035f+x*w*.164f+(y%2)*w*.025f;
            float t=h*.08f+y*h*.23f;
            p.setColor(a((x+y)%2==0?soft:accent,30+(x+y)%3*7));
            c.drawRoundRect(new RectF(l,t,l+rw,t+rh),rh*.46f,rh*.46f,p);
        }
    }

    private void floatingTiles(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<12;i++) {
            float x=w*((i*37%91)/100f),y=h*((i*61%83)/100f);
            float rw=w*(.08f+(i%4)*.018f),rh=h*(.07f+(i%3)*.026f);
            p.setColor(a(i%2==0?soft:accent,26+i%3*9));
            c.save();
            c.rotate((i%2==0?1:-1)*(4+i%6),x,y);
            c.drawRoundRect(new RectF(x,y,x+rw,y+rh),dp(9),dp(9),p);
            c.restore();
        }
    }

    private void splitDeck(Canvas c,float w,float h,int accent,int deep) {
        path.reset();
        path.moveTo(0,0); path.lineTo(w*.55f,0); path.lineTo(w*.38f,h); path.lineTo(0,h); path.close();
        p.setColor(a(accent,42)); c.drawPath(path,p);
        path.reset();
        path.moveTo(w*.68f,0); path.lineTo(w,h); path.lineTo(w*.48f,h); path.close();
        p.setColor(a(deep,34)); c.drawPath(path,p);
    }

    private void ribbonRows(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeWidth(dp(3.5f));
        for(int r=0;r<4;r++) {
            float y=h*(.14f+r*.23f);
            path.reset(); path.moveTo(-w*.05f,y);
            path.cubicTo(w*.22f,y-h*.07f,w*.34f,y+h*.07f,w*.52f,y);
            path.cubicTo(w*.72f,y-h*.07f,w*.86f,y+h*.07f,w*1.05f,y);
            p.setColor(a(r%2==0?soft:accent,48)); c.drawPath(path,p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void haloField(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE);
        for(int i=0;i<12;i++) {
            float x=w*((i*29%91)/100f),y=h*((i*53%87)/100f);
            float r=Math.min(w,h)*(.025f+(i%5)*.01f);
            p.setStrokeWidth(dp(1+i%2)); p.setColor(a(i%2==0?soft:accent,58));
            c.drawCircle(x,y,r,p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void mosaic(Canvas c,float w,float h,int soft,int accent) {
        int cols=8,rows=5; float cw=w/cols,ch=h/rows;
        for(int y=0;y<rows;y++) for(int x=0;x<cols;x++) if((x*3+y*5+e.world)%4==0) {
            p.setColor(a((x+y)%2==0?soft:accent,30));
            c.drawRect(x*cw,y*ch,(x+1)*cw,(y+1)*ch,p);
        }
    }

    private void orbit(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.2f));
        float cx=w*.52f,cy=h*.48f;
        for(int i=0;i<5;i++) {
            p.setColor(a(i%2==0?soft:accent,54));
            c.drawOval(new RectF(cx-w*(.13f+i*.075f),cy-h*(.07f+i*.05f),cx+w*(.13f+i*.075f),cy+h*(.07f+i*.05f)),p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void bento(Canvas c,float w,float h,int soft,int accent) {
        RectF[] rs={
                new RectF(w*.03f,h*.07f,w*.36f,h*.42f),
                new RectF(w*.38f,h*.07f,w*.97f,h*.24f),
                new RectF(w*.38f,h*.26f,w*.68f,h*.58f),
                new RectF(w*.70f,h*.26f,w*.97f,h*.58f),
                new RectF(w*.03f,h*.44f,w*.36f,h*.93f),
                new RectF(w*.38f,h*.60f,w*.97f,h*.93f)
        };
        for(int i=0;i<rs.length;i++) {
            p.setColor(a(i%2==0?soft:accent,28+i%3*7));
            c.drawRoundRect(rs[i],dp(11),dp(11),p);
        }
    }

    private void prisms(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<10;i++) {
            float x=w*((i*23%90)/100f),y=h*((i*47%84)/100f),s=Math.min(w,h)*(.045f+(i%3)*.018f);
            path.reset(); path.moveTo(x,y-s); path.lineTo(x+s,y+s*.7f); path.lineTo(x-s,y+s*.7f); path.close();
            p.setColor(a(i%2==0?soft:accent,40)); c.drawPath(path,p);
        }
    }

    private void cloudDeck(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<6;i++) {
            float x=w*(.07f+i*.17f),y=h*((i%2==0)?.16f:.76f),r=Math.min(w,h)*.04f;
            p.setColor(a(i%2==0?soft:accent,38));
            c.drawCircle(x,y,r,p); c.drawCircle(x+r*.8f,y-r*.2f,r*.8f,p);
            c.drawRoundRect(new RectF(x-r*.6f,y,x+r*1.5f,y+r*.65f),r*.3f,r*.3f,p);
        }
    }

    private void glassDock(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1));
        p.setColor(a(Color.WHITE,e.dark?62:118));
        for(int i=0;i<4;i++) {
            float in=w*(.04f+i*.025f);
            c.drawRoundRect(new RectF(in,h*.05f+i*h*.015f,w-in,h*.95f-i*h*.015f),dp(17+i*2),dp(17+i*2),p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void arcadeMatrix(Canvas c,float w,float h,int soft,int accent) {
        float s=Math.min(w,h)*.022f;
        for(int y=0;y<7;y++) for(int x=0;x<13;x++) if(((x*3+y*5+e.world)%4)==0) {
            p.setColor(a((x+y)%2==0?soft:accent,54));
            float px=w*.025f+x*w*.075f,py=h*.06f+y*h*.135f;
            c.drawRect(px,py,px+s,py+s,p);
        }
    }

    private void decoSteps(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.6f));
        for(int i=0;i<5;i++) {
            float inset=w*(.04f+i*.04f);
            path.reset(); path.moveTo(inset,h*(.13f+i*.04f));
            path.lineTo(w*(.22f+i*.03f),h*(.13f+i*.04f));
            path.lineTo(w*(.27f+i*.03f),h*(.20f+i*.04f));
            path.lineTo(w-inset,h*(.20f+i*.04f));
            p.setColor(a(i%2==0?soft:accent,46)); c.drawPath(path,p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void waveBoard(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.7f));
        for(int r=0;r<5;r++) {
            float y=h*(.11f+r*.19f);
            path.reset(); path.moveTo(-w*.05f,y);
            for(int i=0;i<6;i++) {
                float x=w*i*.20f;
                path.quadTo(x+w*.05f,y-h*.035f,x+w*.10f,y);
                path.quadTo(x+w*.15f,y+h*.035f,x+w*.20f,y);
            }
            p.setColor(a(r%2==0?soft:accent,43)); c.drawPath(path,p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void petalRows(Canvas c,float w,float h,int soft,int accent) {
        for(int y=0;y<4;y++) for(int x=0;x<7;x++) {
            float cx=w*(.08f+x*.145f),cy=h*(.12f+y*.25f);
            p.setColor(a((x+y)%2==0?soft:accent,36));
            c.save(); c.rotate((x*17+y*11)%70-35,cx,cy);
            c.drawOval(new RectF(cx-w*.012f,cy-h*.043f,cx+w*.012f,cy+h*.043f),p); c.restore();
        }
    }

    private void jewelGrid(Canvas c,float w,float h,int soft,int accent) {
        for(int y=0;y<4;y++) for(int x=0;x<7;x++) {
            float cx=w*(.08f+x*.145f),cy=h*(.12f+y*.25f),sx=w*.03f,sy=h*.05f;
            path.reset(); path.moveTo(cx,cy-sy); path.lineTo(cx+sx,cy); path.lineTo(cx,cy+sy); path.lineTo(cx-sx,cy); path.close();
            p.setColor(a((x+y)%2==0?soft:accent,34)); c.drawPath(path,p);
        }
    }

    private void dottedRails(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<16;i++) {
            p.setColor(a(i%2==0?soft:accent,65));
            c.drawCircle(w*(.04f+i*.061f),h*.08f,dp(1.3f+i%2),p);
            c.drawCircle(w*(.96f-i*.061f),h*.92f,dp(1.3f+i%2),p);
        }
    }

    private void cornerFrames(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2)); p.setColor(a(accent,66));
        float l=w*.04f,t=h*.08f,r=w*.96f,b=h*.92f,s=Math.min(w,h)*.12f;
        c.drawLine(l,t,l+s,t,p); c.drawLine(l,t,l,t+s,p);
        c.drawLine(r,t,r-s,t,p); c.drawLine(r,t,r,t+s,p);
        c.drawLine(l,b,l+s,b,p); c.drawLine(l,b,l,b-s,p);
        c.drawLine(r,b,r-s,b,p); c.drawLine(r,b,r,b-s,p);
        p.setStyle(Paint.Style.FILL);
    }

    private void portalDots(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.2f));
        for(int i=0;i<8;i++) {
            p.setColor(a(i%2==0?soft:accent,54));
            c.drawCircle(w*(.07f+i*.125f),h*.5f,Math.min(w,h)*(.025f+(i%3)*.009f),p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void diagonalGlass(Canvas c,float w,float h,int soft,int accent) {
        for(int i=-2;i<8;i++) {
            path.reset();
            float x=w*i*.18f;
            path.moveTo(x,0); path.lineTo(x+w*.08f,0); path.lineTo(x+w*.35f,h); path.lineTo(x+w*.27f,h); path.close();
            p.setColor(a(i%2==0?soft:accent,22)); c.drawPath(path,p);
        }
    }

    private void metroLine(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2));
        float[][] pts={{.07f,.18f},{.27f,.18f},{.27f,.45f},{.55f,.45f},{.55f,.72f},{.90f,.72f}};
        for(int i=0;i<pts.length-1;i++) {
            p.setColor(a(i%2==0?soft:accent,68));
            c.drawLine(w*pts[i][0],h*pts[i][1],w*pts[i+1][0],h*pts[i+1][1],p);
        }
        p.setStyle(Paint.Style.FILL);
        for(float[] pt:pts) { p.setColor(a(accent,90)); c.drawCircle(w*pt[0],h*pt[1],dp(2.4f),p); }
    }

    private void lanternLine(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1)); p.setColor(a(accent,55)); c.drawLine(w*.04f,h*.12f,w*.96f,h*.12f,p);
        p.setStyle(Paint.Style.FILL);
        for(int i=0;i<8;i++) {
            float x=w*(.08f+i*.12f);
            p.setColor(a(i%2==0?soft:accent,48)); c.drawOval(new RectF(x-w*.023f,h*.14f,x+w*.023f,h*.22f),p);
        }
    }

    private void pebbleBand(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<18;i++) {
            float x=w*((i*31%93)/100f),y=h*((i*47%89)/100f),rw=w*(.018f+(i%4)*.008f),rh=h*(.012f+(i%3)*.009f);
            p.setColor(a(i%2==0?soft:accent,34)); c.drawOval(new RectF(x-rw,y-rh,x+rw,y+rh),p);
        }
    }

    private void circuitRail(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.2f));
        for(int i=0;i<5;i++) {
            float y=h*(.13f+i*.17f);
            path.reset(); path.moveTo(w*.04f,y); path.lineTo(w*(.23f+i*.04f),y); path.lineTo(w*(.29f+i*.04f),y+h*.05f); path.lineTo(w*.95f,y+h*.05f);
            p.setColor(a(i%2==0?soft:accent,54)); c.drawPath(path,p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void gardenFrame(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.6f)); p.setColor(a(accent,54));
        path.reset(); path.moveTo(w*.03f,h*.9f); path.cubicTo(w*.14f,h*.7f,w*.04f,h*.35f,w*.2f,h*.08f); c.drawPath(path,p);
        path.reset(); path.moveTo(w*.97f,h*.1f); path.cubicTo(w*.86f,h*.3f,w*.96f,h*.65f,w*.8f,h*.92f); c.drawPath(path,p);
        p.setStyle(Paint.Style.FILL);
    }

    private void velvetBands(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<8;i++) {
            float l=w*i*.125f;
            p.setShader(new LinearGradient(l,0,l+w*.13f,0,a(i%2==0?soft:accent,30),Color.TRANSPARENT,Shader.TileMode.CLAMP));
            c.drawRect(l,0,l+w*.13f,h,p);
        }
        p.setShader(null);
    }

    private void sunsetDisc(Canvas c,float w,float h,int soft,int accent) {
        for(int i=4;i>=1;i--) {
            p.setColor(a(i%2==0?soft:accent,18+i*7));
            c.drawCircle(w*.79f,h*.21f,Math.min(w,h)*(.035f+i*.03f),p);
        }
    }

    private void auroraSweep(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(5)); p.setStrokeCap(Paint.Cap.ROUND);
        for(int i=0;i<3;i++) {
            path.reset(); path.moveTo(-w*.05f,h*(.18f+i*.2f)); path.cubicTo(w*.27f,h*(.02f+i*.13f),w*.62f,h*(.34f+i*.12f),w*1.05f,h*(.15f+i*.18f));
            p.setColor(a(i%2==0?soft:accent,34+i*5)); c.drawPath(path,p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void quiltDiamonds(Canvas c,float w,float h,int soft,int accent) {
        for(int y=0;y<5;y++) for(int x=0;x<8;x++) {
            float cx=w*(.06f+x*.125f),cy=h*(.08f+y*.20f),sx=w*.03f,sy=h*.04f;
            path.reset(); path.moveTo(cx,cy-sy); path.lineTo(cx+sx,cy); path.lineTo(cx,cy+sy); path.lineTo(cx-sx,cy); path.close();
            p.setColor(a((x+y)%2==0?soft:accent,24)); c.drawPath(path,p);
        }
    }

    private void skyline(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<14;i++) {
            float l=w*i/14f,t=h*(.52f-((i*37)%26)/100f);
            p.setColor(a(i%2==0?soft:accent,32)); c.drawRect(l,t,l+w*.06f,h*.93f,p);
        }
    }

    private void facets(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<10;i++) {
            float x=w*((i*29%91)/100f),y=h*((i*43%83)/100f),s=Math.min(w,h)*(.04f+(i%3)*.016f);
            path.reset(); path.moveTo(x,y-s); path.lineTo(x+s*.8f,y-s*.15f); path.lineTo(x+s*.45f,y+s*.8f); path.lineTo(x-s*.55f,y+s*.65f); path.lineTo(x-s*.85f,y-s*.15f); path.close();
            p.setColor(a(i%2==0?soft:accent,26)); c.drawPath(path,p);
        }
    }

    private void zenFrame(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1));
        for(int i=0;i<6;i++) {
            p.setColor(a(i%2==0?soft:accent,38));
            float in=Math.min(w,h)*(.022f+i*.017f);
            c.drawRoundRect(new RectF(w*.05f+in,h*.08f+in,w*.95f-in,h*.92f-in),dp(18),dp(18),p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void petals(Canvas c,float w,float h,int accent,int soft) {
        for(int i=0;i<18;i++) {
            float x=w*((i*37%96)/100f),y=h*((i*61%91)/100f);
            p.setColor(a(i%2==0?accent:soft,60));
            c.save(); c.rotate((i*29)%80-40,x,y);
            c.drawOval(new RectF(x-dp(2),y-dp(6),x+dp(2),y+dp(6)),p); c.restore();
        }
    }

    private void moonStars(Canvas c,float w,float h,int soft,int accent) {
        p.setColor(a(soft,92)); c.drawCircle(w*.82f,h*.18f,Math.min(w,h)*.08f,p);
        p.setColor(a(e.dark?e.start:Color.WHITE,205)); c.drawCircle(w*.85f,h*.15f,Math.min(w,h)*.071f,p);
        for(int i=0;i<15;i++) { p.setColor(a(i%2==0?soft:accent,78)); c.drawCircle(w*((i*23%93)/100f),h*((i*47%87)/100f),dp(1+i%2),p); }
    }

    private void bubbles(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1));
        for(int i=0;i<12;i++) { p.setColor(a(i%2==0?soft:accent,64)); c.drawCircle(w*((i*31%94)/100f),h*((i*43%90)/100f),dp(3+i%4),p); }
        p.setStyle(Paint.Style.FILL);
    }

    private void leaves(Canvas c,float w,float h,int deep,int soft) {
        for(int i=0;i<13;i++) {
            float x=w*((i*41%95)/100f),y=h*((i*53%91)/100f);
            p.setColor(a(i%2==0?deep:soft,54)); c.save(); c.rotate((i*31)%120-60,x,y);
            c.drawOval(new RectF(x-dp(3),y-dp(8),x+dp(3),y+dp(8)),p); c.restore();
        }
    }

    private void dunes(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2.4f));
        for(int i=0;i<3;i++) {
            float y=h*(.62f+i*.12f);
            path.reset(); path.moveTo(-w*.05f,y); path.cubicTo(w*.25f,y-h*.15f,w*.45f,y+h*.08f,w*.68f,y-h*.05f); path.cubicTo(w*.85f,y-h*.12f,w*.95f,y+h*.03f,w*1.05f,y-h*.04f);
            p.setColor(a(i%2==0?soft:accent,44)); c.drawPath(path,p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void snow(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1));
        for(int i=0;i<12;i++) {
            float x=w*((i*37%94)/100f),y=h*((i*59%88)/100f),r=dp(4+i%4);
            p.setColor(a(i%2==0?soft:accent,66));
            for(int j=0;j<3;j++) {
                double ang=Math.PI*j/3.0;
                c.drawLine(x-(float)Math.cos(ang)*r,y-(float)Math.sin(ang)*r,x+(float)Math.cos(ang)*r,y+(float)Math.sin(ang)*r,p);
            }
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void candy(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<13;i++) {
            float x=w*((i*29%93)/100f),y=h*((i*47%90)/100f),r=dp(3+i%4);
            p.setColor(a(i%2==0?soft:accent,56)); c.drawCircle(x,y,r,p);
            p.setColor(a(Color.WHITE,50)); c.drawRect(x-r*.7f,y-dp(1),x+r*.7f,y+dp(1),p);
        }
    }

    private void circuitWorld(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1));
        for(int i=0;i<7;i++) {
            float y=h*(.12f+i*.12f); p.setColor(a(i%2==0?soft:accent,62));
            c.drawLine(w*.06f,y,w*.34f,y,p); c.drawLine(w*.34f,y,w*.42f,y+h*.05f,p); c.drawLine(w*.42f,y+h*.05f,w*.94f,y+h*.05f,p); c.drawCircle(w*.94f,y+h*.05f,dp(2),p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void cosmos(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<28;i++) { p.setColor(a(i%3==0?accent:soft,60+i%3*12)); c.drawCircle(w*((i*37%97)/100f),h*((i*67%91)/100f),dp(1+i%3),p); }
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.2f)); p.setColor(a(accent,70)); c.drawOval(new RectF(w*.12f,h*.18f,w*.34f,h*.31f),p); p.setStyle(Paint.Style.FILL);
    }

    private void coffee(Canvas c,float w,float h,int soft,int deep) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.7f));
        for(int i=0;i<7;i++) { float x=w*(.1f+i*.14f),y=h*((i%2==0)?.2f:.75f),r=dp(6+i%3); p.setColor(a(i%2==0?soft:deep,44)); c.drawCircle(x,y,r,p); }
        p.setStyle(Paint.Style.FILL);
    }

    private void botanical(Canvas c,float w,float h,int soft,int deep) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1)); p.setColor(a(deep,58));
        for(int i=0;i<4;i++) { float x=w*(.10f+i*.25f); c.drawLine(x,h*.08f,x+w*.05f,h*.30f,p); c.drawLine(x+w*.05f,h*.30f,x+w*.02f,h*.48f,p); }
        p.setStyle(Paint.Style.FILL);
        for(int i=0;i<9;i++) { float x=w*((i*17%90)/100f),y=h*((i*43%82)/100f); p.setColor(a(soft,52)); c.drawOval(new RectF(x,y,x+dp(7),y+dp(13)),p); }
    }

    private void crystal(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<10;i++) {
            float x=w*((i*31%92)/100f),y=h*((i*57%87)/100f),s=dp(7+i%4);
            path.reset(); path.moveTo(x,y-s); path.lineTo(x+s*.55f,y-s*.2f); path.lineTo(x+s*.35f,y+s); path.lineTo(x-s*.35f,y+s); path.lineTo(x-s*.55f,y-s*.2f); path.close();
            p.setColor(a(i%2==0?soft:accent,42)); c.drawPath(path,p);
        }
    }

    private void lava(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<8;i++) { float x=w*((i*29%93)/100f),y=h*((i*47%90)/100f),r=dp(6+i%5); p.setColor(a(i%2==0?soft:accent,38)); c.drawCircle(x,y,r,p); c.drawCircle(x+r*.7f,y-r*.3f,r*.55f,p); }
    }

    private void rain(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.2f)); p.setStrokeCap(Paint.Cap.ROUND);
        for(int i=0;i<22;i++) { float x=w*((i*31%97)/100f),y=h*((i*43%88)/100f); p.setColor(a(i%2==0?soft:accent,62)); c.drawLine(x,y,x-dp(3),y+dp(10+i%4),p); }
        p.setStyle(Paint.Style.FILL);
    }

    private void aurora(Canvas c,float w,float h,int soft,int accent) { auroraSweep(c,w,h,soft,accent); }

    private void sunset(Canvas c,float w,float h,int soft,int accent) {
        p.setColor(a(accent,54)); c.drawCircle(w*.78f,h*.18f,dp(18),p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.6f));
        for(int i=0;i<4;i++) { p.setColor(a(i%2==0?soft:accent,38)); c.drawLine(0,h*(.65f+i*.07f),w,h*(.65f+i*.07f),p); }
        p.setStyle(Paint.Style.FILL);
    }

    private void city(Canvas c,float w,float h,int soft,int accent) { skyline(c,w,h,soft,accent); }

    private void pixel(Canvas c,float w,float h,int soft,int accent) {
        float s=dp(4.5f);
        for(int i=0;i<30;i++) { float x=w*((i*17%96)/100f),y=h*((i*37%91)/100f); p.setColor(a(i%2==0?soft:accent,50)); c.drawRect(x,y,x+s,y+s,p); }
    }

    private void paper(Canvas c,float w,float h,int soft,int accent) {
        p.setColor(a(soft,34)); for(int i=1;i<8;i++) c.drawRect(0,h*i/8f,w,h*i/8f+dp(1),p);
        p.setColor(a(accent,50)); c.save(); c.rotate(-10,w*.18f,h*.14f); c.drawRoundRect(new RectF(w*.08f,h*.10f,w*.30f,h*.16f),dp(2),dp(2),p); c.restore();
    }

    private void velvet(Canvas c,float w,float h,int soft,int accent) { velvetBands(c,w,h,soft,accent); }

    private void marble(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1));
        for(int i=0;i<7;i++) { path.reset(); path.moveTo(-w*.05f,h*(.12f+i*.12f)); path.cubicTo(w*.24f,h*(.05f+i*.15f),w*.56f,h*(.26f+i*.08f),w*1.05f,h*(.09f+i*.13f)); p.setColor(a(i%2==0?soft:accent,40)); c.drawPath(path,p); }
        p.setStyle(Paint.Style.FILL);
    }

    private void glass(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1)); p.setColor(a(Color.WHITE,e.dark?72:130));
        for(int i=0;i<4;i++) c.drawRoundRect(new RectF(w*(.05f+i*.05f),h*(.08f+i*.04f),w*(.42f+i*.10f),h*(.35f+i*.08f)),dp(11),dp(11),p);
        p.setStyle(Paint.Style.FILL);
    }

    private void neon(Canvas c,float w,float h,int soft,int accent) {
        p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeWidth(dp(2.7f));
        for(int i=0;i<6;i++) { p.setColor(a(i%2==0?soft:accent,90)); float y=h*(.12f+i*.15f); c.drawLine(w*.08f,y,w*(.28f+i*.11f),y,p); }
    }

    private void ink(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<9;i++) { float x=w*((i*31%91)/100f),y=h*((i*47%84)/100f),r=dp(4+i%5); p.setColor(a(i%2==0?soft:accent,40)); c.drawCircle(x,y,r,p); for(int j=0;j<3;j++) c.drawCircle(x+dp((j-1)*(6+i%3)),y+dp((j%2)*5),r*.45f,p); }
    }

    private void meadow(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<16;i++) { float x=w*((i*31%94)/100f),y=h*((i*53%88)/100f); p.setColor(a(i%2==0?soft:accent,62)); c.drawCircle(x,y,dp(2+i%2),p); c.drawRect(x-dp(.5f),y+dp(2),x+dp(.5f),y+dp(9),p); }
    }

    private void coral(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.8f));
        for(int i=0;i<5;i++) { float x=w*(.12f+i*.19f); path.reset(); path.moveTo(x,h*.86f); path.cubicTo(x-dp(8),h*.70f,x+dp(9),h*.52f,x,h*.34f); p.setColor(a(i%2==0?soft:accent,54)); c.drawPath(path,p); }
        p.setStyle(Paint.Style.FILL);
    }

    private void storm(Canvas c,float w,float h,int soft,int accent) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.8f));
        for(int i=0;i<4;i++) { float x=w*(.18f+i*.22f); path.reset(); path.moveTo(x,h*.18f); path.lineTo(x-dp(8),h*.42f); path.lineTo(x+dp(3),h*.42f); path.lineTo(x-dp(5),h*.68f); p.setColor(a(i%2==0?soft:accent,62)); c.drawPath(path,p); }
        p.setStyle(Paint.Style.FILL);
    }

    private void pumpkin(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<7;i++) { float x=w*(.1f+i*.14f),y=h*((i%2==0)?.2f:.78f); p.setColor(a(i%2==0?soft:accent,48)); c.drawOval(new RectF(x-dp(8),y-dp(6),x+dp(8),y+dp(6)),p); p.setColor(a(accent,56)); c.drawRect(x-dp(1),y-dp(10),x+dp(1),y-dp(6),p); }
    }

    private void winter(Canvas c,float w,float h,int soft,int accent) {
        snow(c,w,h,soft,accent);
        p.setColor(a(Color.WHITE,e.dark?60:105)); for(int i=0;i<18;i++) c.drawCircle(w*((i*41%97)/100f),h*((i*59%91)/100f),dp(1+i%2),p);
    }

    private void festival(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<24;i++) { float x=w*((i*37%97)/100f),y=h*((i*53%91)/100f); p.setColor(a(i%2==0?soft:accent,70)); c.save(); c.rotate((i*23)%90,x,y); c.drawRect(x-dp(1),y-dp(4),x+dp(1),y+dp(4),p); c.restore(); }
    }

    private void galaxy(Canvas c,float w,float h,int soft,int accent) {
        cosmos(c,w,h,soft,accent);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.3f)); p.setColor(a(accent,64)); c.drawOval(new RectF(w*.62f,h*.16f,w*.90f,h*.30f),p); p.setStyle(Paint.Style.FILL);
    }

    private void orchard(Canvas c,float w,float h,int soft,int accent) {
        for(int i=0;i<10;i++) { float x=w*((i*31%91)/100f),y=h*((i*47%84)/100f); p.setColor(a(i%2==0?soft:accent,54)); c.drawCircle(x,y,dp(5+i%3),p); p.setColor(a(accent,48)); c.drawOval(new RectF(x+dp(3),y-dp(8),x+dp(8),y-dp(3)),p); }
    }

    private int a(int color,int amount) {
        int aa=Math.max(0,Math.min(255,amount*alpha/255));
        return Color.argb(aa,Color.red(color),Color.green(color),Color.blue(color));
    }

    private float dp(float v) { return v*d; }

    @Override public void setAlpha(int value) { alpha=Math.max(0,Math.min(255,value)); invalidateSelf(); }
    @Override public void setColorFilter(android.graphics.ColorFilter filter) { p.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}


final class NeoThemeKeyDrawable extends Drawable {

    private final NeoThemeCatalog.Entry e;
    private final boolean special;
    private final boolean space;
    private final boolean pressed;
    private final float d;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();

    private NeoThemeKeyDrawable(Context context,int pack,boolean special,boolean space,boolean pressed) {
        e=NeoThemeCatalog.get(pack);
        this.special=special;
        this.space=space;
        this.pressed=pressed;
        d=context.getResources().getDisplayMetrics().density;
    }

    static StateListDrawable state(Context context,int pack,boolean special,boolean space) {
        StateListDrawable state=new StateListDrawable();
        state.addState(new int[]{android.R.attr.state_pressed},new NeoThemeKeyDrawable(context,pack,special,space,true));
        state.addState(new int[]{},new NeoThemeKeyDrawable(context,pack,special,space,false));
        return state;
    }

    @Override public void draw(Canvas c) {
        if(e==null) return;

        Rect b=getBounds();
        float w=b.width(),h=b.height();
        if(w<=0 || h<=0) return;

        int start=e.dark
                ? NeoThemeCatalog.mix(e.start,Color.WHITE,special||space?18:9)
                : NeoThemeCatalog.mix(e.end,e.accent,special||space?18:6);

        int end=e.dark
                ? NeoThemeCatalog.mix(e.end,e.accent,special||space?26:13)
                : NeoThemeCatalog.mix(e.start,Color.WHITE,special||space?42:68);

        if(pressed) {
            start=NeoThemeCatalog.mix(start,e.accent,28);
            end=NeoThemeCatalog.mix(end,Color.WHITE,16);
        }

        int opacity=pressed?230:(e.dark?198:220);
        start=Color.argb(opacity,Color.red(start),Color.green(start),Color.blue(start));
        end=Color.argb(opacity,Color.red(end),Color.green(end),Color.blue(end));

        p.setShader(new LinearGradient(0,0,w,h,start,end,Shader.TileMode.CLAMP));
        p.setStyle(Paint.Style.FILL);

        RectF r=new RectF(dp(1),dp(1),w-dp(1),h-dp(1));
        float radius=dp(Math.max(4,Math.min(24,e.corner)));

        switch(e.keyMode) {
            case 1: c.drawRoundRect(r,radius,radius,p); break;
            case 2: c.drawRoundRect(r,h*.48f,h*.48f,p); break;
            case 3: cut(r,Math.min(w,h)*.16f); c.drawPath(path,p); break;
            case 4: c.drawRoundRect(r,radius*.65f,radius*.65f,p); break;
            case 5: c.drawRoundRect(r,radius,radius,p); rail(c,r,true); break;
            case 6: gem(r,Math.min(w,h)*.14f); c.drawPath(path,p); break;
            case 7: ticket(r,Math.min(w,h)*.12f); c.drawPath(path,p); break;
            case 8: c.drawRoundRect(r,radius,radius,p); inner(c,r); break;
            case 9: notch(r,Math.min(w,h)*.13f); c.drawPath(path,p); break;
            case 10: c.drawRoundRect(r,radius*.75f,radius*.75f,p); rail(c,r,false); break;
            case 11: split(c,r,start,end); break;
            case 12: c.drawOval(r,p); break;
            case 13: c.drawRoundRect(r,radius,radius,p); dots(c,r); break;
            case 14: diamond(r,Math.min(w,h)*.10f); c.drawPath(path,p); break;
            case 15: c.drawRoundRect(r,radius,radius,p); inset(c,r); break;
            default: stepped(r,Math.min(w,h)*.12f); c.drawPath(path,p); break;
        }

        p.setShader(null);

        if(e.borders) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(pressed?1.5f:1f));
            p.setColor(Color.argb(e.dark?112:90,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent)));
            c.drawRoundRect(r,Math.min(radius,h*.42f),Math.min(radius,h*.42f),p);
            p.setStyle(Paint.Style.FILL);
        }

        if(space) {
            p.setColor(Color.argb(e.dark?125:108,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent)));
            float cy=h*.5f;
            c.drawRoundRect(new RectF(w*.38f,cy-dp(1.4f),w*.62f,cy+dp(1.4f)),dp(2),dp(2),p);
        }
    }

    private void cut(RectF r,float x) {
        path.reset(); path.moveTo(r.left+x,r.top); path.lineTo(r.right-x,r.top); path.lineTo(r.right,r.top+x); path.lineTo(r.right,r.bottom-x); path.lineTo(r.right-x,r.bottom); path.lineTo(r.left+x,r.bottom); path.lineTo(r.left,r.bottom-x); path.lineTo(r.left,r.top+x); path.close();
    }

    private void gem(RectF r,float x) {
        path.reset(); path.moveTo(r.left+x,r.top); path.lineTo(r.right-x,r.top); path.lineTo(r.right,r.centerY()); path.lineTo(r.right-x,r.bottom); path.lineTo(r.left+x,r.bottom); path.lineTo(r.left,r.centerY()); path.close();
    }

    private void ticket(RectF r,float x) {
        path.reset(); path.moveTo(r.left+x,r.top); path.lineTo(r.right-x,r.top); path.quadTo(r.right,r.top,r.right,r.top+x); path.lineTo(r.right,r.centerY()-x*.45f); path.quadTo(r.right-x*.8f,r.centerY(),r.right,r.centerY()+x*.45f); path.lineTo(r.right,r.bottom-x); path.quadTo(r.right,r.bottom,r.right-x,r.bottom); path.lineTo(r.left+x,r.bottom); path.quadTo(r.left,r.bottom,r.left,r.bottom-x); path.lineTo(r.left,r.centerY()+x*.45f); path.quadTo(r.left+x*.8f,r.centerY(),r.left,r.centerY()-x*.45f); path.lineTo(r.left,r.top+x); path.quadTo(r.left,r.top,r.left+x,r.top); path.close();
    }

    private void notch(RectF r,float x) {
        path.reset(); path.moveTo(r.left+x,r.top); path.lineTo(r.centerX()-x,r.top); path.lineTo(r.centerX(),r.top+x*.65f); path.lineTo(r.centerX()+x,r.top); path.lineTo(r.right-x,r.top); path.quadTo(r.right,r.top,r.right,r.top+x); path.lineTo(r.right,r.bottom-x); path.quadTo(r.right,r.bottom,r.right-x,r.bottom); path.lineTo(r.left+x,r.bottom); path.quadTo(r.left,r.bottom,r.left,r.bottom-x); path.lineTo(r.left,r.top+x); path.quadTo(r.left,r.top,r.left+x,r.top); path.close();
    }

    private void diamond(RectF r,float x) {
        path.reset(); path.moveTo(r.left+x,r.top); path.lineTo(r.right-x,r.top); path.lineTo(r.right,r.centerY()); path.lineTo(r.right-x,r.bottom); path.lineTo(r.left+x,r.bottom); path.lineTo(r.left,r.centerY()); path.close();
    }

    private void stepped(RectF r,float x) {
        path.reset(); path.moveTo(r.left+x,r.top); path.lineTo(r.right-x,r.top); path.lineTo(r.right-x,r.top+x*.45f); path.lineTo(r.right,r.top+x*.45f); path.lineTo(r.right,r.bottom-x*.45f); path.lineTo(r.right-x,r.bottom-x*.45f); path.lineTo(r.right-x,r.bottom); path.lineTo(r.left+x,r.bottom); path.lineTo(r.left+x,r.bottom-x*.45f); path.lineTo(r.left,r.bottom-x*.45f); path.lineTo(r.left,r.top+x*.45f); path.lineTo(r.left+x,r.top+x*.45f); path.close();
    }

    private void rail(Canvas c,RectF r,boolean top) {
        p.setShader(null); p.setColor(Color.argb(118,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent)));
        float y=top?r.top+dp(2):r.bottom-dp(4);
        c.drawRoundRect(new RectF(r.left+dp(6),y,r.right-dp(6),y+dp(2)),dp(2),dp(2),p);
    }

    private void inner(Canvas c,RectF r) {
        p.setShader(null); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1)); p.setColor(Color.argb(74,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent)));
        c.drawRoundRect(new RectF(r.left+dp(3),r.top+dp(3),r.right-dp(3),r.bottom-dp(3)),dp(6),dp(6),p); p.setStyle(Paint.Style.FILL);
    }

    private void split(Canvas c,RectF r,int start,int end) {
        p.setShader(null); p.setColor(start); c.drawRoundRect(r,dp(8),dp(8),p);
        path.reset(); path.moveTo(r.centerX(),r.top); path.lineTo(r.right,r.top); path.lineTo(r.right,r.bottom); path.lineTo(r.centerX()-r.width()*.12f,r.bottom); path.close(); p.setColor(end); c.drawPath(path,p);
    }

    private void dots(Canvas c,RectF r) {
        p.setShader(null); p.setColor(Color.argb(100,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent)));
        c.drawCircle(r.left+dp(6),r.top+dp(6),dp(2),p); c.drawCircle(r.right-dp(6),r.bottom-dp(6),dp(2),p);
    }

    private void inset(Canvas c,RectF r) {
        p.setShader(null); p.setColor(Color.argb(42,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent)));
        c.drawRoundRect(new RectF(r.left+dp(4),r.top+dp(4),r.right-dp(4),r.bottom-dp(4)),dp(5),dp(5),p);
    }

    private float dp(float v) { return v*d; }

    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(android.graphics.ColorFilter filter) { p.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
