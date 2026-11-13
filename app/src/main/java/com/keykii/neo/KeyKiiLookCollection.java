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

    static boolean isFreeThemePack(int pack){
        return KeyKiiThemeCollection.isThemePack(pack)&&!KeyKiiAccess.themeNeedsAccess(pack);
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
        if(activity==null||activity.isFinishing())return;
        if(ads==null){
            Toast.makeText(activity,WATCH_FULL_AD_TOAST,Toast.LENGTH_SHORT).show();
            return;
        }
        ads.showRewardedForFreeTheme(
                onUserEarnedReward,
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
