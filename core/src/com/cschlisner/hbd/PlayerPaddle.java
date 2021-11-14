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
    float defpaddlex;
    float defpaddley;
    Rectangle boundingBox;

    InputListener paddleInputListener = new InputListener() {
        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            float xx = getX()+Gdx.input.getDeltaX();
            float finalx = xx < 0 ? 0 : xx+getWidth()>SCR_W ? SCR_W-getWidth() : xx;
            moveTo(finalx);
        }

        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
        }
    };

    public PlayerPaddle(Camera camera){

        this.setName("Paddle");
        Texture texture = new Texture("texture/paddleSprite.png");

        defaultWidth = texture.getWidth() / 5;
        defaultHeight = texture.getHeight();


        defpaddlex = camera.viewportWidth / 2.0f - (defaultWidth);
        float GH = camera.viewportHeight;
        defpaddley = (GH/25.0f);
        animator = new TextureAnimator(texture, 5, 1, 0.3f);
        setBounds(defpaddlex,defpaddley,defaultWidth,1.2f*defaultHeight);
        boundingBox = new Rectangle(defpaddlex,defpaddley,defaultWidth,1.2f*defaultHeight);
        stateTime = 0.2f;

        this.addListener(paddleInputListener);

        SCR_W = camera.viewportWidth;
        SCR_H = camera.viewportHeight;
    }

    private void moveTo(float x){
        setX(x);
        boundingBox.setX(x);
    }

    public void reset(){
        setBounds(defpaddlex,defpaddley,defaultWidth,1.2f*defaultHeight);
        boundingBox = new Rectangle(defpaddlex,defpaddley,defaultWidth,1.2f*defaultHeight);
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        boundingBox.setWidth(width);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        // Get current frame of animation for the current stateTime
        batch.draw(animating?animator.getFrame(stateTime, true):animator.still, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), 1, 1, getRotation());

        if (animating)
            animating = !animator.animations.get(0).isAnimationFinished(stateTime);
    }
    long animating_s;
    public void handleBallCollision(){
        animating = true;
        animating_s = System.currentTimeMillis();
        stateTime = 0.2f;
    }

    @Override
    public Rectangle getBoundingBox() {
        return boundingBox;
    }
}
