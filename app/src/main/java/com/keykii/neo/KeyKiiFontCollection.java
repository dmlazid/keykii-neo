package com.keykii.neo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/** Local-only saved-font and recently applied font history. */
final class KeyKiiFontCollection {
    private static final String FAVORITES="favorite_font_styles";
    private static final String RECENTS="recent_font_styles";
    private static final int RECENT_LIMIT=12;

    private KeyKiiFontCollection(){}

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
                if(value>=0)out.add(value);
            }catch(Exception ignored){}
        }
        return out;
    }

    private static void write(Context context,String key,Iterable<Integer> values){
        StringBuilder b=new StringBuilder();
        for(Integer value:values){
            if(value==null||value<0)continue;
            if(b.length()>0)b.append(',');
            b.append(value);
        }
        prefs(context).edit().putString(key,b.toString()).apply();
    }

    static boolean isFavorite(Context context,int style){
        return read(context,FAVORITES).contains(style);
    }

    static boolean toggleFavorite(Context context,int style){
        LinkedHashSet<Integer> set=read(context,FAVORITES);
        boolean saved;
        if(set.contains(style)){
            set.remove(style);
            saved=false;
        }else{
            set.add(style);
            saved=true;
        }
        write(context,FAVORITES,set);
        return saved;
    }

    static void rememberApplied(Context context,int style){
        if(style<0)return;
        LinkedHashSet<Integer> old=read(context,RECENTS);
        ArrayList<Integer> ordered=new ArrayList<>();
        ordered.add(style);
        for(Integer value:old){
            if(value!=style)ordered.add(value);
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
