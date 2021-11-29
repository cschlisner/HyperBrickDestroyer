package com.cschlisner.hbd.actor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.cschlisner.hbd.HyperBrickGame;
import com.cschlisner.hbd.actor.Ball;
import com.cschlisner.hbd.actor.PlayerPaddle;
import com.cschlisner.hbd.util.Const;
import com.cschlisner.hbd.util.Level;

import org.graalvm.compiler.lir.LIR;
import org.w3c.dom.Text;

public class PaddleInputHandler extends Actor {

    boolean drawIP = true;
    public InputListener paddleInputListener = new InputListener() {
        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            drawIP=true;
            setPosition(x-getWidth()/2,y-getHeight()/2);
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            if (Const.PADDLE_MOVE_MODE == 0) {
                float xx = getX() + Gdx.input.getDeltaX();
                float finalx = xx < 0 ? 0 : xx + getWidth() > SCR_W ? SCR_W - getWidth() : xx;
                setX(finalx);
            }
            else setX(x-getWidth()/2);
            setY(y-getHeight()/2);
        }


        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
            drawIP=false;
        }
    };

    PlayerPaddle paddle;
    HyperBrickGame game;

    float SCR_W, SCR_H;
    float SCRWR, SCRHR;
    float OX, OY;
    ShapeRenderer shapeRenderer;
    Color defColor;
    public PaddleInputHandler(PlayerPaddle paddle){
        this.paddle=paddle;
        this.game = paddle.screen.game;
        this.curLevel = paddle.screen.levelManager.curLevel;

//        this.addListener(paddleInputListener);

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
        defColor = getColor();
        defColor.a = 0.2f;
        IPCOl = defColor;
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
        this.setBounds(pcpos.x, OY+pcpos.y, dims.x-pcpos.x, dims.y-pcpos.y);

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

        // Update position of the world paddle
        updateWorldPaddle();

        relP = getRelPos();
        relP.y = 0;
        relS = getRelSize();
    }

    float colorDelta=0.0f;
    float bcolorDelta=0.0f;
    float colDir = 0.01f;
    Color relWorldDispCol = new Color(1,0.0f,0.0f, 0.4f);
    Color relPadCol;
    float H, S, L, a;
    public boolean blink = false;
    public boolean kickoff = blink;
    float kickoffColDelay = 0.5f; // seconds -- total animation length
    float kickoffblinkt = 0.05f; // seconds -- how long blinks are
    float kickoffColDelayt = 0; // seconds -- counter
    float last_blink=0;
    Color IPCOl;

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (kickoff || blink) {
            kickoffColDelayt += Gdx.graphics.getDeltaTime();
            if (kickoff) IPCOl = blink ? Color.RED : defColor;
            if (kickoffColDelayt-last_blink > kickoffblinkt) {
                blink = !blink;
                last_blink=kickoffColDelayt;
            }
        }
        if (kickoffColDelayt >= kickoffColDelay) {
            blink = kickoff = false;
            kickoffColDelayt=last_blink=0;
            IPCOl = defColor;
        }

//        colorDelta += colDir;
//        if (colorDelta>=0.9f||colorDelta<0.01f)
//            colDir=-colDir;
        colorDelta = paddle.getTop() / paddle.screen.ball.position.y;

        if (enabled) {
            // draw input paddle
            if (drawIP) {
                batch.setProjectionMatrix(game.textCamera.combined);
                batch.setColor(IPCOl);
                TextureRegion frame = paddle.animating ? paddle.animator.getFrame(paddle.stateTime, true) : paddle.animator.still;
                batch.draw(frame, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), 1f, 1f, getRotation());
            }
            batch.end();

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            // draw relative world width
            shapeRenderer.setProjectionMatrix(paddle.screen.UIcamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            relWorldDispCol = new Color(relWorldDispCol.r, relWorldDispCol.g, relWorldDispCol.b, parentAlpha*relWorldDispCol.a);
            shapeRenderer.setColor(relWorldDispCol);
            shapeRenderer.rect(game.TSCRX, game.TSCRY, SCR_W, relS.y/3);

            // draw relative paddle
            H = Interpolation.fastSlow.apply(160.0f, 259.9999f, colorDelta);
            S = Interpolation.bounceOut.apply(0.2f, 1f, colorDelta);
            L = Interpolation.circleOut.apply(0.32f, 0.5f, colorDelta);
            relPadCol = game.HSLtoColor(H, S, L, parentAlpha * 0.4f);
            shapeRenderer.setColor(relPadCol);
            shapeRenderer.rect(relP.x,  game.TSCRY, relS.x, relS.y/3);

            // draw ball x position
            H = Interpolation.fastSlow.apply(100.0f, 0.0f, colorDelta);
            S = Interpolation.exp10.apply(0.53f, 1f, colorDelta);
            L = Interpolation.exp10.apply(0.56f, 0.36f, colorDelta);
            a = Interpolation.exp10.apply(0.9f, 1f, colorDelta);
            shapeRenderer.setColor(game.HSLtoColor(H,S,L,a));
            Vector3 ballpos = convertWorldPos(new Vector3(paddle.screen.ball.position.x, 0,0));
            shapeRenderer.circle(ballpos.x,  game.TSCRY+relS.y/6, relS.y/6);


            //draw frame around input paddle
            if (drawIP) {
                shapeRenderer.set(ShapeRenderer.ShapeType.Line);
                relPadCol.a = 0.9f * parentAlpha;
                shapeRenderer.setColor( blink ? Color.RED :relPadCol);
                shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
            }
            shapeRenderer.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);

            batch.begin();
        }

    }
}
