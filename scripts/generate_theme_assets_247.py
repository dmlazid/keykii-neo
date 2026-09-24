#!/usr/bin/env python3
from PIL import Image, ImageDraw
import os, math, random

W,H=600,360
OUT="app/src/main/res/drawable-nodpi"
os.makedirs(OUT,exist_ok=True)

def rgb(h):
    h=h.lstrip("#")
    return tuple(int(h[i:i+2],16) for i in (0,2,4))

def mix(a,b,t):
    return tuple(round(a[i]*(1-t)+b[i]*t) for i in range(3))

def base(c1,c2):
    a,b=rgb(c1),rgb(c2)
    im=Image.new("RGBA",(W,H),(0,0,0,0))
    p=im.load()
    for y in range(H):
        t=y/(H-1)
        c=mix(a,b,t)
        for x in range(W):
            p[x,y]=(*c,255)
    m=Image.new("L",(W,H),0)
    ImageDraw.Draw(m).rounded_rectangle((0,0,W-1,H-1),32,fill=255)
    im.putalpha(m)
    return im

def star(d,x,y,r,c,a=255):
    pts=[]
    for i in range(10):
        z=-math.pi/2+i*math.pi/5
        rr=r if i%2==0 else r*.42
        pts.append((x+math.cos(z)*rr,y+math.sin(z)*rr))
    d.polygon(pts,fill=(*c,a))

def flower(d,x,y,s,c1,c2,a=255):
    for i in range(5):
        z=-math.pi/2+i*2*math.pi/5
        cx=x+math.cos(z)*s*.3; cy=y+math.sin(z)*s*.3
        d.ellipse((cx-s*.22,cy-s*.28,cx+s*.22,cy+s*.28),fill=(*c1,a))
    d.ellipse((x-s*.16,y-s*.16,x+s*.16,y+s*.16),fill=(*c2,a))

def bow(d,x,y,s,c1,c2,a=255):
    d.polygon([(x,y),(x-s*.38,y-s*.22),(x-s*.55,y-s*.12),(x-s*.48,y+s*.18),(x-s*.15,y+s*.12)],fill=(*c1,a))
    d.polygon([(x,y),(x+s*.38,y-s*.22),(x+s*.55,y-s*.12),(x+s*.48,y+s*.18),(x+s*.15,y+s*.12)],fill=(*c1,a))
    d.rounded_rectangle((x-s*.12,y-s*.13,x+s*.12,y+s*.13),max(2,int(s*.06)),fill=(*c2,a))
    d.polygon([(x-s*.08,y+s*.1),(x-s*.24,y+s*.48),(x,y+s*.33)],fill=(*c1,a))
    d.polygon([(x+s*.08,y+s*.1),(x+s*.24,y+s*.48),(x,y+s*.33)],fill=(*c1,a))

def heart(d,x,y,s,c,a=255):
    d.ellipse((x-s*.35,y-s*.12,x-s*.02,y+s*.22),fill=(*c,a))
    d.ellipse((x+s*.02,y-s*.12,x+s*.35,y+s*.22),fill=(*c,a))
    d.polygon([(x-s*.34,y+s*.05),(x+s*.34,y+s*.05),(x,y+s*.48)],fill=(*c,a))

def cloud(d,x,y,s,c,a=255):
    d.ellipse((x-s*.55,y-s*.08,x-s*.08,y+s*.28),fill=(*c,a))
    d.ellipse((x-s*.25,y-s*.35,x+s*.2,y+s*.28),fill=(*c,a))
    d.ellipse((x+s*.05,y-s*.18,x+s*.5,y+s*.28),fill=(*c,a))
    d.rounded_rectangle((x-s*.48,y,x+s*.48,y+s*.25),max(2,int(s*.1)),fill=(*c,a))

