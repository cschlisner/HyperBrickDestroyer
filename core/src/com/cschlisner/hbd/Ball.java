package com.cschlisner.hbd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

import org.w3c.dom.css.Rect;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import javax.swing.CellEditor;

import jdk.tools.jlink.internal.plugins.VendorBugURLPlugin;

public class Ball extends Actor {

    public boolean isPrimary = false;

    public boolean isDead = true;

    Random rng = new Random();

    TextureRegion tex;
    Vector2 velocity;
    float defSpeed = 1100.0f;
    float speed = defSpeed;
    float spin = 0;

    float SCRW, SCRH;
    int tw, th;

    float r;
    float cx;
    float cy;

    ParticleEffect traceEffect, bounceEffect;
    Sound bounceSound;

    LevelManager levelManager;

    public Ball(Camera cam, LevelManager manager){
        this(cam, manager, true);
    }

    public Ball(Camera camera, LevelManager manager, boolean isPrimary){
        this.levelManager = manager;
        this.isPrimary = isPrimary;
        setName("Ball");
        Texture texture = new Texture("texture/Ball2.png");
        tex = new TextureRegion(texture);
        tw = texture.getWidth();
        th = texture.getHeight();

        SCRH = camera.viewportHeight;
        SCRW = camera.viewportWidth;

        float x = SCRW / 2.0f - (tw/2.0f);
        float y = SCRH / 4.0f;
        setBounds(x,y,tw,th);
        velocity = new Vector2(0, -1);

        setOrigin(tw/2.0f, th/2.0f);

        cx = getX()+(getWidth()/2);
        cy = getY()+(getHeight()/2);
        r = getWidth()/2;
        if (isPrimary) {
            traceEffect = new ParticleEffect();
            traceEffect.load(Gdx.files.internal("particle/balltracer.p"), Gdx.files.internal("particle"));
            traceEffect.getEmitters().first().setPosition(cx, cy);
            bounceEffect = new ParticleEffect();
            bounceEffect.load(Gdx.files.internal("particle/bounce.p"), Gdx.files.internal("particle"));
        }
        bounceSound = Gdx.audio.newSound(Gdx.files.internal("sound/click.wav"));

        updateCollisionPoints();
    }

    ArrayList<Vector2> ballCollisionPoints;
    Circle collisionCircle;
    Rectangle collisionRect;
    // just use 8 collision points instead of doing pixel perfect
    private void updateCollisionPoints(){
        int samplerate = 1; // 1-360 sample rate for points
        ArrayList<Vector2> points = new ArrayList<>();
        r = getWidth()/2;
        cx = getX()+getWidth()/2;
        cy = getY()+getHeight()/2;
        for (double i = 0; i < 360; i += samplerate)
            points.add(new Vector2((int)(cx+(float)Math.cos(i)*r), (int)(cy+(float)Math.sin(i)*r)));
        ballCollisionPoints = points;
//        collisionCircle = new Circle(cx,cy,r);
        collisionRect = new Rectangle(getX(),getY(),getWidth(),getHeight());
    }

    private void reset(){
        speed = defSpeed;
        float x = SCRW / 2.0f - (tw/2.0f);
        float y = SCRH / 4.0f;
        setPosition(x,y);
    }

    private void handleCollision(Vector2 collisionPoint){
        bounceSound.play();
        Vector2 collTraj = new Vector2(-(collisionPoint.x-cx), -(collisionPoint.y-cy)).nor();
        velocity = velocity.mulAdd(collTraj,1.5f).nor();
        spin = - (velocity.crs(collTraj)*18 % 360);
        if (isPrimary) {
            bounceEffect.getEmitters().first().setPosition(collisionPoint.x,collisionPoint.y);
            bounceEffect.start();
        }
    }

