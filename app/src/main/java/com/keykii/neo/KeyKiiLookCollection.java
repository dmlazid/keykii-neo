package com.keykii.neo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

/** Local-only saved theme + font combinations for the Mine tab. */
final class KeyKiiLookCollection {
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
        return (pack>=100&&pack<=139)||NeoThemeCatalog.isNeoPack(pack);
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