def moon(d,x,y,s,c,bg,a=255):
    d.ellipse((x-s*.5,y-s*.5,x+s*.5,y+s*.5),fill=(*c,a))
    d.ellipse((x-s*.15,y-s*.45,x+s*.55,y+s*.4),fill=(*bg,255))

def bunny(d,x,y,s,body,accent,a=255):
    d.ellipse((x-s*.34,y-s*.68,x-s*.06,y-s*.12),fill=(*body,a)); d.ellipse((x+s*.06,y-s*.68,x+s*.34,y-s*.12),fill=(*body,a))
    d.ellipse((x-s*.28,y-s*.58,x-s*.12,y-s*.2),fill=(*accent,a)); d.ellipse((x+s*.12,y-s*.58,x+s*.28,y-s*.2),fill=(*accent,a))
    d.ellipse((x-s*.48,y-s*.28,x+s*.48,y+s*.5),fill=(*body,a))
    e=(70,60,70)
    d.ellipse((x-s*.2,y,x-s*.14,y+s*.07),fill=(*e,a)); d.ellipse((x+s*.14,y,x+s*.2,y+s*.07),fill=(*e,a))

def bear(d,x,y,s,body,accent,a=255):
    d.ellipse((x-s*.5,y-s*.42,x-s*.14,y-s*.08),fill=(*body,a)); d.ellipse((x+s*.14,y-s*.42,x+s*.5,y-s*.08),fill=(*body,a))
    d.ellipse((x-s*.48,y-s*.3,x+s*.48,y+s*.5),fill=(*body,a))
    d.ellipse((x-s*.12,y+s*.08,x+s*.12,y+s*.28),fill=(*accent,a))
    e=(70,55,50)
    d.ellipse((x-s*.2,y-s*.02,x-s*.14,y+s*.05),fill=(*e,a)); d.ellipse((x+s*.14,y-s*.02,x+s*.2,y+s*.05),fill=(*e,a))

def frog(d,x,y,s,body,cream,a=255):
    d.ellipse((x-s*.5,y-s*.28,x+s*.5,y+s*.48),fill=(*body,a))
    d.ellipse((x-s*.4,y-s*.5,x-s*.06,y-s*.14),fill=(*body,a)); d.ellipse((x+s*.06,y-s*.5,x+s*.4,y-s*.14),fill=(*body,a))
    d.ellipse((x-s*.33,y-s*.4,x-s*.17,y-s*.24),fill=(*cream,a)); d.ellipse((x+s*.17,y-s*.4,x+s*.33,y-s*.24),fill=(*cream,a))

def cat(d,x,y,s,body,accent,a=255):
    d.polygon([(x-s*.42,y-s*.25),(x-s*.3,y-s*.65),(x-s*.08,y-s*.35)],fill=(*body,a)); d.polygon([(x+s*.42,y-s*.25),(x+s*.3,y-s*.65),(x+s*.08,y-s*.35)],fill=(*body,a))
    d.ellipse((x-s*.48,y-s*.34,x+s*.48,y+s*.5),fill=(*body,a))
    e=(70,55,70)
    d.ellipse((x-s*.2,y-s*.02,x-s*.14,y+s*.05),fill=(*e,a)); d.ellipse((x+s*.14,y-s*.02,x+s*.2,y+s*.05),fill=(*e,a))
    d.ellipse((x-s*.04,y+s*.08,x+s*.04,y+s*.15),fill=(*accent,a))

def cup(d,x,y,s,cupc,accent,a=255):
    d.rounded_rectangle((x-s*.38,y-s*.12,x+s*.25,y+s*.45),max(2,int(s*.08)),fill=(*cupc,a))
    d.arc((x+s*.12,y,x+s*.55,y+s*.38),-80,80,fill=(*accent,a),width=max(2,int(s*.07)))
    for q in (-.15,.03,.18):
        d.arc((x+s*q,y-s*.48,x+s*(q+.18),y-s*.08),180,360,fill=(*accent,a),width=max(1,int(s*.04)))

