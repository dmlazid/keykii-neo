package com.keykii.neo;

final class ColorFontCatalog {
    static final int FIRST_STYLE = 3000;
    static final int COUNT = 300;
    static final int FREE_AD_COUNT = 60;

    static final int[] BASE_STYLES = {100,101,102,103,104,105,106,108,119,126};
    static final String[] BASE_NAMES = {
        "Fredoka","DynaPuff","Rubik Bubbles","Patrick Hand","Lobster",
        "Bungee","Press Start 2P","Pacifico","Baloo 2","Bubblegum Sans"
    };

    static final String[] PALETTE_NAMES = {
        "Cotton Candy","Sakura Pop","Ocean Candy","Mint Berry","Sunset Peach",
        "Grape Soda","Lemon Berry","Neon Dream","Peach Bloom","Blueberry Pop",
        "Rainbow Jelly","Cherry Lime","Aqua Pink","Lavender Sky","Candy Coral",
        "Citrus Pop","Galaxy Glow","Rose Gold","Ice Cream","Tropical Pop",
        "Watermelon","Moon Candy","Bubble Tea","Arcade Glow","Kawaii Pastel",
        "Mermaid","Firework","Jellybean","Cherry Blossom","Aurora"
    };

    static final int[][] PALETTES = {
        {0xffff78b8,0xffa77cff,0xff72c7ff},{0xffff6f9e,0xffffa6c8,0xffffd1df},
        {0xff3aa8ff,0xff61d4ff,0xff8d8bff},{0xff49d7b0,0xff8ce59e,0xffff7eb3},
        {0xffff7858,0xffffb45e,0xffff73a8},{0xff8f5cff,0xffc067ff,0xffff72c6},
        {0xffffd447,0xffff76a9,0xff9b79ff},{0xff00d8ff,0xffff4fd8,0xffa96cff},
        {0xffff9e7a,0xffff7bb4,0xffffc28a},{0xff556dff,0xff8f7dff,0xff66c9ff},
        {0xffff5e78,0xffffa447,0xffffe052},{0xffff476f,0xff8dd84d,0xffffca50},
        {0xff45d7e8,0xffff68b8,0xff7f88ff},{0xffb480ff,0xff78b9ff,0xffff9dcb},
        {0xffff7d88,0xffffa86b,0xffff70bd},{0xffff8d3a,0xffffd73e,0xff7fd85c},
        {0xff5f5cff,0xff9a58ff,0xffff4fc6},{0xffd59069,0xffffb7a7,0xffff7da2},
        {0xffff8fc7,0xffffc765,0xff89dfff},{0xff22c7a9,0xffffb132,0xffff598b},
        {0xffff456d,0xff6bd352,0xffffa3bb},{0xff7e65ff,0xffff77c8,0xff6ac8ff},
        {0xffb4795a,0xffffa6bb,0xff73cdb6},{0xff00e7ff,0xffff3bd4,0xffffdf3a},
        {0xffffa3d7,0xffb8a1ff,0xff87d7ff},{0xff49d5ca,0xff6b9dff,0xffff7fc4},
        {0xffff4d6d,0xffffb83d,0xff48d8ff},{0xffff6fae,0xff72d9ff,0xffffd65c},
        {0xffff6d96,0xffffa8c7,0xffffd8e7},{0xff52d6ff,0xff8f7cff,0xff67e4bd}
    };

    static final class Entry {
        final int style, baseStyle;
        final String name;
        final boolean pro;
        final int[] palette;
        Entry(int style,int baseStyle,String name,boolean pro,int[] palette) {
            this.style=style; this.baseStyle=baseStyle; this.name=name;
            this.pro=pro; this.palette=palette;
        }
    }

    static boolean isColorStyle(int style) {
        return style>=FIRST_STYLE && style<FIRST_STYLE+COUNT;
    }

    static Entry find(int style) {
        if(!isColorStyle(style)) return null;
        int index=style-FIRST_STYLE;
        int paletteIndex=index/BASE_STYLES.length;
        int baseIndex=index%BASE_STYLES.length;
        return new Entry(
            style,
            BASE_STYLES[baseIndex],
            PALETTE_NAMES[paletteIndex]+" "+BASE_NAMES[baseIndex],
            index>=FREE_AD_COUNT,
            PALETTES[paletteIndex]
        );
    }

    static int colorFor(int style,String token,int fallback) {
        Entry entry=find(style);
        if(entry==null) return fallback;
        int hash=0;
        if(token!=null) for(int i=0;i<token.length();i++) hash=(hash*31)+token.charAt(i);
        if(hash==Integer.MIN_VALUE) hash=0;
        hash=Math.abs(hash);
        return entry.palette[hash%entry.palette.length];
    }

    private ColorFontCatalog() {}
}
