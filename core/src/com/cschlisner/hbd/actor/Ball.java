package com.cschlisner.hbd.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.cschlisner.hbd.util.Const;
import com.cschlisner.hbd.util.Level;

import java.util.ArrayList;
import java.util.Random;

public class Ball extends Actor {
    Level level;
    public boolean isPrimary;
    public boolean isDead = true;

    Random rng = new Random();

    float WRLDW, WRLDH;
    TextureRegion tex;
    ParticleEffect traceEffect, bounceEffect;
    Sound bounceSound;

    float r;
    Vector2 velocity;
    public Vector2 position;
    Vector2 defPosition;
    public float defSpeed = Const.BALL_SPEED;
    float speed = defSpeed;
    float spin = 0;

    // naive-phys-x
    ArrayList<Vector2> ballCollisionPoints = new ArrayList<>();
    Circle collisionCircle;
    Rectangle collisionRect;

    // Box2d stuff
    BodyDef bodyDef;
    public Body body;
    Fixture fixture;
    CircleShape ballCircle;

    public Ball(Level level, boolean isPrimary){
        setName("Ball");
        this.level = level;
        this.isPrimary = isPrimary;

        // Drawing vars
        WRLDH = level.WRLDH;
        WRLDW = level.WRLDW;

        tex = new TextureRegion(level.game.assetManager.get(Const.TEXTURES[1], Texture.class));
        float tw = tex.getRegionWidth() / Const.PPM; // convert to world units
        float th = tex.getRegionHeight() / Const.PPM;

        // position
        float x = - (tw/2.0f);
        float y = WRLDH / 4.0f - (tw/2.0f);
        setBounds(x, y, tw ,th);
        setOrigin(tw/2.0f, (tw/2.0f));
        defPosition = position = new Vector2(0, WRLDH /2.0f);
        velocity = new Vector2(0, -1);
        r = getWidth()/2;


        if (isPrimary) {
            traceEffect = level.game.assetManager.get(Const.PARTICLES[0], ParticleEffect.class);
            bounceEffect = level.game.assetManager.get(Const.PARTICLES[1], ParticleEffect.class);
            traceEffect.getEmitters().first().setPosition(position.x, position.y);
        }
        bounceSound = level.game.assetManager.get(Const.SOUNDS[2], Sound.class);

        // Box2D setup -- UNITS METERES
        this.body = createBody(position);
        isDead=false;

    }

    private Body createBody(Vector2 position){
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);

        Body body = level.game.getWorld().createBody(bodyDef);
        ballCircle = new CircleShape();
        ballCircle.setRadius(r);
        FixtureDef fDef = new FixtureDef();
        fDef.filter.categoryBits = Const.BALL_FLAG;
        fDef.filter.maskBits = Const._COLLISION_MASK;
        fDef.shape = ballCircle;
        fDef.density = Const.BALL_DENSITY;
        fDef.friction = Const.BALL_FRICTION;
        fDef.restitution = Const.BALL_RESTITUTION;
        fixture = body.createFixture(fDef);
        fixture.setUserData(this);
        ballCircle.dispose();
        body.setBullet(true);
        body.setLinearDamping(-0.02f);
        body.setAngularDamping(-0.1f);
        return body;
    }

    private void reset(){
        speed = defSpeed;
        float x = WRLDW / 2.0f - (getWidth()/2.0f);
        float y = WRLDH / 4.0f;
        position = new Vector2(defPosition);
        level.game.getWorld().destroyBody(this.body);
        this.body=createBody(position);
    }

    public void dispose(){
        bounceEffect.dispose();
        traceEffect.dispose();
    }

    public void handleDeath(){
        if (!isPrimary) {
            remove();
            return;
        }
        reset();
        velocity = new Vector2((rng.nextFloat()*2)-1, -1);
        isDead=true;
        traceEffect.getEmitters().first().setPosition(position.x, position.y);
        traceEffect.reset();
    }


    @Override
    public void act(float delta) {
        // center of body
        this.position = this.body.getPosition();
        // x,y = lower left of ball image
        setBounds(position.x-r, position.y-r, getWidth(),getHeight());
//        Gdx.app.debug(getName(), this.body.getPosition()+", ("+getX()+","+getY()+")");
        if (!isDead) {
            if (isPrimary && traceEffect.isComplete()) {
                traceEffect.start();
            }

            rotateBy(this.body.getAngularVelocity());

            if (isPrimary)
                traceEffect.getEmitters().first().setPosition(position.x, position.y);

            if (position.y < 0)
                handleDeath();
        }
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!isDead && isPrimary) {
            traceEffect.update(Gdx.graphics.getDeltaTime());
            //bounceEffect.update(Gdx.graphics.getDeltaTime());
            traceEffect.draw(batch);
            //bounceEffect.draw(batch);
        }
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        Vector2 loc=body.getPosition();
        batch.draw(tex, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), 1, 1, getRotation());
    }

    public void kickOff() {
        kickOff(-1);
    }
    public void kickOff(float dir) {
        this.isDead = false;
        this.body.setLinearVelocity(new Vector2(rng.nextFloat()*0.1f-0.05f, dir).scl(speed));
    }

    @Override
    public boolean remove() {
        level.game.getWorld().destroyBody(body);
        return super.remove();
    }

    public void onContact(){
        bounceSound.play();
        this.body.setAwake(true);
        Vector2 linVelNorm = body.getLinearVelocity().nor();
        if (linVelNorm.x==0) linVelNorm.x = rng.nextFloat()*2-1;
        if (linVelNorm.y==0) linVelNorm.y = rng.nextFloat()*2-1;
        body.setLinearVelocity(linVelNorm.scl(speed));
        body.setAngularVelocity(body.getAngularVelocity());
    }

    public void incSpeed(float v) {
        speed *= v;
        body.setLinearVelocity(body.getLinearVelocity().nor().scl(speed));
    }
}
