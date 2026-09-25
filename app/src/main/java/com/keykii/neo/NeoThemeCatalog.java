package com.keykii.neo;

import android.graphics.Color;

final class NeoThemeCatalog {

    static final int FIRST_PACK = 1000;
    static final int COUNT = 512;
    static final int PAGE_SIZE = 24;

    static final String[] FILTERS = {
            "all","new","free","pro","cute","aesthetic","dark","nature",
            "dreamy","gaming","retro","minimal","luxury","space","ocean",
            "food","seasonal"
    };

    static final String[] WORLD_NAMES = {
            "Sakura","Lunar","Ocean","Forest","Desert","Arctic","Candy","Circuit",
            "Cosmos","Coffee","Botanical","Crystal","Lava","Rain","Aurora","Sunset",
            "City","Arcade","Paper","Velvet","Marble","Glass","Neon","Ink",
            "Meadow","Coral","Storm","Pumpkin","Winter","Festival","Galaxy","Orchard"
    };

    static final String[] ARCH_NAMES = {
            "Capsule Grid","Floating Tiles","Split Deck","Ribbon Rows","Halo Keys",
            "Mosaic Board","Orbit Deck","Bento Keys","Prism Stack","Cloud Deck",
            "Glass Dock","Arcade Matrix","Deco Steps","Wave Board","Petal Rows",
            "Jewel Grid","Ticket Keys","Loft Panels","Portal Grid","Soft Brick",
            "Metro Deck","Lantern Rows","Pebble Keys","Circuit Rail","Garden Frame",
            "Velvet Blocks","Sunset Deck","Aurora Steps","Quilt Board","Skyline Keys",
            "Facet Board","Zen Deck"
    };

    static final String[] KEY_NAMES = {
            "soft capsule","pill","cut-corner","beveled","accent-rail","gem",
            "ticket-notch","double-glass","top-notch","underline","diagonal split",
            "oval","corner-dot","diamond-edge","inset","stepped"
    };

    static final int[][] BASE_PALETTES = {
            {0xffffb8cf,0xffffedf5,0xffdc5b91},
            {0xff6878c9,0xffd6ddff,0xff7d78d9},
            {0xff53b7e5,0xffd8f5ff,0xff2986b8},
            {0xff81bb7a,0xffe5f4dc,0xff4a8a55},
            {0xffd9a66d,0xffffedd0,0xffb9733e},
            {0xffb9d8ef,0xfff3fbff,0xff6f9cc2},
            {0xffff9fca,0xffffedf7,0xffff5d9d},
            {0xff24304f,0xff7ce7ff,0xff31bdd9},
            {0xff26224e,0xff7775d9,0xffb88cff},
            {0xff9c6a4d,0xfff0d9bd,0xff6f4332},
            {0xffb9d6ac,0xfff4f6e8,0xff71945f},
            {0xffbdc7ef,0xfff5f4ff,0xff7f86d4},
            {0xff702837,0xffff845b,0xffffc55d},
            {0xff6fa5c8,0xffdcecf8,0xff4f739f},
            {0xff625ac8,0xff8ef1de,0xffb293ff},
            {0xffffa36f,0xffffe0b4,0xffe76377},
            {0xff38425c,0xff95a7c6,0xffd5d9e8},
            {0xff3d1f59,0xffff59d7,0xff48d9ff},
            {0xffe3d8c8,0xfffffbf3,0xffa58a71},
            {0xff503449,0xffc69fb5,0xffd5af7c},
            {0xffd9d8da,0xfff7f7f8,0xff8c8b90},
            {0xffcce7ef,0xfff7ffff,0xff7ec7d8},
            {0xff24162f,0xffff4ed7,0xff48e5ff},
            {0xff242229,0xffebe6e8,0xff7c6e78},
            {0xffb8d79c,0xfff4f7de,0xffefb84c},
            {0xff58b8bc,0xffd9f4f0,0xffff8e9c},
            {0xff252c3f,0xff8792b5,0xffb8c8e9},
            {0xffd87e45,0xffffd19d,0xff6f8c4a},
            {0xffcce7f5,0xfff8f3ff,0xffc76c95},
            {0xffffbf62,0xffffecb6,0xffe85f88},
            {0xff241b58,0xff6a5bc5,0xff49d4ef},
            {0xffa5c975,0xffffe9b4,0xffd35f75}
    };

