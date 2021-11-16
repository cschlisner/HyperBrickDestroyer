package com.cschlisner.hbd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.Hashtable;
import java.util.Random;

public class Brick extends Actor implements Collision {

    public static Hashtable<BrickType,Color> brickColors = new Hashtable<>();

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
    public static final int BRICKTYPE_COUNT = BrickType.values().length;
    static Random rng = new Random();

    static {

        Color[] palate = {Color.SLATE, Color.CYAN, Color.LIGHT_GRAY, Color.WHITE,
                Color.MAGENTA, Color.LIME, Color.GOLDENROD, Color.SKY, Color.FIREBRICK};
        // fixed color palate for special bricks
        int i = 0;
        for (BrickType t : BrickType.values())
            brickColors.put(t, palate[i++]);
    }

    Rectangle boundingBox;

    @Override
    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    @Override
    public void handleCollision() {
        takeDamage();
    }


    LevelManager manager;
    BrickType type;

    TextureRegion brickTexture;
    TextureRegion[][] dmgTextures;

    float maxHealth;
    float health;
    ParticleEffect breakEffect;
    ParticleEffect explodeEffect;
    Sound breakSound, hitSound, hitImmune, explodeSound;

    int mapx,mapy;
    float cx;
    float cy;

    /*
        x,y = coordinates in LevelManager's brick table
     */
    public Brick(LevelManager manager, int x, int y, int typeInd){
        this.manager = manager;
        this.type = BrickType.values()[typeInd-1];
        setColor(brickColors.get(type));

        // textures: brick type texture and damage textures
        Texture dmgTexture = manager.screen.assManager.get(Const.TEXTURES[2], Texture.class);
        dmgTextures = TextureRegion.split(dmgTexture, dmgTexture.getWidth() / 6, dmgTexture.getHeight());
        Texture brickTex = manager.screen.assManager.get(Const.TEXTURES[2+typeInd], Texture.class);
        brickTexture = new TextureRegion(brickTex);


        // spacial vars
        mapx = x;
        mapy = y;
        float tw = brickTex.getWidth();
        float th = brickTex.getHeight();

        float dx = manager.DRAW_X+(x*tw);
        float dy = manager.DRAW_Y-(y*th);
        setBounds(dx,dy,tw,th);
        boundingBox = new Rectangle(dx,dy,tw,th);

        cx = dx + (tw/2);
        cy = dy + (th/2);

        // Effects: break and explode (for tnt)
        breakEffect = new ParticleEffect();
        breakEffect.load(Gdx.files.internal(type==BrickType.Explosive?"particle/explosion.p":"particle/brickbroke.p"),
                Gdx.files.internal("particle"));
        breakEffect.getEmitters().first().setPosition(cx,cy);
        if (type != BrickType.Explosive) {
            float[] temp = breakEffect.getEmitters().first().getTint().getColors();
            Color col = getColor();
            temp[0] = col.r;
            temp[1] = col.g;
            temp[2] = col.b;
        }

        // sounds sometimes get unloaded if application gets paused?
        reloadSounds();

        // handle brick type specific attributes
        maxHealth = type==BrickType.Weak?1:type==BrickType.Tough?4:2;
        health= maxHealth;

    }

    public void reloadSounds(){
        breakSound = manager.screen.assManager.get(Const.SOUNDS[3], Sound.class);
        hitSound = manager.screen.assManager.get(Const.SOUNDS[6], Sound.class);
        hitImmune = manager.screen.assManager.get(Const.SOUNDS[4], Sound.class);
        explodeSound = manager.screen.assManager.get(Const.SOUNDS[5], Sound.class);
    }

    // return a Texture based on how damaged the brick is
    private TextureRegion getCurDmgTex(){
        int len = dmgTextures[0].length;
        int i = (int)((health/maxHealth)*(float)len);
        return dmgTextures[0][len-i];
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        if (health > 0)
            batch.draw(brickTexture, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), 1, 1, getRotation());
        if (health <= 0) {
            breakEffect.update(Gdx.graphics.getDeltaTime());
            breakEffect.draw(batch);
            if (breakEffect.isComplete()) {
                breakEffect.reset();
                --manager.levelBrickCount;
                remove();
            }
        }
        else if (health < maxHealth)
            batch.draw(getCurDmgTex(), getX(), getY(), getOriginX(), getOriginY(),
                    getWidth(), getHeight(), 1, 1, getRotation());
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
        if (health==0)
            brickBroken();
        else hitSound.play();
        getInfobar().score += 10;
    }

    private void brickBroken(){
        boundingBox = null;
        breakEffect.start();
        breakSound.play();
        switch (type){
            case BallSpawn:
                Ball[] balls = new Ball[2];
                for (Ball b : balls) {
                    b = new Ball(manager.screen, manager, false);
                    b.setPosition(cx+rng.nextFloat()*40-20,cy);
                    b.velocity = new Vector2(rng.nextFloat()*2-1, 1.0f);
                    b.isDead=false;
                    manager.spawnedBalls.addActor(b);
                }
                getStage().addActor(manager.spawnedBalls);
                break;
            case PaddleSmall:
                getPaddle().setWidth(getPaddle().getWidth()/1.5f);
                break;
            case PaddleBig:
                getPaddle().setWidth(getPaddle().getWidth()*1.5f);
                break;
            case BallSpeed:
                getBall().speed*=1.5;
                break;
            case Explosive:
                explodeSound.play();
                for (Brick b : manager.getNeighbors(mapx,mapy,1)) {
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

    public void disposeAssests(){
        if (breakEffect!=null) breakEffect.dispose();
        breakSound.dispose();
        explodeSound.dispose();
        hitSound.dispose();
        if (explodeEffect!=null) explodeEffect.dispose();
        hitImmune.dispose();
    }
}
