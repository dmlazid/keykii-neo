package com.keykii.neo;

/** Pure geometry shared by the IME, shop and the geometry regression runner.
 * Coordinates are normalized to the keyboard area, not the toolbar or navigation inset.
 * Logical order and actions come from the user's language. Art never substitutes input.
 */
final class NeoGeometry {
    static final int FAMILY_COUNT=48;
    static final class Key {
        final String action; final float weight; final boolean special;
        Key(String action,float weight,boolean special){this.action=action;this.weight=weight;this.special=special;}
    }
    static final class Box {
        final int row,col; final float left,top,right,bottom;
        Box(int r,int c,float l,float t,float rr,float b){row=r;col=c;left=l;top=t;right=rr;bottom=b;}
    }
    // Each row is an authored layout profile: side insets, center split,
    // horizontal gap, vertical gap, column stagger, key height, row stagger,
    // modifier width, space width, scene band. These change real view bounds.
    static final float[][] PROFILES={
        { .012f,.010f,.025f,.065f,.05f,.055f,.84f,.02f,1.15f,1.12f,.025f}, // candy floats
        { .020f,.028f,.000f,.070f,.04f,.080f,.86f,.10f,1.10f,1.10f,.025f}, // scrapbook
        { .005f,.005f,.065f,.036f,.07f,.000f,.94f,.00f,1.15f,.90f,.025f}, // split mechanical
        { .010f,.014f,.035f,.100f,.05f,.065f,.82f,.04f,1.25f,1.18f,.035f}, // cloud islands
        { .015f,.015f,.000f,.095f,.06f,.000f,.90f,.04f,1.20f,1.12f,.025f}, // bubble
        { .008f,.010f,.020f,.035f,.05f,.040f,.90f,.03f,1.42f,1.28f,.025f}, // bento
        { .030f,.016f,.000f,.045f,.09f,.000f,.90f,.09f,1.25f,1.18f,.015f}, // tickets
        { .008f,.008f,.000f,.022f,.09f,.000f,.98f,.00f,1.08f,1.22f,.025f}, // glass shelves
        { .005f,.005f,.020f,.045f,.05f,.000f,.94f,.00f,1.42f,.94f,.045f}, // pixel console
        { .048f,.010f,.000f,.034f,.07f,.000f,.91f,.00f,1.12f,1.12f,.025f}, // notebook tabs
        { .016f,.016f,.025f,.082f,.04f,.075f,.83f,.07f,1.32f,1.25f,.030f}, // petals
        { .010f,.010f,.036f,.040f,.05f,.000f,.92f,.02f,1.38f,.93f,.020f}, // arcade
        { .008f,.025f,.000f,.025f,.10f,.000f,.96f,.05f,1.05f,1.30f,.020f}, // ribbons
        { .040f,.005f,.020f,.045f,.05f,.035f,.90f,.14f,1.42f,1.30f,.025f}, // asymmetric
        { .016f,.016f,.000f,.082f,.04f,.065f,.85f,.05f,1.26f,1.12f,.025f}, // jewel
        { .006f,.006f,.000f,.060f,.045f,.025f,.90f,.025f,1.26f,1.16f,.020f}, // plush
        { .012f,.024f,.000f,.065f,.075f,.040f,.86f,.09f,1.15f,1.15f,.030f}, // photo frames
        { .006f,.006f,.000f,.042f,.04f,.070f,.83f,.00f,1.35f,1.20f,.045f}, // city blocks
        { .012f,.012f,.045f,.075f,.04f,.078f,.82f,.09f,1.25f,.88f,.035f}, // orbit
        { .010f,.010f,.000f,.030f,.025f,.060f,.90f,.10f,1.20f,1.08f,.025f}, // mosaic
        { .006f,.006f,.038f,.035f,.065f,.000f,.94f,.025f,1.32f,1.12f,.025f}, // cyber rails
        { .018f,.018f,.000f,.060f,.08f,.030f,.88f,.035f,1.17f,1.15f,.030f}, // trellis
        { .006f,.006f,.000f,.070f,.04f,.055f,.86f,.025f,1.38f,1.24f,.030f}, // dessert
        { .014f,.014f,.022f,.090f,.045f,.055f,.86f,.060f,1.30f,1.20f,.025f}, // pebbles
        { .018f,.018f,.000f,.048f,.08f,.000f,.96f,.000f,1.30f,1.32f,.030f}, // luxury
        { .008f,.008f,.000f,.065f,.06f,.045f,.88f,.080f,1.40f,1.12f,.025f}, // chrome
        { .012f,.012f,.042f,.055f,.08f,.000f,.91f,.000f,1.24f,1.08f,.035f}, // gothic
        { .010f,.040f,.000f,.045f,.055f,.020f,.92f,.080f,1.34f,1.28f,.035f}, // bedroom
        { .024f,.024f,.055f,.032f,.075f,.000f,.96f,.000f,1.06f,1.36f,.025f}, // cassette
        { .008f,.018f,.000f,.065f,.05f,.035f,.88f,.040f,1.32f,1.22f,.045f}, // kawaii
        { .008f,.008f,.060f,.035f,.05f,.045f,.88f,.020f,1.30f,.90f,.045f}, // space console
        { .016f,.016f,.000f,.018f,.095f,.000f,.94f,.000f,1.02f,1.18f,.010f}, // minimal
        { .020f,.020f,.000f,.044f,.06f,.035f,.90f,.120f,1.24f,1.24f,.030f}, // lantern steps
        { .004f,.004f,.024f,.028f,.04f,.000f,.98f,.025f,1.16f,1.00f,.020f}, // mechanical deck
        { .010f,.036f,.000f,.070f,.06f,.075f,.82f,.130f,1.18f,1.14f,.035f}, // polaroids
        { .035f,.015f,.040f,.025f,.09f,.000f,.96f,.000f,1.14f,1.20f,.020f}, // book page
        { .006f,.006f,.035f,.024f,.07f,.050f,.88f,.030f,1.40f,1.02f,.030f}, // circuit
        { .024f,.024f,.000f,.055f,.08f,.000f,.94f,.020f,1.28f,1.35f,.025f}, // frost
        { .010f,.010f,.000f,.075f,.07f,.030f,.88f,.025f,1.35f,1.24f,.030f}, // teddy shelf
        { .018f,.022f,.000f,.060f,.06f,.040f,.88f,.050f,1.22f,1.22f,.040f}, // flower market
        { .008f,.008f,.055f,.034f,.065f,.000f,.93f,.000f,1.40f,1.02f,.025f}, // game desk
        { .006f,.012f,.020f,.046f,.04f,.072f,.82f,.020f,1.22f,1.30f,.035f}, // skyline
        { .010f,.010f,.000f,.075f,.04f,.042f,.88f,.040f,1.32f,1.24f,.025f}, // candy shop
        { .010f,.010f,.030f,.050f,.065f,.055f,.86f,.060f,1.18f,1.20f,.025f}, // aurora
        { .040f,.010f,.000f,.060f,.06f,.000f,.94f,.070f,1.25f,1.36f,.040f}, // moon window
        { .008f,.025f,.000f,.072f,.045f,.070f,.85f,.090f,1.30f,1.14f,.020f}, // stickers
        { .012f,.012f,.025f,.050f,.04f,.080f,.82f,.100f,1.25f,1.26f,.020f}, // wave
        { .028f,.028f,.000f,.085f,.06f,.038f,.89f,.050f,1.18f,1.10f,.025f}  // stones
    };

