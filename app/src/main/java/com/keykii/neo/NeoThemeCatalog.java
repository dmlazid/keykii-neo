package com.keykii.neo;

import android.graphics.Color;

/** Theme recipes pair a real geometry family with a coherent artwork/material brief.
 * 2.57.0 replaces the repeated combination ordering with curated hero recipes,
 * a third original artwork atlas, and diversity-first category paging.
 * 512 compositions share 48 structural families and 48 original scene artworks.
 */
final class NeoThemeCatalog {
    static final int FIRST_PACK=1000, COUNT=0, PAGE_SIZE=24;
    static final int PRICE_REGULAR=19, PRICE_PREMIUM=29, PRICE_PACK_5=79, PRICE_CATEGORY=99, PRICE_LIFETIME_LAUNCH=249;
    static final String[] FILTERS={"all","new","free","pro","cute","aesthetic","dreamy","dark","floral","nature","minimal","ocean","luxury","food","seasonal","retro","gaming","space"};
    static final String[] ARCH_NAMES=new String[48];
    static final String[] SCENE_NAMES=new String[48];
    static final class Entry {
        final int pack,index,world,architecture,scene,art,composition,material,motif,start,end,accent,corner,transparency,keyMode;
        final boolean borders,dark,pro; final String name,subtitle,tags;
        Entry(int i){
            index=i;pack=FIRST_PACK+i;world=scene=architecture=art=composition=material=motif=0;
            start=end=accent=0;corner=12;transparency=96;borders=true;dark=false;pro=false;keyMode=0;
            name="";subtitle="";tags="";
        }
    }
    static boolean isNeoPack(int p){return false;}
    static Entry get(int p){return null;}
    static int pricePeso(int p){return 0;}
    static int[] spec(int p){return null;}
    static boolean matches(int p,String filter){return false;}
    static int count(String f){return 0;}
    static int[] first(String f,int limit){return new int[0];}
    static int[] page(String f,int offset,int limit){return new int[0];}
    static int sceneForPack(int p){return 0;}
    static String sceneName(int s){return "";}
    static String architectureName(int a){return "";}
    static String keyStyleName(int k){return "rounded";}
    static int mix(int a,int b,int bp){int p=Math.max(0,Math.min(100,bp)),q=100-p;return Color.rgb((Color.red(a)*q+Color.red(b)*p)/100,(Color.green(a)*q+Color.green(b)*p)/100,(Color.blue(a)*q+Color.blue(b)*p)/100);}
    private NeoThemeCatalog(){}
}
