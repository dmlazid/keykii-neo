package com.keykii.neo;

import android.graphics.*;

/** All art sizes are relative to bounds, so shop thumbnails and live keys agree. */
final class NeoThemeRenderer {
    private final NeoThemeCatalog.Entry e;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Path shape=new Path();
    private final RectF rect=new RectF();
    // Authored primary silhouettes for each structural family.
    static final int[] SHAPES={13,14,3,5,2,1,4,21,11,16,8,3,19,14,15,7,21,3,2,1,3,9,10,18,12,13,12,1,3,6,15,20,13,3,16,16,3,21,7,8,3,12,10,21,12,14,22,18};
    NeoThemeRenderer(int pack){e=NeoThemeCatalog.get(pack);}
    static int textColor(NeoThemeCatalog.Entry e){return e.dark?Color.WHITE:0xff2d2d2d;}
    void background(Canvas c,float w,float h){
        if(e==null)return;
        rect.set(0,0,w,h);p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(e.start);
        c.drawRoundRect(rect,w*.04f,w*.04f,p);
        c.save();shape.reset();shape.addRoundRect(rect,w*.04f,w*.04f,Path.Direction.CW);c.clipPath(shape);
        p.setAlpha(255);NeoArt.draw(c,e.art,rect,p);
        // Keep scene legible rather than fading every artwork into a pastel wash.
        p.setColor(e.dark?0x24060b13:0x12fff9f1);c.drawRect(rect,p);c.restore();
    }
    void structure(Canvas c,float w,float h,NeoGeometry.Box[] boxes){
        if(e==null)return;
        p.setShader(null);p.setStyle(Paint.Style.FILL);
        // Board construction is aligned to the ACTUAL keys, not guessed background rows.
        int a=e.architecture;
        if(a==2||a==30||a==40){
            p.setColor(e.dark?0xb80a111b:0xaadcd3bf);
            c.drawRoundRect(new RectF(w*.002f,h*.015f,w*.477f,h*.997f),w*.02f,w*.02f,p);
            c.drawRoundRect(new RectF(w*.523f,h*.015f,w*.998f,h*.997f),w*.02f,w*.02f,p);
        }
        if(a==7||a==12||a==21||a==28||a==33||a==38){
            int last=-1;float left=0,top=0,right=0,bottom=0;
            for(int i=0;i<=boxes.length;i++){
                NeoGeometry.Box b=i<boxes.length?boxes[i]:null;
                if(b==null||b.row!=last){
                    if(last>=0){p.setColor(e.dark?0x88333e51:0x889c7865);c.drawRoundRect(new RectF(left*w-2,top*h-2,right*w+2,bottom*h+2),w*.008f,w*.008f,p);}
                    if(b==null)break;
                    last=b.row;left=b.left;right=b.right;top=b.top;bottom=b.bottom;
                }else{left=Math.min(left,b.left);right=Math.max(right,b.right);top=Math.min(top,b.top);bottom=Math.max(bottom,b.bottom);}
            }
        }
        if(a==9||a==35){
            p.setColor(0xdd846c58);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,w*.004f));
            for(int i=0;i<8;i++)c.drawOval(new RectF(w*.004f,h*(.07f+i*.115f),w*.038f,h*(.105f+i*.115f)),p);
            p.setStyle(Paint.Style.FILL);
        }
    }
    void key(Canvas c,float w,float h,NeoGeometry.Box box,String action,boolean special,boolean pressed){
        if(e==null||w<=0||h<=0)return;
        boolean space="SPACE".equals(action);
        int col=box==null?0:box.col,row=box==null?0:box.row;
        float unit=Math.min(w,h),margin=unit*.035f,depth=pressed?unit*.018f:unit*.09f;
        rect.set(margin,margin,w-margin,h-margin-depth);
        int mode=SHAPES[e.architecture];
        // Specific modifier and spacebar silhouettes, not one universal rounded rectangle.
        if(special&&(e.architecture==0||e.architecture==4||e.architecture==18||e.architecture==23))mode=2;
        if(space){
            if(mode==2||mode==8||mode==15)mode=13;
            if(mode==6||mode==7)mode=5;
        }
        makeShape(rect,mode,space);
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(e.dark?0x99000000:0x70553b45);
        c.save();c.translate(0,depth);c.drawPath(shape,p);c.restore();
        int base=e.dark?NeoThemeCatalog.mix(e.start,0xff17212e,35):NeoThemeCatalog.mix(e.end,Color.WHITE,55);
        int edge=e.dark?NeoThemeCatalog.mix(e.accent,Color.BLACK,70):NeoThemeCatalog.mix(e.accent,Color.WHITE,50);
        if(special||space)base=NeoThemeCatalog.mix(base,e.accent,e.dark?25:22);
        // Material variation belongs to selected keys, not a whole-board palette permutation.
        if(e.architecture==0||e.architecture==19||e.architecture==22||e.architecture==42){
            int[] colors={0xfff7bacb,0xffccebdc,0xffffe6a0,0xffcddcf1,0xffdccdf1};
            base=e.dark?NeoThemeCatalog.mix(colors[(row+col)%5],Color.BLACK,62):colors[(row+col)%5];
        }
        if(pressed)base=NeoThemeCatalog.mix(base,e.accent,35);
        p.setShader(new LinearGradient(0,0,0,h,new int[]{NeoThemeCatalog.mix(base,Color.WHITE,e.dark?12:26),base,edge},new float[]{0,.7f,1},Shader.TileMode.CLAMP));
        c.drawPath(shape,p);p.setShader(null);
        boolean imageKey=e.material==1||e.material==3||e.architecture==16||e.architecture==23||e.architecture==34||e.architecture==46;
        boolean clearKey=e.material==2||e.architecture==7||e.architecture==37||e.architecture==43;
        if(imageKey||clearKey||space){
            c.save();c.clipPath(shape);p.setColor(Color.WHITE);p.setAlpha(clearKey?135:space?180:230);
            if(box!=null&&imageKey){
                NeoArt.drawSlice(c,e.art,rect,p,box.left,box.top,box.right,box.bottom);
            }else NeoArt.draw(c,e.art,rect,p);
            p.setAlpha(255);p.setColor(e.dark?0x8808121c:0x9efdf7ef);c.drawRect(rect,p);c.restore();
        }
        // Clear glass uses an extra bright edge and reflected light, never blur on draw.
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(unit*(clearKey?.035f:.022f));
        p.setColor(e.dark?NeoThemeCatalog.mix(e.accent,Color.WHITE,25):NeoThemeCatalog.mix(e.accent,Color.BLACK,40));c.drawPath(shape,p);
        p.setStyle(Paint.Style.FILL);
        if(mode==3||mode==21||clearKey){
            rect.inset(unit*.095f,unit*.10f);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(.5f,unit*.018f));p.setColor(e.dark?0x80d3edf4:0xbfffffff);c.drawRoundRect(rect,unit*.10f,unit*.10f,p);p.setStyle(Paint.Style.FILL);
        }
        if(mode==7){ // Plush seam follows the perimeter; blank central label area.
            p.setColor(e.dark?0x88ffffff:0x805f4340);float yy=h*.82f;
            for(int k=1;k<7;k++)c.drawLine(w*k/8f,yy,w*k/8f+unit*.035f,yy+unit*.025f,p);
        }
        if(mode==10){ // Frosting drips occupy the top edge, never the letter.
            p.setColor(0xeef8e5dc);for(int k=0;k<4;k++)c.drawOval(new RectF(w*(.08f+k*.22f),h*.07f,w*(.24f+k*.22f),h*(k%2==0?.22f:.16f)),p);
        }
        if(e.material==4 || e.architecture==1 || e.architecture==10 || e.architecture==21 || e.architecture==39 || e.architecture==45){
            ornament(c,space?w*.90f:w*.83f,h*.18f,unit*.11f,(row+col+e.art)%5);
        }
        if(space){ornament(c,w*.08f,h*.47f,unit*.16f,e.art%5);ornament(c,w*.92f,h*.47f,unit*.16f,(e.art+1)%5);}
    }
    private void ornament(Canvas c,float x,float y,float r,int style){
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(e.accent);
        if(style==0){for(int k=0;k<5;k++){double a=k*Math.PI*2/5;c.drawCircle(x+(float)Math.cos(a)*r*.6f,y+(float)Math.sin(a)*r*.6f,r*.53f,p);}p.setColor(0xffffd788);c.drawCircle(x,y,r*.3f,p);}
        else if(style==1){c.drawOval(new RectF(x-r,y-r*.65f,x,y+r*.65f),p);c.drawOval(new RectF(x,y-r*.65f,x+r,y+r*.65f),p);}
        else if(style==2){p.setColor(e.dark?0xfff8d68e:0xffd6a261);c.drawCircle(x,y,r,p);p.setColor(e.dark?0xff152133:0xfff9f2ec);c.drawCircle(x+r*.5f,y-r*.4f,r*.85f,p);}
        else if(style==3){p.setColor(e.dark?0xffa7f3e5:0xff638775);c.save();c.rotate(-30,x,y);c.drawOval(new RectF(x-r*.5f,y-r,x+r*.5f,y+r),p);c.restore();}
        else{p.setColor(0xffffd18f);c.drawCircle(x-r*.5f,y-r*.65f,r*.45f,p);c.drawCircle(x+r*.5f,y-r*.65f,r*.45f,p);c.drawCircle(x,y,r*.75f,p);p.setColor(0xff59443e);c.drawCircle(x-r*.23f,y,r*.10f,p);c.drawCircle(x+r*.23f,y,r*.10f,p);}
    }
    private void makeShape(RectF r,int mode,boolean space){
        shape.reset();float w=r.width(),h=r.height(),u=Math.min(w,h),cx=r.centerX(),cy=r.centerY();
        if(mode==2){shape.addCircle(cx,cy,u*.5f,Path.Direction.CW);return;}
        if(mode==3||mode==15||mode==11){float d=u*(mode==15?.23f:mode==11?.13f:.12f);shape.moveTo(r.left+d,r.top);shape.lineTo(r.right-d,r.top);shape.lineTo(r.right,r.top+d);shape.lineTo(r.right,r.bottom-d);shape.lineTo(r.right-d,r.bottom);shape.lineTo(r.left+d,r.bottom);shape.lineTo(r.left,r.bottom-d);shape.lineTo(r.left,r.top+d);shape.close();return;}
        if(mode==12){shape.moveTo(r.left,r.bottom);shape.lineTo(r.left,r.top+h*.30f);shape.cubicTo(r.left,r.top+h*.08f,cx,r.top,cx,r.top);shape.cubicTo(cx,r.top,r.right,r.top+h*.08f,r.right,r.top+h*.30f);shape.lineTo(r.right,r.bottom);shape.close();return;}
        if(mode==4){float d=u*.12f;shape.moveTo(r.left+d,r.top);shape.lineTo(r.right-d,r.top);shape.quadTo(r.right,r.top,r.right,r.top+d);shape.lineTo(r.right,cy-d);shape.quadTo(r.right-d,cy,r.right,cy+d);shape.lineTo(r.right,r.bottom-d);shape.quadTo(r.right,r.bottom,r.right-d,r.bottom);shape.lineTo(r.left+d,r.bottom);shape.quadTo(r.left,r.bottom,r.left,r.bottom-d);shape.lineTo(r.left,cy+d);shape.quadTo(r.left+d,cy,r.left,cy-d);shape.lineTo(r.left,r.top+d);shape.quadTo(r.left,r.top,r.left+d,r.top);shape.close();return;}
        if(mode==5||mode==6||mode==7){
            shape.moveTo(r.left+u*.2f,r.bottom);shape.quadTo(r.left,r.bottom,r.left,r.bottom-u*.22f);
            shape.lineTo(r.left,r.top+u*.3f);shape.quadTo(r.left,r.top+u*.12f,r.left+u*.14f,r.top+u*.12f);
            shape.cubicTo(r.left+u*.07f,r.top-u*.02f,r.left+u*.35f,r.top-u*.02f,r.left+u*.37f,r.top+u*.10f);
            shape.lineTo(r.right-u*.37f,r.top+u*.10f);shape.cubicTo(r.right-u*.35f,r.top-u*.02f,r.right-u*.07f,r.top-u*.02f,r.right-u*.14f,r.top+u*.12f);
            shape.quadTo(r.right,r.top+u*.12f,r.right,r.top+u*.30f);shape.lineTo(r.right,r.bottom-u*.22f);shape.quadTo(r.right,r.bottom,r.right-u*.22f,r.bottom);shape.close();return;
        }
        if(mode==8){for(int k=0;k<40;k++){double a=k*Math.PI/20;float radius=1+.075f*(float)Math.cos(a*6);float x=cx+(w*.46f)*(float)Math.cos(a)*radius,y=cy+(h*.46f)*(float)Math.sin(a)*radius;if(k==0)shape.moveTo(x,y);else shape.lineTo(x,y);}shape.close();return;}
        if(mode==9){shape.moveTo(r.left,r.bottom);shape.cubicTo(r.left,r.top,cx,r.top,r.right,r.top);shape.cubicTo(r.right,r.bottom,cx,r.bottom,r.left,r.bottom);shape.close();return;}
        if(mode==14||mode==18||mode==22){shape.moveTo(r.left+u*.20f,r.top+u*.04f);shape.cubicTo(cx,r.top-u*.02f,r.right-u*.04f,r.top,r.right,r.top+u*.30f);shape.cubicTo(r.right+u*.02f,cy,r.right,r.bottom-u*.04f,r.right-u*.25f,r.bottom);shape.cubicTo(cx,r.bottom+u*.02f,r.left,r.bottom,r.left,r.bottom-u*.27f);shape.cubicTo(r.left,cy,r.left,r.top+u*.08f,r.left+u*.20f,r.top+u*.04f);shape.close();return;}
        if(mode==19){shape.moveTo(r.left+u*.15f,r.top);shape.lineTo(r.right,r.top);shape.lineTo(r.right-u*.15f,r.bottom);shape.lineTo(r.left,r.bottom);shape.close();return;}
        float radius=mode==13?u*.48f:mode==16?u*.045f:mode==20?u*.12f:u*.23f;
        shape.addRoundRect(r,radius,radius,Path.Direction.CW);
    }
}
