package com.cschlisner.hbd.screen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cschlisner.hbd.HyperBrickGame;
import com.cschlisner.hbd.actor.Wall;
import com.cschlisner.hbd.actor.ui.InfoBar;
import com.cschlisner.hbd.util.LevelManager;
import com.cschlisner.hbd.actor.ui.PauseMenu;
import com.cschlisner.hbd.actor.PlayerPaddle;
import com.cschlisner.hbd.actor.Ball;
import com.cschlisner.hbd.actor.Brick;
import com.cschlisner.hbd.util.Const;

public class GameScreen implements Screen {
	public final HyperBrickGame game;
	public AssetManager assManager;

	// Camera
	public Camera camera;
	public Camera UIcamera;

	// Scene2D
	private Stage gameStage, UIStage;
	PlayerPaddle paddle;
	PauseMenu menuOverlay;
	Ball ball;
	public LevelManager levelManager;
	public InfoBar infoBar;

	InputMultiplexer inputMultiplexer = new InputMultiplexer();

	private boolean waitingOnKickOff = true;
	private boolean paused = false;

	private InputListener ballKickOffListener = new InputListener(){
		@Override
		public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
			stopEngine = false;
			ball.kickOff();
			waitingOnKickOff = false;
			paddle.paddleInput.removeListener(this);
			return true;
		}
	};



	public GameScreen(final HyperBrickGame game){
		this.game = game;
		this.assManager = game.assetManager;
		this.camera = game.camera;
		this.UIcamera = game.textCamera;

		// Scene2d things
		gameStage = new Stage(game.gameVP);
		gameStage.getBatch().setProjectionMatrix(camera.combined);

		UIStage = new Stage(game.textVP);
		UIStage.getBatch().setProjectionMatrix(game.textCamera.combined);

		markerFont = assManager.get(Const.fontr(1,0));

		game.debug = false;
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

        gameStage.addActor(paddle);
        UIStage.addActor(paddle.paddleInput);

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
		ScreenUtils.clear(Color.BLACK);
		game.updateCamera();

		// Draw our stages
		gameStage.draw();
		UIStage.draw();

		// Draw debugging info
		if (game.debug) {
			game.debugRenderer.render(game.getWorld(), camera.combined);
			for (Actor a : gameStage.getActors())
				markActor(UIStage.getBatch(), a, Color.MAGENTA);
		}

		// Update physics amd cameras
		if (!this.paused) {
			update(delta);
		}
	}
	boolean stopEngine;
	public void update(float delta){
		if (!stopEngine){
			// update box2d physics
			game.getWorld().step(1/ Const.FRAMERATE, 6,2);
		}

		// update actors in scene2d scene
		gameStage.act(delta);
		UIStage.act(delta);

		// check game conditions
		if (ball.isDead && !waitingOnKickOff) {
			--infoBar.lives;
			waitingOnKickOff = true;
			paddle.paddleInput.addListener(ballKickOffListener);
		}
		if (infoBar.lives == 0){
			dispose();
			game.setScreen(new TitleScreen(game));
		}
		if (levelManager.curLevel.bricksToClear <= 0) {
			stopEngine = true;
			advanceLevel();
		}

//		// update camera
////		if (ball.position.x < game.CAMOX)
////			game.translateCamera(ball.position.x-game.CAMOX, 0);
////		else if (ball.position.x > game.CAMOX+game.SCRW)
////			game.translateCamera(ball.position.x-game.CAMOX+game.SCRW, 0);
//		game.updateCamera(ball.position.x, ball.position.y);
	}

	public void advanceLevel(){
		if (infoBar.level >= 1) {
			// reset paddle to center
			paddle.reset();
			// reset ball
			if (ball != null) {
				ball.remove();
			}
			// get rid of extra balls
			for (Actor a : gameStage.getActors()) {
				if (a instanceof Ball && !((Ball) a).isPrimary) {
					a.remove();
				}
			}
			// remove all bricks/walls from previous level
			for (Actor b : levelManager.curLevel.brickGroup.getChildren())
				((Brick) b).brickBroken();
			for (Actor w : levelManager.curLevel.wallGroup.getChildren())
				((Wall) w).destroy();
		}

		// NEW LEVEL //
		++infoBar.level;
		if (game.getMode()== HyperBrickGame.GameMode.CHALLENGE && infoBar.level == Const.testLevels.length){
			dispose();
			game.setScreen(new TitleScreen(game));
			return;
		}
		levelManager.newLevel(infoBar.level);

		ball = new Ball(levelManager.curLevel, true);
		ball.defSpeed *= 1+((float)infoBar.level/100.0f);
		waitingOnKickOff = true;
		infoBar.lives = 3;

		gameStage.addActor(ball);
		gameStage.addActor(levelManager.curLevel.actorGroup);
		paddle.paddleInput.addListener(ballKickOffListener);
	}

	ShapeRenderer shapeRend = new ShapeRenderer();
	BitmapFont markerFont;

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
		paddle.paddleInput.switchOff();
	}

	public void unpause(){
		infoBar.pauseBtn.setText(Const.TEXT[6]);
		menuOverlay.menuGroup.remove();
		this.paused = false;
		inputMultiplexer.addProcessor(gameStage);
		paddle.paddleInput.switchOn();
	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}
	
	@Override
	public void dispose () {
		Array<Body> bodies = new Array<>();
		game.getWorld().getBodies(bodies);
		for (Body body : bodies)
			game.getWorld().destroyBody(body);
		gameStage.dispose();
		UIStage.dispose();
	}
}
