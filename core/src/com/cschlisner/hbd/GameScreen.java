package com.cschlisner.hbd;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.logging.Logger;

public class GameScreen implements Screen {
	final HyperBrickGame game;
	AssetManager assManager;
	PauseMenu menuOverlay;

	private Stage stage;
	OrthographicCamera camera;
	PlayerPaddle paddle;
	Ball ball;
	LevelManager levelManager;
	InfoBar infoBar;
	private boolean waitingOnKickOff = true;
	private boolean paused = false;

	public GameScreen(final HyperBrickGame game){
		this.game = game;
		this.assManager = game.assetManager;
		camera = new OrthographicCamera();
		camera.setToOrtho(false);
		Viewport stretchViewport = new StretchViewport(1300,2533,camera);
		stretchViewport.apply();
		stage = new Stage(stretchViewport);
		stage.getBatch().setProjectionMatrix(camera.combined);
	}

	public void advanceLevel(){
		++infoBar.level;
		if (game.getMode()== HyperBrickGame.GameMode.CHALLENGE && infoBar.level == LevelManager.testLevels.length){
			dispose();
			game.setScreen(new TitleScreen(game));
			return;
		}


		// remove any leftover balls that spawned
		levelManager.spawnedBalls.clearChildren();
		ball.defSpeed *= 1+((float)infoBar.level/100.0f);
		ball.handleDeath();
		waitingOnKickOff = true;
		infoBar.lives = 3;
		levelManager.createBricks(infoBar.level);
		stage.addActor(levelManager.brickGroup);
		paddle.reset();

		// get rid of extra balls
		for (Actor a : stage.getActors()){
			if (a instanceof Ball && !((Ball) a).isPrimary) {
				a.remove();
			}
		}

		paddle.addListener(new InputListener(){
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				ball.isDead=false;
				waitingOnKickOff = false;
				paddle.removeListener(this);
				return true;
			}
		});
	}

	@Override
	public void show() {
        ScreenUtils.clear(0.52f, 0.73f, 0.94f, 0.6f);
		stage.getRoot().getColor().a = 0;
		stage.getRoot().addAction(fadeIn(2.0f));

		Gdx.input.setInputProcessor(stage);


		infoBar = new InfoBar(this);
		paddle = new PlayerPaddle(this);
		levelManager = new LevelManager(this);
		ball = new Ball(this, levelManager);

		stage.addActor(infoBar);
		stage.addActor(infoBar.pauseBtn);
		stage.addActor(paddle);
		stage.addActor(ball);
		stage.addActor(levelManager.brickGroup);
		menuOverlay = new PauseMenu(this);

		infoBar.pauseBtn.setOnClick(new Runnable() {
			@Override
			public void run() {
				if (!paused)
					pause();
				else unpause();
			}
		});
		menuOverlay.quitBtn.setOnClick(new Runnable() {
			@Override
			public void run() {
				dispose();
				game.setScreen(new TitleScreen(game));
			}
		});
		advanceLevel();
    }

	@Override
	public void render(float delta) {
//		ScreenUtils.clear(0.52f, 0.73f, 0.94f, 0.6f);
		ScreenUtils.clear(Color.BLACK);

		if (!this.paused) {
			stage.act(delta);
		}
		else infoBar.pauseBtn.act(delta);

		// ball will collide with screen on its own
		if (ball.isDead){
			if (!waitingOnKickOff) {
				--infoBar.lives;
			}
			waitingOnKickOff = true;
			paddle.addListener(new InputListener(){
				@Override
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					ball.isDead=false;
					waitingOnKickOff = false;
					stage.removeListener(this);
					return true;
				}
			});
		}

		stage.draw();

		if (levelManager.levelBrickCount <= 0)
			advanceLevel();

		if (infoBar.lives == 0){
			dispose();
			game.setScreen(new TitleScreen(game));
		}
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
		ScreenUtils.clear(0, 0, 0, 0.6f);
	}

	@Override
	public void pause() {
		paused = true;
		infoBar.pauseBtn.setText(Const.TEXT[7]);
		stage.addActor(menuOverlay.menuGroup);
		paddle.removeListener(paddle.paddleInputListener);
	}

	public void unpause(){
		infoBar.pauseBtn.setText(Const.TEXT[6]);
		menuOverlay.menuGroup.remove();
		this.paused = false;
		paddle.addListener(paddle.paddleInputListener);
	}

	@Override
	public void resume() {
		// sounds sometimes get unloaded if application gets paused????
		for (Actor b : levelManager.brickGroup.getChildren())
			((Brick)b).reloadSounds();
	}

	@Override
	public void hide() {

	}
	
	@Override
	public void dispose () {
		ball.dispose();
		paddle.dispose();
		infoBar.dispose();
		stage.dispose();
		for (Actor b : levelManager.brickGroup.getChildren())
			((Brick)b).disposeAssests();
	}
}
