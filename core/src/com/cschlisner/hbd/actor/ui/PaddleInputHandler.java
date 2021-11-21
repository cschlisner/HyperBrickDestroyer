package com.cschlisner.hbd.actor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.cschlisner.hbd.HyperBrickGame;
import com.cschlisner.hbd.actor.PlayerPaddle;
import com.cschlisner.hbd.util.Const;

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

    // converts world position to text camera position
    private Vector3 convertWorldPos(Vector3 worldPos){
        Vector3 gameCamPos = worldPos.cpy();
        paddle.screen.camera.project(gameCamPos);
        float x = gameCamPos.x + SCRWR + OX;
        float y = gameCamPos.y +OY ;
        return new Vector3(x,y,0);
    }

    // converts text camera position to world position
    private Vector3 convertCameraPos(Vector3 cameraPos){
        Vector3 worldPos = cameraPos.cpy();
        paddle.screen.camera.unproject(worldPos);
        worldPos.y = game.SCRH-worldPos.y;
        return worldPos;
    }

    public boolean enabled = true;
    public void switchOff(){
        this.removeListener(paddleInputListener);
        enabled = false;
    }

    public void switchOn(){
        this.addListener(paddleInputListener);
        enabled = true;
    }

    public void reset(){
        Vector3 pcpos = convertWorldPos(new Vector3(paddle.getX(),paddle.getY(),0));
        Vector3 dims = convertWorldPos(new Vector3(paddle.getX()+ paddle.getWidth(), paddle.getY() + paddle.getHeight(), 0));
        this.setBounds(pcpos.x, pcpos.y, dims.x-pcpos.x, dims.y-pcpos.y);
    }

    Vector3 ipWpos = new Vector3();
    @Override
    public void act(float delta) {
        // Update position of the world paddle

        // get world position of inputpaddle (center)
        ipWpos = convertCameraPos(new Vector3(getX()+(getWidth()/2), getY()+(getHeight()/2),0));
        // difference between world padddle and input paddle in world units
        Vector2 padPos = paddle.body.getPosition();
        float posDelta = ipWpos.x - padPos.x;
        if (Math.abs(posDelta) < 0.1)
            posDelta=0;
        paddle.body.setLinearVelocity(new Vector2(posDelta*Const.PADDLE_SPEED, 0));
//        paddle.body.applyLinearImpulse(new Vector2(posDelta, 0), new Vector2(0,0), true);
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
//            shapeRenderer.setProjectionMatrix(paddle.screen.camera.combined);
//            shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
//            shapeRenderer.setColor(Color.RED);
//            shapeRenderer.circle(ipWpos.x, ipWpos.y, 0.2f, 30);
            shapeRenderer.end();
            batch.begin();
        }
    }
}
