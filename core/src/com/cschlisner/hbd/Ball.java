package com.cschlisner.hbd;

import static com.brashmonkey.spriter.Spriter.dispose;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.brashmonkey.spriter.Spriter;

import org.w3c.dom.css.Rect;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import javax.swing.CellEditor;

import jdk.tools.jlink.internal.plugins.VendorBugURLPlugin;

public class Ball extends Actor {
    GameScreen screen;
    LevelManager levelManager;
    public boolean isPrimary;
    public boolean isDead = true;

    Random rng = new Random();

    float SCRW, SCRH;
    TextureRegion tex;
    ParticleEffect traceEffect, bounceEffect;
    Sound bounceSound;

    float r;
    Vector2 velocity;
    Vector2 position;
    float defSpeed = 1100.0f;
    float speed = defSpeed;
    float spin = 0;

    // naive-phys-x
    ArrayList<Vector2> ballCollisionPoints = new ArrayList<>();
    Circle collisionCircle;
    Rectangle collisionRect;

    // Box2d stuff
    BodyDef bodyDef;
    Body body;
    Fixture fixture;
    CircleShape ballCircle;

    public Ball(GameScreen screen, boolean isPrimary){
        setName("Ball");
        this.screen = screen;
        this.levelManager = screen.levelManager;
        this.isPrimary = isPrimary;

        // Drawing vars
        SCRH = screen.camera.viewportHeight;
        SCRW = screen.camera.viewportWidth;
        tex = new TextureRegion(screen.assManager.get(Const.TEXTURES[1], Texture.class));
        float tw = tex.getRegionWidth();
        float th = tex.getRegionHeight();

        // position -- UNITS PIXELS
        float x = SCRW / 2.0f - (tw/2.0f);
        float y = SCRH / 4.0f;
        setBounds(x/Const.PPM,y/Const.PPM,tw / Const.PPM,th / Const.PPM);
        setOrigin(getX()+tw/2.0f, getY()+(tw/2.0f));
        position = new Vector2(SCRW/2.0f, SCRH/2.0f);
        velocity = new Vector2(0, -1);
        r = getWidth()/2;


        if (isPrimary) {
            traceEffect = new ParticleEffect();
            traceEffect.load(Gdx.files.internal("particle/balltracer.p"), Gdx.files.internal("particle"));
            traceEffect.getEmitters().first().setPosition(position.x, position.y);
            bounceEffect = new ParticleEffect();
            bounceEffect.load(Gdx.files.internal("particle/bounce.p"), Gdx.files.internal("particle"));
        }
        bounceSound = screen.assManager.get(Const.SOUNDS[2], Sound.class);

        // Box2D setup -- UNITS METERES
        this.body = createBody(position);
        isDead=false;

    }

    private Body createBody(Vector2 position){
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        Body body = screen.game.getWorld().createBody(bodyDef);
        ballCircle = new CircleShape();
        ballCircle.setRadius(r / Const.PPM);
        FixtureDef fDef = new FixtureDef();
        fDef.filter.categoryBits = Const.BALL_FLAG;
        fDef.filter.maskBits = Const._COLLISION_MASK;
        fDef.shape = ballCircle;
        fDef.density = 0.3f;
        fDef.friction = 0.4f;
        fDef.restitution = 0.2f;
        fixture = body.createFixture(fDef);
        fixture.setUserData(this);
        ballCircle.dispose();
        return body;
    }

    private void reset(){
        speed = defSpeed;
        float x = SCRW / 2.0f - (getWidth()/2.0f);
        float y = SCRH / 4.0f;
        screen.game.getWorld().destroyBody(this.body);
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
        this.position = this.body.getPosition();
        setBounds(position.x, position.y, getWidth(),getHeight());
        System.out.println(this.body.getPosition()+", ("+getX()+","+getY()+")");

        if (!isDead) {
            if (isPrimary && traceEffect.isComplete())
                traceEffect.start();

            rotateBy(spin);

            if (isPrimary)
                traceEffect.getEmitters().first().setPosition(position.x, position.y);
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
        batch.draw(tex, loc.x, loc.y, getOriginX(), getOriginY(),
                getWidth(), getHeight(), 1, 1, getRotation());
    }

    public void kickOff() {

    }
}
