package com.keykii.neo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.PixelFormat;

final class ThemeSceneDrawable extends Drawable {

    private final Context context;
    private final int pack;
    private final int accent;
    private final boolean dark;
    private final float density;
    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);
    private final java.util.HashMap<Integer, Drawable> assets =
            new java.util.HashMap<>();
    private int globalAlpha = 255;

    ThemeSceneDrawable(
            Context context,
            int pack,
            int accent,
            boolean dark
    ) {
        this.context = context.getApplicationContext();
        this.pack = pack;
        this.accent = accent;
        this.dark = dark;
        this.density = context.getResources()
                .getDisplayMetrics().density;
    }

    @Override
    public void draw(Canvas canvas) {
        if (pack == 116)
            return;

        Rect bounds = getBounds();
        float w = bounds.width();
        float h = bounds.height();

        if (w <= 0 || h <= 0)
            return;

        canvas.save();
        canvas.translate(bounds.left, bounds.top);

        float s = Math.min(w, h);
        int soft = blend(accent, Color.WHITE, dark ? 28 : 58);
        int softer = blend(accent, Color.WHITE, dark ? 15 : 76);
        int deep = blend(accent, Color.BLACK, dark ? 12 : 22);
        int white = dark
                ? Color.rgb(238, 235, 248)
                : Color.WHITE;

        switch (pack) {
            case 100: // Cherry Blossom Love
                branch(canvas, w, h, accent, 92);
                asset(canvas, R.drawable.theme_motif_flower, soft, 230,
                        .01f*w, .01f*h, .25f*s);
                asset(canvas, R.drawable.theme_motif_flower, accent, 205,
                        .13f*w, .07f*h, .16f*s);
                asset(canvas, R.drawable.theme_motif_cherry, deep, 215,
                        .78f*w, .04f*h, .20f*s);
                asset(canvas, R.drawable.theme_motif_bow, soft, 220,
                        .76f*w, .72f*h, .20f*s);
                asset(canvas, R.drawable.theme_motif_heart, accent, 180,
                        .45f*w, .80f*h, .11f*s);
                petals(canvas, w, h, accent);
                break;

            case 101: // Blueberry Jelly Sky
                jellyBubbles(canvas, w, h, accent, soft);
                blueberryCluster(canvas, .09f*w, .15f*h, .07f*s, accent, deep);
                blueberryCluster(canvas, .82f*w, .70f*h, .065f*s, accent, deep);
                asset(canvas, R.drawable.theme_motif_cloud, white, 205,
                        .66f*w, .02f*h, .24f*s);
                asset(canvas, R.drawable.theme_motif_moonstar, soft, 215,
                        .02f*w, .70f*h, .17f*s);
                asset(canvas, R.drawable.theme_motif_star, white, 190,
                        .47f*w, .06f*h, .10f*s);
                break;

            case 102: // Matcha Bunny Café
                cafeStripes(canvas, w, h, soft, 52);
                asset(canvas, R.drawable.theme_motif_bunny, white, 230,
                        .01f*w, .03f*h, .26f*s);
                asset(canvas, R.drawable.theme_motif_cup, accent, 220,
                        .72f*w, .02f*h, .24f*s);
                asset(canvas, R.drawable.theme_motif_leaf, deep, 210,
                        .79f*w, .70f*h, .20f*s);
                asset(canvas, R.drawable.theme_motif_leaf, soft, 195,
                        .18f*w, .74f*h, .15f*s);
                steam(canvas, .80f*w, .10f*h, s, white);
                break;

            case 103: // Peach Teddy Dessert
                dessertDots(canvas, w, h, soft, accent);
                asset(canvas, R.drawable.theme_motif_bear, soft, 235,
                        .01f*w, .02f*h, .26f*s);
                asset(canvas, R.drawable.theme_motif_cake, white, 220,
                        .73f*w, .03f*h, .23f*s);
                asset(canvas, R.drawable.theme_motif_bear, accent, 200,
                        .76f*w, .70f*h, .20f*s);
                asset(canvas, R.drawable.theme_motif_heart, deep, 180,
                        .18f*w, .77f*h, .12f*s);
                frostingWave(canvas, w, h, white);
                break;

            case 104: // Lilac Butterfly Diary
                diaryLines(canvas, w, h, soft);
                asset(canvas, R.drawable.theme_motif_butterfly, accent, 235,
                        .01f*w, .02f*h, .22f*s);
                asset(canvas, R.drawable.theme_motif_bow, soft, 220,
                        .78f*w, .02f*h, .18f*s);
                asset(canvas, R.drawable.theme_motif_butterfly, deep, 210,
                        .75f*w, .69f*h, .23f*s);
                asset(canvas, R.drawable.theme_motif_flower, white, 210,
                        .10f*w, .73f*h, .18f*s);
                tapeCorner(canvas, .05f*w, .04f*h, .18f*s, white);
                break;

            case 105: // Midnight Neon Arcade
                arcadeGrid(canvas, w, h);
                asset(canvas, R.drawable.theme_motif_star,
                        Color.rgb(255, 63, 226), 235,
                        .02f*w, .03f*h, .20f*s);
                asset(canvas, R.drawable.theme_motif_moonstar,
                        Color.rgb(72, 231, 255), 240,
                        .77f*w, .02f*h, .22f*s);
                asset(canvas, R.drawable.theme_motif_bubble,
                        Color.rgb(255, 73, 236), 205,
                        .74f*w, .70f*h, .24f*s);
                neonBars(canvas, w, h);
                break;

            case 106: // Strawberry Ribbon Milk
                polka(canvas, w, h, Color.rgb(255,255,255), 64);
                asset(canvas, R.drawable.theme_motif_strawberry, accent, 235,
                        .01f*w, .03f*h, .24f*s);
                asset(canvas, R.drawable.theme_motif_bow, white, 230,
                        .76f*w, .02f*h, .23f*s);
                asset(canvas, R.drawable.theme_motif_strawberry, deep, 205,
                        .78f*w, .70f*h, .20f*s);
                asset(canvas, R.drawable.theme_motif_heart, soft, 210,
                        .12f*w, .73f*h, .15f*s);
                ribbon(canvas, w, h, accent);
                break;

            case 107: // Cloudy Moon Sleep
                stars(canvas, w, h, white, 115);
                asset(canvas, R.drawable.theme_motif_cloud, white, 220,
                        .00f*w, .04f*h, .27f*s);
                asset(canvas, R.drawable.theme_motif_moonstar, soft, 235,
                        .76f*w, .02f*h, .22f*s);
                asset(canvas, R.drawable.theme_motif_cloud, softer, 210,
                        .73f*w, .70f*h, .25f*s);
                asset(canvas, R.drawable.theme_motif_star, white, 195,
                        .12f*w, .74f*h, .14f*s);
                break;

            case 108: // Mint Frog Garden
                gardenVines(canvas, w, h, deep);
                asset(canvas, R.drawable.theme_motif_frog, white, 235,
                        .01f*w, .03f*h, .25f*s);
                asset(canvas, R.drawable.theme_motif_leaf, accent, 215,
                        .76f*w, .02f*h, .22f*s);
                asset(canvas, R.drawable.theme_motif_flower, white, 215,
                        .77f*w, .70f*h, .19f*s);
                asset(canvas, R.drawable.theme_motif_leaf, deep, 195,
                        .14f*w, .74f*h, .17f*s);
                break;

            case 109: // Rosy Bear Picnic
                gingham(canvas, w, h, soft, 58);
                asset(canvas, R.drawable.theme_motif_bear, soft, 235,
                        .01f*w, .02f*h, .25f*s);
                asset(canvas, R.drawable.theme_motif_bow, accent, 220,
                        .77f*w, .02f*h, .21f*s);
                asset(canvas, R.drawable.theme_motif_bear, white, 210,
                        .76f*w, .70f*h, .21f*s);
                asset(canvas, R.drawable.theme_motif_heart, deep, 190,
                        .15f*w, .74f*h, .14f*s);
                break;

            case 110: // Lavender Lace Dream
                lace(canvas, w, h, white, accent);
                asset(canvas, R.drawable.theme_motif_butterfly, accent, 235,
                        .01f*w, .03f*h, .22f*s);
                asset(canvas, R.drawable.theme_motif_flower, white, 225,
                        .76f*w, .02f*h, .21f*s);
                asset(canvas, R.drawable.theme_motif_bow, soft, 215,
                        .77f*w, .70f*h, .20f*s);
                asset(canvas, R.drawable.theme_motif_butterfly, deep, 195,
                        .13f*w, .73f*h, .16f*s);
                break;

            case 111: // Sakura Cherry Soda
                sodaBubbles(canvas, w, h, white, accent);
                asset(canvas, R.drawable.theme_motif_flower, soft, 230,
                        .01f*w, .02f*h, .23f*s);
                asset(canvas, R.drawable.theme_motif_cherry, accent, 225,
                        .77f*w, .02f*h, .21f*s);
                asset(canvas, R.drawable.theme_motif_cup, white, 215,
                        .76f*w, .70f*h, .22f*s);
                asset(canvas, R.drawable.theme_motif_cherry, deep, 190,
                        .13f*w, .74f*h, .16f*s);
                break;

            case 112: // Ocean Jelly Star
                oceanWaves(canvas, w, h, white, accent);
                asset(canvas, R.drawable.theme_motif_bubble, white, 220,
                        .01f*w, .03f*h, .24f*s);
                asset(canvas, R.drawable.theme_motif_star, soft, 230,
                        .77f*w, .02f*h, .20f*s);
                asset(canvas, R.drawable.theme_motif_bubble, accent, 205,
                        .76f*w, .69f*h, .24f*s);
                asset(canvas, R.drawable.theme_motif_moonstar, white, 190,
                        .13f*w, .73f*h, .16f*s);
                break;

            case 113: // Cozy Cocoa Bunny
                cafeStripes(canvas, w, h, soft, 40);
                asset(canvas, R.drawable.theme_motif_bunny, soft, 235,
                        .01f*w, .02f*h, .25f*s);
                asset(canvas, R.drawable.theme_motif_cup, white, 225,
                        .77f*w, .03f*h, .22f*s);
                asset(canvas, R.drawable.theme_motif_bear, accent, 205,
                        .75f*w, .69f*h, .23f*s);
                asset(canvas, R.drawable.theme_motif_heart, white, 190,
                        .13f*w, .74f*h, .15f*s);
                steam(canvas, .84f*w, .10f*h, s, white);
                break;

            case 114: // Pink Kitty Bow
                pawDots(canvas, w, h, soft, accent);
                asset(canvas, R.drawable.theme_motif_cat, white, 235,
                        .01f*w, .03f*h, .24f*s);
                asset(canvas, R.drawable.theme_motif_bow, accent, 230,
                        .77f*w, .02f*h, .22f*s);
                asset(canvas, R.drawable.theme_motif_cat, soft, 205,
                        .76f*w, .70f*h, .21f*s);
                asset(canvas, R.drawable.theme_motif_heart, deep, 190,
                        .13f*w, .74f*h, .15f*s);
                break;

            case 115: // Blue Porcelain Bloom
                porcelain(canvas, w, h, accent, soft);
                asset(canvas, R.drawable.theme_motif_flower, accent, 225,
                        .01f*w, .02f*h, .23f*s);
                asset(canvas, R.drawable.theme_motif_lotus, soft, 215,
                        .77f*w, .02f*h, .21f*s);
                asset(canvas, R.drawable.theme_motif_flower, deep, 195,
                        .76f*w, .70f*h, .20f*s);
                asset(canvas, R.drawable.theme_motif_star, accent, 175,
                        .14f*w, .74f*h, .13f*s);
                break;

            case 117: // Cream Heart Minimal
                elegantFrame(canvas, w, h, soft, accent);
                asset(canvas, R.drawable.theme_motif_heart, accent, 205,
                        .03f*w, .04f*h, .18f*s);
                asset(canvas, R.drawable.theme_motif_bow, soft, 205,
                        .80f*w, .04f*h, .17f*s);
                asset(canvas, R.drawable.theme_motif_flower, white, 190,
                        .78f*w, .72f*h, .17f*s);
                asset(canvas, R.drawable.theme_motif_heart, deep, 165,
                        .15f*w, .76f*h, .11f*s);
                break;

            case 118: // Brown Butterfly Noir
                noirFrame(canvas, w, h, accent);
                asset(canvas, R.drawable.theme_motif_butterfly, accent, 240,
                        .01f*w, .02f*h, .23f*s);
                asset(canvas, R.drawable.theme_motif_star, soft, 220,
                        .78f*w, .03f*h, .18f*s);
                asset(canvas, R.drawable.theme_motif_butterfly, soft, 215,
                        .75f*w, .69f*h, .23f*s);
                asset(canvas, R.drawable.theme_motif_heart, accent, 185,
                        .14f*w, .74f*h, .13f*s);
                break;

            case 119: // Snowy Pastel Christmas
                snow(canvas, w, h, white);
                asset(canvas, R.drawable.theme_motif_snow, white, 235,
                        .01f*w, .02f*h, .23f*s);
                asset(canvas, R.drawable.theme_motif_gift, accent, 225,
                        .77f*w, .02f*h, .21f*s);
                asset(canvas, R.drawable.theme_motif_bow, soft, 215,
                        .77f*w, .70f*h, .20f*s);
                asset(canvas, R.drawable.theme_motif_snow, white, 190,
                        .13f*w, .73f*h, .15f*s);
                break;
        }

        canvas.restore();
    }

    private void asset(
            Canvas canvas,
            int res,
            int tint,
            int alpha,
            float x,
            float y,
            float size
    ) {
        Drawable drawable = assets.get(res);

        if (drawable == null) {
            try {
                drawable = context.getDrawable(res);

                if (drawable != null) {
                    drawable = drawable.mutate();
                    assets.put(res, drawable);
                }
            } catch (Throwable ignored) {
                drawable = null;
            }
        }

        if (drawable == null)
            return;

        drawable.setTint(tint);
        drawable.setAlpha(alpha(alpha));

        int left = Math.round(x);
        int top = Math.round(y);
        int side = Math.max(1, Math.round(size));

        drawable.setBounds(
                left,
                top,
                left + side,
                top + side
        );

        drawable.draw(canvas);
    }

    private void branch(
            Canvas canvas,
            float w,
            float h,
            int color,
            int alpha
    ) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(dp(1), h*.010f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(withAlpha(color, alpha));

        Path path = new Path();
        path.moveTo(-w*.02f, h*.22f);
        path.cubicTo(
                w*.14f, h*.08f,
                w*.28f, h*.03f,
                w*.43f, h*.10f
        );
        canvas.drawPath(path, paint);

        path.reset();
        path.moveTo(w*.78f, h*.86f);
        path.cubicTo(
                w*.88f, h*.78f,
                w*.95f, h*.72f,
                w*1.03f, h*.62f
        );
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void petals(
            Canvas canvas,
            float w,
            float h,
            int color
    ) {
        paint.setColor(withAlpha(color, 62));
        for (int i=0;i<9;i++) {
            float x = w*(.12f + (i%5)*.18f);
            float y = h*(i<5 ? .14f : .82f);
            RectF oval = new RectF(
                    x-dp(3),
                    y-dp(7),
                    x+dp(3),
                    y+dp(7)
            );
            canvas.save();
            canvas.rotate(i%2==0 ? 32 : -32, x, y);
            canvas.drawOval(oval, paint);
            canvas.restore();
        }
    }

    private void blueberryCluster(
            Canvas canvas,
            float x,
            float y,
            float r,
            int color,
            int deep
    ) {
        paint.setColor(withAlpha(color, 150));
        canvas.drawCircle(x, y, r, paint);
        canvas.drawCircle(x+r*.95f, y+r*.20f, r*.82f, paint);
        canvas.drawCircle(x+r*.42f, y+r*.88f, r*.76f, paint);
        paint.setColor(withAlpha(deep, 150));
        canvas.drawCircle(x-r*.12f, y-r*.12f, r*.18f, paint);
        canvas.drawCircle(x+r*.84f, y+r*.07f, r*.15f, paint);
    }

    private void jellyBubbles(
            Canvas canvas,
            float w,
            float h,
            int color,
            int soft
    ) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.5f));
        paint.setColor(withAlpha(Color.WHITE, dark ? 70 : 115));
        canvas.drawCircle(w*.29f,h*.16f,Math.min(w,h)*.055f,paint);
        canvas.drawCircle(w*.62f,h*.80f,Math.min(w,h)*.070f,paint);
        canvas.drawCircle(w*.90f,h*.46f,Math.min(w,h)*.040f,paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(soft,70));
        canvas.drawCircle(w*.44f,h*.10f,Math.min(w,h)*.035f,paint);
        canvas.drawCircle(w*.70f,h*.24f,Math.min(w,h)*.028f,paint);
    }

    private void cafeStripes(
            Canvas canvas,
            float w,
            float h,
            int color,
            int alpha
    ) {
        paint.setColor(withAlpha(color, alpha));
        float stripe = w*.055f;
        for(float x=-w*.2f;x<w*1.2f;x+=stripe*2f) {
            Path p = new Path();
            p.moveTo(x,0);
            p.lineTo(x+stripe,0);
            p.lineTo(x+stripe+w*.18f,h);
            p.lineTo(x+w*.18f,h);
            p.close();
            canvas.drawPath(p,paint);
        }
    }

    private void steam(
            Canvas canvas,
            float x,
            float y,
            float s,
            int color
    ) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.4f));
        paint.setColor(withAlpha(color,110));
        for(int i=0;i<3;i++) {
            Path p = new Path();
            float sx=x+i*s*.035f;
            p.moveTo(sx,y);
            p.cubicTo(
                    sx-s*.018f,y-s*.04f,
                    sx+s*.025f,y-s*.07f,
                    sx,y-s*.11f
            );
            canvas.drawPath(p,paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void dessertDots(
            Canvas canvas,
            float w,
            float h,
            int soft,
            int color
    ) {
        paint.setColor(withAlpha(soft,58));
        float s=Math.min(w,h);
        canvas.drawCircle(w*.34f,h*.13f,s*.045f,paint);
        canvas.drawCircle(w*.64f,h*.18f,s*.030f,paint);
        canvas.drawCircle(w*.56f,h*.83f,s*.050f,paint);
        paint.setColor(withAlpha(color,48));
        canvas.drawCircle(w*.91f,h*.48f,s*.036f,paint);
    }

    private void frostingWave(
            Canvas canvas,
            float w,
            float h,
            int color
    ) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(withAlpha(color,75));
        Path p=new Path();
        p.moveTo(0,h*.10f);
        for(int i=0;i<6;i++) {
            float x=w*i/5f;
            p.quadTo(
                    x+w*.05f,
                    h*(i%2==0 ? .17f : .05f),
                    x+w*.10f,
                    h*.10f
            );
        }
        canvas.drawPath(p,paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void diaryLines(
            Canvas canvas,
            float w,
            float h,
            int color
    ) {
        paint.setColor(withAlpha(color,42));
        paint.setStrokeWidth(dp(1));
        for(int i=1;i<8;i++) {
            float y=h*i/8f;
            canvas.drawLine(w*.04f,y,w*.96f,y,paint);
        }
        paint.setColor(withAlpha(accent,70));
        canvas.drawLine(w*.12f,h*.02f,w*.12f,h*.98f,paint);
    }

    private void tapeCorner(
            Canvas canvas,
            float x,
            float y,
            float size,
            int color
    ) {
        paint.setColor(withAlpha(color,75));
        canvas.save();
        canvas.rotate(-15,x,y);
        canvas.drawRoundRect(
                new RectF(x-size*.50f,y-size*.12f,x+size*.50f,y+size*.12f),
                dp(2),dp(2),paint
        );
        canvas.restore();
    }

    private void arcadeGrid(
            Canvas canvas,
            float w,
            float h
    ) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(withAlpha(Color.rgb(58,225,255),60));
        for(int i=0;i<7;i++) {
            float y=h*(.56f+i*.07f);
            canvas.drawLine(0,y,w,y,paint);
        }
        paint.setColor(withAlpha(Color.rgb(255,44,224),50));
        for(int i=-2;i<9;i++) {
            canvas.drawLine(
                    w*.50f,h*.48f,
                    w*(i*.14f),h,
                    paint
            );
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void neonBars(
            Canvas canvas,
            float w,
            float h
    ) {
        paint.setStrokeWidth(dp(3));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(withAlpha(Color.rgb(62,231,255),150));
        canvas.drawLine(w*.22f,h*.10f,w*.40f,h*.10f,paint);
        paint.setColor(withAlpha(Color.rgb(255,58,221),150));
        canvas.drawLine(w*.61f,h*.86f,w*.82f,h*.86f,paint);
    }

    private void polka(
            Canvas canvas,
            float w,
            float h,
            int color,
            int alpha
    ) {
        paint.setColor(withAlpha(color,alpha));
        float s=Math.min(w,h);
        for(int y=0;y<4;y++) {
            for(int x=0;x<7;x++) {
                if((x+y)%2==0)
                    canvas.drawCircle(
                            w*(.08f+x*.15f),
                            h*(.12f+y*.25f),
                            s*.012f,
                            paint
                    );
            }
        }
    }

    private void ribbon(
            Canvas canvas,
            float w,
            float h,
            int color
    ) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(withAlpha(color,70));
        Path p=new Path();
        p.moveTo(-w*.05f,h*.73f);
        p.cubicTo(w*.18f,h*.60f,w*.34f,h*.92f,w*.55f,h*.78f);
        p.cubicTo(w*.72f,h*.66f,w*.85f,h*.88f,w*1.05f,h*.73f);
        canvas.drawPath(p,paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void stars(
            Canvas canvas,
            float w,
            float h,
            int color,
            int alpha
    ) {
        paint.setColor(withAlpha(color,alpha));
        float s=Math.min(w,h);
        float[][] pts={{.31f,.10f},{.54f,.17f},{.68f,.10f},{.40f,.79f},{.62f,.84f},{.90f,.55f}};
        for(float[] pt:pts)
            canvas.drawCircle(w*pt[0],h*pt[1],s*.009f,paint);
    }

    private void gardenVines(
            Canvas canvas,
            float w,
            float h,
            int color
    ) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(withAlpha(color,72));
        Path left=new Path();
        left.moveTo(w*.02f,h*.92f);
        left.cubicTo(w*.10f,h*.72f,w*.04f,h*.42f,w*.18f,h*.18f);
        canvas.drawPath(left,paint);
        Path right=new Path();
        right.moveTo(w*.98f,h*.10f);
        right.cubicTo(w*.88f,h*.34f,w*.96f,h*.63f,w*.82f,h*.88f);
        canvas.drawPath(right,paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void gingham(
            Canvas canvas,
            float w,
            float h,
            int color,
            int alpha
    ) {
        paint.setColor(withAlpha(color,alpha));
        float step=Math.max(dp(14),w*.11f);
        for(float x=0;x<w;x+=step)
            canvas.drawRect(x,0,x+step*.40f,h,paint);
        for(float y=0;y<h;y+=step)
            canvas.drawRect(0,y,w,y+step*.40f,paint);
    }

    private void lace(
            Canvas canvas,
            float w,
            float h,
            int color,
            int accentColor
    ) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.2f));
        paint.setColor(withAlpha(color,105));
        float r=Math.min(w,h)*.035f;
        for(float x=r;x<w;x+=r*1.65f) {
            canvas.drawCircle(x,r*.60f,r,paint);
            canvas.drawCircle(x,h-r*.60f,r,paint);
        }
        paint.setColor(withAlpha(accentColor,75));
        canvas.drawLine(0,r*1.35f,w,r*1.35f,paint);
        canvas.drawLine(0,h-r*1.35f,w,h-r*1.35f,paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void sodaBubbles(
            Canvas canvas,
            float w,
            float h,
            int color,
            int color2
    ) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.2f));
        float s=Math.min(w,h);
        float[][] pts={{.22f,.13f,.035f},{.55f,.09f,.025f},{.69f,.22f,.045f},{.35f,.83f,.03f},{.61f,.78f,.05f},{.92f,.54f,.025f}};
        for(int i=0;i<pts.length;i++) {
            paint.setColor(withAlpha(i%2==0?color:color2,95));
            canvas.drawCircle(w*pts[i][0],h*pts[i][1],s*pts[i][2],paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void oceanWaves(
            Canvas canvas,
            float w,
            float h,
            int white,
            int color
    ) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.6f));
        for(int row=0;row<4;row++) {
            paint.setColor(withAlpha(row%2==0?white:color,70));
            Path p=new Path();
            float y=h*(.18f+row*.21f);
            p.moveTo(-w*.05f,y);
            for(int i=0;i<6;i++) {
                float x=w*i*.20f;
                p.quadTo(x+w*.05f,y-h*.035f,x+w*.10f,y);
                p.quadTo(x+w*.15f,y+h*.035f,x+w*.20f,y);
            }
            canvas.drawPath(p,paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void pawDots(
            Canvas canvas,
            float w,
            float h,
            int soft,
            int color
    ) {
        float s=Math.min(w,h);
        float[][] pts={{.31f,.13f},{.62f,.15f},{.42f,.82f},{.88f,.50f}};
        for(int i=0;i<pts.length;i++) {
            float x=w*pts[i][0], y=h*pts[i][1];
            paint.setColor(withAlpha(i%2==0?soft:color,55));
            canvas.drawCircle(x,y,s*.026f,paint);
            canvas.drawCircle(x-s*.025f,y-s*.030f,s*.010f,paint);
            canvas.drawCircle(x,y-s*.038f,s*.010f,paint);
            canvas.drawCircle(x+s*.025f,y-s*.030f,s*.010f,paint);
        }
    }

    private void porcelain(
            Canvas canvas,
            float w,
            float h,
            int color,
            int soft
    ) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.4f));
        paint.setColor(withAlpha(color,85));
        RectF frame=new RectF(w*.02f,h*.04f,w*.98f,h*.96f);
        canvas.drawRoundRect(frame,Math.min(w,h)*.08f,Math.min(w,h)*.08f,paint);
        Path p=new Path();
        p.moveTo(w*.18f,h*.10f);
        p.cubicTo(w*.30f,h*.22f,w*.36f,h*.04f,w*.48f,h*.15f);
        p.cubicTo(w*.60f,h*.26f,w*.70f,h*.08f,w*.82f,h*.19f);
        canvas.drawPath(p,paint);
        paint.setColor(withAlpha(soft,70));
        canvas.drawCircle(w*.50f,h*.86f,Math.min(w,h)*.055f,paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void elegantFrame(
            Canvas canvas,
            float w,
            float h,
            int soft,
            int color
    ) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.3f));
        paint.setColor(withAlpha(color,65));
        canvas.drawRoundRect(
                new RectF(w*.025f,h*.045f,w*.975f,h*.955f),
                Math.min(w,h)*.08f,
                Math.min(w,h)*.08f,
                paint
        );
        paint.setColor(withAlpha(soft,75));
        canvas.drawLine(w*.30f,h*.09f,w*.70f,h*.09f,paint);
        canvas.drawLine(w*.30f,h*.91f,w*.70f,h*.91f,paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void noirFrame(
            Canvas canvas,
            float w,
            float h,
            int color
    ) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(withAlpha(color,125));
        canvas.drawRoundRect(
                new RectF(w*.025f,h*.045f,w*.975f,h*.955f),
                Math.min(w,h)*.06f,
                Math.min(w,h)*.06f,
                paint
        );
        canvas.drawLine(w*.03f,h*.18f,w*.18f,h*.05f,paint);
        canvas.drawLine(w*.82f,h*.95f,w*.97f,h*.82f,paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void snow(
            Canvas canvas,
            float w,
            float h,
            int color
    ) {
        paint.setColor(withAlpha(color,125));
        float s=Math.min(w,h);
        for(int i=0;i<22;i++) {
            float x=w*((i*37%97)/100f);
            float y=h*((i*61%89)/100f);
            float r=s*(.006f+(i%3)*.003f);
            canvas.drawCircle(x,y,r,paint);
        }
    }

    private int blend(
            int a,
            int b,
            int bPercent
    ) {
        int p=Math.max(0,Math.min(100,bPercent));
        int ap=100-p;

        return Color.rgb(
                (Color.red(a)*ap+Color.red(b)*p)/100,
                (Color.green(a)*ap+Color.green(b)*p)/100,
                (Color.blue(a)*ap+Color.blue(b)*p)/100
        );
    }

    private int withAlpha(
            int color,
            int alpha
    ) {
        return Color.argb(
                alpha(alpha),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }

    private int alpha(int alpha) {
        return Math.max(
                0,
                Math.min(
                        255,
                        alpha*globalAlpha/255
                )
        );
    }

    private float dp(float value) {
        return value*density;
    }

    @Override
    public void setAlpha(int alpha) {
        globalAlpha=Math.max(0,Math.min(255,alpha));
        invalidateSelf();
    }

    @Override
    public void setColorFilter(
            android.graphics.ColorFilter colorFilter
    ) {
        paint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
