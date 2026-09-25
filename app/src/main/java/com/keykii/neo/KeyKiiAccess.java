package com.keykii.neo;

import android.content.Context;
import android.content.SharedPreferences;

/** Shared entitlement rules for ad-earned 24-hour access and future purchases. */
final class KeyKiiAccess {
    static final String PREF_PASS_UNTIL="reward_access_until";
    static final String PREF_LIFETIME_PRO="lifetime_pro_owned";
    static final long PASS_MS=24L*60L*60L*1000L;

    private KeyKiiAccess(){}

    static SharedPreferences prefs(Context context){
        return context.getSharedPreferences("keykii_prefs",Context.MODE_PRIVATE);
    }

    static boolean hasLifetimePro(Context context){
        return prefs(context).getBoolean(PREF_LIFETIME_PRO,false);
    }

    static boolean has24HourPass(Context context){
        return prefs(context).getLong(PREF_PASS_UNTIL,0L)>System.currentTimeMillis();
    }

    static boolean canUsePremium(Context context){
        return hasLifetimePro(context)||has24HourPass(context);
    }

    static long remainingMs(Context context){
        return Math.max(0L,prefs(context).getLong(PREF_PASS_UNTIL,0L)-System.currentTimeMillis());
    }

    static String remainingLabel(Context context){
        long ms=remainingMs(context);
        if(ms<=0L)return "Not active";
        long minutes=(ms+59999L)/60000L;
        long hours=minutes/60L;
        long mins=minutes%60L;
        if(hours>=1L)return hours+"h "+mins+"m remaining";
        return Math.max(1L,mins)+"m remaining";
    }

    static void grant24Hours(Context context){
        prefs(context).edit()
                .putLong(PREF_PASS_UNTIL,System.currentTimeMillis()+PASS_MS)
                .apply();
    }

    static boolean themeNeedsAccess(int pack){
        NeoThemeCatalog.Entry neo=NeoThemeCatalog.get(pack);
        if(neo!=null)return neo.pro;
        switch(pack){
            case 100: case 102: case 106: case 108: case 113: case 117:
            case 120: case 122: case 126: case 128: case 133: case 137:
                return false;
            default:
                return pack>=100&&pack<=139;
        }
    }

    static boolean fontNeedsAccess(int style){
        if(ColorFontCatalog.isColorStyle(style))return true;
        if(style>=RemoteFontCatalog.FIRST_STYLE)return true;
        return (style>=100&&style<=123)||(style>=136&&style<=151);
    }

    /**
     * Ad access is time-limited. When the pass expires, a premium theme/font
     * cannot stay applied forever just because it was selected earlier.
     */
    static void enforceExpiredSelections(Context context){
        if(canUsePremium(context))return;
        SharedPreferences p=prefs(context);
        SharedPreferences.Editor edit=p.edit();
        boolean changed=false;

        int pack=p.getInt("keykii_style_pack",-1);
        if(p.getInt("theme_surface_mode",0)==2&&themeNeedsAccess(pack)){
            edit.putInt("theme_surface_mode",0)
                    .putInt("keykii_style_pack",-1);
            changed=true;
        }

        int font=p.getInt("keyboard_font_style",0);
        if(fontNeedsAccess(font)){
            edit.putInt("keyboard_font_style",0);
            changed=true;
        }

        if(changed)edit.apply();
    }
}
