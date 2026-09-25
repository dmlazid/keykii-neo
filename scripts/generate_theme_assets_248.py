#!/usr/bin/env python3
from PIL import Image, ImageDraw
import os, math, random

W,H=1200,720
OUT="app/src/main/res/drawable-nodpi"
os.makedirs(OUT,exist_ok=True)

def rgb(h):
    h=h.lstrip("#")
    return tuple(int(h[i:i+2],16) for i in (0,2,4))

def mix(a,b,t):
    return tuple(round(a[i]*(1-t)+b[i]*t) for i in range(3))

def gradient(c1,c2):
    c1,c2=rgb(c1),rgb(c2)
    im=Image.new("RGBA",(W,H))
    p=im.load()
    for y in range(H):
        t=y/(H-1)
        c=mix(c1,c2,t)
        for x in range(W):
            p[x,y]=(*c,255)
    return im

def rr(d,b,r,fill=None,outline=None,width=1):
    d.rounded_rectangle(b,radius=r,fill=fill,outline=outline,width=width)

def flower(d,x,y,s,petal,center,a=255):
    for ang in range(0,360,72):
        z=math.radians(ang)
        cx=x+math.cos(z)*s*.34
        cy=y+math.sin(z)*s*.34
        d.ellipse((cx-s*.27,cy-s*.35,cx+s*.27,cy+s*.35),fill=(*petal,a))
    d.ellipse((x-s*.16,y-s*.16,x+s*.16,y+s*.16),fill=(*center,a))

def leaf(d,x,y,s,c,a=255):
    d.ellipse((x-s*.55,y-s*.2,x+s*.55,y+s*.2),fill=(*c,a))

def heart(d,x,y,s,c,a=255):
    d.ellipse((x-s*.45,y-s*.2,x-s*.02,y+s*.25),fill=(*c,a))
    d.ellipse((x+s*.02,y-s*.2,x+s*.45,y+s*.25),fill=(*c,a))
    d.polygon([(x-s*.42,y+s*.05),(x+s*.42,y+s*.05),(x,y+s*.55)],fill=(*c,a))

def bow(d,x,y,s,c,hl,a=255):
    d.polygon([(x,y),(x-s*.75,y-s*.35),(x-s*.95,y-s*.08),(x-s*.72,y+s*.35),(x-s*.12,y+s*.18)],fill=(*c,a))
    d.polygon([(x,y),(x+s*.75,y-s*.35),(x+s*.95,y-s*.08),(x+s*.72,y+s*.35),(x+s*.12,y+s*.18)],fill=(*c,a))
    rr(d,(x-s*.18,y-s*.18,x+s*.18,y+s*.2),max(4,int(s*.1)),(*hl,a))
    d.polygon([(x-s*.08,y+s*.12),(x-s*.35,y+s*.85),(x,y+s*.55)],fill=(*c,a))
    d.polygon([(x+s*.08,y+s*.12),(x+s*.35,y+s*.85),(x,y+s*.55)],fill=(*c,a))

def bunny(d,x,y,s,body,accent,a=255):
    ow=max(2,int(s*.03))
    outline=(105,78,88,a)
    d.ellipse((x-s*.4,y-s*.9,x-s*.08,y-s*.25),fill=(*body,a),outline=outline,width=ow)
    d.ellipse((x+s*.08,y-s*.9,x+s*.4,y-s*.25),fill=(*body,a),outline=outline,width=ow)
    d.ellipse((x-s*.55,y-s*.45,x+s*.55,y+s*.55),fill=(*body,a),outline=outline,width=ow)
    d.ellipse((x-s*.22,y-s*.65,x-s*.12,y-s*.33),fill=(*accent,a))
    d.ellipse((x+s*.12,y-s*.65,x+s*.22,y-s*.33),fill=(*accent,a))
    d.ellipse((x-s*.2,y-s*.05,x-s*.12,y+s*.03),fill=(55,45,55,a))
    d.ellipse((x+s*.12,y-s*.05,x+s*.2,y+s*.03),fill=(55,45,55,a))
    d.ellipse((x-s*.04,y+s*.12,x+s*.04,y+s*.18),fill=(*accent,a))

