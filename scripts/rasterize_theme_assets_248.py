#!/usr/bin/env python3
from pathlib import Path
import io
import cairosvg
from PIL import Image, ImageDraw, ImageEnhance

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"theme-assets"/"2.48"/"theme_atlas.svg"
OUT=ROOT/"app"/"src"/"main"/"res"/"drawable-nodpi"
OUT.mkdir(parents=True,exist_ok=True)

png=cairosvg.svg2png(bytestring=SRC.read_bytes(),output_width=1200,output_height=14400)
atlas=Image.open(io.BytesIO(png)).convert("RGBA")

for idx,pack in enumerate(range(120,140)):
    board=atlas.crop((0,idx*720,1200,(idx+1)*720))
    board=ImageEnhance.Contrast(board).enhance(1.03)
    board=ImageEnhance.Color(board).enhance(1.04)
    board.save(OUT/f"theme_ill_bg_{pack}.webp","WEBP",quality=94,method=6)

    top=board.crop((0,0,1200,230)).resize((1000,105),Image.Resampling.LANCZOS)
    bottom=board.crop((0,500,1200,720)).resize((1000,85),Image.Resampling.LANCZOS)
    space=Image.new("RGBA",(1000,180),(255,255,255,0))
    space.alpha_composite(top,(0,0))
    space.alpha_composite(bottom,(0,95))
    veil=Image.new("RGBA",space.size,(255,255,255,0))
    draw=ImageDraw.Draw(veil)
    draw.rounded_rectangle((5,5,995,175),radius=34,fill=(255,255,255,72),outline=(255,255,255,120),width=4)
    space=Image.alpha_composite(space,veil)
    mask=Image.new("L",space.size,0)
    ImageDraw.Draw(mask).rounded_rectangle((0,0,999,179),radius=36,fill=255)
    space.putalpha(mask)
    space.save(OUT/f"theme_ill_space_{pack}.webp","WEBP",quality=92,method=6)

print("Rasterized 20 committed illustrated boards and matching spacebars.")
