package com.cschlisner.hbd.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.cschlisner.hbd.actor.ui.PaddleInputHandler;
import com.cschlisner.hbd.util.Level;
import com.cschlisner.hbd.util.TextureAnimator;
import com.cschlisner.hbd.screen.GameScreen;
import com.cschlisner.hbd.util.Const;

public class PlayerPaddle extends Actor {
    float stateTime;
    TextureAnimator animator;
    boolean animating = false;

    // Paddle Input (draw on UI) that we update world paddle position from
    public PaddleInputHandler paddleInput;

    // position data
    float defaultWidth;
    float defaultHeight;
    float defpaddlex;
    float defpaddley;

    // Box2d stuff
    Vector2 position;
    BodyDef bodyDef;
    public Body body;
    Fixture fixture;
    PolygonShape paddleShape;
    float movementSpeed = 0.1f;

    public GameScreen screen;
    public Level level;
    public PlayerPaddle(GameScreen screen, Level level){
        this.setName("Paddle");
        this.screen=screen;
        this.level = level;
        // Textures
        Texture texture = screen.assManager.get(Const.TEXTURES[0], Texture.class);
        animator = new TextureAnimator(texture, 5, 1, 0.3f);
        TextureRegion frame = animator.getFrame(0,false);
        stateTime = 0.2f;


        // position, size
        defaultWidth = (float)frame.getRegionWidth() / Const.PPM;
        defaultHeight = (float)frame.getRegionHeight() / Const.PPM;
        defpaddlex = 0;
        defpaddley = 3;
        position = new Vector2(defpaddlex,defpaddley);

        float x=defpaddlex, y=defpaddley, w=defaultWidth, h=defaultHeight;
        setBounds(x-w/2, y-h/2, w, h);

        // Box2d init
        this.body = createBody(new Vector2(defpaddlex,defpaddley));

        // paddleInput will draw a scaled version of this paddle on the UI camera

//        drawBounds();
        paddleInput = new PaddleInputHandler(this);
    }

    private Body createBody(Vector2 position){
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(position);
        Body body = screen.game.getWorld().createBody(bodyDef);
        paddleShape = new PolygonShape();
        paddleShape.setAsBox(getWidth()/2, defaultHeight/2);
        FixtureDef fDef = new FixtureDef();
        fDef.filter.categoryBits = Const.PADDLE_FLAG;
        fDef.filter.maskBits = Const._COLLISION_MASK;
        fDef.shape = paddleShape;
        fDef.density = Const.PADDLE_DENSITY;
        fDef.friction = Const.PADDLE_FRICTION;
        fDef.restitution = Const.PADDLE_RESTITUTION;
        fixture = body.createFixture(fDef);
        fixture.setUserData(this);
        return body;
    }

    private void moveTo(float x){
//        body.applyForceToCenter(Gdx.input.getDeltaX(),0, true);
        body.setTransform(new Vector2(x, body.getPosition().y), body.getAngle());
    }


    public void reset(Level level){
        this.level = level;
        float x=defpaddlex, y=defpaddley, w=defaultWidth, h=defaultHeight;
        body.setTransform(new Vector2(x,y), 0);
        setBounds(x-w/2, y-h/2, w, h);
        setWidth(defaultWidth);
        paddleInput.reset();
    }


    @Override
    public void setWidth(float width) {
        paddleShape = new PolygonShape();
        float x=getX(), y=getY(), h=defaultHeight;
        setBounds(x, y, width, h);
        paddleShape.setAsBox(width/2, defaultHeight/2);
        FixtureDef fDef = new FixtureDef();
        fDef.filter.categoryBits = Const.PADDLE_FLAG;
        fDef.filter.maskBits = Const._COLLISION_MASK;
        fDef.shape = paddleShape;
        fDef.density = Const.PADDLE_DENSITY;
        fDef.friction = Const.PADDLE_FRICTION;
        fDef.restitution = Const.PADDLE_RESTITUTION;
        body.destroyFixture(fixture);
        fixture = body.createFixture(fDef);
        fixture.setUserData(this);
        paddleInput.reset();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // Set position based on body position
        this.position = body.getPosition();
        setPosition(position.x-getWidth()/2, position.y-defaultHeight/2);
    }

    public void drawBounds(){
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setColor(Color.LIME);
    }

    ShapeRenderer shapeRenderer;

    @Override
    public void draw(Batch batch, float parentAlpha) {
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        // Get current frame of animation for the current stateTime
        batch.draw(animating?animator.getFrame(stateTime, true):animator.still, getX(),
                getY(), getOriginX(), getOriginY(), getWidth(), defaultHeight, 1f, 1f, getRotation());

        if (animating) {
            animating = !animator.animations.get(0).isAnimationFinished(stateTime);
        }

        if (shapeRenderer != null) {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setProjectionMatrix(this.screen.camera.combined);
            shapeRenderer.setColor(Color.LIME);
            shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
            shapeRenderer.end();
            batch.begin();
        }
    }

    long animating_s;

    public void handleCollision() {
        animating = true;
        animating_s = System.currentTimeMillis();
        stateTime = 0.2f;
    }

    public void dispose(){
        paddleShape.dispose();
    }
}
