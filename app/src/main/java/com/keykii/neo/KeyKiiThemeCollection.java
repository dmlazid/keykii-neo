package com.keykii.neo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/** Local-only theme favorites and recently applied theme history. */
final class KeyKiiThemeCollection {
    private static final String FAVORITES="favorite_theme_packs";
    private static final String RECENTS="recent_theme_packs";
    private static final int RECENT_LIMIT=12;

    private KeyKiiThemeCollection(){}

    private static SharedPreferences prefs(Context context){
        return context.getSharedPreferences("keykii_prefs",Context.MODE_PRIVATE);
    }

    private static LinkedHashSet<Integer> read(Context context,String key){
        LinkedHashSet<Integer> out=new LinkedHashSet<>();
        String raw=prefs(context).getString(key,"");
        if(raw==null||raw.trim().isEmpty())return out;
        for(String part:raw.split(",")){
            try{
                int value=Integer.parseInt(part.trim());
                if(isThemePack(value))out.add(value);
            }catch(Exception ignored){}
        }
        return out;
    }

    private static void write(Context context,String key,Iterable<Integer> values){
        StringBuilder b=new StringBuilder();
        for(Integer value:values){
            if(value==null||!isThemePack(value))continue;
            if(b.length()>0)b.append(',');
            b.append(value);
        }
        prefs(context).edit().putString(key,b.toString()).apply();
    }

    private static boolean isThemePack(int pack){
        return (pack>=100&&pack<=139)||NeoThemeCatalog.isNeoPack(pack);
    }

    static boolean isFavorite(Context context,int pack){
        return read(context,FAVORITES).contains(pack);
    }

    static boolean toggleFavorite(Context context,int pack){
        LinkedHashSet<Integer> set=read(context,FAVORITES);
        boolean nowFavorite;
        if(set.contains(pack)){
            set.remove(pack);
            nowFavorite=false;
        }else{
            set.add(pack);
            nowFavorite=true;
        }
        write(context,FAVORITES,set);
        return nowFavorite;
    }

    static void rememberApplied(Context context,int pack){
        if(!isThemePack(pack))return;
        LinkedHashSet<Integer> previous=read(context,RECENTS);
        ArrayList<Integer> ordered=new ArrayList<>();
        ordered.add(pack);
        for(Integer value:previous){
            if(value!=pack)ordered.add(value);
            if(ordered.size()>=RECENT_LIMIT)break;
        }
        write(context,RECENTS,ordered);
    }

    static int[] favorites(Context context,int limit){
        return toArray(read(context,FAVORITES),limit);
    }

    static int[] recents(Context context,int limit){
        return toArray(read(context,RECENTS),limit);
    }

    static int favoriteCount(Context context){
        return read(context,FAVORITES).size();
    }

    private static int[] toArray(LinkedHashSet<Integer> set,int limit){
        int count=Math.min(Math.max(0,limit),set.size());
        int[] out=new int[count];
        int i=0;
        for(Integer value:set){
            if(i>=count)break;
            out[i++]=value;
        }
        return out;
    }
}
