package com.cschlisner.hbd;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.logging.Logger;

public class GameScreen implements Screen {
	final HyperBrickGame game;
	AssetManager assManager;

	// Camera
	Camera camera, UIcamera;

	// Scene2D
	private Stage gameStage, UIStage;
	PlayerPaddle paddle;
	PauseMenu menuOverlay;
	Ball ball;
	LevelManager levelManager;
	InfoBar infoBar;

	InputMultiplexer inputMultiplexer = new InputMultiplexer();

	// Box2D


	private boolean waitingOnKickOff = true;
	private boolean paused = false;


	public GameScreen(final HyperBrickGame game){
		this.game = game;
		this.assManager = game.assetManager;
		this.camera = game.camera;
		this.UIcamera = game.textCamera;
		Viewport fitViewPort = game.gameVP;
//		fitViewPort.apply();

		// Scene2d things
		gameStage = new Stage(fitViewPort);
		gameStage.getBatch().setProjectionMatrix(camera.combined);

		UIStage = new Stage(game.textVP);
		UIStage.getBatch().setProjectionMatrix(game.textCamera.combined);

		FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
		p.size=20;
		markerFont = game.fontGenerator.generateFont(p);
	}

	public void advanceLevel(){
		++infoBar.level;
		if (game.getMode()== HyperBrickGame.GameMode.CHALLENGE && infoBar.level == LevelManager.testLevels.length){
			dispose();
			game.setScreen(new TitleScreen(game));
			return;
		}


		// remove any leftover balls that spawned
		//levelManager.spawnedBalls.clearChildren();
		ball.defSpeed *= 1+((float)infoBar.level/100.0f);
		ball.handleDeath();
		waitingOnKickOff = true;
		infoBar.lives = 3;
		levelManager.createBricks(infoBar.level);
		gameStage.addActor(levelManager.brickGroup);
		paddle.reset();

		// get rid of extra balls
		for (Actor a : gameStage.getActors()){
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
		// Fade in
        ScreenUtils.clear(0.52f, 0.73f, 0.94f, 0.6f);
		gameStage.getRoot().getColor().a = 0;
		gameStage.getRoot().addAction(fadeIn(2.0f));

		// Set up initial input processors
		inputMultiplexer.addProcessor(gameStage);
		inputMultiplexer.addProcessor(UIStage);
		Gdx.input.setInputProcessor(inputMultiplexer);

		// UI Elements
		infoBar = new InfoBar(this);
		UIStage.addActor(infoBar);
		UIStage.addActor(infoBar.pauseBtn);
		menuOverlay = new PauseMenu(this);


		// Game Actors (Box2D bodies)
		paddle = new PlayerPaddle(this);
		levelManager = new LevelManager(this);
		ball = new Ball(this, true);

		gameStage.addActor(paddle);
		gameStage.addActor(ball);
		gameStage.addActor(levelManager.brickGroup);


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

		game.updateCamera();

		advanceLevel();
	}

    @Override
	public void render(float delta) {
//		ScreenUtils.clear(0.52f, 0.73f, 0.94f, 0.6f);
		ScreenUtils.clear(Color.BLACK);

		game.updateCamera();

		// ball will collide with screen on its own
		if (ball.isDead){
			if (!waitingOnKickOff) {
				--infoBar.lives;

				waitingOnKickOff = true;
				paddle.addListener(new InputListener(){
					@Override
					public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
						ball.isDead=false;
						waitingOnKickOff = false;
						gameStage.removeListener(this);
						return true;
					}
				});
			}
		}
		gameStage.draw();

//		if (levelManager.levelBrickCount <= 0)
//			advanceLevel();
//
//		if (infoBar.lives == 0){
//			dispose();
//			game.setScreen(new TitleScreen(game));
//		}
		if (game.debug) {
			game.debugRenderer.render(game.getWorld(), camera.combined);
			for (Actor a : gameStage.getActors())
				markActor(UIStage.getBatch(), a, Color.MAGENTA);
		}

		UIStage.draw();

		if (!this.paused) {
			update(delta);
		}
//		else infoBar.pauseBtn.act(delta);
	}


    ShapeRenderer shapeRend = new ShapeRenderer();
	BitmapFont markerFont;;
	public void markActor(Batch batch, Actor a, Color color){
		Vector3 actorPos = camera.project(new Vector3(a.getX(), a.getY(), a.getZIndex()));
		String str = a.getName();
		shapeRend.setColor(color);
		shapeRend.setProjectionMatrix(UIcamera.combined);
		shapeRend.begin(ShapeRenderer.ShapeType.Filled);
		shapeRend.circle(actorPos.x, actorPos.y, 10);
		shapeRend.end();
		batch.begin();
		batch.setColor(color);
		markerFont.setColor(color);
		markerFont.draw(batch, String.format("%s(%d,%d)",str,(int)actorPos.x,(int)actorPos.y), actorPos.x-50, actorPos.y-50);
		markerFont.draw(batch, String.format("%s(%d,%d)",str,(int)a.getX(), (int)a.getY()), actorPos.x-50, actorPos.y-100);
		batch.end();
	}

	public void update(float delta){
		// update box2d physics
		game.getWorld().step(1/Const._FRAMERATE, 6,2);

		// update actors in scene2d scene
		gameStage.act(delta);
	}

	@Override
	public void resize (int width, int height) {
		gameStage.getViewport().update(width, height, true);
		UIStage.getViewport().update(width,height,true);
		ScreenUtils.clear(0, 0, 0, 0.6f);
	}

	@Override
	public void pause() {
		paused = true;
		infoBar.pauseBtn.setText(Const.TEXT[7]);
		UIStage.addActor(menuOverlay.menuGroup);
		inputMultiplexer.removeProcessor(gameStage);
	}

	public void unpause(){
		infoBar.pauseBtn.setText(Const.TEXT[6]);
		menuOverlay.menuGroup.remove();
		this.paused = false;
		inputMultiplexer.addProcessor(gameStage);
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
		gameStage.dispose();
		UIStage.dispose();
		for (Actor b : levelManager.brickGroup.getChildren())
			((Brick)b).disposeAssests();
	}
}