def bear(d,x,y,s,body,accent,a=255):
    ow=max(2,int(s*.03))
    outline=(92,66,58,a)
    d.ellipse((x-s*.5,y-s*.48,x-s*.12,y-s*.1),fill=(*body,a),outline=outline,width=ow)
    d.ellipse((x+s*.12,y-s*.48,x+s*.5,y-s*.1),fill=(*body,a),outline=outline,width=ow)
    d.ellipse((x-s*.55,y-s*.4,x+s*.55,y+s*.58),fill=(*body,a),outline=outline,width=ow)
    d.ellipse((x-s*.16,y+s*.08,x+s*.16,y+s*.32),fill=(*accent,a))
    d.ellipse((x-s*.2,y-s*.05,x-s*.12,y+s*.03),fill=(60,45,40,a))
    d.ellipse((x+s*.12,y-s*.05,x+s*.2,y+s*.03),fill=(60,45,40,a))

def frog(d,x,y,s,body,cream,a=255):
    ow=max(2,int(s*.03))
    d.ellipse((x-s*.55,y-s*.35,x+s*.55,y+s*.55),fill=(*body,a),outline=(48,95,52,a),width=ow)
    d.ellipse((x-s*.48,y-s*.58,x-s*.06,y-s*.15),fill=(*body,a),outline=(48,95,52,a),width=ow)
    d.ellipse((x+s*.06,y-s*.58,x+s*.48,y-s*.15),fill=(*body,a),outline=(48,95,52,a),width=ow)
    d.ellipse((x-s*.36,y-s*.48,x-s*.22,y-s*.34),fill=(*cream,a))
    d.ellipse((x+s*.22,y-s*.48,x+s*.36,y-s*.34),fill=(*cream,a))
    d.arc((x-s*.18,y+s*.03,x+s*.18,y+s*.22),0,180,fill=(48,90,48,a),width=ow)

def cloud(d,x,y,s,c,a=255):
    d.ellipse((x-s*.65,y-s*.1,x-s*.12,y+s*.33),fill=(*c,a))
    d.ellipse((x-s*.3,y-s*.45,x+s*.2,y+s*.33),fill=(*c,a))
    d.ellipse((x+s*.05,y-s*.25,x+s*.6,y+s*.33),fill=(*c,a))
    rr(d,(x-s*.62,y,x+s*.58,y+s*.35),int(s*.15),(*c,a))

def moon(d,x,y,s,c,bg,a=255):
    d.ellipse((x-s*.5,y-s*.5,x+s*.5,y+s*.5),fill=(*c,a))
    d.ellipse((x-s*.1,y-s*.55,x+s*.55,y+s*.38),fill=(*bg,255))

def star(d,x,y,s,c,a=255):
    pts=[]
    for i in range(10):
        z=-math.pi/2+i*math.pi/5
        r=s if i%2==0 else s*.42
        pts.append((x+math.cos(z)*r,y+math.sin(z)*r))
    d.polygon(pts,fill=(*c,a))

def jelly(d,x,y,s,c1,c2,a=255):
    d.pieslice((x-s*.5,y-s*.48,x+s*.5,y+s*.2),180,360,fill=(*c1,a))
    d.rectangle((x-s*.5,y-s*.12,x+s*.5,y+s*.08),fill=(*c1,a))
    for dx in (-.32,-.12,.12,.32):
        pts=[]
        for j in range(12):
            pts.append((x+s*dx+math.sin(j*.9)*s*.05,y+s*.06+j*s*.07))
        d.line(pts,fill=(*c2,a),width=max(2,int(s*.04)))

def cup(d,x,y,s,body,accent,a=255):
    rr(d,(x-s*.5,y-s*.15,x+s*.28,y+s*.5),int(s*.12),(*body,a))
    d.arc((x+s*.12,y-s*.03,x+s*.62,y+s*.44),-75,80,fill=(*accent,a),width=max(2,int(s*.06)))
    for off in (-.18,.04,.22):
        d.arc((x+s*off,y-s*.62,x+s*(off+.22),y-s*.18),185,355,fill=(*accent,a),width=max(2,int(s*.05)))

def cake(d,x,y,s,b,c,ch,a=255):
    rr(d,(x-s*.55,y,x+s*.55,y+s*.42),int(s*.08),(*b,a))
    d.pieslice((x-s*.58,y-s*.28,x+s*.58,y+s*.22),180,360,fill=(*c,a))
    d.ellipse((x-s*.09,y-s*.43,x+s*.09,y-s*.25),fill=(*ch,a))

