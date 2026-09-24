package com.keykii.neo;

final class IllustratedThemeAssets {

    private IllustratedThemeAssets() {
    }

    static boolean isNewPack(int pack) {
        return pack >= 120 && pack <= 139;
    }

    static int art(int pack) {
        switch(pack) {
            case 120: return R.drawable.theme_ill_art_120;
            case 121: return R.drawable.theme_ill_art_121;
            case 122: return R.drawable.theme_ill_art_122;
            case 123: return R.drawable.theme_ill_art_123;
            case 124: return R.drawable.theme_ill_art_124;
            case 125: return R.drawable.theme_ill_art_125;
            case 126: return R.drawable.theme_ill_art_126;
            case 127: return R.drawable.theme_ill_art_127;
            case 128: return R.drawable.theme_ill_art_128;
            case 129: return R.drawable.theme_ill_art_129;
            case 130: return R.drawable.theme_ill_art_130;
            case 131: return R.drawable.theme_ill_art_131;
            case 132: return R.drawable.theme_ill_art_132;
            case 133: return R.drawable.theme_ill_art_133;
            case 134: return R.drawable.theme_ill_art_134;
            case 135: return R.drawable.theme_ill_art_135;
            case 136: return R.drawable.theme_ill_art_136;
            case 137: return R.drawable.theme_ill_art_137;
            case 138: return R.drawable.theme_ill_art_138;
            case 139: return R.drawable.theme_ill_art_139;
            default: return 0;
        }
    }

    static int primarySticker(int pack) {
        switch(pack) {
            case 120: return R.drawable.theme_motif_flower;
            case 121: return R.drawable.theme_motif_moonstar;
            case 122: return R.drawable.theme_motif_bunny;
            case 123: return R.drawable.theme_motif_bear;
            case 124: return R.drawable.theme_motif_butterfly;
            case 125: return R.drawable.theme_motif_star;
            case 126: return R.drawable.theme_motif_strawberry;
            case 127: return R.drawable.theme_motif_cloud;
            case 128: return R.drawable.theme_motif_frog;
            case 129: return R.drawable.theme_motif_bear;
            case 130: return R.drawable.theme_motif_butterfly;
            case 131: return R.drawable.theme_motif_flower;
            case 132: return R.drawable.theme_motif_bubble;
            case 133: return R.drawable.theme_motif_bunny;
            case 134: return R.drawable.theme_motif_cat;
            case 135: return R.drawable.theme_motif_flower;
            case 136: return R.drawable.theme_motif_lotus;
            case 137: return R.drawable.theme_motif_heart;
            case 138: return R.drawable.theme_motif_butterfly;
            case 139: return R.drawable.theme_motif_snow;
            default: return 0;
        }
    }

    static int secondarySticker(int pack) {
        switch(pack) {
            case 120: return R.drawable.theme_motif_bow;
            case 121: return R.drawable.theme_motif_cloud;
            case 122: return R.drawable.theme_motif_cup;
            case 123: return R.drawable.theme_motif_cake;
            case 124: return R.drawable.theme_motif_flower;
            case 125: return R.drawable.theme_motif_bubble;
            case 126: return R.drawable.theme_motif_bow;
            case 127: return R.drawable.theme_motif_moonstar;
            case 128: return R.drawable.theme_motif_leaf;
            case 129: return R.drawable.theme_motif_bow;
            case 130: return R.drawable.theme_motif_flower;
            case 131: return R.drawable.theme_motif_cup;
            case 132: return R.drawable.theme_motif_star;
            case 133: return R.drawable.theme_motif_cup;
            case 134: return R.drawable.theme_motif_bow;
            case 135: return R.drawable.theme_motif_butterfly;
            case 136: return R.drawable.theme_motif_butterfly;
            case 137: return R.drawable.theme_motif_bow;
            case 138: return R.drawable.theme_motif_star;
            case 139: return R.drawable.theme_motif_gift;
            default: return 0;
        }
    }

    static int tertiarySticker(int pack) {
        switch(pack) {
            case 120: return R.drawable.theme_motif_heart;
            case 121: return R.drawable.theme_motif_star;
            case 122: return R.drawable.theme_motif_leaf;
            case 123: return R.drawable.theme_motif_heart;
            case 124: return R.drawable.theme_motif_bow;
            case 125: return R.drawable.theme_motif_moonstar;
            case 126: return R.drawable.theme_motif_heart;
            case 127: return R.drawable.theme_motif_star;
            case 128: return R.drawable.theme_motif_flower;
            case 129: return R.drawable.theme_motif_heart;
            case 130: return R.drawable.theme_motif_bow;
            case 131: return R.drawable.theme_motif_cherry;
            case 132: return R.drawable.theme_motif_moonstar;
            case 133: return R.drawable.theme_motif_bear;
            case 134: return R.drawable.theme_motif_heart;
            case 135: return R.drawable.theme_motif_lotus;
            case 136: return R.drawable.theme_motif_lotus;
            case 137: return R.drawable.theme_motif_flower;
            case 138: return R.drawable.theme_motif_heart;
            case 139: return R.drawable.theme_motif_bow;
            default: return 0;
        }
    }
}
