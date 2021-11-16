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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import org.w3c.dom.css.Rect;

public class PlayerPaddle extends Actor implements Collision {
    float stateTime;
    TextureAnimator animator;
    boolean animating = false;
    float SCR_W;
    float SCR_H;
    int defaultWidth;
    int defaultHeight;
    int twidth, theight;
    float defpaddlex;
    float defpaddley;
    Rectangle boundingBox;
    int marginx, marginy;

    InputListener paddleInputListener = new InputListener() {
        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            float xx = boundingBox.getX()+Gdx.input.getDeltaX();
            float finalx = xx < 0 ? 0 : xx+boundingBox.getWidth()>SCR_W ? SCR_W- boundingBox.getWidth() : xx;
            moveTo(finalx);
        }

        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
        }
    };

    GameScreen screen;
    public PlayerPaddle(GameScreen screen){
        marginx = marginy = 200;
        this.screen=screen;
        this.setName("Paddle");
        Texture texture = screen.assManager.get(Const.TEXTURES[0], Texture.class);

        defaultWidth = texture.getWidth() / 5;
        defaultHeight = texture.getHeight();


        defpaddlex = screen.camera.viewportWidth / 2.0f - (defaultWidth/2.0f);
        float GH = screen.camera.viewportHeight;
        defpaddley = (GH/25.0f);
        animator = new TextureAnimator(texture, 5, 1, 0.3f);
        boundingBox = new Rectangle(defpaddlex,defpaddley,defaultWidth,1.2f*defaultHeight);
        stateTime = 0.2f;

        setBounds(defpaddlex-marginx/2.0f,defpaddley-(marginy/2.0f),defaultWidth+marginx, 1.2f*defaultHeight+marginy);

        this.addListener(paddleInputListener);

        SCR_W = screen.camera.viewportWidth;
        SCR_H = screen.camera.viewportHeight;
    }

    private void moveTo(float x){
        setX(x-(marginx/2.0f));
        boundingBox.setX(x);
    }

    public void reset(){
        setBounds(defpaddlex-marginx/2.0f,defpaddley-(marginy/2.0f),defaultWidth+marginx, 1.2f*defaultHeight+marginy);
        boundingBox = new Rectangle(defpaddlex,defpaddley,defaultWidth,1.2f*defaultHeight);
    }

    @Override
    public void setWidth(float width) {
        boundingBox.setWidth(width);
        boundingBox.setWidth(width+marginx);
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
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(boundingBox.getX(), boundingBox.getY(), boundingBox.getWidth(), boundingBox.getHeight());
            shapeRenderer.end();
            batch.begin();
        }
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        // Get current frame of animation for the current stateTime
        batch.draw(animating?animator.getFrame(stateTime, true):animator.still, boundingBox.getX(),
                boundingBox.getY(), getOriginX(), getOriginY(), boundingBox.getWidth(), boundingBox.getHeight(), 1, 1, getRotation());

        if (animating)
            animating = !animator.animations.get(0).isAnimationFinished(stateTime);
    }

    @Override
    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    long animating_s;
    @Override
    public void handleCollision() {
        animating = true;
        animating_s = System.currentTimeMillis();
        stateTime = 0.2f;
    }

    public void dispose(){

    }
}