def strawberry(d,x,y,s,body,leafc,a=255):
    d.polygon([(x,y+s*.58),(x-s*.45,y-s*.06),(x-s*.3,y-s*.38),(x+s*.3,y-s*.38),(x+s*.45,y-s*.06)],fill=(*body,a))
    d.polygon([(x-s*.32,y-s*.35),(x-s*.1,y-s*.6),(x,y-s*.4),(x+s*.12,y-s*.62),(x+s*.32,y-s*.35)],fill=(*leafc,a))
    for ox,oy in [(-.15,0),(.12,-.02),(-.02,.22)]:
        d.ellipse((x+s*ox-2,y+s*oy-2,x+s*ox+2,y+s*oy+2),fill=(255,245,200,a))

def cherry(d,x,y,s,red,green,a=255):
    d.line((x,y-s*.45,x-s*.2,y-s*.05),fill=(*green,a),width=max(2,int(s*.05)))
    d.line((x,y-s*.45,x+s*.2,y-s*.05),fill=(*green,a),width=max(2,int(s*.05)))
    d.ellipse((x-s*.38,y-s*.05,x-s*.02,y+s*.32),fill=(*red,a))
    d.ellipse((x+s*.02,y-s*.05,x+s*.38,y+s*.32),fill=(*red,a))

def shell(d,x,y,s,c1,c2,a=255):
    d.pieslice((x-s*.5,y-s*.35,x+s*.5,y+s*.48),180,360,fill=(*c1,a))
    for i in range(-3,4):
        d.line((x,y+s*.02,x+i*s*.11,y-s*.23),fill=(*c2,a),width=max(1,int(s*.03)))

def bottle(d,x,y,s,body,accent,a=255):
    rr(d,(x-s*.24,y-s*.55,x+s*.24,y+s*.48),int(s*.09),(*body,a),outline=(*accent,a),width=max(2,int(s*.03)))
    rr(d,(x-s*.12,y-s*.78,x+s*.12,y-s*.52),int(s*.05),(*accent,a))
    heart(d,x,y+s*.02,s*.25,accent,a)

def gift(d,x,y,s,c1,c2,a=255):
    d.rectangle((x-s*.5,y-s*.12,x+s*.5,y+s*.48),fill=(*c1,a))
    d.rectangle((x-s*.06,y-s*.12,x+s*.06,y+s*.48),fill=(*c2,a))
    d.rectangle((x-s*.55,y-s*.28,x+s*.55,y-s*.08),fill=(*c1,a))
    bow(d,x,y-s*.32,s*.45,c2,c2,a)

def paw(d,x,y,s,c,a=255):
    d.ellipse((x-s*.18,y,x+s*.18,y+s*.26),fill=(*c,a))
    for ox,oy in [(-.25,-.14),(-.08,-.25),(.1,-.25),(.27,-.13)]:
        d.ellipse((x+s*ox-s*.08,y+s*oy-s*.08,x+s*ox+s*.08,y+s*oy+s*.08),fill=(*c,a))

def sparkles(d,c,n,seed,a=125):
    rrn=random.Random(seed)
    cc=rgb(c)
    for _ in range(n):
        x=rrn.randint(30,W-30); y=rrn.randint(28,H-28); r=rrn.randint(3,8)
        d.polygon([(x,y-r),(x+r*.3,y-r*.25),(x+r,y),(x+r*.3,y+r*.25),(x,y+r),(x-r*.3,y+r*.25),(x-r,y),(x-r*.3,y-r*.25)],fill=(*cc,a))

def frame(d,accent):
    ac=rgb(accent)
    rr(d,(16,16,W-17,H-17),46,(255,255,255,30),outline=(*ac,85),width=3)
    rr(d,(34,34,W-35,H-35),38,None,outline=(255,255,255,65),width=2)

