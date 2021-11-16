//package com.cschlisner.hbd;
//
//import com.badlogic.gdx.math.Circle;
//import com.badlogic.gdx.math.Rectangle;
//import com.badlogic.gdx.math.Vector2;
//import com.badlogic.gdx.scenes.scene2d.Actor;
//import com.badlogic.gdx.scenes.scene2d.Stage;
//
//import java.util.ArrayList;
//import java.util.Random;
//
//public class CollisionThread extends Thread {
//
//    private Thread intThread;
//
//    float SCRW, SCRH;
//    float cx,cy,r;
//    float ballw=2*r, ballh =2*r ;
//
//    ArrayList<Vector2> ballCollisionPoints;
//    ArrayList<Collision> actorBounds = new ArrayList<>();
//
//    float w = 2*ballw;
//    private Rectangle leftWall;
//    private Rectangle rightWall;
//    private Rectangle topWall;
//
//    private PlayerPaddle paddle;
//    Circle collisionCircle;
//    Rectangle collisionRect;
//
//    Random rng = new Random();
//
//    public CollisionThread(Ball.CollisionHandler handler){
//        this.collisionHandler = handler;
//    }
//
//    @Override
//    public void run() {
//        super.run();
//
//        updateCollisionPoints();
//        collidesWithWalls();
//        for (Collision c : actorBounds) {
//            if (collidesWith(c)) {
//                c.handleCollision();
//            }
//        }
//    }
//
//    public void start(float cx, float cy, float r, float sw, float sh, ArrayList<Collision> actors){
//        this.cx = cx;
//        this.cy = cy;
//        this.r = r;
//        this.SCRH = sh;
//        this.SCRW = sw;
//        this.actorBounds = actors;
//        leftWall = new Rectangle(-w,-w,w,SCRH+w);
//        rightWall = new Rectangle(SCRW,-w,w,SCRH+w);
//        topWall = new Rectangle(-w,SCRH,SCRW+w,w);
////        if (intThread == null || !intThread.isAlive()) {
////            intThread = new Thread (this, "peepee");
////            intThread.start ();
////        }
//    }
//
//    public void cancel(){
//        if (intThread!= null && intThread.isAlive())
//            intThread.interrupt();
//    }
//
//    public boolean running(){
//        return intThread != null && intThread.isAlive();
//    }
//    ArrayList<Vector2> points = new ArrayList<>();
//    public void updateCollisionPoints(){
//        int samplerate = 1; // 1-360 sample rate for points
//        points.clear();
//        for (double i = 0; i < 360; i += samplerate)
//            points.add(new Vector2((int)(cx+(float)Math.cos(i)*r), (int)(cy+(float)Math.sin(i)*r)));
//        ballCollisionPoints = points;
////        collisionCircle = new Circle(cx,cy,r);
//        collisionRect = new Rectangle(cx-r,cy-r,ballw,ballh);
//    }
//
//    ArrayList<Vector2> containedCLP = new ArrayList<>();
//
//    public boolean collidesWith(Collision sq_other) {
//        if (sq_other==null)
//            return false;
//        Rectangle bounding = sq_other.getBoundingBox();
//        return collidesWith(bounding);
//    }
//    public boolean collidesWith(Rectangle bounding){
//        if (bounding==null) return false;
//        float bx = bounding.getX();
//        float by = bounding.getY();
//        if (!bounding.overlaps(collisionRect)) return false;
//        // get points contained in object
//        containedCLP.clear();
//        for (Vector2 cp : ballCollisionPoints) {
//            if (bounding.contains(cp))
//                containedCLP.add(cp);
//        }
//        if (containedCLP.isEmpty())
//            return false;
//        // how far furthest point made it into object
//        float maxDst = 0;
//        // dst to reset ball in x/y dimensions
//        float xAdj = 0, yAdj=0;
//        Vector2 closest = containedCLP.get(rng.nextInt(containedCLP.size()));
//        for (Vector2 ccp : containedCLP){
//            // distance from left to point
//            float dstl = Math.abs(bx-ccp.x);
//            // distance from right to point
//            float dstr = Math.abs(bx+bounding.getWidth()-ccp.x);
//            // dst from top to point
//            float dstt = Math.abs(by+ bounding.getHeight()-ccp.y);
//            // dst from bottom to point
//            float dstb = Math.abs(by-ccp.x);
//            // lesser of dstl,dstr and dstt,dstb determine coordinates of point within bounding rect
//            float dstx = Math.min(dstl, dstr);
//            float dsty = Math.min(dstb, dstt);
//
//            float dst = dstx*dsty;
//            if (dst>maxDst) {
//                maxDst = dst;
//                xAdj = dstx * dstl>dstr?1:-1;
//                yAdj = dsty * dstb>dstt?1:-1;
//                closest = ccp;
//            }
//        }
//        Vector2 adjust = new Vector2(xAdj,yAdj);
//        collisionHandler.run(closest, adjust);
//        return true;
//    }
//    // get closest point in $points to $ref
//
//    private Vector2 getClosest(ArrayList<Vector2> points, Vector2 ref){
//        double dstc = 1000000000000.0f;
//        Vector2 closest = null;
//        for (Vector2 ccp : points){
//            double dstx = Math.abs(ccp.x-ref.x);
//            double dsty = Math.abs(ccp.y-ref.y);
//            double dst = Math.sqrt(dsty*dsty+dstx*dstx);
//            if (dst < dstc) {
//                dstc = dst;
//                closest = ccp;
//            }
//        }
//        return closest;
//    }
//
//    public void collidesWithWalls(){
//        if (cx-r-1 <= 0)
//            collidesWith(leftWall);
//        if (cx+r+1 >= SCRW)
//            collidesWith(rightWall);
//        if (cy+r+1 >= SCRH)
//            collidesWith(topWall);
//    }
//}
