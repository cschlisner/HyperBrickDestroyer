package com.cschlisner.hbd.actor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.cschlisner.hbd.HyperBrickGame;
import com.cschlisner.hbd.actor.PlayerPaddle;
import com.cschlisner.hbd.util.Const;
import com.cschlisner.hbd.util.Level;

public class PaddleInputHandler extends Actor {
    InputListener paddleInputListener = new InputListener() {
        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            float xx = getX()+Gdx.input.getDeltaX();
            float finalx = xx < 0 ? 0 : xx+getWidth()>SCR_W ? SCR_W- getWidth() : xx;
            setX(finalx);
        }


        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
        }
    };

    PlayerPaddle paddle;
    HyperBrickGame game;

    float SCR_W, SCR_H;
    float SCRWR, SCRHR;
    float OX, OY;
    ShapeRenderer shapeRenderer;
    public PaddleInputHandler(PlayerPaddle paddle){
        this.paddle=paddle;
        this.game = paddle.screen.game;
        this.curLevel = paddle.screen.levelManager.curLevel;

        this.addListener(paddleInputListener);

        // drawing to UI screen, using pixel units
        SCR_W = paddle.screen.UIcamera.viewportWidth;
        SCR_H = paddle.screen.UIcamera.viewportHeight;
        SCRHR = 0.5f * SCR_H;
        SCRWR = 0.5f * SCR_W;
        OX = paddle.screen.game.TSCRX;
        OY = paddle.screen.game.TSCRY;

        // Initial bounds are 1:1 with world paddle
        // These will be scaled when we zoom
        reset();

        // just draw some type of geometric indicator
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setProjectionMatrix(paddle.screen.UIcamera.combined);
        shapeRenderer.setColor(Color.YELLOW);
    }

    // converts world position to screen(viewport) position
    private Vector3 convertWorldPos(Vector3 worldPos){
        Vector3 gameCamPos = worldPos.cpy();
        paddle.screen.camera.project(gameCamPos);
        float x = gameCamPos.x + OX;
        float y = gameCamPos.y +OY ;
        return new Vector3(x,y,0);
    }

    // converts screen(viewport) position to world position
    private Vector3 convertCameraPos(Vector3 cameraPos){
        Vector3 worldPos = cameraPos.cpy();
        paddle.screen.camera.unproject(worldPos);
        worldPos.y = game.SCRH-worldPos.y;
        return worldPos;
    }


    // reset paddle position and size. also updates level
    public void reset(){
        relP = getRelPos();
        relS = getRelSize();
        // updates input paddle size based on screen size of input paddle -- 1:1 scale
        Vector3 pcpos = convertWorldPos(new Vector3(paddle.getX(),paddle.getY(),0));
        Vector3 dims = convertWorldPos(new Vector3(paddle.getX()+ paddle.getWidth(), paddle.getY() + paddle.getHeight(), 0));
        this.setBounds(pcpos.x, pcpos.y, dims.x-pcpos.x, dims.y-pcpos.y);

        // update level (for world size)
        this.curLevel = paddle.screen.levelManager.curLevel;
        // relative position, size of input paddle on screen to paddle in world
//        Vector2 relsize = getRelSize();
//        Vector2 relpos = getRelPos();
//        // now we can control the paddle regardless of where it is in the world
//        this.setBounds(relpos.x, relpos.y, relsize.x, relsize.y);
    }


    // get size of input paddle relative to world paddle
    public Vector2 getRelSize(){
        float relW = paddle.getWidth() / curLevel.WRLDW;
        float relH = paddle.getHeight() / curLevel.WRLDH;
        return new Vector2(relW * SCR_W, relH * SCR_H);
    }

    // get position of input paddle relative to world paddle
    private Vector2 getRelPos(){
        float relX = (paddle.getX()+ curLevel.WRLDWR) / curLevel.WRLDW;
        float relY = (paddle.getY()) / curLevel.WRLDH;
        return new Vector2(relX * SCR_W, relY * SCR_H);
    }

    private void updateWorldPaddle(){
        // get world position of inputpaddle (center)
        ipWpos = convertCameraPos(new Vector3(getX()+(getWidth()/2), getY()+(getHeight()/2),0));
        // difference between world padddle and input paddle in world units
        Vector2 padPos = paddle.body.getPosition();
        float posDelta = ipWpos.x - padPos.x;
        if (Math.abs(posDelta) < 0.1)
            posDelta=0;

        float pw = paddle.getWidth()/2;
        float xx = padPos.x+posDelta;
        float pdf = xx-pw < -curLevel.WRLDWR ? pw-padPos.x-curLevel.WRLDWR :
                xx+pw > curLevel.WRLDWR ? curLevel.WRLDWR-pw-padPos.x : posDelta*Const.PADDLE_SPEED;

        paddle.body.setLinearVelocity(new Vector2(pdf, 0));
//        paddle.body.applyLinearImpulse(new Vector2(posDelta, 0), new Vector2(0,0), true);
    }

    Vector3 ipWpos = new Vector3();

    Level curLevel;
    public boolean enabled = true;
    public void switchOff(){
        this.removeListener(paddleInputListener);
        enabled = false;
    }

    public void switchOn(){
        this.addListener(paddleInputListener);
        enabled = true;
    }

    Rectangle relPaddle = new Rectangle();
    Vector2 relP = new Vector2();
    Vector2 relS = new Vector2();
    @Override
    public void act(float delta) {

        // update the level we are on if it changed
        if (curLevel.level_c < paddle.screen.levelManager.curLevel.level_c) {
            reset();
        }

        // Update position of the world paddle
        updateWorldPaddle();

        relP = getRelPos();
        relS = getRelSize();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (enabled) {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setProjectionMatrix(paddle.screen.UIcamera.combined);
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(relP.x, relP.y, relS.x, relS.y);
//            shapeRenderer.setProjectionMatrix(paddle.screen.camera.combined);
//            shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
//            shapeRenderer.setColor(Color.RED);
//            shapeRenderer.circle(ipWpos.x, ipWpos.y, 0.2f, 30);
            shapeRenderer.end();
            batch.begin();
        }
    }
}