def edge_repeat(d,kind,c1,c2):
    for i,x in enumerate(range(55,1150,82)):
        for y in (45,675):
            s=13+(i%3)*4
            if kind=="flower": flower(d,x,y,s,c1,c2,120)
            elif kind=="heart": heart(d,x,y,s,c1,100)
            elif kind=="star": star(d,x,y,s,c1,125)
            elif kind=="leaf": leaf(d,x,y,s,c1,100)
            elif kind=="cherry": cherry(d,x,y,s,c1,c2,115)
            elif kind=="strawberry": strawberry(d,x,y,s,c1,c2,110)

def render(tid,c1,c2,accent,kind):
    im=gradient(c1,c2)
    ov=Image.new("RGBA",(W,H),(0,0,0,0))
    d=ImageDraw.Draw(ov,"RGBA")
    ac=rgb(accent); white=(255,255,255); bg=rgb(c1)
    frame(d,accent)
    rr(d,(105,112,W-105,H-95),42,(255,255,255,26))

    # subtle structural texture
    if kind in ("sakura","matcha","peach","teddy","daisy","cherry","strawberry","caramel"):
        for x in range(0,W,92):
            d.rectangle((x,0,x+44,H),fill=(255,255,255,14))
    elif kind in ("violet","diary","porcelain"):
        for y in range(65,H,58):
            d.line((25,y,W-25,y),fill=(*ac,22),width=1)
    else:
        sparkles(d,"#FFFFFF",50,tid,100)

    if kind=="sakura":
        edge_repeat(d,"flower",rgb("#F27FAA"),rgb("#FFE6A5"))
        d.arc((-80,-40,430,230),195,350,fill=(120,90,65,150),width=10)
        for x,y,s in [(85,90,50),(170,58,36),(255,98,43),(1000,85,52),(1110,120,36),(95,610,58),(250,650,42),(985,632,52),(1120,595,38)]:
            flower(d,x,y,s,rgb("#F38DB4"),rgb("#FFE8A4"),235)
        bunny(d,1070,172,72,white,rgb("#F4B7CA"),230)
        bow(d,600,635,68,rgb("#E76599"),rgb("#FFD9E7"),235)
        rr(d,(930,255,1080,350),18,(*rgb("#C99566"),215),outline=(*rgb("#9C6842"),220),width=4)
        d.arc((955,210,1055,320),190,350,fill=(*rgb("#9C6842"),220),width=6)

    elif kind=="blueberry":
        edge_repeat(d,"star",rgb("#FFF3A6"),white)
        moon(d,1020,92,92,rgb("#FFF0A8"),bg,240)
        for x,y,s in [(110,85,76),(365,65,52),(900,175,54),(1080,560,66)]:
            cloud(d,x,y,s,white,150)
        bow(d,600,65,54,rgb("#8B72DC"),rgb("#D7CBFF"),210)
        for x,y in [(90,600),(175,630),(260,590),(1000,610),(1090,575)]:
            for ox,oy in [(0,0),(28,7),(14,30)]:
                d.ellipse((x+ox-16,y+oy-16,x+ox+16,y+oy+16),fill=(*rgb("#5967B8"),210))

    elif kind=="matcha":
        edge_repeat(d,"leaf",rgb("#5C944F"),rgb("#F3D667"))
        for x,y,s in [(70,90,55),(170,150,34),(1060,130,45),(1030,590,48)]:
            flower(d,x,y,s,white,rgb("#F0D36D"),220)
        bunny(d,1035,145,72,white,rgb("#F3C5CF"),230)
        cup(d,135,125,70,rgb("#6EA25F"),white,230)
        d.arc((92,84,178,151),10,330,fill=(255,255,255,180),width=7)
        frog(d,1030,590,58,rgb("#78B96C"),rgb("#F7F1D1"),220)

    elif kind=="peach":
        edge_repeat(d,"heart",rgb("#E78982"),white)
        bear(d,160,115,74,rgb("#D7A17D"),rgb("#F6D9C6"),230)
        cake(d,610,105,76,rgb("#FFD78B"),white,rgb("#E96370"),235)
        for x,y,s in [(1000,100,48),(1085,155,34),(100,610,42),(1080,600,38)]:
            flower(d,x,y,s,white,rgb("#E6B65D"),220)
        strawberry(d,1010,570,48,rgb("#EE797A"),rgb("#68A061"),220)
        bow(d,570,635,58,rgb("#E78E88"),rgb("#FFDAD0"),210)

    elif kind=="violet":
        edge_repeat(d,"flower",rgb("#9B73C8"),white)
        for x in range(40,W-20,42):
            d.ellipse((x-20,22,x+20,58),outline=(255,255,255,125),width=3)
            d.ellipse((x-20,H-58,x+20,H-22),outline=(255,255,255,125),width=3)
        bow(d,115,86,70,rgb("#8A60BC"),rgb("#D9C0F1"),230)
        bow(d,1020,615,54,rgb("#9B76C8"),rgb("#E8D8F7"),200)
        for x,y,s in [(980,100,45),(1080,190,36),(140,585,34)]:
            d.ellipse((x-s*.6,y-s*.35,x,y+s*.3),fill=(*rgb("#8E62C1"),180))
            d.ellipse((x,y-s*.35,x+s*.6,y+s*.3),fill=(*rgb("#B89AE0"),180))
            d.line((x,y-s*.3,x,y+s*.45),fill=(*rgb("#694394"),220),width=3)
        bottle(d,935,560,48,rgb("#EFE1FF"),rgb("#8B63BE"),205)

    elif kind=="moon":
        edge_repeat(d,"star",rgb("#FFF1A0"),white)
        moon(d,1010,100,88,rgb("#FFF0A5"),bg,235)
        for x,y,s in [(130,90,70),(330,70,48),(900,190,55),(1100,600,70)]:
            cloud(d,x,y,s,white,120)
        for x,y,s in [(130,590,60),(1040,555,58),(990,245,38)]:
            jelly(d,x,y,s,rgb("#8898F2"),rgb("#DAE0FF"),210)

    elif kind=="frog":
        edge_repeat(d,"flower",white,rgb("#E6C65C"))
        frog(d,150,115,75,rgb("#70B064"),rgb("#FFF7D8"),230)
        frog(d,1030,590,68,rgb("#68A95E"),rgb("#FFF4D0"),220)
        for x,y,s in [(80,580,50),(250,620,35),(1000,95,42),(1110,140,28)]:
            flower(d,x,y,s,white,rgb("#E9C660"),210)
        cup(d,980,135,56,rgb("#86B878"),white,170)

    elif kind=="teddy":
        edge_repeat(d,"heart",rgb("#B36B67"),rgb("#FBE5DB"))
        bear(d,1010,110,78,rgb("#BC806D"),rgb("#F4D0C3"),230)
        bear(d,120,610,58,rgb("#C58A78"),rgb("#F8DCCF"),220)
        cake(d,610,105,65,rgb("#E5A5A1"),white,rgb("#D94A68"),230)
        rr(d,(820,220,1140,255),12,(*rgb("#B77D68"),110))
        for x in (865,960,1050):
            cake(d,x,210,35,rgb("#E7B09E"),white,rgb("#D45B70"),200)
        bow(d,600,640,58,rgb("#A96868"),rgb("#F3D3CC"),210)

    elif kind=="snow":
        edge_repeat(d,"star",white,rgb("#E7698D"))
        gift(d,1050,120,72,rgb("#E66489"),white,230)
        d.ellipse((75,70,190,185),fill=(255,255,255,210),outline=(170,195,220,160),width=3)
        d.ellipse((95,25,170,100),fill=(255,255,255,220),outline=(170,195,220,160),width=3)
        d.ellipse((120,55,128,63),fill=(60,60,70,230))
        d.ellipse((145,55,153,63),fill=(60,60,70,230))
        bow(d,1010,605,54,rgb("#E4668A"),rgb("#FFD8E7"),200)

    elif kind=="ocean":
        for y in (70,130,600,650):
            d.arc((-80,y-30,W+80,y+80),180,350,fill=(*rgb("#2A8DBD"),70),width=5)
        for x,y,s in [(100,105,68),(1080,135,58),(1040,580,65)]:
            jelly(d,x,y,s,rgb("#86D7F2"),white,210)
        for x,y,s in [(250,95,48),(930,630,44),(120,615,40)]:
            shell(d,x,y,s,rgb("#FFD8E6"),rgb("#E9A3B8"),205)
        bottle(d,960,100,52,(225,248,255),rgb("#4B9FC8"),190)
        for i,x in enumerate(range(70,1150,88)):
            r=8+(i%3)*3
            d.ellipse((x-r,680-r,x+r,680+r),outline=(255,255,255,150),width=3)

    elif kind=="daisy":
        edge_repeat(d,"flower",white,rgb("#E2C15D"))
        for x,y,s in [(80,95,48),(210,65,32),(1030,90,42),(1100,620,36),(110,600,44)]:
            flower(d,x,y,s,white,rgb("#E0BD55"),220)
        bottle(d,1010,180,55,(255,250,236),rgb("#9EA982"),200)
        bear(d,1030,580,55,rgb("#E8B17A"),rgb("#FFF3DE"),205)
        bow(d,590,640,54,rgb("#9DB482"),rgb("#F0F2D8"),180)

    elif kind=="cherry":
        edge_repeat(d,"cherry",rgb("#D93866"),rgb("#5C9A60"))
        for x,y,s in [(80,95,48),(1070,90,45),(1050,600,42),(120,620,38)]:
            cherry(d,x,y,s,rgb("#D93765"),rgb("#5B9E62"),220)
        bow(d,600,62,56,rgb("#DB4A79"),rgb("#FFD2E0"),220)

    elif kind=="cocoa":
        edge_repeat(d,"star",rgb("#E4BD7C"),rgb("#F5D8A2"))
        rr(d,(30,30,W-30,H-30),42,(*rgb("#2A1B16"),150),outline=(*rgb("#D3A56B"),140),width=3)
        bear(d,1050,125,82,rgb("#A56F4B"),rgb("#E1B995"),230)
        cup(d,155,120,70,rgb("#F4E2D2"),rgb("#D3A56B"),220)
        for x,y,s in [(120,610,45),(300,640,34),(980,620,40)]:
            heart(d,x,y,s,rgb("#D3A56B"),120)

    elif kind=="diary":
        edge_repeat(d,"flower",rgb("#A184D1"),white)
        for y in range(70,H,55):
            d.line((40,y,W-40,y),fill=(*rgb("#8A62B6"),20),width=1)
        d.line((140,30,140,H-30),fill=(*rgb("#C7688F"),50),width=2)
        bow(d,100,90,60,rgb("#8A5DBA"),rgb("#DCCAF1"),220)
        rr(d,(910,65,1100,165),18,(255,255,255,80),outline=(*rgb("#8A62B6"),70),width=2)
        d.line((940,100,1060,100),fill=(*rgb("#8A62B6"),80),width=3)
        d.line((940,125,1035,125),fill=(*rgb("#8A62B6"),60),width=3)

    elif kind=="strawberry":
        edge_repeat(d,"strawberry",rgb("#E94D76"),rgb("#5C9E61"))
        for x,y,s in [(90,95,48),(1070,90,44),(1030,590,48),(125,610,42)]:
            strawberry(d,x,y,s,rgb("#E94E77"),rgb("#5F9D61"),220)
        bow(d,600,630,58,rgb("#E85B87"),rgb("#FFD2DF"),220)
        bottle(d,1010,190,52,rgb("#FFF5FB"),rgb("#E85B87"),190)

    elif kind=="porcelain":
        edge_repeat(d,"flower",rgb("#557DB9"),rgb("#F1D56E"))
        d.arc((-100,-20,500,260),190,355,fill=(*rgb("#355E9A"),140),width=8)
        d.arc((700,470,1300,760),20,170,fill=(*rgb("#355E9A"),140),width=8)
        for x,y,s in [(75,85,48),(160,55,32),(1045,590,44),(1120,625,30)]:
            flower(d,x,y,s,rgb("#517AB7"),rgb("#F3D976"),220)
        bow(d,1040,95,46,rgb("#6E8EC2"),white,170)

    elif kind=="kitty":
        edge_repeat(d,"heart",rgb("#E76C9F"),white)
        def cat(x,y,s):
            d.polygon([(x-s*.48,y-s*.28),(x-s*.3,y-s*.72),(x-s*.06,y-s*.38)],fill=(255,255,255,225))
            d.polygon([(x+s*.48,y-s*.28),(x+s*.3,y-s*.72),(x+s*.06,y-s*.38)],fill=(255,255,255,225))
            d.ellipse((x-s*.5,y-s*.4,x+s*.5,y+s*.55),fill=(255,255,255,225),outline=(*rgb("#D96A9C"),150),width=3)
            d.ellipse((x-s*.2,y-s*.02,x-s*.12,y+s*.05),fill=(70,55,70,230))
            d.ellipse((x+s*.12,y-s*.02,x+s*.2,y+s*.05),fill=(70,55,70,230))
        cat(110,120,70)
        cat(1030,590,58)
        bow(d,1030,100,52,rgb("#DF5B95"),rgb("#FFD7E6"),220)
        for x,y,s in [(200,60,28),(1080,250,24),(160,610,25)]:
            paw(d,x,y,s,rgb("#E989B2"),160)

    elif kind=="caramel":
        edge_repeat(d,"star",rgb("#E3BC8E"),white)
        bear(d,1050,110,80,rgb("#B97C54"),rgb("#E6C3A2"),230)
        cup(d,145,115,68,rgb("#F2E0CE"),rgb("#8B5D40"),220)
        for x,y,s in [(100,610,40),(300,640,32),(960,620,34)]:
            d.ellipse((x-s,y-s,x+s,y+s),fill=(*rgb("#C99262"),210),outline=(*rgb("#8B5D40"),150),width=3)
        bow(d,600,640,50,rgb("#9B6946"),rgb("#E8CCAE"),180)

    elif kind=="pajama":
        edge_repeat(d,"star",rgb("#FFF0A0"),white)
        moon(d,1030,95,78,rgb("#FFF0A8"),bg,230)
        for x,y,s in [(115,95,68),(365,68,45),(1000,590,60)]:
            cloud(d,x,y,s,white,135)
        bow(d,590,70,50,rgb("#8567C0"),rgb("#D8C7F0"),190)

    elif kind=="noir":
        edge_repeat(d,"star",rgb("#E1B46E"),rgb("#F5D79D"))
        rr(d,(34,34,W-34,H-34),34,None,outline=(*rgb("#D0A15F"),150),width=4)
        bow(d,110,95,60,rgb("#D0A15F"),rgb("#8B613A"),190)
        for x,y,s in [(1040,100,34),(980,610,42),(160,620,28)]:
            flower(d,x,y,s,rgb("#C79258"),rgb("#F0D39C"),170)

    im=Image.alpha_composite(im,ov)
    mask=Image.new("L",(W,H),0)
    md=ImageDraw.Draw(mask)
    md.rounded_rectangle((0,0,W-1,H-1),46,fill=255)
    im.putalpha(mask)
    return im

