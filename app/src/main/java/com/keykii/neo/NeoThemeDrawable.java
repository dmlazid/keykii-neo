package com.keykii.neo;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

final class NeoThemeDrawable extends Drawable {
    private final NeoThemeCatalog.Entry e;
    private final float d;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private int alpha=255;

    NeoThemeDrawable(Context context,int pack){
        e=NeoThemeCatalog.get(pack);
        d=context.getResources().getDisplayMetrics().density;
    }

    @Override public void draw(Canvas c){
        if(e==null) return;
        Rect b=getBounds(); float w=b.width(),h=b.height(); if(w<=0||h<=0) return;
        c.save(); c.translate(b.left,b.top);
        drawBase(c,w,h);
        drawArchitecture(c,w,h,e.architecture);
        drawScene(c,w,h,e.scene);
        c.restore();
    }

    private void drawBase(Canvas c,float w,float h){
        int s=withAlpha(e.start,e.dark?210:190), en=withAlpha(e.end,e.dark?220:205);
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0,0,w,h,s,en,Shader.TileMode.CLAMP));
        c.drawRoundRect(new RectF(0,0,w,h),dp(24),dp(24),p);
        p.setShader(null);
    }

    private void drawArchitecture(Canvas c,float w,float h,int a){
        int soft=NeoThemeCatalog.mix(e.accent,Color.WHITE,e.dark?25:78);
        int deep=NeoThemeCatalog.mix(e.accent,Color.BLACK,e.dark?8:48);
        switch(a%48){
            case 0: floatingCandy(c,w,h,soft); break;
            case 1: scrapbook(c,w,h,soft,deep); break;
            case 2: splitMechanical(c,w,h,deep); break;
            case 3: cloudIslands(c,w,h,soft); break;
            case 4: bubbleField(c,w,h,soft); break;
            case 5: offsetBento(c,w,h,soft,deep); break;
            case 6: ticketRows(c,w,h,soft); break;
            case 7: glassShelves(c,w,h,soft); break;
            case 8: pixelConsole(c,w,h,deep); break;
            case 9: notebookTabs(c,w,h,soft,deep); break;
            case 10: petalCluster(c,w,h,soft); break;
            case 11: arcadeBlocks(c,w,h,deep); break;
            case 12: ribbonStacks(c,w,h,soft); break;
            case 13: asymmetricCards(c,w,h,soft,deep); break;
            case 14: jewelFacets(c,w,h,soft); break;
            case 15: plushBoard(c,w,h,soft); break;
            case 16: photoFrames(c,w,h,soft,deep); break;
            case 17: cityPanels(c,w,h,deep); break;
            case 18: orbitDeck(c,w,h,soft); break;
            case 19: mosaicTiles(c,w,h,soft,deep); break;
            case 20: cyberRails(c,w,h,deep); break;
            case 21: gardenTrellis(c,w,h,soft,deep); break;
            case 22: dessertBoard(c,w,h,soft); break;
            case 23: oceanPebbles(c,w,h,soft); break;
            case 24: luxuryPlaques(c,w,h,soft,deep); break;
            case 25: y2kChrome(c,w,h,soft); break;
            case 26: gothicWindows(c,w,h,deep); break;
            case 27: dreamBedroom(c,w,h,soft,deep); break;
            case 28: retroCassette(c,w,h,soft,deep); break;
            case 29: kawaiiRoom(c,w,h,soft,deep); break;
            case 30: spaceConsole(c,w,h,soft,deep); break;
            case 31: minimalLines(c,w,h,deep); break;
            case 32: lanternSteps(c,w,h,soft); break;
            case 33: mechanicalDeck(c,w,h,deep); break;
            case 34: polaroidWall(c,w,h,soft,deep); break;
            case 35: bookPage(c,w,h,soft,deep); break;
            case 36: neonCircuit(c,w,h,deep); break;
            case 37: frostedDock(c,w,h,soft); break;
            case 38: teddyShelf(c,w,h,soft,deep); break;
            case 39: flowerMarket(c,w,h,soft,deep); break;
            case 40: gameDesk(c,w,h,soft,deep); break;
            case 41: nightSkyline(c,w,h,deep); break;
            case 42: candyShop(c,w,h,soft); break;
            case 43: auroraGlass(c,w,h,soft); break;
            case 44: moonWindow(c,w,h,soft,deep); break;
            case 45: stickerParade(c,w,h,soft,deep); break;
            case 46: waveDeck(c,w,h,soft); break;
            default: zenStones(c,w,h,soft,deep); break;
        }
    }

    private void floatingCandy(Canvas c,float w,float h,int soft){
        fill(soft,54); for(int r=0;r<4;r++) for(int i=0;i<7;i++){ float x=w*(.08f+i*.135f)+(r%2)*w*.025f; float y=h*(.18f+r*.19f); c.drawRoundRect(new RectF(x-w*.045f,y-h*.055f,x+w*.045f,y+h*.055f),dp(16),dp(16),p); }
    }
    private void scrapbook(Canvas c,float w,float h,int soft,int deep){
        for(int i=0;i<7;i++){ float x=w*(.08f+(i%4)*.23f),y=h*(.08f+(i/4)*.40f); c.save(); c.rotate((i%2==0?-8:7),x,y); fill(i%2==0?soft:Color.WHITE,75); c.drawRect(x,y,x+w*.19f,y+h*.28f,p); fill(deep,38); c.drawRect(x+w*.025f,y+h*.03f,x+w*.165f,y+h*.17f,p); c.restore(); }
    }
    private void splitMechanical(Canvas c,float w,float h,int deep){
        fill(deep,74); c.drawRoundRect(new RectF(w*.03f,h*.10f,w*.47f,h*.92f),dp(18),dp(18),p); c.drawRoundRect(new RectF(w*.53f,h*.10f,w*.97f,h*.92f),dp(18),dp(18),p); stroke(e.accent,58,2); c.drawLine(w*.5f,h*.15f,w*.5f,h*.88f,p);
    }
    private void cloudIslands(Canvas c,float w,float h,int soft){
        fill(soft,48); for(int r=0;r<4;r++){ float y=h*(.18f+r*.19f); for(int i=0;i<3;i++){ float x=w*(.18f+i*.31f)+(r%2)*w*.08f; cloud(c,x,y,dp(16+r)); } }
    }
    private void bubbleField(Canvas c,float w,float h,int soft){
        fill(soft,45); for(int i=0;i<18;i++){ float x=w*(.07f+(i%6)*.17f),y=h*(.16f+(i/6)*.25f); c.drawCircle(x,y,dp(16+(i%3)*5),p); }
    }
    private void offsetBento(Canvas c,float w,float h,int soft,int deep){
        fill(soft,48); c.drawRoundRect(new RectF(w*.04f,h*.10f,w*.62f,h*.40f),dp(20),dp(20),p); c.drawRoundRect(new RectF(w*.67f,h*.10f,w*.96f,h*.60f),dp(20),dp(20),p); fill(deep,24); c.drawRoundRect(new RectF(w*.04f,h*.45f,w*.42f,h*.90f),dp(20),dp(20),p); c.drawRoundRect(new RectF(w*.47f,h*.65f,w*.96f,h*.90f),dp(20),dp(20),p);
    }
    private void ticketRows(Canvas c,float w,float h,int soft){
        fill(soft,44); for(int r=0;r<4;r++){ float y=h*(.15f+r*.19f); RectF rr=new RectF(w*.05f,y,w*.95f,y+h*.12f); c.drawRoundRect(rr,dp(12),dp(12),p); fill(e.accent,30); for(int i=1;i<9;i++) c.drawCircle(w*(.05f+i*.10f),rr.centerY(),dp(3),p); fill(soft,44); }
    }
    private void glassShelves(Canvas c,float w,float h,int soft){
        fill(Color.WHITE,e.dark?20:42); for(int r=0;r<4;r++){ float y=h*(.14f+r*.19f); c.drawRoundRect(new RectF(w*.04f,y,w*.96f,y+h*.13f),dp(14),dp(14),p); stroke(soft,54,1); c.drawLine(w*.08f,y+h*.13f,w*.92f,y+h*.13f,p); }
    }
    private void pixelConsole(Canvas c,float w,float h,int deep){
        fill(deep,78); c.drawRect(w*.03f,h*.08f,w*.97f,h*.92f,p); fill(e.accent,42); for(int y=0;y<5;y++) for(int x=0;x<9;x++) if(((x+y)&1)==0) c.drawRect(w*(.07f+x*.10f),h*(.13f+y*.15f),w*(.11f+x*.10f),h*(.18f+y*.15f),p);
    }
    private void notebookTabs(Canvas c,float w,float h,int soft,int deep){
        fill(Color.WHITE,e.dark?18:60); c.drawRoundRect(new RectF(w*.04f,h*.07f,w*.96f,h*.93f),dp(12),dp(12),p); stroke(deep,32,1); for(int i=0;i<10;i++) c.drawLine(w*.08f,h*(.16f+i*.075f),w*.92f,h*(.16f+i*.075f),p); fill(soft,66); for(int i=0;i<4;i++) c.drawRoundRect(new RectF(w*.08f+i*w*.20f,h*.04f,w*.19f+i*w*.20f,h*.13f),dp(8),dp(8),p);
    }
    private void petalCluster(Canvas c,float w,float h,int soft){
        fill(soft,46); for(int i=0;i<12;i++){ float x=w*(.12f+(i%4)*.25f),y=h*(.18f+(i/4)*.25f); for(int k=0;k<5;k++){ double a=k*Math.PI*2/5; c.drawOval(new RectF(x+(float)Math.cos(a)*dp(12)-dp(8),y+(float)Math.sin(a)*dp(12)-dp(5),x+(float)Math.cos(a)*dp(12)+dp(8),y+(float)Math.sin(a)*dp(12)+dp(5)),p); } }
    }
    private void arcadeBlocks(Canvas c,float w,float h,int deep){
        fill(deep,72); for(int r=0;r<4;r++) for(int i=0;i<8;i++){ float l=w*(.04f+i*.118f),t=h*(.13f+r*.19f); c.drawRoundRect(new RectF(l,t,l+w*.095f,t+h*.13f),dp(5),dp(5),p); fill(e.accent,36+(i%3)*8); c.drawRect(l+w*.02f,t+h*.02f,l+w*.075f,t+h*.045f,p); fill(deep,72); }
    }
    private void ribbonStacks(Canvas c,float w,float h,int soft){
        for(int r=0;r<4;r++){ fill(r%2==0?soft:e.accent,38); float y=h*(.12f+r*.20f); path.reset(); path.moveTo(w*.03f,y); path.lineTo(w*.90f,y); path.lineTo(w*.97f,y+h*.07f); path.lineTo(w*.90f,y+h*.14f); path.lineTo(w*.03f,y+h*.14f); path.lineTo(w*.10f,y+h*.07f); path.close(); c.drawPath(path,p); }
    }
    private void asymmetricCards(Canvas c,float w,float h,int soft,int deep){
        fill(soft,48); c.drawRoundRect(new RectF(w*.03f,h*.08f,w*.38f,h*.47f),dp(24),dp(24),p); c.drawRoundRect(new RectF(w*.42f,h*.08f,w*.97f,h*.30f),dp(12),dp(12),p); fill(deep,26); c.drawRoundRect(new RectF(w*.42f,h*.34f,w*.72f,h*.92f),dp(20),dp(20),p); c.drawRoundRect(new RectF(w*.76f,h*.34f,w*.97f,h*.92f),dp(30),dp(30),p);
    }
    private void jewelFacets(Canvas c,float w,float h,int soft){
        fill(soft,42); for(int i=0;i<14;i++){ float x=w*(.08f+(i%7)*.14f),y=h*(.18f+(i/7)*.36f); polygon(c,x,y,dp(20),6); }
    }
    private void plushBoard(Canvas c,float w,float h,int soft){
        fill(soft,50); c.drawRoundRect(new RectF(w*.03f,h*.08f,w*.97f,h*.92f),dp(42),dp(42),p); fill(Color.WHITE,e.dark?15:28); for(int i=0;i<7;i++) c.drawCircle(w*(.08f+i*.14f),h*.14f,dp(6),p); for(int i=0;i<7;i++) c.drawCircle(w*(.08f+i*.14f),h*.86f,dp(6),p);
    }
    private void photoFrames(Canvas c,float w,float h,int soft,int deep){
        for(int i=0;i<6;i++){ float x=w*(.06f+(i%3)*.31f),y=h*(.10f+(i/3)*.39f); fill(Color.WHITE,e.dark?45:90); c.drawRoundRect(new RectF(x,y,x+w*.26f,y+h*.31f),dp(8),dp(8),p); fill(i%2==0?soft:deep,40); c.drawRect(x+w*.025f,y+h*.025f,x+w*.235f,y+h*.21f,p); }
    }
    private void cityPanels(Canvas c,float w,float h,int deep){
        fill(deep,66); for(int i=0;i<9;i++){ float l=w*(.02f+i*.11f),top=h*(.16f-((i*3)%5)*.018f); c.drawRect(l,top,l+w*.095f,h*.92f,p); fill(e.accent,45); for(int y=0;y<5;y++) c.drawRect(l+w*.025f,top+h*(.04f+y*.11f),l+w*.045f,top+h*(.07f+y*.11f),p); fill(deep,66); }
    }
    private void orbitDeck(Canvas c,float w,float h,int soft){
        stroke(soft,62,2); c.drawOval(new RectF(w*.12f,h*.10f,w*.88f,h*.88f),p); c.drawOval(new RectF(w*.28f,h*.18f,w*.72f,h*.82f),p); fill(soft,52); for(int i=0;i<10;i++){ double a=i*Math.PI*2/10; c.drawCircle(w*.5f+(float)Math.cos(a)*w*.34f,h*.49f+(float)Math.sin(a)*h*.34f,dp(10),p); }
    }
    private void mosaicTiles(Canvas c,float w,float h,int soft,int deep){
        for(int r=0;r<5;r++) for(int i=0;i<8;i++){ fill(((r+i)&1)==0?soft:deep,34); float l=w*(.02f+i*.125f),t=h*(.08f+r*.17f); c.save(); c.rotate(((r+i)&1)==0?-3:3,l,t); c.drawRoundRect(new RectF(l,t,l+w*.11f,t+h*.145f),dp(8),dp(8),p); c.restore(); }
    }
    private void cyberRails(Canvas c,float w,float h,int deep){
        fill(deep,60); c.drawRoundRect(new RectF(w*.03f,h*.08f,w*.97f,h*.92f),dp(14),dp(14),p); stroke(e.accent,70,2); for(int r=0;r<5;r++){ float y=h*(.14f+r*.16f); path.reset(); path.moveTo(w*.05f,y); path.lineTo(w*.25f,y); path.lineTo(w*.32f,y+h*.05f); path.lineTo(w*.68f,y+h*.05f); path.lineTo(w*.75f,y); path.lineTo(w*.95f,y); c.drawPath(path,p); }
    }
    private void gardenTrellis(Canvas c,float w,float h,int soft,int deep){
        stroke(deep,32,2); for(int i=0;i<8;i++){ c.drawLine(w*(.05f+i*.13f),h*.08f,w*(.05f+i*.13f),h*.92f,p); } for(int i=0;i<6;i++) c.drawLine(w*.04f,h*(.1f+i*.16f),w*.96f,h*(.1f+i*.16f),p); fill(soft,58); for(int i=0;i<11;i++) c.drawCircle(w*(.07f+(i%6)*.17f),h*(.12f+(i/6)*.52f),dp(8),p);
    }
    private void dessertBoard(Canvas c,float w,float h,int soft){
        fill(soft,54); c.drawRoundRect(new RectF(w*.03f,h*.08f,w*.97f,h*.92f),dp(26),dp(26),p); fill(e.accent,44); for(int i=0;i<8;i++){ float x=w*(.09f+(i%4)*.24f),y=h*(.18f+(i/4)*.45f); c.drawCircle(x,y,dp(17),p); fill(Color.WHITE,e.dark?35:65); c.drawCircle(x,y-dp(4),dp(8),p); fill(e.accent,44); }
    }
    private void oceanPebbles(Canvas c,float w,float h,int soft){
        fill(soft,43); for(int i=0;i<16;i++){ float x=w*(.08f+(i%5)*.21f),y=h*(.15f+(i/5)*.22f); c.drawOval(new RectF(x-dp(18+(i%3)*4),y-dp(12),x+dp(18+(i%3)*4),y+dp(12)),p); }
    }
    private void luxuryPlaques(Canvas c,float w,float h,int soft,int deep){
        fill(deep,46); c.drawRoundRect(new RectF(w*.03f,h*.08f,w*.97f,h*.92f),dp(18),dp(18),p); stroke(soft,74,2); c.drawRoundRect(new RectF(w*.06f,h*.11f,w*.94f,h*.89f),dp(14),dp(14),p); for(int r=0;r<4;r++) c.drawLine(w*.09f,h*(.21f+r*.18f),w*.91f,h*(.21f+r*.18f),p);
    }
    private void y2kChrome(Canvas c,float w,float h,int soft){
        stroke(soft,70,3); for(int i=0;i<8;i++){ float x=w*(.08f+i*.12f); c.drawOval(new RectF(x-dp(18),h*.16f,x+dp(18),h*.84f),p); } fill(Color.WHITE,e.dark?18:34); c.drawRoundRect(new RectF(w*.12f,h*.36f,w*.88f,h*.64f),dp(36),dp(36),p);
    }
    private void gothicWindows(Canvas c,float w,float h,int deep){
        fill(deep,65); for(int i=0;i<5;i++){ float l=w*(.04f+i*.195f),r=l+w*.15f; path.reset(); path.moveTo(l,h*.90f); path.lineTo(l,h*.28f); path.quadTo((l+r)/2,h*.06f,r,h*.28f); path.lineTo(r,h*.90f); path.close(); c.drawPath(path,p); }
    }
    private void dreamBedroom(Canvas c,float w,float h,int soft,int deep){
        fill(soft,48); c.drawRoundRect(new RectF(w*.05f,h*.50f,w*.95f,h*.90f),dp(26),dp(26),p); fill(Color.WHITE,e.dark?22:44); c.drawRoundRect(new RectF(w*.08f,h*.54f,w*.45f,h*.72f),dp(20),dp(20),p); stroke(deep,35,2); c.drawRoundRect(new RectF(w*.60f,h*.10f,w*.92f,h*.44f),dp(20),dp(20),p); c.drawLine(w*.76f,h*.10f,w*.76f,h*.44f,p);
    }
    private void retroCassette(Canvas c,float w,float h,int soft,int deep){
        fill(soft,48); c.drawRoundRect(new RectF(w*.04f,h*.12f,w*.96f,h*.88f),dp(14),dp(14),p); fill(deep,50); c.drawRoundRect(new RectF(w*.16f,h*.24f,w*.84f,h*.54f),dp(12),dp(12),p); fill(soft,68); c.drawCircle(w*.35f,h*.39f,dp(26),p); c.drawCircle(w*.65f,h*.39f,dp(26),p); fill(deep,46); c.drawRoundRect(new RectF(w*.25f,h*.66f,w*.75f,h*.80f),dp(8),dp(8),p);
    }
    private void kawaiiRoom(Canvas c,float w,float h,int soft,int deep){
        fill(Color.WHITE,e.dark?14:32); c.drawRoundRect(new RectF(w*.04f,h*.08f,w*.96f,h*.92f),dp(28),dp(28),p); fill(soft,50); c.drawRoundRect(new RectF(w*.08f,h*.60f,w*.52f,h*.87f),dp(18),dp(18),p); c.drawRoundRect(new RectF(w*.65f,h*.20f,w*.90f,h*.55f),dp(20),dp(20),p); fill(deep,38); c.drawRect(w*.56f,h*.58f,w*.60f,h*.88f,p);
    }
    private void spaceConsole(Canvas c,float w,float h,int soft,int deep){
        fill(deep,62); c.drawRoundRect(new RectF(w*.03f,h*.08f,w*.97f,h*.92f),dp(24),dp(24),p); stroke(soft,60,2); c.drawOval(new RectF(w*.20f,h*.16f,w*.80f,h*.74f),p); fill(soft,64); for(int i=0;i<12;i++) c.drawCircle(w*(.08f+(i*7%89)/100f),h*(.12f+(i*13%73)/100f),dp(2+(i%3)),p); c.drawCircle(w*.74f,h*.28f,dp(25),p);
    }
    private void minimalLines(Canvas c,float w,float h,int deep){
        stroke(deep,34,1); for(int r=0;r<5;r++) c.drawLine(w*.07f,h*(.15f+r*.17f),w*.93f,h*(.15f+r*.17f),p); for(int i=0;i<7;i++) c.drawLine(w*(.08f+i*.14f),h*.10f,w*(.08f+i*.14f),h*.90f,p);
    }
    private void lanternSteps(Canvas c,float w,float h,int soft){
        for(int r=0;r<5;r++){ fill(soft,44+r*4); float l=w*(.05f+r*.055f),rr=w*(.95f-r*.055f),t=h*(.10f+r*.15f); c.drawRoundRect(new RectF(l,t,rr,t+h*.10f),dp(10),dp(10),p); }
    }
    private void mechanicalDeck(Canvas c,float w,float h,int deep){
        fill(deep,68); for(int r=0;r<4;r++) for(int i=0;i<9;i++){ float l=w*(.02f+i*.108f),t=h*(.12f+r*.19f); c.drawRoundRect(new RectF(l,t,l+w*.095f,t+h*.14f),dp(5),dp(5),p); stroke(e.accent,36,1); c.drawRoundRect(new RectF(l+dp(2),t+dp(2),l+w*.095f-dp(2),t+h*.14f-dp(2)),dp(3),dp(3),p); fill(deep,68); }
    }
    private void polaroidWall(Canvas c,float w,float h,int soft,int deep){ scrapbook(c,w,h,soft,deep); stroke(e.accent,42,2); c.drawLine(w*.04f,h*.48f,w*.96f,h*.48f,p); }
    private void bookPage(Canvas c,float w,float h,int soft,int deep){
        fill(Color.WHITE,e.dark?20:62); c.drawRoundRect(new RectF(w*.04f,h*.07f,w*.48f,h*.93f),dp(12),dp(12),p); c.drawRoundRect(new RectF(w*.52f,h*.07f,w*.96f,h*.93f),dp(12),dp(12),p); stroke(deep,25,1); for(int i=0;i<9;i++){ c.drawLine(w*.08f,h*(.16f+i*.075f),w*.44f,h*(.16f+i*.075f),p); c.drawLine(w*.56f,h*(.16f+i*.075f),w*.92f,h*(.16f+i*.075f),p); } fill(soft,55); c.drawRoundRect(new RectF(w*.47f,h*.07f,w*.53f,h*.93f),dp(3),dp(3),p);
    }
    private void neonCircuit(Canvas c,float w,float h,int deep){
        fill(deep,66); c.drawRoundRect(new RectF(w*.03f,h*.08f,w*.97f,h*.92f),dp(12),dp(12),p); stroke(e.accent,82,2); for(int i=0;i<8;i++){ float y=h*(.14f+i*.10f); c.drawLine(w*.06f,y,w*.30f,y,p); c.drawLine(w*.30f,y,w*.36f,y+h*.04f,p); c.drawLine(w*.36f,y+h*.04f,w*.94f,y+h*.04f,p); c.drawCircle(w*.30f,y,dp(3),p); }
    }
    private void frostedDock(Canvas c,float w,float h,int soft){
        fill(Color.WHITE,e.dark?18:44); c.drawRoundRect(new RectF(w*.06f,h*.12f,w*.94f,h*.88f),dp(34),dp(34),p); stroke(soft,46,1); c.drawRoundRect(new RectF(w*.09f,h*.15f,w*.91f,h*.85f),dp(28),dp(28),p);
    }
    private void teddyShelf(Canvas c,float w,float h,int soft,int deep){
        fill(soft,45); for(int r=0;r<3;r++) c.drawRoundRect(new RectF(w*.06f,h*(.18f+r*.25f),w*.94f,h*(.23f+r*.25f)),dp(6),dp(6),p); for(int i=0;i<6;i++){ float x=w*(.12f+(i%3)*.38f),y=h*(.14f+(i/3)*.48f); teddy(c,x,y,dp(18),soft,deep); }
    }
    private void flowerMarket(Canvas c,float w,float h,int soft,int deep){
        fill(deep,28); c.drawRoundRect(new RectF(w*.05f,h*.62f,w*.95f,h*.90f),dp(12),dp(12),p); for(int i=0;i<15;i++){ float x=w*(.08f+(i%5)*.21f),y=h*(.14f+(i/5)*.18f); flower(c,x,y,dp(10),soft,deep); }
    }
    private void gameDesk(Canvas c,float w,float h,int soft,int deep){
        fill(deep,44); c.drawRoundRect(new RectF(w*.06f,h*.16f,w*.94f,h*.70f),dp(14),dp(14),p); fill(soft,46); c.drawRoundRect(new RectF(w*.25f,h*.24f,w*.75f,h*.53f),dp(8),dp(8),p); c.drawRoundRect(new RectF(w*.18f,h*.73f,w*.82f,h*.84f),dp(8),dp(8),p); fill(e.accent,58); c.drawCircle(w*.80f,h*.38f,dp(9),p);
    }
    private void nightSkyline(Canvas c,float w,float h,int deep){ cityPanels(c,w,h,deep); fill(Color.WHITE,60); c.drawCircle(w*.83f,h*.18f,dp(18),p); }
    private void candyShop(Canvas c,float w,float h,int soft){
        fill(soft,52); c.drawRoundRect(new RectF(w*.04f,h*.18f,w*.96f,h*.90f),dp(20),dp(20),p); for(int i=0;i<7;i++){ fill(i%2==0?e.accent:Color.WHITE,48); float l=w*(.05f+i*.13f); path.reset(); path.moveTo(l,h*.18f); path.lineTo(l+w*.12f,h*.18f); path.lineTo(l+w*.10f,h*.32f); path.lineTo(l+w*.02f,h*.32f); path.close(); c.drawPath(path,p); } fill(soft,60); c.drawRoundRect(new RectF(w*.16f,h*.52f,w*.84f,h*.82f),dp(18),dp(18),p);
    }
    private void auroraGlass(Canvas c,float w,float h,int soft){
        fill(Color.WHITE,e.dark?12:30); c.drawRoundRect(new RectF(w*.04f,h*.08f,w*.96f,h*.92f),dp(28),dp(28),p); p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeWidth(dp(8)); for(int i=0;i<4;i++){ p.setColor(withAlpha(i%2==0?soft:e.accent,90)); path.reset(); path.moveTo(w*.06f,h*(.18f+i*.15f)); path.cubicTo(w*.28f,h*(.03f+i*.16f),w*.67f,h*(.44f+i*.08f),w*.95f,h*(.18f+i*.14f)); c.drawPath(path,p); } p.setStyle(Paint.Style.FILL);
    }
    private void moonWindow(Canvas c,float w,float h,int soft,int deep){
        stroke(soft,62,2); c.drawRoundRect(new RectF(w*.57f,h*.08f,w*.94f,h*.48f),dp(20),dp(20),p); c.drawLine(w*.755f,h*.08f,w*.755f,h*.48f,p); c.drawLine(w*.57f,h*.28f,w*.94f,h*.28f,p); fill(e.accent,70); c.drawCircle(w*.80f,h*.20f,dp(18),p); fill(e.dark?e.start:Color.WHITE,180); c.drawCircle(w*.86f,h*.16f,dp(16),p); fill(deep,28); c.drawRoundRect(new RectF(w*.05f,h*.55f,w*.48f,h*.90f),dp(22),dp(22),p);
    }
    private void stickerParade(Canvas c,float w,float h,int soft,int deep){
        fill(Color.WHITE,e.dark?18:50); for(int i=0;i<14;i++){ float x=w*(.08f+(i%7)*.14f),y=h*(.16f+(i/7)*.48f); c.save(); c.rotate((i%3-1)*10,x,y); c.drawRoundRect(new RectF(x-dp(18),y-dp(14),x+dp(18),y+dp(14)),dp(8),dp(8),p); fill(i%2==0?soft:deep,45); c.drawCircle(x,y,dp(8),p); fill(Color.WHITE,e.dark?18:50); c.restore(); }
    }
    private void waveDeck(Canvas c,float w,float h,int soft){
        fill(soft,36); for(int r=0;r<5;r++){ path.reset(); float y=h*(.12f+r*.17f); path.moveTo(0,y); path.cubicTo(w*.25f,y-h*.08f,w*.75f,y+h*.08f,w,y); path.lineTo(w,y+h*.09f); path.cubicTo(w*.75f,y+h*.17f,w*.25f,y+h*.01f,0,y+h*.09f); path.close(); c.drawPath(path,p); }
    }
    private void zenStones(Canvas c,float w,float h,int soft,int deep){
        fill(soft,36); for(int i=0;i<10;i++){ float x=w*(.11f+(i%5)*.20f),y=h*(.20f+(i/5)*.45f); c.drawOval(new RectF(x-dp(24),y-dp(14),x+dp(24),y+dp(14)),p); } stroke(deep,24,1); for(int i=0;i<4;i++) c.drawOval(new RectF(w*(.10f+i*.08f),h*(.08f+i*.03f),w*(.90f-i*.08f),h*(.92f-i*.03f)),p);
    }

    private void drawScene(Canvas c,float w,float h,int s){
        int soft=NeoThemeCatalog.mix(e.accent,Color.WHITE,e.dark?30:80);
        int deep=NeoThemeCatalog.mix(e.accent,Color.BLACK,e.dark?10:45);
        switch(s&31){
            case 0: bow(c,w*.83f,h*.17f,dp(18),soft,e.accent); break;
            case 1: moon(c,w*.84f,h*.16f,dp(18),soft); break;
            case 2: fill(soft,82); cloud(c,w*.82f,h*.15f,dp(18)); break;
            case 3: miniConsole(c,w*.82f,h*.16f,soft,deep); break;
            case 4: flower(c,w*.84f,h*.17f,dp(11),soft,deep); break;
            case 5: cat(c,w*.84f,h*.17f,dp(17),soft,deep); break;
            case 6: bunny(c,w*.84f,h*.17f,dp(16),soft,deep); break;
            case 7: teddy(c,w*.84f,h*.17f,dp(17),soft,deep); break;
            case 8: lamp(c,w*.84f,h*.17f,soft); break;
            case 9: jelly(c,w*.84f,h*.18f,soft); break;
            case 10: mushroom(c,w*.84f,h*.18f,soft,deep); break;
            case 11: crystal(c,w*.84f,h*.18f,soft); break;
            case 12: miniCassette(c,w*.84f,h*.18f,soft,deep); break;
            case 13: miniGame(c,w*.84f,h*.18f,soft,deep); break;
            case 14: shell(c,w*.84f,h*.18f,soft); break;
            case 15: books(c,w*.84f,h*.18f,soft,deep); break;
            case 16: butterfly(c,w*.84f,h*.18f,soft); break;
            case 17: perfume(c,w*.84f,h*.18f,soft); break;
            case 18: balcony(c,w*.84f,h*.18f,soft,deep); break;
            case 19: rain(c,w*.84f,h*.18f,soft); break;
            case 20: candy(c,w*.84f,h*.18f,soft); break;
            case 21: bouquet(c,w*.84f,h*.18f,soft,deep); break;
            case 22: skylineMark(c,w*.84f,h*.18f,deep); break;
            case 23: auroraMark(c,w*.84f,h*.18f,soft); break;
            case 24: polaroid(c,w*.84f,h*.18f,soft); break;
            case 25: milk(c,w*.84f,h*.18f,soft); break;
            case 26: pumpkin(c,w*.84f,h*.18f,soft); break;
            case 27: snow(c,w*.84f,h*.18f,soft); break;
            case 28: lantern(c,w*.84f,h*.18f,soft); break;
            case 29: planet(c,w*.84f,h*.18f,soft); break;
            case 30: music(c,w*.84f,h*.18f,soft); break;
            default: leaf(c,w*.84f,h*.18f,soft,deep); break;
        }
    }

    private void bow(Canvas c,float x,float y,float r,int soft,int ac){ fill(soft,95); c.drawOval(new RectF(x-r*1.5f,y-r*.7f,x-r*.1f,y+r*.7f),p); c.drawOval(new RectF(x+r*.1f,y-r*.7f,x+r*1.5f,y+r*.7f),p); fill(ac,95); c.drawCircle(x,y,r*.35f,p); }
    private void moon(Canvas c,float x,float y,float r,int col){ fill(col,88); c.drawCircle(x,y,r,p); fill(e.dark?e.start:Color.WHITE,190); c.drawCircle(x+r*.38f,y-r*.24f,r*.92f,p); }
    private void miniConsole(Canvas c,float x,float y,int s,int d){ fill(d,80); c.drawRoundRect(new RectF(x-dp(28),y-dp(15),x+dp(28),y+dp(15)),dp(8),dp(8),p); fill(s,90); c.drawCircle(x+dp(13),y,dp(4),p); }
    private void cat(Canvas c,float x,float y,float r,int s,int d){ fill(s,88); c.drawCircle(x,y,r,p); path.reset(); path.moveTo(x-r*.8f,y-r*.6f); path.lineTo(x-r*.4f,y-r*1.3f); path.lineTo(x-r*.05f,y-r*.6f); path.close(); c.drawPath(path,p); path.reset(); path.moveTo(x+r*.8f,y-r*.6f); path.lineTo(x+r*.4f,y-r*1.3f); path.lineTo(x+r*.05f,y-r*.6f); path.close(); c.drawPath(path,p); fill(d,120); c.drawCircle(x-r*.3f,y,dp(1.7f),p); c.drawCircle(x+r*.3f,y,dp(1.7f),p); }
    private void bunny(Canvas c,float x,float y,float r,int s,int d){ fill(s,88); c.drawCircle(x,y,r,p); c.drawOval(new RectF(x-r*.75f,y-r*2f,x-r*.15f,y-r*.55f),p); c.drawOval(new RectF(x+r*.15f,y-r*2f,x+r*.75f,y-r*.55f),p); fill(d,115); c.drawCircle(x-r*.3f,y,dp(1.5f),p); c.drawCircle(x+r*.3f,y,dp(1.5f),p); }
    private void teddy(Canvas c,float x,float y,float r,int s,int d){ fill(s,88); c.drawCircle(x-r*.65f,y-r*.65f,r*.42f,p); c.drawCircle(x+r*.65f,y-r*.65f,r*.42f,p); c.drawCircle(x,y,r,p); fill(d,110); c.drawCircle(x-r*.3f,y-dp(2),dp(1.5f),p); c.drawCircle(x+r*.3f,y-dp(2),dp(1.5f),p); }
    private void lamp(Canvas c,float x,float y,int s){ stroke(s,80,3); c.drawLine(x,y+dp(20),x,y-dp(8),p); c.drawLine(x,y-dp(8),x+dp(12),y-dp(18),p); fill(s,80); path.reset(); path.moveTo(x+dp(7),y-dp(22)); path.lineTo(x+dp(24),y-dp(22)); path.lineTo(x+dp(20),y-dp(10)); path.lineTo(x+dp(11),y-dp(10)); path.close(); c.drawPath(path,p); }
    private void jelly(Canvas c,float x,float y,int s){ fill(s,70); c.drawOval(new RectF(x-dp(16),y-dp(10),x+dp(16),y+dp(10)),p); stroke(s,70,1); for(int i=-1;i<=1;i++) c.drawLine(x+i*dp(7),y+dp(7),x+i*dp(7),y+dp(22),p); }
    private void mushroom(Canvas c,float x,float y,int s,int d){ fill(s,82); c.drawOval(new RectF(x-dp(18),y-dp(10),x+dp(18),y+dp(10)),p); fill(d,55); c.drawRoundRect(new RectF(x-dp(5),y,x+dp(5),y+dp(22)),dp(3),dp(3),p); }
    private void crystal(Canvas c,float x,float y,int s){ fill(s,68); polygon(c,x,y,dp(18),6); }
    private void miniCassette(Canvas c,float x,float y,int s,int d){ fill(s,74); c.drawRoundRect(new RectF(x-dp(24),y-dp(14),x+dp(24),y+dp(14)),dp(6),dp(6),p); fill(d,85); c.drawCircle(x-dp(10),y,dp(6),p); c.drawCircle(x+dp(10),y,dp(6),p); }
    private void miniGame(Canvas c,float x,float y,int s,int d){ fill(d,78); c.drawRoundRect(new RectF(x-dp(28),y-dp(12),x+dp(28),y+dp(12)),dp(12),dp(12),p); fill(s,88); c.drawRect(x-dp(16),y-dp(2),x-dp(6),y+dp(2),p); c.drawRect(x-dp(12),y-dp(6),x-dp(10),y+dp(6),p); c.drawCircle(x+dp(13),y,dp(4),p); }
    private void shell(Canvas c,float x,float y,int s){ fill(s,78); c.drawOval(new RectF(x-dp(20),y-dp(15),x+dp(20),y+dp(15)),p); stroke(e.accent,60,1); for(int i=-2;i<=2;i++) c.drawLine(x,y,x+i*dp(7),y+dp(14),p); }
    private void books(Canvas c,float x,float y,int s,int d){ for(int i=0;i<4;i++){ fill(i%2==0?s:d,65); c.drawRect(x-dp(24)+i*dp(12),y-dp(18),x-dp(14)+i*dp(12),y+dp(18),p); } }
    private void butterfly(Canvas c,float x,float y,int s){ fill(s,80); c.drawOval(new RectF(x-dp(20),y-dp(12),x-dp(2),y+dp(2)),p); c.drawOval(new RectF(x+dp(2),y-dp(12),x+dp(20),y+dp(2)),p); c.drawOval(new RectF(x-dp(16),y,x-dp(2),y+dp(13)),p); c.drawOval(new RectF(x+dp(2),y,x+dp(16),y+dp(13)),p); }
    private void perfume(Canvas c,float x,float y,int s){ fill(s,70); c.drawRoundRect(new RectF(x-dp(15),y-dp(12),x+dp(15),y+dp(18)),dp(5),dp(5),p); c.drawRect(x-dp(7),y-dp(20),x+dp(7),y-dp(12),p); }
    private void balcony(Canvas c,float x,float y,int s,int d){ stroke(s,70,2); c.drawRect(x-dp(24),y-dp(20),x+dp(24),y+dp(18),p); c.drawLine(x,y-dp(20),x,y+dp(18),p); c.drawLine(x-dp(24),y,x+dp(24),y,p); fill(d,45); c.drawRect(x-dp(28),y+dp(18),x+dp(28),y+dp(22),p); }
    private void rain(Canvas c,float x,float y,int s){ stroke(s,70,1); for(int i=-2;i<=2;i++) c.drawLine(x+i*dp(9),y-dp(14)+(i%2)*dp(4),x+i*dp(9)-dp(4),y+dp(14)+(i%2)*dp(4),p); }
    private void candy(Canvas c,float x,float y,int s){ fill(s,82); c.drawCircle(x,y,dp(10),p); path.reset(); path.moveTo(x-dp(10),y); path.lineTo(x-dp(24),y-dp(8)); path.lineTo(x-dp(24),y+dp(8)); path.close(); c.drawPath(path,p); path.reset(); path.moveTo(x+dp(10),y); path.lineTo(x+dp(24),y-dp(8)); path.lineTo(x+dp(24),y+dp(8)); path.close(); c.drawPath(path,p); }
    private void bouquet(Canvas c,float x,float y,int s,int d){ for(int i=0;i<5;i++) flower(c,x+(i-2)*dp(8),y+(i%2)*dp(6),dp(7),s,d); }
    private void skylineMark(Canvas c,float x,float y,int d){ fill(d,65); for(int i=0;i<5;i++){ float l=x-dp(24)+i*dp(10); c.drawRect(l,y-dp(8+i*3),l+dp(8),y+dp(18),p); } }
    private void auroraMark(Canvas c,float x,float y,int s){ stroke(s,70,4); path.reset(); path.moveTo(x-dp(24),y); path.cubicTo(x-dp(8),y-dp(18),x+dp(8),y+dp(18),x+dp(24),y); c.drawPath(path,p); }
    private void polaroid(Canvas c,float x,float y,int s){ fill(Color.WHITE,e.dark?70:170); c.save(); c.rotate(-8,x,y); c.drawRect(x-dp(18),y-dp(22),x+dp(18),y+dp(22),p); fill(s,70); c.drawRect(x-dp(14),y-dp(18),x+dp(14),y+dp(8),p); c.restore(); }
    private void milk(Canvas c,float x,float y,int s){ fill(s,76); c.drawRect(x-dp(14),y-dp(14),x+dp(14),y+dp(20),p); path.reset(); path.moveTo(x-dp(14),y-dp(14)); path.lineTo(x,y-dp(24)); path.lineTo(x+dp(14),y-dp(14)); path.close(); c.drawPath(path,p); }
    private void pumpkin(Canvas c,float x,float y,int s){ fill(s,82); c.drawOval(new RectF(x-dp(21),y-dp(14),x+dp(21),y+dp(14)),p); fill(e.accent,85); c.drawRect(x-dp(2),y-dp(23),x+dp(2),y-dp(13),p); }
    private void snow(Canvas c,float x,float y,int s){ stroke(s,80,2); c.drawCircle(x,y,dp(20),p); for(int i=0;i<6;i++){ double a=i*Math.PI/3; c.drawLine(x,y,x+(float)Math.cos(a)*dp(16),y+(float)Math.sin(a)*dp(16),p); } }
    private void lantern(Canvas c,float x,float y,int s){ fill(s,80); c.drawOval(new RectF(x-dp(12),y-dp(17),x+dp(12),y+dp(17)),p); stroke(e.accent,60,1); c.drawLine(x,y-dp(24),x,y-dp(17),p); }
    private void planet(Canvas c,float x,float y,int s){ fill(s,76); c.drawCircle(x,y,dp(12),p); stroke(s,76,2); c.drawOval(new RectF(x-dp(24),y-dp(6),x+dp(24),y+dp(6)),p); }
    private void music(Canvas c,float x,float y,int s){ fill(s,80); c.drawCircle(x-dp(10),y+dp(10),dp(6),p); c.drawCircle(x+dp(10),y+dp(4),dp(6),p); c.drawRect(x-dp(4),y-dp(18),x-dp(2),y+dp(10),p); c.drawRect(x+dp(16),y-dp(24),x+dp(18),y+dp(4),p); c.drawRect(x-dp(4),y-dp(18),x+dp(18),y-dp(14),p); }
    private void leaf(Canvas c,float x,float y,int s,int d){ fill(s,76); c.drawOval(new RectF(x-dp(20),y-dp(8),x,y+dp(12)),p); c.drawOval(new RectF(x,y-dp(12),x+dp(20),y+dp(8)),p); stroke(d,50,1); c.drawLine(x-dp(15),y+dp(8),x+dp(15),y-dp(8),p); }
    private void flower(Canvas c,float x,float y,float r,int s,int d){ fill(s,82); for(int i=0;i<5;i++){ double a=i*Math.PI*2/5; float px=x+(float)Math.cos(a)*r*.72f,py=y+(float)Math.sin(a)*r*.72f; c.drawCircle(px,py,r*.48f,p); } fill(d,95); c.drawCircle(x,y,r*.30f,p); }

    private void cloud(Canvas c,float x,float y,float r){ c.drawCircle(x-r*.55f,y,r*.65f,p); c.drawCircle(x,y-r*.22f,r*.85f,p); c.drawCircle(x+r*.65f,y,r*.60f,p); c.drawRoundRect(new RectF(x-r*1.1f,y,x+r*1.15f,y+r*.58f),r*.28f,r*.28f,p); }
    private void polygon(Canvas c,float x,float y,float r,int sides){ path.reset(); for(int i=0;i<sides;i++){ double a=-Math.PI/2+i*Math.PI*2/sides; float px=x+(float)Math.cos(a)*r,py=y+(float)Math.sin(a)*r; if(i==0) path.moveTo(px,py); else path.lineTo(px,py); } path.close(); c.drawPath(path,p); }
    private void fill(int color,int a){ p.setShader(null); p.setStyle(Paint.Style.FILL); p.setColor(withAlpha(color,a)); }
    private void stroke(int color,int a,float width){ p.setShader(null); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(width)); p.setColor(withAlpha(color,a)); }
    private int withAlpha(int color,int a){ return Color.argb(Math.max(0,Math.min(255,a*alpha/255)),Color.red(color),Color.green(color),Color.blue(color)); }
    private float dp(float v){ return v*d; }
    @Override public void setAlpha(int a){ alpha=a; invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter f){ p.setColorFilter(f); invalidateSelf(); }
    @Override public int getOpacity(){ return PixelFormat.TRANSLUCENT; }
}