    static final class Entry {
        final int pack;
        final int index;
        final int world;
        final int architecture;
        final String name;
        final String subtitle;
        final int start;
        final int end;
        final int accent;
        final int corner;
        final int transparency;
        final boolean borders;
        final boolean dark;
        final boolean pro;
        final int keyMode;

        Entry(
                int pack,
                int index,
                int world,
                int architecture,
                String name,
                String subtitle,
                int start,
                int end,
                int accent,
                int corner,
                int transparency,
                boolean borders,
                boolean dark,
                boolean pro,
                int keyMode
        ) {
            this.pack=pack;
            this.index=index;
            this.world=world;
            this.architecture=architecture;
            this.name=name;
            this.subtitle=subtitle;
            this.start=start;
            this.end=end;
            this.accent=accent;
            this.corner=corner;
            this.transparency=transparency;
            this.borders=borders;
            this.dark=dark;
            this.pro=pro;
            this.keyMode=keyMode;
        }
    }

    static boolean isNeoPack(int pack) {
        return pack>=FIRST_PACK && pack<FIRST_PACK+COUNT;
    }

    static Entry get(int pack) {
        if(!isNeoPack(pack)) return null;

        int index=pack-FIRST_PACK;

        // Interleave the catalog so neighboring cards never share the same
        // architecture. 13 and 7 are coprime with 32, so the first 32 cards
        // cycle through all 32 architecture/world slots instead of showing
        // 32 recolors of one keyboard.
        int architecture=(index*13) & 31;
        int world=(index*7 + (index>>5)*11) & 31;

        int[] base=BASE_PALETTES[world];

        int tint=((architecture*13 + world*7)%19)-9;
        int start=shift(base[0],tint*2,tint,-tint);
        int end=mix(
                shift(base[1],-tint,tint*2,tint),
                base[2],
                4 + ((architecture*7 + world*3)%18)
        );
        int accent=mix(
                shift(base[2],tint,-tint,tint*2),
                ((architecture+world)%2==0) ? Color.WHITE : Color.BLACK,
                3 + ((architecture*5 + world)%12)
        );

        boolean dark=isDarkWorld(world) ||
                architecture==11 ||
                architecture==18 ||
                architecture==23 ||
                architecture==25;

        if(dark) {
            start=mix(start,Color.BLACK,42 + ((architecture+world)%18));
            end=mix(end,Color.BLACK,54 + ((architecture*3+world)%20));
            accent=mix(accent,Color.WHITE,8 + ((architecture+world)%12));
        }

        int corner=5 + ((world*3 + architecture*5)%20);
        int transparency=62 + ((world*7 + architecture*11)%30);
        boolean borders=((world+architecture)%5)!=0;
        boolean pro=(index%10)>=3;
        int keyMode=((index*11 + architecture*5 + world*3)%16)+1;
        int scene=((index*19 + architecture*3 + world*5)&31);

        String name=
                WORLD_NAMES[world]+" "+
                sceneName(scene)+" "+
                architectureName(architecture);

        String subtitle=
                sceneName(scene)+" scene • "+
                ARCH_NAMES[architecture].toLowerCase(java.util.Locale.ROOT)+
                " • "+KEY_NAMES[keyMode-1]+" keys";

        return new Entry(
                pack,index,world,architecture,name,subtitle,
                start,end,accent,corner,transparency,borders,dark,pro,keyMode
        );
    }

    static int[] spec(int pack) {
        Entry e=get(pack);
        if(e==null) return null;

        return new int[]{
                e.start,
                e.end,
                e.accent,
                e.corner,
                e.transparency,
                e.borders ? 1 : 0,
                e.dark ? 0 : 1,
                0,
                0
        };
    }

