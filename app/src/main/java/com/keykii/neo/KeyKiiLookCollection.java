package com.keykii.neo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.ArrayList;

/** Local-only saved theme + font combinations and free-theme rewarded-ad apply helpers. */
final class KeyKiiLookCollection {
    static final String WATCH_FULL_AD_TOAST="Watch the full ad to apply this theme!";
    static final int FIRST_TACTILE_3D_PACK=210;
    static final int LAST_TACTILE_3D_PACK=219;
    static final int[] TACTILE_3D_PACKS={
            210,211,212,213,214,215,216,217,218,219
    };
    private static final String KEY="saved_theme_font_looks";
    private static final int LIMIT=12;

    static final class Look {
        final int pack;
        final int font;
        Look(int pack,int font){this.pack=pack;this.font=font;}
        String key(){return pack+":"+font;}
    }

    private KeyKiiLookCollection(){}

    private static SharedPreferences prefs(Context context){
        return context.getSharedPreferences("keykii_prefs",Context.MODE_PRIVATE);
    }

    private static boolean validPack(int pack){
        return (pack>=100&&pack<=119)||(pack>=200&&pack<=219);
    }

    static boolean isTactile3dPack(int pack){
        return pack>=FIRST_TACTILE_3D_PACK&&pack<=LAST_TACTILE_3D_PACK;
    }

    static int[] tactile3dPacks(){
        return TACTILE_3D_PACKS.clone();
    }

    static String tactileThemeName(int pack){
        switch(pack){
            case 210: return "Carved Walnut Woodcraft";
            case 211: return "Sakura Linen Relief";
            case 212: return "Crystal Tide Diorama";
            case 213: return "Cream Boba Kawaii";
            case 214: return "Obsidian Weather 3D";
            case 215: return "Hydrangea Slate Garden";
            case 216: return "Pastel Glaze Drip";
            case 217: return "Ukiyo Wave Ceramic";
            case 218: return "Fluffy Pastel Cloud";
            case 219: return "Marshmallow Sky Bakery";
            default: return "";
        }
    }

    static int tactileMotifDrawable(int pack){
        switch(pack){
            case 210: return R.drawable.theme_motif_210;
            case 211: return R.drawable.theme_motif_211;
            case 212: return R.drawable.theme_motif_212;
            case 213: return R.drawable.theme_motif_213;
            case 214: return R.drawable.theme_motif_214;
            case 215: return R.drawable.theme_motif_215;
            case 216: return R.drawable.theme_motif_216;
            case 217: return R.drawable.theme_motif_217;
            case 218: return R.drawable.theme_motif_218;
            case 219: return R.drawable.theme_motif_219;
            default: return 0;
        }
    }

    static int keyCycleIndex(String token,int fallbackIndex){
        if(token==null||token.isEmpty())return Math.max(0,fallbackIndex);
        String norm=token.length()==1
                ? token.toLowerCase(java.util.Locale.ROOT)
                : token;
        switch(norm){
            case "1": return 0;
            case "2": return 1;
            case "3": return 2;
            case "4": return 3;
            case "5": return 4;
            case "6": return 5;
            case "7": return 6;
            case "8": return 7;
            case "9": return 8;
            case "0": return 9;
            case "q": return 10;
            case "w": return 11;
            case "e": return 12;
            case "r": return 13;
            case "t": return 14;
            case "y": return 15;
            case "u": return 16;
            case "i": return 17;
            case "o": return 18;
            case "p": return 19;
            case "a": return 20;
            case "s": return 21;
            case "d": return 22;
            case "f": return 23;
            case "g": return 24;
            case "h": return 25;
            case "j": return 26;
            case "k": return 27;
            case "l": return 28;
            case "⇧":
            case "⇪":
            case "SHIFT": return 29;
            case "z": return 30;
            case "x": return 31;
            case "c": return 32;
            case "v": return 33;
            case "b": return 34;
            case "n": return 35;
            case "m": return 36;
            case "⌫":
            case "BACK": return 37;
            case "123":
            case "?123":
            case "ABC":
            case "SYM1":
            case "SYM2": return 38;
            case ",": return 39;
            case "KeyKii":
            case "English":
            case "SPACE": return 40;
            case ".": return 41;
            case "↵":
            case "ENTER": return 42;
            default:
                return Math.max(0,fallbackIndex);
        }
    }

    static boolean isSunsetKey(int pack,String token){
        if(pack!=217||token==null||token.length()!=1)return false;
        char c=Character.toLowerCase(token.charAt(0));
        return c=='g'||c=='h'||c=='j';
    }

