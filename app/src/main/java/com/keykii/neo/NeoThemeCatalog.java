package com.keykii.neo;

import android.graphics.Color;

final class NeoThemeCatalog {
    static final int FIRST_PACK=1000;
    static final int COUNT=512;
    static final int PAGE_SIZE=24;

    static final String[] FILTERS={
        "all","new","free","pro","cute","kawaii","aesthetic","anime-inspired","dreamy","dark",
        "gaming","nature","floral","food","retro","y2k","minimal","luxury","space","ocean","city","seasonal"
    };

    static final String[] ARCH_NAMES={
        "Floating Candy","Sticker Scrapbook","Split Mechanical","Cloud Islands","Round Bubble",
        "Offset Bento","Ticket Rows","Glass Shelves","Pixel Console","Notebook Tabs","Petal Cluster",
        "Arcade Blocks","Ribbon Stacks","Asymmetric Cards","Jewel Facets","Soft Plush","Photo Frames",
        "City Panels","Orbit Keys","Mosaic Tiles","Cyber Rails","Garden Trellis","Dessert Board",
        "Ocean Pebbles","Luxury Plaques","Y2K Chrome","Gothic Windows","Dream Bedroom","Retro Cassette",
        "Kawaii Room","Space Console","Minimal Lines","Lantern Steps","Mechanical Deck","Polaroid Wall",
        "Book Page","Neon Circuit","Frosted Dock","Teddy Shelf","Flower Market","Game Desk",
        "Night Skyline","Candy Shop","Aurora Glass","Moon Window","Sticker Parade","Wave Deck","Zen Stones"
    };

    static final String[] SCENE_NAMES={
        "Ribbon Atelier","Moon Window","Cloud Bedroom","Pixel Station","Cherry Picnic","Cat Cafe",
        "Bunny Studio","Teddy Bakery","Starlight Desk","Jelly Aquarium","Mushroom Garden","Crystal Vanity",
        "Retro Cassette","Game Lounge","Ocean Shell","Book Nook","Butterfly Gallery","Perfume Shelf",
        "Sunset Balcony","Rainy Loft","Candy Counter","Flower Market","Night Skyline","Aurora Room",
        "Polaroid Wall","Strawberry Milk","Pumpkin Porch","Snow Globe","Lantern Festival",
        "Planet Observatory","Music Corner","Glass Greenhouse"
    };

    static final int[][] PALETTES={
        {0xffffc2d7,0xffffedf5,0xffc55586},{0xff6f72c8,0xffdcdcff,0xff8f74dd},
        {0xff55bce7,0xffdcf7ff,0xff2b89b7},{0xff8ac27f,0xffe9f5df,0xff4b8e59},
        {0xffe1ab73,0xffffedd1,0xffb97943},{0xffbddbf0,0xfff6fbff,0xff729ec0},
        {0xffffa9ce,0xffffeef7,0xffff639e},{0xff263653,0xff85ecff,0xff31bdd9},
        {0xff2b2758,0xff7a78d8,0xffbc92ff},{0xff9d6c4f,0xfff1ddc5,0xff704534},
        {0xffbeddb1,0xfff6f8eb,0xff70985d},{0xffc4cdf0,0xfff7f5ff,0xff8088d3},
        {0xff742d3b,0xffff8b61,0xffffc45b},{0xff74a9ca,0xffe0eef8,0xff4e759e},
        {0xff675fcc,0xff94efdf,0xffb99aff},{0xffffa972,0xffffe4ba,0xffe7687c},
        {0xff3a455f,0xff9aaac7,0xffd8dce9},{0xff43215e,0xffff60d9,0xff4adfff},
        {0xffe5daca,0xfffffbf4,0xffa68c74},{0xff55384d,0xffc9a5bb,0xffd7b17e},
        {0xffdddddf,0xfffafafa,0xff8e8d91},{0xffd1eaf1,0xfff9ffff,0xff7ec9da},
        {0xff271a34,0xffff55da,0xff4be8ff},{0xff28252d,0xffeee8eb,0xff7d707b},
        {0xffbcdb9f,0xfff6f8df,0xffefbb4c},{0xff5cbcc0,0xffdcf5f1,0xffff919d},
        {0xff293047,0xff8a95b8,0xffbacbea},{0xffdc8348,0xffffd4a3,0xff708d4d},
        {0xffd1e9f7,0xfffaf5ff,0xffc9729b},{0xffffc469,0xffffefbb,0xffe9638c},
        {0xff281f5e,0xff6c5fc8,0xff4ed8f0},{0xffa8cc78,0xffffecb9,0xffd66278}
    };

    static final class Entry {
        final int pack,index,world,architecture,scene,start,end,accent,corner,transparency,keyMode;
        final boolean borders,dark,pro;
        final String name,subtitle;
        Entry(int pack,int index,int world,int architecture,int scene,String name,String subtitle,
              int start,int end,int accent,int corner,int transparency,boolean borders,boolean dark,boolean pro,int keyMode){
            this.pack=pack; this.index=index; this.world=world; this.architecture=architecture; this.scene=scene;
            this.name=name; this.subtitle=subtitle; this.start=start; this.end=end; this.accent=accent;
            this.corner=corner; this.transparency=transparency; this.borders=borders; this.dark=dark; this.pro=pro; this.keyMode=keyMode;
        }
    }

    static boolean isNeoPack(int pack){ return pack>=FIRST_PACK && pack<FIRST_PACK+COUNT; }

