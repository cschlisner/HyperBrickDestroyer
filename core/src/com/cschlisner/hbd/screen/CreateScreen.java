package com.cschlisner.hbd.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveToAligned;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cschlisner.hbd.HyperBrickGame;
import com.cschlisner.hbd.actor.Brick;
import com.cschlisner.hbd.actor.ui.InfoBar;
import com.cschlisner.hbd.actor.ui.LevelCreator;
import com.cschlisner.hbd.actor.ui.PauseMenu;
import com.cschlisner.hbd.util.Const;
import com.cschlisner.hbd.util.LevelManager;

import jdk.jfr.internal.tool.PrettyWriter;

public class CreateScreen implements Screen, GameViewCtx {
    public HyperBrickGame game;
    Stage gameStage, UIStage;
    public PauseMenu pauseMenu;

    LevelCreator creator;
    private InputMultiplexer inputMultiplexer = new InputMultiplexer();
    private boolean paused;
    LevelManager levelManager;

    ActorGestureListener gestureListener = new ActorGestureListener(){
        @Override
        public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
            if (event.getTarget() instanceof Group) {
                super.pan(event, x, y, deltaX, deltaY);
                game.translateCamera(-deltaX / Const.PPM, -deltaY / Const.PPM);
            }
        }

        @Override
        public void zoom(InputEvent event, float initialDistance, float distance) {
            if (event.getTarget() instanceof Group) {
                super.zoom(event, initialDistance, distance);
                // movement deltas (calc using areas of squares defined by pointer locations)
                float delta = distance - initialDistance;
                game.camera.zoom -= delta / Const.PPM / 500;
                game.updateCamera();
            }
        }

        @Override
        public void tap(InputEvent event, float x, float y, int count, int button) {
            if (event.getTarget() instanceof Group) {
                super.tap(event, x, y, count, button);
                int[] gridplace = screen2Grid(x,y);
                if (gridplace[0]+gridplace[1] >= 0){
                    Brick newB = new Brick(levelManager.curLevel, gridplace[0], gridplace[1], creator.brickSelct.getSelected());
                    levelManager.curLevel.bricks.addActor(newB);
                    gameStage.addActor(newB);
                }
            }
        }
    };

    public int[] screen2Grid(float x, float y){
        Vector3 worldPos = new Vector3(x,game.TSCRH-y,0);
        game.camera.unproject(worldPos);
//        worldPos.y = game.SCRH-worldPos.y;

        if (Math.abs(worldPos.x) > creator._level.WRLDWR ||
            Math.abs(worldPos.y) > creator._level.WRLDH)
            return new int[]{-1,-1};
        // get grid pos
        int[] gridpos = new int[2];
        gridpos[0] = (int)((worldPos.x+levelManager.curLevel.WRLDWR) / levelManager.curLevel.BRKW); // x
        gridpos[1] = (int)((levelManager.curLevel.WRLDH - worldPos.y) / levelManager.curLevel.BRKH)+1; // y
        return gridpos;
    }

//    public Vector2 grid2Screen(int x, y){
//        Vector3 gameCamPos = worldPos.cpy();
//        paddle.screen.camera.project(gameCamPos);
//        float x = gameCamPos.x + OX;
//        float y = gameCamPos.y +OY ;
//        return new Vector3(x,y,0);
//    };

    public CreateScreen(HyperBrickGame game) {
        this.game = game;
        levelManager = new LevelManager(this);
        levelManager.newLevel(1);

        this.creator = new LevelCreator(this, levelManager.curLevel);


        game.resetCamera();

        gameStage = new Stage(game.gameVP);
        gameStage.getBatch().setProjectionMatrix(game.camera.combined);
        UIStage = new Stage(game.textVP);
        UIStage.getBatch().setProjectionMatrix(game.textCamera.combined);

        UIStage.addActor(creator);
        UIStage.addActor(creator.uiContainer);
//        UIStage.addListener(mapMoveListener);
        UIStage.addListener(gestureListener);
    }

    @Override
    public void show() {
        // Fade in
        ScreenUtils.clear(0.52f, 0.73f, 0.94f, 0.6f);
        UIStage.getRoot().getColor().a = 0;
        UIStage.getRoot().addAction(fadeIn(2.0f));

        // Set up initial input processors
        inputMultiplexer.addProcessor(gameStage);
        inputMultiplexer.addProcessor(UIStage);
        inputMultiplexer.addProcessor(backButtonListener);
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        Gdx.input.setInputProcessor(inputMultiplexer);

        pauseMenu = new PauseMenu(this);


        gameStage.addActor(levelManager.curLevel.actorGroup);

        pauseMenu.quitBtn.setOnClick(new Runnable() {
            @Override
            public void run() {
                dispose();
                game.setScreen(new TitleScreen(game));
            }
        });

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        game.updateCamera();

        gameStage.act();
        gameStage.draw();
        UIStage.act();
        UIStage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public Camera getCamera() {
        return this.game.camera;
    }

    @Override
    public Camera getUICamera() {
        return this.game.textCamera;
    }

    @Override
    public HyperBrickGame getGame() {
        return this.game;
    }

    @Override
    public AssetManager getAssManager() {
        return this.game.assetManager;
    }

    @Override
    public void pause() {
        paused = true;
        UIStage.addActor(pauseMenu.menuGroup);
        inputMultiplexer.removeProcessor(gameStage);
    }

    public void unpause(){
        pauseMenu.menuGroup.remove();
        this.paused = false;
        inputMultiplexer.addProcessor(gameStage);
    }



    private InputProcessor backButtonListener = new InputProcessor() {
        @Override
        public boolean keyDown(int keycode) {
            switch (keycode){
                case Input.Keys.BACK:
                    if (paused)
                        unpause();
                    else pause();
                    return true;
            }
            return true;
        }

        @Override
        public boolean keyUp(int keycode) {
            return true;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            return false;
        }
    };
}
