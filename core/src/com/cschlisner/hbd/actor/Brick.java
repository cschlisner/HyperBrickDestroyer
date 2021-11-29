package com.cschlisner.hbd.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.cschlisner.hbd.actor.ui.InfoBar;
import com.cschlisner.hbd.util.Const;
import com.cschlisner.hbd.util.Level;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import javax.swing.plaf.ColorUIResource;

public class Brick extends Actor {
    static Random rng = new Random();

    // Bricktypes / colors
    public enum BrickType {
        Normal,
        Weak,
        Tough,
        Immune,
        PaddleSmall,
        BallSpeed,
        PaddleBig,
        BallSpawn,
        Explosive
    }
    public static Hashtable<BrickType,Color> brickColors = new Hashtable<>();
    public static final int BRICKTYPE_COUNT = BrickType.values().length;
    static {
        Color[] palate = {Color.SLATE, Color.CYAN, Color.LIGHT_GRAY, Color.WHITE,
                Color.MAGENTA, Color.LIME, Color.GOLDENROD, Color.SKY, Color.FIREBRICK};
        // fixed color palate for special bricks
        int i = 0;
        for (BrickType t : BrickType.values())
            brickColors.put(t, palate[i++]);
    }

    Level level;
    public BrickType type;
    float maxHealth;
    public float health;
    int mapx,mapy;

    // Scene2D
    TextureRegion brickTexture;
    TextureRegion[][] dmgTextures;
    public ParticleEffect breakEffect;
    String effectName;
    static Hashtable<String, ParticleEffectPool> effectPools = new Hashtable<>();
    Sound breakSound, hitSound, hitImmune, explodeSound;
    float cx;
    float cy;

    // Box2d stuff
    Vector2 position;
    BodyDef bodyDef;
    public Body body;
    Fixture fixture;
    PolygonShape brickShape;
    float movementSpeed = 0.01f;

    /*
        x,y = coordinates in LevelManager's brick table
     */
    public Brick(Level level, int x, int y, BrickType type){
        int typeInd = type.ordinal()+1;
        this.setName("Brick"+typeInd);
        this.level = level;
        this.type = type;
        if (type != BrickType.Normal)
            setColor(brickColors.get(type));
        else setColor(new Color(rng.nextFloat(), rng.nextFloat(), rng.nextFloat(), 1));

        // textures: brick type texture and damage textures
        Texture dmgTexture = level.game.assetManager.get(Const.TEXTURES[2], Texture.class);
        dmgTextures = TextureRegion.split(dmgTexture, dmgTexture.getWidth() / 6, dmgTexture.getHeight());
        Texture brickTex = level.game.assetManager.get(Const.TEXTURES[2+typeInd], Texture.class);
        brickTexture = new TextureRegion(brickTex);

        // spacial vars
        mapx = x;
        mapy = y;
        float tw = brickTex.getWidth() / Const.PPM; // drawing to world-space
        float th = brickTex.getHeight() / Const.PPM;

        float dx = (x*tw)-level.WRLDWR;
        float dy = level.WRLDH-((y+1)*th);
        setBounds(dx,dy,tw,th);
        cx = dx + (tw/2);
        cy = dy + (th/2);

        // Effects: break and explode (for tnt)
        effectName = type==BrickType.Explosive?Const.PARTICLES[3]:Const.PARTICLES[2];
        if (!effectPools.containsKey(effectName)) {
            breakEffect = level.game.assetManager.get(effectName, ParticleEffect.class);
            effectPools.put(effectName, new ParticleEffectPool(breakEffect, 10, 20));
        }

        breakEffect = effectPools.get(effectName).obtain();

        breakEffect.getEmitters().first().setPosition(cx,cy);
        if (type != BrickType.Explosive) {
            float[] temp = breakEffect.getEmitters().first().getTint().getColors();
            Color col = getColor();
            temp[0] = col.r;
            temp[1] = col.g;
            temp[2] = col.b;
        }

        // sounds sometimes get unloaded if application gets paused?
        breakSound = level.game.assetManager.get(Const.SOUNDS[3], Sound.class);
        hitSound = level.game.assetManager.get(Const.SOUNDS[6], Sound.class);
        hitImmune = level.game.assetManager.get(Const.SOUNDS[4], Sound.class);
        explodeSound = level.game.assetManager.get(Const.SOUNDS[5], Sound.class);

        // handle brick type specific attributes
        maxHealth = type==BrickType.Weak?1:type==BrickType.Tough?4:2;
        health= maxHealth;


        this.body = createBody(new Vector2(cx,cy));

    }