final class NeoThemeKeyDrawable extends Drawable {
    private final NeoThemeCatalog.Entry e;
    private final boolean special,space,pressed;
    private final float d;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();

    private NeoThemeKeyDrawable(Context c,int pack,boolean special,boolean space,boolean pressed){
        e=NeoThemeCatalog.get(pack); this.special=special; this.space=space; this.pressed=pressed;
        d=c.getResources().getDisplayMetrics().density;
    }

    static StateListDrawable state(Context c,int pack,boolean special,boolean space){
        StateListDrawable s=new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_pressed},new NeoThemeKeyDrawable(c,pack,special,space,true));
        s.addState(new int[]{},new NeoThemeKeyDrawable(c,pack,special,space,false));
        return s;
    }

    @Override public void draw(Canvas c){
        if(e==null) return; Rect b=getBounds(); float w=b.width(),h=b.height(); if(w<=0||h<=0) return;
        int a=e.dark?NeoThemeCatalog.mix(e.start,Color.WHITE,special||space?18:8):NeoThemeCatalog.mix(e.end,e.accent,special||space?20:7);
        int z=e.dark?NeoThemeCatalog.mix(e.end,e.accent,special||space?26:13):NeoThemeCatalog.mix(e.start,Color.WHITE,special||space?45:70);
        if(pressed){ a=NeoThemeCatalog.mix(a,e.accent,30); z=NeoThemeCatalog.mix(z,Color.WHITE,14); }
        int op=pressed?235:(e.dark?205:226);
        a=Color.argb(op,Color.red(a),Color.green(a),Color.blue(a)); z=Color.argb(op,Color.red(z),Color.green(z),Color.blue(z));
        RectF r=new RectF(dp(1),dp(1),w-dp(1),h-dp(1));
        p.setShader(new LinearGradient(0,0,w,h,a,z,Shader.TileMode.CLAMP)); p.setStyle(Paint.Style.FILL);
        int m=e.keyMode%24; float rad=dp(Math.max(4,Math.min(24,e.corner)));
        switch(m){
            case 0: c.drawRoundRect(r,rad,rad,p); break;
            case 1: c.drawRoundRect(r,h*.48f,h*.48f,p); break;
            case 2: cut(r,Math.min(w,h)*.16f); c.drawPath(path,p); break;
            case 3: bevel(r,Math.min(w,h)*.10f); c.drawPath(path,p); break;
            case 4: c.drawRoundRect(r,rad,rad,p); rail(c,r,true); break;
            case 5: gem(r,Math.min(w,h)*.15f); c.drawPath(path,p); break;
            case 6: ticket(r,Math.min(w,h)*.12f); c.drawPath(path,p); break;
            case 7: c.drawRoundRect(r,rad,rad,p); inner(c,r); break;
            case 8: notch(r,Math.min(w,h)*.13f); c.drawPath(path,p); break;
            case 9: c.drawRoundRect(r,rad*.75f,rad*.75f,p); rail(c,r,false); break;
            case 10: split(c,r,a,z); break;
            case 11: c.drawOval(r,p); break;
            case 12: c.drawRoundRect(r,rad,rad,p); dots(c,r); break;
            case 13: diamond(r); c.drawPath(path,p); break;
            case 14: c.drawRoundRect(r,rad,rad,p); inset(c,r); break;
            case 15: stepped(r,Math.min(w,h)*.12f); c.drawPath(path,p); break;
            case 16: c.drawCircle(r.centerX(),r.centerY(),Math.min(r.width(),r.height())*.48f,p); break;
            case 17: c.drawRoundRect(r,Math.min(w,h)*.28f,Math.min(w,h)*.28f,p); inner(c,r); break;
            case 18: sticker(c,r,rad); break;
            case 19: mechanical(c,r); break;
            case 20: paper(c,r); break;
            case 21: plush(c,r); break;
            case 22: pixel(c,r); break;
            default: outline(c,r,rad); break;
        }
        p.setShader(null);
        if(e.borders && m!=23){ p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1)); p.setColor(Color.argb(e.dark?115:92,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent))); c.drawRoundRect(r,Math.min(rad,h*.42f),Math.min(rad,h*.42f),p); p.setStyle(Paint.Style.FILL); }
        if(space){ p.setColor(Color.argb(120,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent))); c.drawRoundRect(new RectF(w*.37f,h*.48f,w*.63f,h*.54f),dp(3),dp(3),p); }
    }

    private void cut(RectF r,float x){ path.reset(); path.moveTo(r.left+x,r.top); path.lineTo(r.right-x,r.top); path.lineTo(r.right,r.top+x); path.lineTo(r.right,r.bottom-x); path.lineTo(r.right-x,r.bottom); path.lineTo(r.left+x,r.bottom); path.lineTo(r.left,r.bottom-x); path.lineTo(r.left,r.top+x); path.close(); }
    private void bevel(RectF r,float x){ path.reset(); path.moveTo(r.left+x,r.top); path.lineTo(r.right-x,r.top); path.lineTo(r.right,r.top+x); path.lineTo(r.right,r.bottom); path.lineTo(r.left,r.bottom); path.lineTo(r.left,r.top+x); path.close(); }
    private void gem(RectF r,float x){ path.reset(); path.moveTo(r.left+x,r.top); path.lineTo(r.right-x,r.top); path.lineTo(r.right,r.centerY()); path.lineTo(r.right-x,r.bottom); path.lineTo(r.left+x,r.bottom); path.lineTo(r.left,r.centerY()); path.close(); }
    private void ticket(RectF r,float x){ path.reset(); path.moveTo(r.left+x,r.top); path.lineTo(r.right-x,r.top); path.quadTo(r.right,r.top,r.right,r.top+x); path.lineTo(r.right,r.centerY()-x*.45f); path.quadTo(r.right-x*.8f,r.centerY(),r.right,r.centerY()+x*.45f); path.lineTo(r.right,r.bottom-x); path.quadTo(r.right,r.bottom,r.right-x,r.bottom); path.lineTo(r.left+x,r.bottom); path.quadTo(r.left,r.bottom,r.left,r.bottom-x); path.lineTo(r.left,r.centerY()+x*.45f); path.quadTo(r.left+x*.8f,r.centerY(),r.left,r.centerY()-x*.45f); path.lineTo(r.left,r.top+x); path.quadTo(r.left,r.top,r.left+x,r.top); path.close(); }
    private void notch(RectF r,float x){ path.reset(); path.moveTo(r.left+x,r.top); path.lineTo(r.centerX()-x,r.top); path.lineTo(r.centerX(),r.top+x*.65f); path.lineTo(r.centerX()+x,r.top); path.lineTo(r.right-x,r.top); path.quadTo(r.right,r.top,r.right,r.top+x); path.lineTo(r.right,r.bottom-x); path.quadTo(r.right,r.bottom,r.right-x,r.bottom); path.lineTo(r.left+x,r.bottom); path.quadTo(r.left,r.bottom,r.left,r.bottom-x); path.lineTo(r.left,r.top+x); path.quadTo(r.left,r.top,r.left+x,r.top); path.close(); }
    private void diamond(RectF r){ path.reset(); path.moveTo(r.centerX(),r.top); path.lineTo(r.right,r.centerY()); path.lineTo(r.centerX(),r.bottom); path.lineTo(r.left,r.centerY()); path.close(); }
    private void stepped(RectF r,float x){ path.reset(); path.moveTo(r.left+x,r.top); path.lineTo(r.right-x,r.top); path.lineTo(r.right-x,r.top+x*.45f); path.lineTo(r.right,r.top+x*.45f); path.lineTo(r.right,r.bottom-x*.45f); path.lineTo(r.right-x,r.bottom-x*.45f); path.lineTo(r.right-x,r.bottom); path.lineTo(r.left+x,r.bottom); path.lineTo(r.left+x,r.bottom-x*.45f); path.lineTo(r.left,r.bottom-x*.45f); path.lineTo(r.left,r.top+x*.45f); path.lineTo(r.left+x,r.top+x*.45f); path.close(); }
    private void rail(Canvas c,RectF r,boolean top){ p.setShader(null); p.setColor(Color.argb(120,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent))); float y=top?r.top+dp(2):r.bottom-dp(4); c.drawRoundRect(new RectF(r.left+dp(6),y,r.right-dp(6),y+dp(2)),dp(2),dp(2),p); }
    private void inner(Canvas c,RectF r){ p.setShader(null); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1)); p.setColor(Color.argb(78,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent))); c.drawRoundRect(new RectF(r.left+dp(3),r.top+dp(3),r.right-dp(3),r.bottom-dp(3)),dp(6),dp(6),p); p.setStyle(Paint.Style.FILL); }
    private void split(Canvas c,RectF r,int a,int z){ p.setShader(null); p.setColor(a); c.drawRoundRect(r,dp(8),dp(8),p); path.reset(); path.moveTo(r.centerX(),r.top); path.lineTo(r.right,r.top); path.lineTo(r.right,r.bottom); path.lineTo(r.centerX()-r.width()*.12f,r.bottom); path.close(); p.setColor(z); c.drawPath(path,p); }
    private void dots(Canvas c,RectF r){ p.setShader(null); p.setColor(Color.argb(110,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent))); c.drawCircle(r.left+dp(6),r.top+dp(6),dp(2),p); c.drawCircle(r.right-dp(6),r.bottom-dp(6),dp(2),p); }
    private void inset(Canvas c,RectF r){ p.setShader(null); p.setColor(Color.argb(46,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent))); c.drawRoundRect(new RectF(r.left+dp(4),r.top+dp(4),r.right-dp(4),r.bottom-dp(4)),dp(5),dp(5),p); }
    private void sticker(Canvas c,RectF r,float rad){ c.drawRoundRect(r,rad,rad,p); p.setShader(null); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(3)); p.setColor(Color.argb(170,255,255,255)); c.drawRoundRect(new RectF(r.left+dp(1.5f),r.top+dp(1.5f),r.right-dp(1.5f),r.bottom-dp(1.5f)),rad,rad,p); p.setStyle(Paint.Style.FILL); }
    private void mechanical(Canvas c,RectF r){ c.drawRoundRect(r,dp(5),dp(5),p); p.setShader(null); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2)); p.setColor(Color.argb(80,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent))); c.drawRoundRect(new RectF(r.left+dp(3),r.top+dp(3),r.right-dp(3),r.bottom-dp(3)),dp(3),dp(3),p); p.setStyle(Paint.Style.FILL); }
    private void paper(Canvas c,RectF r){ c.drawRoundRect(r,dp(3),dp(3),p); p.setShader(null); p.setColor(Color.argb(45,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent))); c.drawRect(r.left+dp(4),r.top+dp(5),r.right-dp(4),r.top+dp(7),p); }
    private void plush(Canvas c,RectF r){ c.drawRoundRect(r,Math.min(r.width(),r.height())*.35f,Math.min(r.width(),r.height())*.35f,p); p.setShader(null); p.setColor(Color.argb(40,255,255,255)); c.drawOval(new RectF(r.left+dp(4),r.top+dp(3),r.right-dp(4),r.centerY()),p); }
    private void pixel(Canvas c,RectF r){ c.drawRect(r,p); p.setShader(null); p.setColor(Color.argb(60,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent))); c.drawRect(r.left+dp(3),r.top+dp(3),r.right-dp(3),r.top+dp(6),p); }
    private void outline(Canvas c,RectF r,float rad){ p.setShader(null); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2)); p.setColor(Color.argb(200,Color.red(e.accent),Color.green(e.accent),Color.blue(e.accent))); c.drawRoundRect(r,rad,rad,p); p.setStyle(Paint.Style.FILL); }

    private float dp(float v){ return v*d; }
    @Override public void setAlpha(int a){}
    @Override public void setColorFilter(ColorFilter f){ p.setColorFilter(f); invalidateSelf(); }
    @Override public int getOpacity(){ return PixelFormat.TRANSLUCENT; }
}