THEMES=[
(120,"#FFD4E5","#FFF7FB","#E45D95","sakura"),
(121,"#6F82E8","#DDE6FF","#566ED9","blueberry"),
(122,"#DCEFB9","#F7F5DC","#659B55","matcha"),
(123,"#FFCAB6","#FFF1E8","#D97D67","peach"),
(124,"#DCC7F4","#FAF2FF","#8A62BE","violet"),
(125,"#344D98","#B8C8F7","#6F72D9","moon"),
(126,"#BFE8BD","#EEF8DE","#55975B","frog"),
(127,"#E7C0BC","#F9E7E2","#AD6C68","teddy"),
(128,"#CFEAFF","#F9F2FF","#D85A83","snow"),
(129,"#67BFE5","#D7F4FF","#338FC0","ocean"),
(130,"#F1EBD8","#FBF8EE","#8B9F72","daisy"),
(131,"#FFC0D4","#FFF0F6","#E35382","cherry"),
(132,"#8E6048","#D5AE89","#B57A52","cocoa"),
(133,"#D8C6F1","#F8F0FF","#8960B9","diary"),
(134,"#FFB7D0","#FFF0F7","#E85E8D","strawberry"),
(135,"#D7E5F8","#FCFEFF","#456FAF","porcelain"),
(136,"#FFD0E2","#FFF4F8","#E55E9A","kitty"),
(137,"#D9B38F","#F2DCC5","#9B6946","caramel"),
(138,"#B9A2E6","#EEE7FF","#7A63B6","pajama"),
(139,"#2E2019","#0A0909","#D1A05E","noir"),
]

for tid,c1,c2,a,k in THEMES:
    render(tid,c1,c2,a,k).save(
        os.path.join(OUT,f"theme_ill_bg_{tid}.webp"),
        "WEBP",
        quality=92,
        method=6
    )