    /**
     * Resolves per-key 3D tactile styling matching the studio reference design.
     * Returns [fillStart, fillEnd, strokeColor, skirtColor, inkColor, doubleRimColor].
     */
    static int[] resolveTactileKeyStyle(
            int pack,
            String token,
            int fallbackIndex,
            boolean isSpace,
            boolean isSpecial
    ){
        String safe=token==null ? "" : token;
        boolean isEnter="↵".equals(safe)||"ENTER".equalsIgnoreCase(safe);
        boolean spaceKey=isSpace||"KeyKii".equals(safe)||"SPACE".equalsIgnoreCase(safe)||"English".equalsIgnoreCase(safe);
        boolean modKey=!spaceKey&&(
                isSpecial||
                isEnter||
                "⇧".equals(safe)||
                "⇪".equals(safe)||
                "SHIFT".equalsIgnoreCase(safe)||
                "⌫".equals(safe)||
                "BACK".equalsIgnoreCase(safe)||
                "123".equals(safe)||
                "?123".equals(safe)||
                "ABC".equalsIgnoreCase(safe)||
                "SYM1".equalsIgnoreCase(safe)||
                "SYM2".equalsIgnoreCase(safe)
        );
        int idx=keyCycleIndex(safe,fallbackIndex);

        switch(pack){
            case 210: {
                if(isEnter){
                    return new int[]{0xFF4D87F7,0xFF2D62D4,0xADFFFFFF,0xFF1E4494,0xFFFFFFFF,0};
                }
                if(modKey||spaceKey){
                    return new int[]{0xFF825130,0xFF58341B,0xADFFFFFF,0xFF3C2211,0xFFFFF7ED,0};
                }
                int[][] cycle={
                        {0xFF9C663F,0xFF6E4325},
                        {0xFF8B5834,0xFF5F381E},
                        {0xFFAA744A,0xFF7A4C2B}
                };
                int[] fill=cycle[idx%cycle.length];
                return new int[]{fill[0],fill[1],0x8CE2B080,0xFF3C2211,0xFFFFF7ED,0};
            }
            case 211: {
                if(isEnter){
                    return new int[]{0xFF6DA0EC,0xFF4A7BD1,0x9EF39EB4,0xFF365996,0xFFFFFFFF,0};
                }
                if(spaceKey){
                    return new int[]{0xFFFBF2EC,0xFFF6D8E0,0x9EF39EB4,0xFFBDA8A1,0xFF25212E,0};
                }
                if(modKey){
                    return new int[]{0xFF4A3935,0xFF2F2320,0x9EF39EB4,0xFF1A1311,0xFFFFF5F7,0};
                }
                int[][] cycle={
                        {0xFFFBF2EC,0xFFEFE2D8},
                        {0xFFF8C8D2,0xFFEEA6B6},
                        {0xFFD9E8DE,0xFFC2D4C9},
                        {0xFFCCDCED,0xFFB2C8DF},
                        {0xFFF8E5BE,0xFFECD2A0}
                };
                int[] fill=cycle[idx%cycle.length];
                return new int[]{fill[0],fill[1],0xFFFFFFFF,0xFFBDA8A1,0xFF25212E,0};
            }
            case 212: {
                if(modKey||spaceKey){
                    return new int[]{0xFF4E97B8,0xFF2F7399,0xEBFFFFFF,0xFF235573,0xFFFFFFFF,0};
                }
                int[][] cycle={
                        {0xFFFCE694,0xFFF4D078},
                        {0xFFF4E6CF,0xFFE8D5B7},
                        {0xFFCFEAF2,0xFF8CD2E8},
                        {0xFFF8A387,0xFFE37352}
                };
                int[] fill=cycle[idx%cycle.length];
                return new int[]{fill[0],fill[1],0xE0FFFFFF,0xFF5C8EA6,0xFF1E2A38,0};
            }
            case 213: {
                if(spaceKey){
                    return new int[]{0xFFE8EEF6,0xFFFBF5F9,0xFFFFFFFF,0xFFD7B9AA,0xFF4A3631,0xFFF6DFC4};
                }
                if(modKey){
                    return new int[]{0xFFF6DFC4,0xFFECD0E0,0xFFFFFFFF,0xFFD7B9AA,0xFF4A3631,0xFFF6DFC4};
                }
                return new int[]{0xFFFFFBF8,0xFFF6E6DF,0xFFE8CDB8,0xFFD7B9AA,0xFF4A3631,0xFFF6DFC4};
            }
            case 214: {
                if(modKey||spaceKey){
                    return new int[]{0xFF333128,0xFF181920,0xD9D8B467,0xFF06070A,0xFFF7DFA4,0};
                }
                return new int[]{0xFF24252B,0xFF101116,0x8FD8B467,0xFF06070A,0xFFF7DFA4,0};
            }
            case 215: {
                if(modKey||spaceKey){
                    return new int[]{0xFF536DC9,0xFF3B4F9E,0xB8C7B8F0,0xFF263366,0xFFFFFFFF,0};
                }
                return new int[]{0xFF6B6673,0xFF4A4552,0x7AD4C6EE,0xFF2B2832,0xFFFFFFFF,0};
            }
            case 216: {
                if(modKey||spaceKey){
                    return new int[]{0xFFD9B2C8,0xFF8ABED6,0xFFFFFFFF,0xFF9D8C86,0xFF25212E,0};
                }
                int[][] cycle={
                        {0xFFF9E5DA,0xFFFCEFD6},
                        {0xFFF5D7E4,0xFFE8D8F5},
                        {0xFFDFEDF5,0xFFD6F2EA}
                };
                int[] fill=cycle[idx%cycle.length];
                return new int[]{fill[0],fill[1],0xFFFFFFFF,0xFF9D8C86,0xFF25212E,0};
            }
            case 217: {
                if(modKey||spaceKey){
                    return new int[]{0xFFE3936B,0xFF355C8C,0xB8F5EFE2,0xFF101B27,0xFFFFFFFF,0};
                }
                if(isSunsetKey(217,safe)){
                    return new int[]{0xFFE3936B,0xFFB86646,0xB8F5EFE2,0xFF101B27,0xFFFFFFFF,0};
                }
                return new int[]{0xFF29415A,0xFF1A2B3E,0x73F5EFE2,0xFF101B27,0xFFFFFFFF,0};
            }
            case 218: {
                if(modKey||spaceKey){
                    return new int[]{0xFFE7D3FB,0xFFFCEBC8,0xFFFFFFFF,0xFFC8B9DC,0xFF25212E,0};
                }
                int[][] cycle={
                        {0xFFFAD3E0,0xFFFCE1EC},
                        {0xFFFCEBC8,0xFFFFF4DC},
                        {0xFFD9E6FA,0xFFE9F2FF},
                        {0xFFE7D3FB,0xFFF3E8FF}
                };
                int[] fill=cycle[idx%cycle.length];
                return new int[]{fill[0],fill[1],0xFFFFFFFF,0xFFC8B9DC,0xFF25212E,0};
            }
            case 219: {
                if(modKey||spaceKey){
                    return new int[]{0xFFE8DBFA,0xFFCFEAFA,0xFFFFFFFF,0xFFD5C6DF,0xFF25212E,0};
                }
                int[][] cycle={
                        {0xFFFBD8E4,0xFFFFEBF2},
                        {0xFFFCEBC8,0xFFFFF5DD},
                        {0xFFD8F3EA,0xFFEBFBF6},
                        {0xFFCFEAFA,0xFFE8F6FF}
                };
                int[] fill=cycle[idx%cycle.length];
                return new int[]{fill[0],fill[1],0xFFFFFFFF,0xFFD5C6DF,0xFF25212E,0};
            }
            default:
                return new int[]{0xFFDDD0F4,0xFFFBF8FF,0xFFFFFFFF,0xFFB8A8D0,0xFF25212E,0};
        }
    }