def cake(d,x,y,s,b,c,ch,a=255):
    d.rounded_rectangle((x-s*.4,y,x+s*.4,y+s*.38),max(2,int(s*.05)),fill=(*b,a))
    d.pieslice((x-s*.42,y-s*.2,x+s*.42,y+s*.18),180,360,fill=(*c,a))
    d.ellipse((x-s*.07,y-s*.32,x+s*.07,y-s*.18),fill=(*ch,a))

def strawberry(d,x,y,s,b,l,a=255):
    d.polygon([(x,y+s*.48),(x-s*.38,y-s*.08),(x-s*.25,y-s*.34),(x+s*.25,y-s*.34),(x+s*.38,y-s*.08)],fill=(*b,a))
    d.polygon([(x-s*.26,y-s*.3),(x-s*.1,y-s*.52),(x,y-s*.33),(x+s*.12,y-s*.52),(x+s*.26,y-s*.3)],fill=(*l,a))

def cherries(d,x,y,s,red,green,a=255):
    d.line((x,y-s*.4,x-s*.18,y-s*.05),fill=(*green,a),width=max(1,int(s*.04))); d.line((x,y-s*.4,x+s*.18,y-s*.05),fill=(*green,a),width=max(1,int(s*.04)))
    d.ellipse((x-s*.34,y-s*.08,x-s*.02,y+s*.24),fill=(*red,a)); d.ellipse((x+s*.02,y-s*.08,x+s*.34,y+s*.24),fill=(*red,a))

def jelly(d,x,y,s,c1,c2,a=255):
    d.pieslice((x-s*.42,y-s*.4,x+s*.42,y+s*.25),180,360,fill=(*c1,a)); d.rectangle((x-s*.42,y-s*.08,x+s*.42,y+s*.08),fill=(*c1,a))
    for dx in (-.28,-.1,.1,.28):
        pts=[(x+s*dx+math.sin(j/2)*s*.05,y+s*.05+j*s*.05) for j in range(10)]
        d.line(pts,fill=(*c2,a),width=max(1,int(s*.035)))

def gift(d,x,y,s,c1,c2,a=255):
    d.rectangle((x-s*.38,y-s*.15,x+s*.38,y+s*.45),fill=(*c1,a)); d.rectangle((x-s*.05,y-s*.15,x+s*.05,y+s*.45),fill=(*c2,a)); d.rectangle((x-s*.42,y-s*.28,x+s*.42,y-s*.08),fill=(*c1,a))
    bow(d,x,y-s*.28,s*.65,c2,c2,a)

def cookie(d,x,y,s,c1,c2,a=255):
    d.ellipse((x-s*.45,y-s*.45,x+s*.45,y+s*.45),fill=(*c1,a))
    for ox,oy in [(-.18,-.1),(.16,-.18),(.08,.18),(-.22,.21)]:
        d.ellipse((x+s*ox-s*.06,y+s*oy-s*.06,x+s*ox+s*.06,y+s*oy+s*.06),fill=(*c2,a))

def leaf(d,x,y,s,c,a=255):
    d.polygon([(x-s*.45,y+s*.2),(x,y-s*.45),(x+s*.45,y+s*.2),(x,y+s*.42)],fill=(*c,a))

def milk(d,x,y,s,body,accent,a=255):
    d.polygon([(x-s*.28,y-s*.42),(x+s*.2,y-s*.42),(x+s*.32,y-s*.22),(x+s*.32,y+s*.42),(x-s*.32,y+s*.42),(x-s*.32,y-s*.22)],fill=(*body,a))
    d.rectangle((x-s*.3,y-s*.12,x+s*.3,y+s*.18),fill=(*accent,a))

def shell(d,x,y,s,c1,c2,a=255):
    d.pieslice((x-s*.45,y-s*.3,x+s*.45,y+s*.45),180,360,fill=(*c1,a))
    for i in range(-3,4):
        d.line((x,y+s*.03,x+i*s*.1,y-s*.18),fill=(*c2,a),width=max(1,int(s*.02)))

