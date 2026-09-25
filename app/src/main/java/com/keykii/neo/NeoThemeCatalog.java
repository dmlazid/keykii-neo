package com.keykii.neo;

import android.graphics.Color;

/** Theme recipes pair a real geometry family with a coherent artwork/material brief.
 * 512 compositions are NOT represented as 512 independently illustrated artworks.
 * 32 original illustrated scenes and 48 structural families are shared explicitly.
 */
final class NeoThemeCatalog {
    static final int FIRST_PACK=1000, COUNT=512, PAGE_SIZE=24;
    static final String[] FILTERS={"all","new","free","pro","cute","kawaii","aesthetic","anime-inspired","dreamy","dark","gaming","nature","floral","food","retro","y2k","minimal","luxury","space","ocean","city","seasonal"};
    static final String[] ARCH_NAMES={"Candy Floats","Scrapbook Stickers","Split Mechanical","Cloud Islands","Round Bubbles","Bento Blocks","Ticket Rows","Glass Shelves","Pixel Console","Notebook Tabs","Petal Keys","Arcade Blocks","Ribbon Rows","Asymmetric Cards","Jewel Facets","Plush Keycaps","Picture Frames","City Columns","Orbit Keys","Mosaic Tiles","Cyber Rails","Garden Trellis","Dessert Keys","Ocean Pebbles","Luxury Plaques","Liquid Chrome","Gothic Arches","Dream Bedroom","Retro Cassette","Kawaii Room","Space Console","Minimal Lines","Lantern Steps","Mechanical Deck","Polaroid Wall","Book Pages","Neon Circuit","Frosted Dock","Teddy Shelf","Flower Market","Game Desk","Night Skyline","Candy Counter","Aurora Glass","Moon Window","Sticker Parade","Wave Keys","Zen Stones"};
    static final String[] SCENE_NAMES={"Bunny Atelier","Velvet Rose","Ink Tide","Teddy Bakery","Moonlit Muse","Fruit Ice","Neon Rain","Pressed Memories","Aurora Observatory","Jade Greenhouse","Sakura Path","Field Journal","Pixel Hideaway","Pearl Hologram","Amethyst Abbey","Amber Loft","Lantern Lake","Alpine Frost","Pumpkin Porch","Strawberry Cream","Pearl Aquarium","Sunset Sonata","Dahlia Market","Emerald Deco","Sand & Moss","Carbon Desk","Rose Quartz","Cloud Nursery","Ribbon Linen","Mint Arcade","Rainy Tram","Lotus Reader"};
    static final String[] EDITIONS={"Studio","Zigzag","Terrace","Wide Dock","Mixed Caps","High Notes","Staircase","Island Split","Edge Blocks","Wave Rows","Alternating"};
    // Scene briefs constrain materials and categories. No HSV/palette permutation loop.
    private static final int[][] COLORS={
        {0xffe4c6b2,0xfffff6e9,0xffb46678},{0xff151212,0xff332629,0xffddb77d},{0xff152d45,0xff29465f,0xffb5cecc},{0xffeacac0,0xfffff2e7,0xffac7161},
        {0xff171630,0xff39345d,0xffc9adee},{0xff372123,0xff502b30,0xffe8b16c},{0xff071d28,0xff142f40,0xff45d9e5},{0xffd6c6a9,0xfffaf3e5,0xff947757},
        {0xff17172f,0xff343657,0xffb5a1ec},{0xffb8cbb9,0xfff2f5e5,0xff53867c},{0xffe7b7c5,0xfffff3f3,0xffb66b8d},{0xffdfd0b7,0xfffff7e7,0xff7f9365},
        {0xff2b213a,0xff51405b,0xffcfa4d8},{0xffe6dfef,0xfffbf8ff,0xff9e83b9},{0xff100f18,0xff31253d,0xffbc98de},{0xff2d211b,0xff544033,0xffe8bc7a},
        {0xff2c2224,0xff50423c,0xffe8b278},{0xffb7cfde,0xfff2f9ff,0xff6d97b7},{0xff332619,0xff61422e,0xffeca852},{0xffecc1c4,0xfffff3ed,0xffbb657b},
        {0xff16465b,0xff246880,0xffb4efe5},{0xff302036,0xff644253,0xffffc08b},{0xffe4c6a1,0xfffff4dc,0xffa4773b},{0xff0b2927,0xff23483d,0xffdfc18a},
        {0xffd3ccb9,0xfff6f2e6,0xff849371},{0xff10171b,0xff29333b,0xffee9e4a},{0xffe1b8bf,0xffffeff1,0xffb86d8b},{0xffcedfda,0xfffaf9ef,0xffb1a0c4},
        {0xffdfc8b7,0xfffff5e9,0xffae7680},{0xffaad3c0,0xffeef8e6,0xff4b8e7c},{0xff102631,0xff334453,0xfff2b576},{0xff193933,0xff345b4e,0xffd3bd86}
    };
    private static final String[] TAGS={
        "cute kawaii aesthetic", "dark floral luxury", "ocean nature aesthetic", "cute kawaii food", "anime-inspired dreamy dark", "food cute", "gaming city dark", "aesthetic retro nature floral", "space dreamy dark", "nature floral aesthetic", "floral nature dreamy", "minimal nature floral retro", "gaming retro cute dark", "y2k aesthetic luxury", "dark luxury", "city dreamy dark", "seasonal city dark", "seasonal nature dreamy", "seasonal cute dark", "food cute kawaii", "ocean nature dreamy dark", "anime-inspired city dreamy dark", "floral nature aesthetic", "luxury dark", "minimal nature", "gaming dark", "luxury dreamy aesthetic", "kawaii cute dreamy", "aesthetic floral cute", "retro gaming minimal", "city dark aesthetic", "cute kawaii nature dark"
    };
    // Art direction per structural family. These are deliberate choices, not unrelated
    // backgrounds assigned by modular arithmetic. Editions also alter real geometry.
    private static final int[][] ART={
        {5,19,27,13,26,0}, {7,11,28,10,22,16}, {25,6,29,12,30,23}, {27,4,8,17,26,20},
        {13,26,20,5,27,17}, {0,19,27,3,29,12}, {7,16,15,11,28,24}, {26,17,9,13,20,8},
        {12,29,25,6,8,30}, {11,7,28,24,22,10}, {10,22,1,28,31,9}, {25,12,29,6,13,8},
        {28,0,19,7,3,1}, {7,21,4,30,15,13}, {26,23,14,13,8,17}, {3,0,27,28,19,31},
        {21,4,7,10,15,30}, {6,30,21,15,16,25}, {8,4,20,13,26,27}, {11,10,7,22,29,19},
        {6,25,30,8,13,29}, {9,10,22,31,11,1}, {19,5,3,0,18,27}, {2,20,24,17,9,31},
        {23,1,14,26,16,15}, {13,26,6,8,20,17}, {14,1,23,4,8,30}, {4,27,15,0,3,21},
        {12,29,15,21,25,7}, {0,31,3,27,19,12}, {8,6,25,4,20,17}, {24,11,9,17,28,29},
        {16,18,15,21,1,30}, {25,29,12,6,23,15}, {7,21,4,10,16,22}, {11,7,28,15,31,24},
        {6,25,13,8,30,29}, {17,26,9,20,13,27}, {3,0,27,19,28,18}, {22,10,1,9,11,28},
        {25,12,6,29,8,30}, {30,6,21,16,15,4}, {5,19,3,27,0,18}, {8,13,17,26,4,20},
        {4,8,15,21,27,14}, {0,7,27,31,19,28}, {2,20,17,9,8,13}, {24,9,31,11,2,17}
    };
    // 0 enamel, 1 continuous image mosaic, 2 frosted glass, 3 image caps,
    // 4 illustrated/sticker edges. Kept visible in subtitles for honest catalog labeling.
    private static final int[] MATERIAL={0,4,0,2,2,0,4,2,0,4,4,0,4,3,2,4,3,1,2,1,0,4,4,1,0,2,2,3,0,4,0,2,4,0,3,4,0,2,4,4,0,1,4,2,3,4,1,0};
    static final class Entry {
        final int pack,index,world,architecture,scene,art,composition,material,start,end,accent,corner,transparency,keyMode;
        final boolean borders,dark,pro; final String name,subtitle,tags;
        Entry(int i){
            index=i;pack=FIRST_PACK+i;architecture=i%48;composition=i/48;
            int[] artBrief=ART[architecture];art=artBrief[composition%artBrief.length];world=scene=art;
            material=composition<6?MATERIAL[architecture]:new int[]{1,4,2,3,0}[composition-6];
            start=COLORS[art][0];end=COLORS[art][1];accent=COLORS[art][2];
            tags=TAGS[art];dark=tags.contains("dark");pro=i%10>=3;
            corner=12;transparency=92;borders=true;keyMode=NeoThemeRenderer.SHAPES[architecture];
            String displayName=SCENE_NAMES[art]+" · "+ARCH_NAMES[architecture];
            if(composition>=6)displayName+=" · "+EDITIONS[composition];
            name=displayName;
            subtitle=EDITIONS[composition]+" arrangement · "+new String[]{"enamel","panoramic caps","frosted glass","picture caps","illustrated edges"}[material];
        }
    }
    private static final Entry[] ENTRIES=new Entry[COUNT];
    static {for(int i=0;i<COUNT;i++)ENTRIES[i]=new Entry(i);}
    static boolean isNeoPack(int p){return p>=FIRST_PACK&&p<FIRST_PACK+COUNT;}
    static Entry get(int p){return isNeoPack(p)?ENTRIES[p-FIRST_PACK]:null;}
    static int[] spec(int p){Entry e=get(p);return e==null?null:new int[]{e.start,e.end,e.accent,e.corner,e.transparency,1,e.dark?0:1,0,0};}
    static boolean matches(int p,String filter){
        Entry e=get(p);if(e==null)return false;
        if(filter==null||filter.isEmpty()||filter.equals("all")||filter.equals("new"))return true;
        if(filter.equals("free"))return !e.pro;if(filter.equals("pro"))return e.pro;
        return (" "+e.tags+" ").contains(" "+filter+" ");
    }
    static int count(String f){int n=0;for(Entry e:ENTRIES)if(matches(e.pack,f))n++;return n;}
    static int[] first(String f,int limit){return page(f,0,limit);}
    static int[] page(String f,int offset,int limit){
        int[] tmp=new int[Math.max(0,Math.min(COUNT,limit))];int n=0,skip=0;
        for(Entry e:ENTRIES)if(matches(e.pack,f)){if(skip++<offset)continue;if(n==tmp.length)break;tmp[n++]=e.pack;}
        if(n==tmp.length)return tmp;int[] out=new int[n];System.arraycopy(tmp,0,out,0,n);return out;
    }
    static int sceneForPack(int p){Entry e=get(p);return e==null?0:e.art;}
    static String sceneName(int s){return SCENE_NAMES[Math.floorMod(s,32)];}
    static String architectureName(int a){return ARCH_NAMES[Math.floorMod(a,48)];}
    static String keyStyleName(int k){return "sculpted "+k;}
    static int mix(int a,int b,int bp){int p=Math.max(0,Math.min(100,bp)),q=100-p;return Color.rgb((Color.red(a)*q+Color.red(b)*p)/100,(Color.green(a)*q+Color.green(b)*p)/100,(Color.blue(a)*q+Color.blue(b)*p)/100);}
    private NeoThemeCatalog(){}
}
