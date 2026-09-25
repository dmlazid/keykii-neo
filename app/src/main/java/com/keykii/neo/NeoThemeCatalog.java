package com.keykii.neo;

import android.graphics.Color;

/** Theme recipes pair a real geometry family with a coherent artwork/material brief.
 * 2.57.0 replaces the repeated combination ordering with curated hero recipes,
 * a third original artwork atlas, and diversity-first category paging.
 * 512 compositions share 48 structural families and 48 original scene artworks.
 */
final class NeoThemeCatalog {
    static final int FIRST_PACK=1000, COUNT=512, PAGE_SIZE=24;
    static final String[] FILTERS={"all","new","free","pro","cute","kawaii","aesthetic","anime-inspired","dreamy","dark","gaming","nature","floral","food","retro","y2k","minimal","luxury","space","ocean","city","seasonal"};
    static final String[] ARCH_NAMES={"Candy Floats","Scrapbook Stickers","Split Mechanical","Cloud Islands","Round Bubbles","Bento Blocks","Ticket Rows","Glass Shelves","Pixel Console","Notebook Tabs","Petal Keys","Arcade Blocks","Ribbon Rows","Asymmetric Cards","Jewel Facets","Plush Keycaps","Picture Frames","City Columns","Orbit Keys","Mosaic Tiles","Cyber Rails","Garden Trellis","Dessert Keys","Ocean Pebbles","Luxury Plaques","Liquid Chrome","Gothic Arches","Dream Bedroom","Retro Cassette","Kawaii Room","Space Console","Minimal Lines","Lantern Steps","Mechanical Deck","Polaroid Wall","Book Pages","Neon Circuit","Frosted Dock","Teddy Shelf","Flower Market","Game Desk","Night Skyline","Candy Counter","Aurora Glass","Moon Window","Sticker Parade","Wave Keys","Zen Stones"};
    static final String[] SCENE_NAMES={"Bunny Atelier","Velvet Rose","Ink Tide","Teddy Bakery","Moonlit Muse","Fruit Ice","Neon Rain","Pressed Memories","Aurora Observatory","Jade Greenhouse","Sakura Path","Field Journal","Pixel Hideaway","Pearl Hologram","Amethyst Abbey","Amber Loft","Lantern Lake","Alpine Frost","Pumpkin Porch","Strawberry Cream","Pearl Aquarium","Sunset Sonata","Dahlia Market","Emerald Deco","Sand & Moss","Carbon Desk","Rose Quartz","Cloud Nursery","Ribbon Linen","Mint Arcade","Rainy Tram","Lotus Reader","Galaxy Clouds","Felt Patch Garden","Kawaii Cats","Coffee Break","Pink Self Care","Sunset Tulips","Happy Cloud Sky","Painted Star Field","Pastel Brush Village","Cozy Study Desk","Retro Cute Tech","Chocolate Dream","Mosaic Ceramic","Ocean Shells","Botanical Notebook","City Neon Window"};
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
        {0xffdfc8b7,0xfffff5e9,0xffae7680},{0xffaad3c0,0xffeef8e6,0xff4b8e7c},{0xff102631,0xff334453,0xfff2b576},{0xff193933,0xff345b4e,0xffd3bd86},
        {0xff080b29,0xff2b175d,0xffff88b7},{0xfff3e3ca,0xfffff4e5,0xffb78591},{0xfff7c6d7,0xffffedf3,0xffc96f99},{0xffd8d0c6,0xfff7eadb,0xff7c4d35},
        {0xfff6d9e2,0xfffff5f8,0xffd16f95},{0xffff9ab7,0xffffdfaa,0xff5d73c6},{0xff84bff0,0xfffff4df,0xffff9fc5},{0xff173a79,0xff284c72,0xfff2c857},
        {0xfff5a7bf,0xffffd9cf,0xff668aa0},{0xff7a4d53,0xffd77a70,0xffffd58a},{0xffc8b9e6,0xffffe8ef,0xff465d91},{0xff6d3f2b,0xffa8754f,0xfff0d6b2},
        {0xfff6eee0,0xffdceaf0,0xff6d99ad},{0xff9ce9f0,0xff075c83,0xffffd8b4},{0xfff3ead7,0xfffff7e9,0xff678259},{0xff071924,0xff12384a,0xff23d5e2}
    };
    private static final String[] TAGS={
        "cute kawaii aesthetic", "dark floral luxury", "ocean nature aesthetic", "cute kawaii food", "anime-inspired dreamy dark", "food cute", "gaming city dark", "aesthetic retro nature floral", "space dreamy dark", "nature floral aesthetic", "floral nature dreamy", "minimal nature floral retro", "gaming retro cute dark", "y2k aesthetic luxury", "dark luxury", "city dreamy dark", "seasonal city dark", "seasonal nature dreamy", "seasonal cute dark", "food cute kawaii", "ocean nature dreamy dark", "anime-inspired city dreamy dark", "floral nature aesthetic", "luxury dark", "minimal nature", "gaming dark", "luxury dreamy aesthetic", "kawaii cute dreamy", "aesthetic floral cute", "retro gaming minimal", "city dark aesthetic", "cute kawaii nature dark",
        "space dreamy aesthetic kawaii","cute kawaii aesthetic floral","cute kawaii aesthetic","food retro aesthetic cute","cute kawaii aesthetic y2k","floral dreamy nature aesthetic","dreamy cute kawaii aesthetic","space dreamy dark aesthetic",
        "dreamy floral aesthetic nature","aesthetic cute dreamy","retro y2k gaming cute","food cute retro","minimal retro aesthetic","ocean nature dreamy aesthetic","nature floral minimal aesthetic","city gaming dark y2k"
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
    // Compositions 6-10 use a second hand-authored scene set so the later
    // FREE/PRO pages do not recycle the same artwork from compositions 0-5.
    private static final int[][] ALT_ART={
        {15,21,7,4,16},{15,0,21,3,9},{8,2,4,15,1},{14,2,28,31,30},
        {3,10,18,7,11},{15,2,20,9,4},{1,27,6,23,13},{29,6,18,30,14},
        {0,28,18,1,16},{9,26,23,1,29},{11,16,14,0,26},{17,19,14,11,4},
        {23,20,24,9,31},{1,19,26,22,28},{12,29,15,19,0},{25,11,9,14,16},
        {29,31,18,26,20},{9,26,5,10,4},{29,14,31,22,24},{27,26,1,23,2},
        {0,5,11,7,1},{19,24,29,4,12},{24,7,9,14,1},{6,0,13,16,14},
        {12,3,22,9,0},{21,7,30,9,22},{9,25,6,17,27},{18,6,11,19,13},
        {30,22,10,27,18},{8,7,15,13,22},{23,26,15,28,18},{12,3,26,0,23},
        {4,6,0,11,28},{22,14,19,3,4},{26,11,3,29,8},{20,3,21,10,30},
        {1,20,3,18,21},{8,7,15,14,11},{6,24,2,21,17},{0,31,5,15,24},
        {19,22,24,14,1},{25,14,3,18,7},{28,15,30,14,16},{16,14,3,6,0},
        {2,17,10,11,5},{4,14,17,9,12},{27,25,30,28,0},{23,10,25,28,22}
    };

    // Original visual directions inspired by the user's mood-board references:
    // 0 scene, 1 cosmic, 2 felt/patch, 3 doodle/scrapbook, 4 painterly,
    // 5 cozy flatlay, 6 botanical, 7 cafe, 8 bubble/gem/ocean, 9 sticker/candy.
    private static final int[] MOTIF={
        2,6,8,2,1,9,0,3,1,6,6,6,9,8,0,5,
        0,0,9,9,8,4,6,0,6,5,8,3,2,9,0,6,
        1,2,9,7,5,6,1,4,4,5,9,7,6,8,6,0
    };

    // These are the actual 2.57 designs represented in the concept sheets.
    // They are explicit recipes, not labels applied to a generic keyboard.
    private static final String[] HERO_NAMES={
        "Galaxy Clouds · Orbit Keys","Aurora Observatory · Split Mechanical","Cosmic Console · Mechanical Deck","Moon Window · Round Bubbles",
        "Starry Field · Painterly Wave","Felt Patch Garden · Scrapbook","Polaroid Wall · Collage","Coffee Break · Sticker Parade",
        "Kawaii Cats · Pattern Keys","Study Desk · Cozy Flatlay","Strawberry Cream · Dessert","Candy Floats · Bubble Keys",
        "Teddy Bakery · Plush","Sweet Café · Cute Icons","Chocolate Dream · Bento","Field Journal · Book Pages",
        "Zen Stones · Natural","Mosaic Tiles · Ceramic","Botanical Notebook · Tabs","Sand & Moss · Minimal",
        "Rose Quartz · Jewel Facets","Emerald Deco · Luxury","Lantern Lake · Night Skyline","Ocean Shells · Wave",
        "Ink Tide · Painterly","Cute Tech · Retro Console","Dream Bedroom · Moon Window","Neon Rain · Cyber Rails",
        "Cloud Nursery · Cloud Islands","Sticker Parade · Doodle Cards","Pastel Village · Ribbon Rows","City Neon Window · Glass Shelves"
    };
    private static final int[] HERO_ARCH={18,2,33,4,46,1,34,45,29,27,22,0,15,42,5,35,47,19,9,31,14,24,41,23,16,28,44,20,3,13,12,7};
    private static final int[] HERO_ART ={32,8,32,38,39,33,7,35,34,41,19,5,3,35,43,11,24,44,46,24,26,23,16,45,2,42,27,6,27,33,40,47};
    private static final int[] HERO_MOTIF={1,1,1,1,4,2,3,7,9,5,9,9,2,7,7,6,6,6,6,6,8,8,0,8,4,9,5,0,1,3,4,0};

    // A structural family belongs to exactly one tier. This prevents the same
    // architecture from appearing as both FREE and PRO with only a small skin change.
    private static boolean proFamily(int a){
        switch(a){
            case 1: case 3: case 5: case 7: case 9: case 10:
            case 13: case 14: case 15: case 16: case 18: case 19:
            case 21: case 23: case 24: case 25: case 26: case 27:
            case 34: case 35: case 37: case 38: case 39: case 43:
                return true;
            default:
                return false;
        }
    }

    // 0 enamel, 1 continuous image mosaic, 2 frosted glass, 3 image caps,
    // 4 illustrated/sticker edges. Kept visible in subtitles for honest catalog labeling.
    private static final int[] MATERIAL={0,4,0,2,2,0,4,2,0,4,4,0,4,3,2,4,3,1,2,1,0,4,4,1,0,2,2,3,0,4,0,2,4,0,3,4,0,2,4,4,0,1,4,2,3,4,1,0};
    static final class Entry {
        final int pack,index,world,architecture,scene,art,composition,material,motif,start,end,accent,corner,transparency,keyMode;
        final boolean borders,dark,pro; final String name,subtitle,tags;
        Entry(int i){
            index=i;pack=FIRST_PACK+i;
            String displayName;
            if(i<HERO_NAMES.length){
                architecture=HERO_ARCH[i];composition=0;art=HERO_ART[i];motif=HERO_MOTIF[i];
                material=MATERIAL[architecture];pro=proFamily(architecture);displayName=HERO_NAMES[i];
            }else{
                int slot=i-HERO_NAMES.length;
                composition=slot/48;
                int base=slot%48;
                architecture=Math.floorMod(base*17+composition*13,48);
                art=Math.floorMod(base*11+composition*7+architecture*3,48);
                motif=MOTIF[art];
                material=Math.floorMod(MATERIAL[architecture]+composition,5);
                pro=proFamily(architecture);
                displayName=SCENE_NAMES[art]+" · "+ARCH_NAMES[architecture];
                if(composition>0)displayName+=" · "+EDITIONS[composition];
            }
            world=scene=art;
            start=COLORS[art][0];end=COLORS[art][1];accent=COLORS[art][2];
            tags=TAGS[art];dark=tags.contains("dark");
            corner=12;transparency=92;borders=true;keyMode=NeoThemeRenderer.SHAPES[architecture];
            name=displayName;
            subtitle=EDITIONS[Math.floorMod(composition,EDITIONS.length)]+" arrangement · "+new String[]{"enamel","panoramic caps","frosted glass","picture caps","illustrated edges"}[material]+" · "+new String[]{"scene","cosmic","felt patch","scrapbook doodle","painterly","cozy flatlay","botanical","cafe","bubble gem","sticker candy"}[motif];
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
    private static final java.util.HashMap<String,int[]> ORDER_CACHE=new java.util.HashMap<>();
    static int count(String f){return order(f).length;}
    static int[] first(String f,int limit){return page(f,0,limit);}
    private static synchronized int[] order(String f){
        String key=f==null?"all":f;
        int[] cached=ORDER_CACHE.get(key);if(cached!=null)return cached;
        java.util.ArrayList<Integer>[] buckets=new java.util.ArrayList[48];
        for(int a=0;a<48;a++)buckets[a]=new java.util.ArrayList<>();
        for(Entry e:ENTRIES)if(matches(e.pack,key))buckets[e.architecture].add(e.pack);
        java.util.ArrayList<Integer> out=new java.util.ArrayList<>();
        int round=0;
        while(true){
            boolean added=false;
            for(int step=0;step<48;step++){
                int a=Math.floorMod(step*17+round*13,48);
                if(round<buckets[a].size()){out.add(buckets[a].get(round));added=true;}
            }
            if(!added)break;round++;
        }
        int[] arr=new int[out.size()];for(int i=0;i<arr.length;i++)arr[i]=out.get(i);
        ORDER_CACHE.put(key,arr);return arr;
    }
    static int[] page(String f,int offset,int limit){
        int[] all=order(f);int start=Math.max(0,Math.min(offset,all.length));
        int n=Math.max(0,Math.min(limit,all.length-start));int[] out=new int[n];
        System.arraycopy(all,start,out,0,n);return out;
    }
    static int sceneForPack(int p){Entry e=get(p);return e==null?0:e.art;}
    static String sceneName(int s){return SCENE_NAMES[Math.floorMod(s,48)];}
    static String architectureName(int a){return ARCH_NAMES[Math.floorMod(a,48)];}
    static String keyStyleName(int k){return "sculpted "+k;}
    static int mix(int a,int b,int bp){int p=Math.max(0,Math.min(100,bp)),q=100-p;return Color.rgb((Color.red(a)*q+Color.red(b)*p)/100,(Color.green(a)*q+Color.green(b)*p)/100,(Color.blue(a)*q+Color.blue(b)*p)/100);}
    private NeoThemeCatalog(){}
}