def paw(d,x,y,s,c,a=255):
    d.ellipse((x-s*.18,y,x+s*.18,y+s*.26),fill=(*c,a))
    for ox,oy in [(-.25,-.15),(-.08,-.25),(.1,-.25),(.27,-.12)]:
        d.ellipse((x+s*ox-s*.08,y+s*oy-s*.08,x+s*ox+s*.08,y+s*oy+s*.08),fill=(*c,a))

def stripes(d,cols,w=48,a=38,diag=False):
    if diag:
        for i,x in enumerate(range(-H,W+H,w)):
            c=rgb(cols[i%len(cols)])
            d.polygon([(x,0),(x+w//2,0),(x-H+w//2,H),(x-H,H)],fill=(*c,a))
    else:
        for i,x in enumerate(range(0,W,w)):
            c=rgb(cols[i%len(cols)])
            d.rectangle((x,0,x+w//2,H),fill=(*c,a))

def grid(d,c,step=56,a=32):
    cc=rgb(c)
    for x in range(0,W,step): d.line((x,0,x,H),fill=(*cc,a),width=1)
    for y in range(0,H,step): d.line((0,y,W,y),fill=(*cc,a),width=1)

def dots(d,c,step=50,rad=3,a=40):
    cc=rgb(c)
    for j,y in enumerate(range(25,H,step)):
        off=step//2 if j%2 else 0
        for x in range(25+off,W,step): d.ellipse((x-rad,y-rad,x+rad,y+rad),fill=(*cc,a))

def lace(d,c,a=100):
    cc=rgb(c)
    for x in range(16,W-16,32):
        d.ellipse((x-16,6,x+16,34),outline=(*cc,a),width=2); d.ellipse((x-16,H-34,x+16,H-6),outline=(*cc,a),width=2)

def sparkles(d,c,n,seed,a=120):
    rr=random.Random(seed); cc=rgb(c)
    for _ in range(n): star(d,rr.randint(25,W-25),rr.randint(15,H-15),rr.randint(2,5),cc,a)

def render(tid,c1,c2,accent,kind):
    im=base(c1,c2); d=ImageDraw.Draw(im,"RGBA"); r=random.Random(tid*17)
    white=(255,255,255); bg=rgb(c1); deep=mix(rgb(accent),(40,30,45),.35)
    if kind=="sakura":
        stripes(d,["#FFFFFF","#FFDDE9"],80,30,True)
        for x in range(50,W,120): flower(d,x,34,16,rgb("#F58DB3"),rgb("#FFE38A"),170)
        for x in (60,125,W-70): flower(d,x,H-42,24,rgb("#F27FAA"),rgb("#FFE8A8"),190)
        bow(d,W-105,H-62,38,rgb("#E8669A"),rgb("#FFD3E5"),190)
        for _ in range(18):
            x=r.randint(25,W-25); y=r.randint(40,H-40); d.ellipse((x-2,y-4,x+2,y+4),fill=(*rgb("#F49ABA"),70))
    elif kind=="blueberry":
        sparkles(d,"#FFFFFF",38,tid,140)
        for x,y,s in [(70,48,38),(W-105,52,34),(W//2,H-42,44)]: cloud(d,x,y,s,white,130)
        moon(d,W-70,44,31,rgb("#FFF0A8"),bg,215)
        for x,y in [(55,H-52),(120,H-38),(W-150,34)]:
            for ox,oy in [(0,0),(16,5),(8,16)]: d.ellipse((x+ox-8,y+oy-8,x+ox+8,y+oy+8),fill=(*deep,160))
        bow(d,W//2,31,29,rgb("#9A7DE7"),rgb("#D9CEFF"),175)
    elif kind=="matcha":
        stripes(d,["#FFFFFF","#CBE2A7"],74,36,True); grid(d,"#76985F",95,20)
        for x in (60,220,W-75): bunny(d,x,48,30,rgb("#FFF8EC"),rgb("#F5C5D0"),190)
        for x in (110,W-135): cup(d,x,H-48,32,rgb("#77A967"),white,180)
        for x in range(35,W,95): leaf(d,x,H-20,13,rgb("#65994F"),120)
    elif kind=="peach":
        dots(d,"#FFFFFF",62,3,45); bear(d,65,52,36,rgb("#D9A37A"),rgb("#704D43"),185); cake(d,W-75,50,34,rgb("#F3A0A3"),rgb("#FFF0D9"),rgb("#E46F77"),190)
        for x in range(60,W-30,105): heart(d,x,H-28,15,rgb("#F49A9E"),100)
        strawberry(d,W-105,H-52,32,rgb("#EE7D82"),rgb("#6AA36A"),165)
    elif kind=="violet":
        grid(d,"#8B64BE",70,22); lace(d,"#FFFFFF",125); bow(d,70,38,34,rgb("#976CCB"),rgb("#E7D2F4"),190)
        for x,y,s in [(W-70,48,28),(60,H-42,24),(W-140,H-34,20)]: bow(d,x,y,s,rgb("#7E56B4"),rgb("#CFA7E9"),160)
        for x in range(135,W-120,110): flower(d,x,27,16,rgb("#B38AD8"),rgb("#FFF0A8"),150)
        d.rounded_rectangle((W*.38,10,W*.61,28),6,fill=(255,255,255,65))
    elif kind=="moon":
        sparkles(d,"#CFE1FF",48,tid,155); moon(d,60,48,35,rgb("#FFF0A8"),bg,220); cloud(d,145,48,34,white,125); jelly(d,W-80,55,30,rgb("#A7BFFF"),white,165); jelly(d,75,H-50,24,rgb("#A896F3"),rgb("#D9E4FF"),155)
    elif kind=="frog":
        grid(d,"#5A975E",90,18)
        for x,y,s in [(60,48,32),(W-75,50,30),(115,H-52,25),(W-150,H-42,22)]: frog(d,x,y,s,rgb("#61AD65"),rgb("#F8F1C8"),175)
        for x in range(35,W,85): flower(d,x,22,13,white,rgb("#F1C95C"),115)
    elif kind=="teddy":
        stripes(d,["#FFFFFF","#D8AEB0"],90,25); grid(d,"#A06D6F",100,16)
        for x,y,s in [(65,50,34),(W-75,45,30),(W-140,H-42,24)]: bear(d,x,y,s,rgb("#C98A73"),rgb("#704C45"),175)
        bow(d,W//2,30,31,rgb("#E783A0"),rgb("#FFD5E0"),165)
        for x in range(85,W,125): cookie(d,x,H-30,17,rgb("#D7A06F"),rgb("#6F4A37"),125)
    elif kind=="snow":
        for _ in range(80):
            x=r.randrange(W); y=r.randrange(H); q=r.choice([1,2,3]); d.ellipse((x-q,y-q,x+q,y+q),fill=(255,255,255,r.randint(60,145)))
        for x,y,s in [(58,45,28),(W-65,48,30),(W-130,H-42,24)]: gift(d,x,y,s,rgb("#E86387"),white,175)
        for x,y,s in [(150,30,19),(W-180,25,18),(75,H-48,20)]:
            for ang in (0,math.pi/3,2*math.pi/3):
                dx=math.cos(ang)*s; dy=math.sin(ang)*s; d.line((x-dx,y-dy,x+dx,y+dy),fill=(*white,145),width=2)
    elif kind=="ocean":
        for yy in range(45,H,68):
            pts=[(x,yy+math.sin((x+yy)/55)*5) for x in range(-20,W+20,14)]; d.line(pts,fill=(*white,70),width=2)
        for x,y,s in [(60,50,31),(W-75,45,28),(W-145,H-44,24)]: jelly(d,x,y,s,rgb("#8ED8F4"),white,160)
        for x,y,s in [(150,H-30,18),(W-55,H-30,16)]: shell(d,x,y,s,rgb("#FFE2C9"),rgb("#D99CB4"),145)
        dots(d,"#FFFFFF",85,2,50)
    elif kind=="daisy":
        grid(d,"#8AA780",95,16); stripes(d,["#FFFFFF","#DDE9D3"],86,20)
        for x in range(45,W,100): flower(d,x,30,17,white,rgb("#E6C44C"),175)
        milk(d,65,H-50,28,white,rgb("#B7D0A0"),165); milk(d,W-80,H-50,25,white,rgb("#D6B8C6"),145); bow(d,W//2,H-30,22,rgb("#C8B2C7"),white,125)
    elif kind=="cherry":
        stripes(d,["#FFFFFF","#FFB9CF"],78,27); dots(d,"#FFFFFF",70,3,40)
        for x,y,s in [(60,48,29),(W-70,45,26),(W-140,H-42,23)]: cherries(d,x,y,s,rgb("#DD4766"),rgb("#6AA063"),175)
        cup(d,W//2,38,27,white,rgb("#E5648C"),160); bow(d,65,H-48,26,rgb("#E96793"),rgb("#FFD7E4"),150)
    elif kind=="cocoa":
        grid(d,"#F0D5BA",85,16); stripes(d,["#4C3027","#79513E"],90,20,True)
        bunny(d,60,48,32,rgb("#F6E8D7"),rgb("#D9B09A"),170); cup(d,W-70,48,30,rgb("#F0E4D7"),rgb("#B07751"),185)
        for x in (150,W-150): cookie(d,x,H-36,21,rgb("#D79B64"),rgb("#5E3F2D"),145)
    elif kind=="diary":
        grid(d,"#8F6BB5",58,24)
        for x,y,w in [(30,16,85),(W-140,22,100),(W//2-55,H-36,110)]: d.rounded_rectangle((x,y,x+w,y+13),4,fill=(255,255,255,70))
        for x,y,s in [(60,48,27),(W-70,42,25),(W-130,H-42,22)]: bow(d,x,y,s,rgb("#9667C1"),rgb("#DEC4F0"),155)
        for x in range(130,W-90,110): flower(d,x,H-25,15,rgb("#C49CE4"),white,135)
    elif kind=="strawberry":
        stripes(d,["#FFFFFF","#FFBFD6"],64,30,True); dots(d,"#FFFFFF",68,3,45)
        for x,y,s in [(55,45,28),(W-65,45,26),(130,H-42,23),(W-145,H-44,24)]: strawberry(d,x,y,s,rgb("#EB5D7E"),rgb("#69A764"),175)
        bow(d,W//2,30,30,rgb("#F06D9C"),rgb("#FFD8E5"),150); milk(d,W//2,H-36,23,white,rgb("#F4A0BD"),125)
    elif kind=="porcelain":
        grid(d,"#5A7AA8",70,16); d.arc((5,15,W*.55,125),180,345,fill=(*rgb("#365E9C"),130),width=3)
        for x,y,s in [(55,42,29),(130,24,18),(W-65,H-42,29),(W-145,H-25,18)]: flower(d,x,y,s,rgb("#527BBB"),rgb("#E7C75E"),160)
        for x,y,s in [(W-70,38,23),(65,H-40,20)]: bow(d,x,y,s,rgb("#6D8FC2"),rgb("#DCE6F7"),125)
    elif kind=="kitty":
        grid(d,"#E79ABB",92,17); stripes(d,["#FFFFFF","#FFD8E8"],92,20)
        for x,y,s in [(58,48,32),(W-70,45,28),(W-135,H-42,24)]: cat(d,x,y,s,white,rgb("#E865A0"),175)
        for x,y,s in [(140,28,18),(60,H-40,16)]: paw(d,x,y,s,rgb("#E887AE"),125)
        cake(d,W//2,H-34,24,rgb("#E89AB8"),white,rgb("#D94C79"),125)
    elif kind=="caramel":
        grid(d,"#79543B",88,22); stripes(d,["#F1D3B5","#BF8A5E"],96,20,True)
        for x,y,s in [(58,48,32),(W-65,45,27),(W-130,H-40,23)]: bear(d,x,y,s,rgb("#C58B5E"),rgb("#654433"),175)
        cup(d,150,H-42,25,rgb("#F3E3D4"),rgb("#8A5D40"),135); cookie(d,W//2,28,20,rgb("#D7A06F"),rgb("#6F4A37"),125)
    elif kind=="pajama":
        grid(d,"#866CC4",75,20); sparkles(d,"#FFF2A4",40,tid,120); moon(d,W-60,45,31,rgb("#FFF0A8"),bg,190)
        for x,y,s in [(55,45,29),(W-145,H-42,27)]: cloud(d,x,y,s,white,115)
        bow(d,W//2,30,28,rgb("#9B79D5"),rgb("#D9C9F1"),145)
        for x in range(100,W-60,125): d.rounded_rectangle((x-16,H-38,x+16,H-15),8,fill=(*rgb("#CBB8EA"),60))
    elif kind=="noir":
        gold=rgb("#D3A45F"); d.rounded_rectangle((10,8,W-11,H-9),24,outline=(*gold,135),width=2); sparkles(d,"#E8C681",38,tid,130)
        for x,y,s in [(55,45,29),(W-70,50,25),(W-140,H-42,23),(60,H-42,20)]: bow(d,x,y,s,gold,rgb("#8E6438"),145)
        moon(d,W//2,30,21,rgb("#DDB36E"),bg,125)
    d.rounded_rectangle((2,2,W-3,H-3),30,outline=(255,255,255,55),width=1)
    return im

THEMES=[
(120,"#FFD4E5","#FFF7FB","#E45D95","sakura"),(121,"#6F82E8","#DDE6FF","#566ED9","blueberry"),
(122,"#DCEFB9","#F7F5DC","#659B55","matcha"),(123,"#FFCAB6","#FFF1E8","#D97D67","peach"),
(124,"#DCC7F4","#FAF2FF","#8A62BE","violet"),(125,"#344D98","#B8C8F7","#6F72D9","moon"),
(126,"#BFE8BD","#EEF8DE","#55975B","frog"),(127,"#E7C0BC","#F9E7E2","#AD6C68","teddy"),
(128,"#CFEAFF","#F9F2FF","#D85A83","snow"),(129,"#67BFE5","#D7F4FF","#338FC0","ocean"),
(130,"#F1EBD8","#FBF8EE","#8B9F72","daisy"),(131,"#FFC0D4","#FFF0F6","#E35382","cherry"),
(132,"#8E6048","#D5AE89","#B57A52","cocoa"),(133,"#D8C6F1","#F8F0FF","#8960B9","diary"),
(134,"#FFB7D0","#FFF0F7","#E85E8D","strawberry"),(135,"#D7E5F8","#FCFEFF","#456FAF","porcelain"),
(136,"#FFD0E2","#FFF4F8","#E55E9A","kitty"),(137,"#D9B38F","#F2DCC5","#9B6946","caramel"),
(138,"#B9A2E6","#EEE7FF","#7A63B6","pajama"),(139,"#2E2019","#0A0909","#D1A05E","noir"),
]

for tid,c1,c2,a,k in THEMES:
    render(tid,c1,c2,a,k).save(os.path.join(OUT,f"theme_ill_bg_{tid}.webp"),"WEBP",quality=84,method=2)
