package com.cschlisner.hbd.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cschlisner.hbd.HyperBrickGame;
import com.cschlisner.hbd.actor.ui.InfoBar;
import com.cschlisner.hbd.actor.ui.LevelCreator;
import com.cschlisner.hbd.actor.ui.PauseMenu;
import com.cschlisner.hbd.util.Const;
import com.cschlisner.hbd.util.LevelManager;

import jdk.jfr.internal.tool.PrettyWriter;

public class CreateScreen implements Screen, GameViewCtx {
    public HyperBrickGame game;
    Stage gameStage, UIStage;
    PauseMenu pauseMenu;

    LevelCreator creator;
    private InputMultiplexer inputMultiplexer = new InputMultiplexer();
    private boolean paused;
    LevelManager levelManager;

    InputListener mapMoveListener = new InputListener(){
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            game.translateCamera(1,1);
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            super.touchDragged(event,x,y,pointer);
            Vector3 tpos = game.camera.unproject(new Vector3(x,y,0));

            game.translateCamera(game.CAMX-tpos.x, game.CAMY-tpos.y);
        }
    };

    public CreateScreen(HyperBrickGame game) {
        this.game = game;
        this.creator = new LevelCreator(this);
        levelManager = new LevelManager(this);


        game.resetCamera();

        gameStage = new Stage(game.gameVP);
        gameStage.getBatch().setProjectionMatrix(game.camera.combined);
        UIStage = new Stage(game.textVP);
        UIStage.getBatch().setProjectionMatrix(game.textCamera.combined);

        UIStage.addActor(creator);
        UIStage.addListener(mapMoveListener);
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

        levelManager.newLevel(1);

        gameStage.addActor(levelManager.curLevel.actorGroup);

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        game.updateCamera();

        gameStage.act();
        UIStage.act();
        UIStage.draw();
        gameStage.draw();
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
