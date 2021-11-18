package com.cschlisner.hbd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import org.w3c.dom.css.Rect;

public class PlayerPaddle extends Actor {
    float stateTime;
    TextureAnimator animator;
    boolean animating = false;

    // position data
    float SCR_W;
    float SCR_H;
    float defaultWidth;
    float width;
    float defaultHeight;
    float defpaddlex;
    float defpaddley;

    // Box2d stuff
    Vector2 position;
    BodyDef bodyDef;
    Body body;
    Fixture fixture;
    PolygonShape paddleShape;
    float movementSpeed = 0.01f;

    InputListener paddleInputListener = new InputListener() {
        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            float xx = (Gdx.input.getDeltaX()*movementSpeed);
            float l = body.getPosition().x-(width/2);
            float finalx = l < 0 ? xx+l : SCR_W-l+width < 0 ? xx+(SCR_W-l+width) : xx;
            body.setTransform(new Vector2(body.getPosition().x+finalx, body.getPosition().y), body.getAngle());
        }

        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
        }
    };

    GameScreen screen;
    public PlayerPaddle(GameScreen screen){
        this.setName("Paddle");
        this.screen=screen;
        SCR_W = screen.camera.viewportWidth;
        SCR_H = screen.camera.viewportHeight;

        // Textures
        Texture texture = screen.assManager.get(Const.TEXTURES[0], Texture.class);
        animator = new TextureAnimator(texture, 5, 1, 0.3f);
        TextureRegion frame = animator.getFrame(0,false);
        stateTime = 0.2f;

        // position, size
        defaultWidth = frame.getRegionWidth() / Const.PPM;
        width = defaultWidth;
        defaultHeight = frame.getRegionHeight() / Const.PPM;
        defpaddlex = SCR_W / 2.0f;
        defpaddley = 3;
        position = new Vector2(defpaddlex,defpaddley);

        float x=defpaddlex, y=defpaddley, w=defaultWidth, h=defaultHeight;
        setBounds(x-w/2, y-h/2, w, h);

        // Box2d init
        this.body = createBody(new Vector2(defpaddlex,defpaddley));
        this.addListener(paddleInputListener);
        drawBounds();

    }


    private Body createBody(Vector2 position){
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(position);
        Body body = screen.game.getWorld().createBody(bodyDef);
        paddleShape = new PolygonShape();
        paddleShape.setAsBox(width/2, defaultHeight/2);
        FixtureDef fDef = new FixtureDef();
        fDef.filter.categoryBits = Const.PADDLE_FLAG;
        fDef.filter.maskBits = Const._COLLISION_MASK;
        fDef.shape = paddleShape;
        fDef.density = 1.0f;
        fDef.friction = 0.9f;
        fDef.restitution = 0.1f;
        fixture = body.createFixture(fDef);
        fixture.setUserData(this);
        return body;
    }

    private void moveTo(float x){
//        body.applyForceToCenter(Gdx.input.getDeltaX(),0, true);
        body.setTransform(new Vector2(x, body.getPosition().y), body.getAngle());
    }


    public void reset(){
        float x=defpaddlex, y=defpaddley, w=defaultWidth, h=defaultHeight;
        setBounds(x-w/2, y-h/2, w, h);
        screen.game.getWorld().destroyBody(body);
        width = defaultWidth;
        body = createBody(new Vector2(defpaddlex,defpaddley));
    }

    @Override
    public void setWidth(float width) {
        paddleShape = new PolygonShape();
        float x=getX(), y=defpaddley, w=width, h=defaultHeight;
        setBounds(x, y, w, h);
        paddleShape.setAsBox(width/2, defaultHeight/2);
        body.destroyFixture(fixture);
        FixtureDef fDef = new FixtureDef();
        fDef.filter.categoryBits = Const.PADDLE_FLAG;
        fDef.filter.maskBits = Const._COLLISION_MASK;
        fDef.shape = paddleShape;
        fDef.density = 1.0f;
        fDef.friction = 0.9f;
        fDef.restitution = 0.1f;
        fixture = body.createFixture(fDef);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        this.position = body.getPosition();
        setPosition(position.x-width/2, position.y-defaultHeight/2);
    }

    public void drawBounds(){
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setProjectionMatrix(this.screen.camera.combined);
        shapeRenderer.setColor(Color.LIME);
    }

    ShapeRenderer shapeRenderer;

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shapeRenderer != null) {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.LIME);
            shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
            shapeRenderer.end();
            batch.begin();
        }
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        // Get current frame of animation for the current stateTime
        batch.draw(animating?animator.getFrame(stateTime, true):animator.still, getX(),
                getY(), getOriginX(), getOriginY(), width, defaultHeight, 1f, 1f, getRotation());

        if (animating)
            animating = !animator.animations.get(0).isAnimationFinished(stateTime);
    }

    long animating_s;

    public void handleCollision() {
        animating = true;
        animating_s = System.currentTimeMillis();
        stateTime = 0.2f;
    }

    public void dispose(){

    }
}