    static Box[] layout(Key[][] rows,int family,int composition) {
        float[] p=PROFILES[Math.floorMod(family,FAMILY_COUNT)];
        int n=0; for(Key[] r:rows)n+=r.length;
        Box[] out=new Box[n]; int index=0;
        float band=p[10],height=1-band;
        // Composition is a defined arrangement edition; its changes are spatial,
        // not hue permutations. All editions retain one uninterrupted spacebar.
        int edition=Math.floorMod(composition,11);
        float[] rowShare=new float[rows.length]; float shareTotal=0;
        for(int r=0;r<rows.length;r++){
            boolean numbers=rows[r].length>0&&"1".equals(rows[r][0].action);
            rowShare[r]=numbers?.86f:1f;
            if(r==rows.length-1)rowShare[r]=(edition==3||edition==8)?1.16f:1f;
            else if(edition==5 && r==rows.length-2)rowShare[r]=1.10f;
            shareTotal+=rowShare[r];
        }
        float y=band;
        for(int r=0;r<rows.length;r++){
            Key[] row=rows[r]; boolean bottom=r==rows.length-1;
            float rh=height*rowShare[r]/shareTotal;
            float stagger=p[7]*(r%2==0?.0f:.22f);
            float left=p[0]+stagger, right=p[1]+p[7]*(r%2==0?.12f:0);
            if(row.length==9&&!bottom){left+=.026f;right+=.026f;}
            if(edition==1){left+=(r%2)*.018f;right+=(1-r%2)*.010f;}
            if(edition==2){left+=.013f*r/rows.length;right+=.013f*r/rows.length;}
            if(edition==4){left+=bottom?0:.015f;right+=bottom?0:.005f;}
            if(edition==6){left+=(rows.length-r-1)*.006f;right+=r*.004f;}
            if(edition==7){left+=.01f;right+=.01f;}
            if(edition==9){left+=r%2==0?.019f:0;right+=r%2==1?.019f:0;}
            if(edition==10){left+=bottom?.0f:.012f;right+=bottom?.0f:.012f;}
            float split=bottom?0:p[2];
            if(!bottom && edition==7)split=Math.max(split,.065f);
            if(!bottom && edition==3)split=Math.max(split,.035f);
            if(!bottom && edition==2){left+=.020f*r;right+=.012f*r;}
            if(!bottom && edition==6){left+=.012f*(rows.length-r-1);right+=.008f*r;}
            float gap=(p[3]+(edition==7?.018f:0))/Math.max(9,row.length);
            float usable=1-left-right-gap*Math.max(0,row.length-1)-split;
            float[] weights=new float[row.length];float sum=0;
            for(int k=0;k<row.length;k++){
                Key key=row[k];float v=key.weight;
                if(bottom && "SPACE".equals(key.action))v*=p[9]*(edition==3?1.32f:edition==8?.87f:1f);
                else if(key.special)v*=p[8];
                if(!bottom && !key.special){
                    if(family==5||family==13||family==19)v*=k%3==0?1.17f:.95f;
                    if(family==17||family==41)v*=k%4==1?1.16f:.96f;
                    if(edition==4)v*=k%3==0?1.12f:.98f;
                    if(edition==8)v*=k==0||k==row.length-1?1.10f:1;
                    if(edition==10)v*=k%2==0?1.06f:.97f;
                }
                weights[k]=v;sum+=v;
            }
            float x=left;
            for(int k=0;k<row.length;k++){
                if(k==row.length/2)x+=split;
                float kw=usable*weights[k]/sum;
                float phase=(float)Math.sin((k/(float)Math.max(1,row.length-1))*Math.PI);
                float offset=p[5]*(family==18||family==46?phase:(k%2));
                if(edition==5&&!bottom)offset+=.018f*(k%3);
                if(edition==9&&!bottom)offset+=.02f*phase;
                float kh=p[6];
                if(bottom) {offset=0;kh=Math.max(.89f,kh);}
                if(edition==10&&!bottom)kh-=.03f*(k%2);
                float t=y+rh*(p[4]*.5f+offset);
                float b=Math.min(y+rh*(1-p[4]*.5f),t+rh*kh);
                out[index++]=new Box(r,k,x,t,x+kw,b);
                x+=kw+gap;
            }
            y+=rh;
        }
        return out;
    }
    private NeoGeometry(){}
}