    private Body createBody(Vector2 position){
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(position);
        Body body = level.game.getWorld().createBody(bodyDef);
        brickShape = new PolygonShape();
        brickShape.setAsBox(getWidth()/2, getHeight()/2);
        FixtureDef fDef = new FixtureDef();
        fDef.filter.categoryBits = Const.BRICK_FLAG;
        fDef.filter.maskBits = Const._COLLISION_MASK;
        fDef.shape = brickShape;
        fDef.density = Const.BRICK_DENSITY;
        fDef.friction = Const.BRICK_FRICTION;
        fDef.restitution = Const.BRICK_RESTITUTION;
        fixture = body.createFixture(fDef);
        fixture.setUserData(this);
        return body;
    }

    // return a Texture based on how damaged the brick is
    private TextureRegion getCurDmgTex(){
        int len = dmgTextures[0].length;
        int i = (int)((health/maxHealth)*(float)len);
        return dmgTextures[0][len-i];
    }

    boolean stopeffect = false;
    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.setProjectionMatrix(getStage().getCamera().combined);


        if (!broken && health <= 0) {
            brickBroken();
        }
        if (broken) {
            if (breakEffect.isComplete()) {
                if (type != BrickType.Immune) {
                    --level.bricksToClear;
                }
                breakEffect.reset();
                stopeffect = true;
                remove();
            }
            if (!stopeffect) {
                breakEffect.update(Gdx.graphics.getDeltaTime());
                breakEffect.draw(batch);
            }
        }
        else if (health > 0){
            batch.draw(brickTexture, getX(), getY(), getOriginX(), getOriginY(),
                    getWidth(), getHeight(), 1, 1, getRotation());
            if (health < maxHealth) {
                batch.draw(getCurDmgTex(), getX(), getY(), getOriginX(), getOriginY(),
                        getWidth(), getHeight(), 1, 1, getRotation());
            }
        }

    }

    @Override
    public void act(float delta) {

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

    public void takeDamage(){
        if (type==BrickType.Immune){
            hitImmune.play();
            return;
        }
        --health;
        hitSound.play();
        level.manager.incScore();
    }
    public volatile boolean broken;
    public void brickBroken(){
        // remove from world
        level.game.getWorld().destroyBody(this.body);
        this.broken=true;
        if (breakEffect.isComplete())
            breakEffect.start();
        breakSound.play();
        switch (type){
            case BallSpawn:
                Ball[] balls = new Ball[2];
                for (Ball b : balls) {
                    b = new Ball(level, false);
                    b.body.setTransform(new Vector2(cx+rng.nextFloat()*4-4,cy),0);
                    level.balls.addActor(b);
                    b.kickOff(2);
                }
                getStage().addActor(level.balls);
                break;
            case PaddleSmall:
                getPaddle().setWidth(getPaddle().getWidth()/1.5f);
                break;
            case PaddleBig:
                getPaddle().setWidth(getPaddle().getWidth()*1.5f);
                break;
            case BallSpeed:
                getBall().incSpeed(1.5f);
                break;
            case Explosive:
                explodeSound.play();
                for (Brick b : level.getNeighbors(mapx,mapy,1)) {
                    // avoids circular reference in case two explosives are next to eachother
                    if (b.health>0) {
                        b.health = 0;
                        b.brickBroken();
                    }
                }
        }
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
    Ball gameBall = null;
    private Ball getBall(){
        if (gameBall==null){
            for (Actor a : getStage().getActors())
                if (a.getName()!=null)
                    if (a.getName().equals("Ball"))
                        gameBall = (Ball) a;
        }
        return gameBall;
    }

    @Override
    public boolean remove() {
        effectPools.get(effectName).free((ParticleEffectPool.PooledEffect) breakEffect);
        // remove from stage
        return super.remove();
    }

    public void disposeAssests(){
        if (breakEffect!=null) breakEffect.dispose();
    }
}