    static int tactileStickerTint(int pack,String token,int defaultAccent){
        if(pack==217){
            if(isSunsetKey(217,token)||"KeyKii".equals(token)||"SPACE".equalsIgnoreCase(token)||"English".equalsIgnoreCase(token)){
                return 0xFFFFF3E2;
            }
            return 0xFFEA8458;
        }
        if(pack==211){
            return 0xFFE86B8B;
        }
        if(pack==212&&("KeyKii".equals(token)||"SPACE".equalsIgnoreCase(token)||"English".equalsIgnoreCase(token))){
            return 0xFFFFE49B;
        }
        return defaultAccent;
    }

    static boolean isFreeThemePack(int pack){
        return KeyKiiThemeCollection.isThemePack(pack)&&!KeyKiiAccess.themeNeedsAccess(pack);
    }

    static boolean isAdGatedThemePack(int pack){
        return KeyKiiThemeCollection.isThemePack(pack)
                &&(isTactile3dPack(pack)||isFreeThemePack(pack)||KeyKiiAccess.themeNeedsAccess(pack));
    }

    static boolean shouldApplyAfterReward(boolean rewardEarned){
        return rewardEarned;
    }

    static void applyThemePackToPrefs(Context context,int pack,int[] spec){
        if(context==null||!validPack(pack)||spec==null||spec.length<9)return;
        prefs(context).edit()
                .putInt("theme_surface_mode",2)
                .putInt("theme",spec[6])
                .putInt("theme_custom_start",spec[0])
                .putInt("theme_custom_end",spec[1])
                .putInt("accent_color",spec[2])
                .putInt("key_corner_radius",spec[3])
                .putInt("theme_transparency",spec[4])
                .putBoolean("theme_key_borders",spec[5]==1)
                .putBoolean("photo_key_borders",spec[5]==1)
                .putInt("theme_decor_style",spec[7])
                .putInt("theme_key_style",spec[8])
                .putBoolean("theme_auto_day_night",false)
                .putInt("keykii_style_pack",pack)
                .apply();
        KeyKiiThemeCollection.rememberApplied(context,pack);
    }