    static Entry get(int pack){
        if(!isNeoPack(pack)) return null;
        int i=pack-FIRST_PACK;
        int architecture=i%48;
        int cycle=i/48;
        int world=(i*11 + cycle*7)&31;
        int scene=(i*17 + architecture*5 + cycle*13)&31;
        int[] b=PALETTES[world];

        int shift=((architecture*9 + cycle*7)%21)-10;
        int start=shift(b[0],shift*2,shift,-shift);
        int end=mix(shift(b[1],-shift,shift*2,shift),b[2],6+((i*5)%18));
        int accent=mix(shift(b[2],shift,-shift,shift*2),((i&1)==0)?Color.WHITE:Color.BLACK,5+((i*7)%12));

        boolean dark=in(world,1,7,8,12,16,17,19,22,23,26,30) || in(architecture,8,11,20,25,26,30,36,40,41);
        if(dark){ start=mix(start,Color.BLACK,46); end=mix(end,Color.BLACK,58); accent=mix(accent,Color.WHITE,10); }

        int keyMode=(i*7 + architecture*3 + scene)%24;
        int corner=4+((architecture*5 + scene)%22);
        int transparency=66+((i*9)%28);
        boolean borders=((architecture+scene)%4)!=0;
        boolean pro=(i%10)>=3;

        String name=SCENE_NAMES[scene]+" • "+ARCH_NAMES[architecture];
        String subtitle=ARCH_NAMES[architecture].toLowerCase(java.util.Locale.ROOT)+" • "+keyStyleName(keyMode)+" • "+SCENE_NAMES[scene];
        return new Entry(pack,i,world,architecture,scene,name,subtitle,start,end,accent,corner,transparency,borders,dark,pro,keyMode);
    }

    static int[] spec(int pack){
        Entry e=get(pack); if(e==null) return null;
        return new int[]{e.start,e.end,e.accent,e.corner,e.transparency,e.borders?1:0,e.dark?0:1,0,0};
    }

    static boolean matches(int pack,String filter){
        Entry e=get(pack); if(e==null) return false;
        if(filter==null||filter.isEmpty()||"all".equals(filter)||"new".equals(filter)) return true;
        if("free".equals(filter)) return !e.pro;
        if("pro".equals(filter)) return e.pro;
        if("dark".equals(filter)) return e.dark;

        int a=e.architecture,s=e.scene,w=e.world;
        if("cute".equals(filter)||"kawaii".equals(filter)) return in(a,0,1,4,5,15,22,29,38,42,45)||in(s,4,5,6,7,20,25);
        if("aesthetic".equals(filter)) return in(a,1,7,14,16,17,24,27,34,37,43,44,47);
        if("anime-inspired".equals(filter)) return in(a,13,16,18,27,29,30,34,41,44,45);
        if("dreamy".equals(filter)) return in(a,3,18,27,30,43,44,46)||in(s,1,2,8,23,29);
        if("gaming".equals(filter)) return in(a,8,11,20,30,33,36,40)||in(s,3,13,29);
        if("nature".equals(filter)) return in(a,10,21,23,39,46,47)||in(w,0,3,10,24,31);
        if("floral".equals(filter)) return in(a,10,21,39)||in(s,4,10,16,21,31);
        if("food".equals(filter)) return in(a,22,42)||in(s,5,7,20,25);
        if("retro".equals(filter)) return in(a,8,12,28,33,35)||in(s,3,12,30);
        if("y2k".equals(filter)) return in(a,5,14,25,34,36,43,45);
        if("minimal".equals(filter)) return in(a,6,7,31,35,37,47);
        if("luxury".equals(filter)) return in(a,14,24,25,37)||in(w,11,19,20,21);
        if("space".equals(filter)) return in(a,18,30,43)||in(s,1,8,23,29);
        if("ocean".equals(filter)) return in(a,23,46)||in(s,9,14);
        if("city".equals(filter)) return in(a,17,20,41)||in(s,18,22);
        if("seasonal".equals(filter)) return in(s,26,27,28)||in(w,0,5,27,28,29);
        return false;
    }

    static int count(String filter){ int n=0; for(int i=0;i<COUNT;i++) if(matches(FIRST_PACK+i,filter)) n++; return n; }
    static int[] first(String filter,int limit){
        int[] tmp=new int[Math.max(0,Math.min(COUNT,limit))]; int n=0;
        for(int i=0;i<COUNT && n<tmp.length;i++){ int p=FIRST_PACK+i; if(matches(p,filter)) tmp[n++]=p; }
        if(n==tmp.length) return tmp; int[] out=new int[n]; System.arraycopy(tmp,0,out,0,n); return out;
    }

    static int sceneForPack(int pack){ Entry e=get(pack); return e==null?0:e.scene; }
    static String sceneName(int scene){ return SCENE_NAMES[scene&31]; }
    static String architectureName(int architecture){ return ARCH_NAMES[Math.floorMod(architecture,ARCH_NAMES.length)]; }

    static String keyStyleName(int k){
        String[] n={"capsule","pill","cut corner","bevel","rail","gem","ticket","double glass","top notch","underline","split","oval",
                "corner dots","diamond","inset","stepped","circle","squircle","sticker","mechanical","paper tab","plush","pixel","outline"};
        return n[Math.floorMod(k,n.length)];
    }

    private static boolean in(int value,int... values){ for(int v:values) if(value==v) return true; return false; }
    static int mix(int a,int b,int bp){ int p=Math.max(0,Math.min(100,bp)),ap=100-p; return Color.rgb((Color.red(a)*ap+Color.red(b)*p)/100,(Color.green(a)*ap+Color.green(b)*p)/100,(Color.blue(a)*ap+Color.blue(b)*p)/100); }
    static int shift(int c,int r,int g,int b){ return Color.rgb(clamp(Color.red(c)+r),clamp(Color.green(c)+g),clamp(Color.blue(c)+b)); }
    private static int clamp(int v){ return Math.max(0,Math.min(255,v)); }
    private NeoThemeCatalog(){}
}