    static boolean matches(int pack,String filter) {
        Entry e=get(pack);
        if(e==null) return false;

        if(filter==null || filter.isEmpty() || "all".equals(filter) || "new".equals(filter))
            return true;

        if("free".equals(filter)) return !e.pro;
        if("pro".equals(filter)) return e.pro;
        if("dark".equals(filter)) return e.dark;

        int w=e.world;
        int a=e.architecture;

        if("cute".equals(filter)) {
            return in(w,0,6,24,25,29,31) || in(a,0,4,9,14,22,28,31);
        }

        if("aesthetic".equals(filter)) {
            return in(w,0,4,10,14,15,18,20,21,24) ||
                    in(a,1,3,5,10,12,17,19,26,27,30,31);
        }

        if("nature".equals(filter)) {
            return in(w,0,3,4,10,24,31) || in(a,14,24,31);
        }

        if("dreamy".equals(filter)) {
            return in(w,1,8,11,13,14,28,30) || in(a,4,6,9,18,27,31);
        }

        if("gaming".equals(filter)) {
            return in(w,7,12,16,17,22,30) || in(a,11,18,20,23,29,30);
        }

        if("retro".equals(filter)) {
            return in(w,9,17,18,19,27,29) || in(a,3,8,11,12,16,20,28);
        }

        if("minimal".equals(filter)) {
            return in(w,5,18,20,21,23) || in(a,0,2,10,17,19,22,31);
        }

        if("luxury".equals(filter)) {
            return in(w,11,19,20,21) || in(a,5,12,15,25,30);
        }

        if("space".equals(filter)) {
            return in(w,1,8,14,30) || in(a,4,6,18,27);
        }

        if("ocean".equals(filter)) {
            return in(w,2,13,25) || in(a,9,13,22);
        }

        if("food".equals(filter)) {
            return in(w,6,9,27,31) || in(a,7,16,19);
        }

        if("seasonal".equals(filter)) {
            return in(w,0,5,27,28,29,31) || in(a,21,26,28);
        }

        return false;
    }

    static int count(String filter) {
        int count=0;
        for(int i=0;i<COUNT;i++) {
            if(matches(FIRST_PACK+i,filter)) count++;
        }
        return count;
    }

    static int[] first(String filter,int limit) {
        int safe=Math.max(0,Math.min(COUNT,limit));
        int[] temp=new int[safe];
        int count=0;

        for(int i=0;i<COUNT && count<safe;i++) {
            int pack=FIRST_PACK+i;
            if(matches(pack,filter)) {
                temp[count++]=pack;
            }
        }

        if(count==temp.length) return temp;

        int[] out=new int[count];
        System.arraycopy(temp,0,out,0,count);
        return out;
    }

    static int sceneForPack(int pack) {
        Entry e=get(pack);
        if(e==null) return 0;
        return ((e.index*19 + e.architecture*3 + e.world*5)&31);
    }

    static String sceneName(int scene) {
        String[] names={
                "Ribbon Atelier","Moon Window","Cloud Bedroom","Pixel Station",
                "Cherry Picnic","Cat Cafe","Bunny Studio","Teddy Bakery",
                "Starlight Desk","Jelly Aquarium","Mushroom Garden","Crystal Vanity",
                "Retro Cassette","Game Lounge","Ocean Shell","Book Nook",
                "Butterfly Gallery","Perfume Shelf","Sunset Balcony","Rainy Loft",
                "Candy Counter","Flower Market","Night Skyline","Aurora Room",
                "Polaroid Wall","Strawberry Milk","Pumpkin Porch","Snow Globe",
                "Lantern Festival","Planet Observatory","Music Corner","Glass Greenhouse"
        };
        return names[scene&31];
    }

    static String architectureName(int architecture) {
        return ARCH_NAMES[architecture&31];
    }

    private static boolean isDarkWorld(int world) {
        return in(world,1,7,8,12,16,17,19,22,23,26,30);
    }

    private static boolean in(int value,int... values) {
        for(int item:values) if(value==item) return true;
        return false;
    }

    static int mix(int a,int b,int bPercent) {
        int p=Math.max(0,Math.min(100,bPercent));
        int ap=100-p;

        return Color.rgb(
                (Color.red(a)*ap + Color.red(b)*p)/100,
                (Color.green(a)*ap + Color.green(b)*p)/100,
                (Color.blue(a)*ap + Color.blue(b)*p)/100
        );
    }

    static int shift(int color,int r,int g,int b) {
        return Color.rgb(
                clamp(Color.red(color)+r),
                clamp(Color.green(color)+g),
                clamp(Color.blue(color)+b)
        );
    }

    private static int clamp(int value) {
        return Math.max(0,Math.min(255,value));
    }

    private NeoThemeCatalog() {}
}