    static void applySavedLookToPrefs(Context context,int pack,int font,int[] spec){
        if(context==null||!validPack(pack)||font<0||spec==null||spec.length<9)return;
        prefs(context).edit()
                .putInt("theme_surface_mode",2)
                .putInt("theme",spec[6])
                .putInt("theme_custom_start",spec[0])
                .putInt("theme_custom_end",spec[1])
                .putInt("accent_color",spec[2])
                .putInt("key_corner_radius",spec[3])
                .putInt("theme_transparency",spec[4])
                .putBoolean("theme_key_borders",spec[5]==1)
                .putBoolean("photo_key_borders",spec[5]==1)
                .putInt("theme_decor_style",spec[7])
                .putInt("theme_key_style",spec[8])
                .putBoolean("theme_auto_day_night",false)
                .putInt("keykii_style_pack",pack)
                .putInt("keyboard_font_style",font)
                .apply();
        KeyKiiThemeCollection.rememberApplied(context,pack);
        KeyKiiFontCollection.rememberApplied(context,font);
    }

    static void runWithFreeThemeRewardedAd(
            Activity activity,
            KeyKiiAds ads,
            Runnable onUserEarnedReward
    ){
        runWithThemeRewardedAd(activity,ads,200,onUserEarnedReward);
    }

    static void runWithThemeRewardedAd(
            Activity activity,
            KeyKiiAds ads,
            int pack,
            Runnable onUserEarnedReward
    ){
        if(activity==null||activity.isFinishing())return;
        if(ads==null){
            Toast.makeText(activity,WATCH_FULL_AD_TOAST,Toast.LENGTH_SHORT).show();
            return;
        }
        if(!ads.isRewardedReady()){
            Toast.makeText(activity,"Loading rewarded ad…",Toast.LENGTH_SHORT).show();
        }
        boolean grant24h=KeyKiiAccess.themeNeedsAccess(pack);
        ads.showRewardedForThemeApply(
                grant24h,
                () -> {
                    if(shouldApplyAfterReward(true)&&onUserEarnedReward!=null){
                        onUserEarnedReward.run();
                    }
                },
                () -> Toast.makeText(activity,WATCH_FULL_AD_TOAST,Toast.LENGTH_SHORT).show()
        );
    }

    static ArrayList<Look> all(Context context){
        ArrayList<Look> out=new ArrayList<>();
        String raw=prefs(context).getString(KEY,"");
        if(raw==null||raw.trim().isEmpty())return out;
        for(String item:raw.split(",")){
            String[] parts=item.split(":");
            if(parts.length!=2)continue;
            try{
                int pack=Integer.parseInt(parts[0].trim());
                int font=Integer.parseInt(parts[1].trim());
                if(validPack(pack)&&font>=0)out.add(new Look(pack,font));
            }catch(Exception ignored){}
            if(out.size()>=LIMIT)break;
        }
        return out;
    }

    private static void write(Context context,ArrayList<Look> looks){
        StringBuilder b=new StringBuilder();
        for(Look look:looks){
            if(look==null||!validPack(look.pack)||look.font<0)continue;
            if(b.length()>0)b.append(',');
            b.append(look.key());
            if(b.toString().split(",").length>=LIMIT)break;
        }
        prefs(context).edit().putString(KEY,b.toString()).apply();
    }

    static boolean contains(Context context,int pack,int font){
        String key=pack+":"+font;
        for(Look look:all(context))if(look.key().equals(key))return true;
        return false;
    }

    static void save(Context context,int pack,int font){
        if(!validPack(pack)||font<0)return;
        ArrayList<Look> old=all(context);
        ArrayList<Look> next=new ArrayList<>();
        next.add(new Look(pack,font));
        String key=pack+":"+font;
        for(Look look:old){
            if(!look.key().equals(key))next.add(look);
            if(next.size()>=LIMIT)break;
        }
        write(context,next);
    }

    static void remove(Context context,int pack,int font){
        String key=pack+":"+font;
        ArrayList<Look> next=new ArrayList<>();
        for(Look look:all(context))
            if(!look.key().equals(key))next.add(look);
        write(context,next);
    }

    static int count(Context context){
        return all(context).size();
    }
}