    ArrayList<Vector2> containedCP = new ArrayList<>();
    public boolean collidesWith(Collision sq_other) {
        Rectangle bounding = sq_other.getBoundingBox();
        return collidesWith(bounding);
    }
    public boolean collidesWith(Rectangle bounding){
        if (bounding==null) return false;
        float bx = bounding.getX();
        float by = bounding.getY();
        if (!bounding.overlaps(collisionRect)) return false;
        // get points contained in object
        containedCP.clear();
        for (Vector2 cp : ballCollisionPoints) {
            if (bounding.contains(cp))
                containedCP.add(cp);
        }
        if (containedCP.isEmpty())
            return false;
        // how far furthest point made it into object
        float maxDst = 0;
        // dst to reset ball in x/y dimensions
        float xAdj = 0, yAdj=0;
        Vector2 closest = containedCP.get(rng.nextInt(containedCP.size()));
        for (Vector2 ccp : containedCP){
            // distance from left to point
            float dstl = Math.abs(bx-ccp.x);
            // distance from right to point
            float dstr = Math.abs(bx+bounding.getWidth()-ccp.x);
            // dst from top to point
            float dstt = Math.abs(by+ bounding.getHeight()-ccp.y);
            // dst from bottom to point
            float dstb = Math.abs(by-ccp.x);
            // lesser of dstl,dstr and dstt,dstb determine coordinates of point within bounding rect
            float dstx = Math.min(dstl, dstr);
            float dsty = Math.min(dstb, dstt);

            float dst = dstx*dsty;
            if (dst>maxDst) {
                maxDst = dst;
                xAdj = dstx * dstl>dstr?1:-1;
                yAdj = dsty * dstb>dstt?1:-1;
                closest = ccp;
            }
        }
        handleCollision(closest);
        Vector2 adjust = new Vector2(xAdj,yAdj);
        moveBy(adjust.x,adjust.y);
        return true;
    }

    // get closest point in $points to $ref
    private Vector2 getClosest(ArrayList<Vector2> points, Vector2 ref){
        double dstc = 1000000000000.0f;
        Vector2 closest = null;
        for (Vector2 ccp : points){
            double dstx = Math.abs(ccp.x-ref.x);
            double dsty = Math.abs(ccp.y-ref.y);
            double dst = Math.sqrt(dsty*dsty+dstx*dstx);
            if (dst < dstc) {
                dstc = dst;
                closest = ccp;
            }
        }
        return closest;
    }

    public void collidesWithWalls(){
        float w = 2*getWidth();
        if (getX() <= 0)
            collidesWith(new Rectangle(-w,-w,w,SCRH+w));
        if (getRight() >= SCRW)
            collidesWith(new Rectangle(SCRW,-w,w,SCRH+w));
        if (getTop() >= SCRH)
            collidesWith(new Rectangle(-w,SCRH,SCRW+w,w));

        // ball off screen, reset
        if (getTop() < 0)
            handleDeath();
    }

    public void handleDeath(){
        if (!isPrimary) {
            remove();
            return;
        }
        reset();
        velocity = new Vector2((rng.nextFloat()*2)-1, -1);
        isDead=true;
        traceEffect.getEmitters().first().setPosition(cx,cy);
        traceEffect.reset();
    }

    PlayerPaddle paddle = null;
    private PlayerPaddle getPaddle(){
        if (paddle==null){
            for (Actor a : getStage().getActors()) {
                if (a.getName() != null) {
                    if (a.getName().equals("Paddle"))
                        paddle = (PlayerPaddle) a;
                }
            }
        }
        return paddle;
    }

    InfoBar infobar = null;
    private InfoBar getInfobar(){
        if (infobar==null){
            for (Actor a : getStage().getActors()) {
                if (a.getName() != null) {
                    if (a.getName().equals("Info"))
                        infobar = (InfoBar) a;
                }
            }
        }
        return infobar;
    }

    @Override
    public void act(float delta) {
        if (!isDead) {
            if (isPrimary && traceEffect.isComplete())
                traceEffect.start();
            // collisions....
            updateCollisionPoints();
            collidesWithWalls();
            collidesWith(getInfobar());
            if (collidesWith(getPaddle()))
                getPaddle().handleBallCollision();
            for (Actor b : levelManager.brickGroup.getChildren()) {
                if (collidesWith((Collision) b)) {
                    ((Brick) b).takeDamage();
                }
            }

            // movement
            rotateBy(spin);
            // make sure we don't move the ball out of the screen
            float xmvmt = velocity.x * speed * delta;
            float ymvmt = velocity.y * speed * delta;
            if (getX()+xmvmt < 0)
                xmvmt = -getX()-1;
            else if (getRight()+xmvmt > SCRW)
                xmvmt = SCRW-getRight()+1;
            if (getTop()+ymvmt > getInfobar().getY())
                ymvmt = getInfobar().getY()-getTop()+1;
            moveBy(xmvmt, ymvmt);
            if (isPrimary)
                traceEffect.getEmitters().first().setPosition(cx,cy);
        }
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!isDead && isPrimary) {
            traceEffect.update(Gdx.graphics.getDeltaTime());
            bounceEffect.update(Gdx.graphics.getDeltaTime());
            traceEffect.draw(batch);
            bounceEffect.draw(batch);
        }
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(tex, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), 1, 1, getRotation());
    }

    public void kickOff() {

    }
}
