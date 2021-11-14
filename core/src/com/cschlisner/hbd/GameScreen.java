package com.cschlisner.hbd;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.logging.Logger;

public class GameScreen implements Screen {
	final HyperBrickGame game;
	private Stage stage;
	OrthographicCamera camera;
	PlayerPaddle paddle;
	Ball ball;
	LevelManager levelManager;
	InfoBar infoBar;
	private boolean waitingOnKickOff = true;

	public GameScreen(final HyperBrickGame game){
		this.game = game;
		camera = new OrthographicCamera();
		camera.setToOrtho(false);
		Viewport stretchViewport = new StretchViewport(1300,2533,camera);
		stretchViewport.apply();
		stage = new Stage(stretchViewport);
		stage.getBatch().setProjectionMatrix(camera.combined);



	}

	public void advanceLevel(){
		++infoBar.level;
		// remove any leftover balls that spawned
		levelManager.spawnedBalls.clearChildren();
		ball.handleDeath();
		ball.speed *= 1+((float)infoBar.level/10.0f);
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

		stage.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (((InputEvent) event).getType() != InputEvent.Type.exit)
					return true;
				if (event.getTarget() == infoBar.pauseBtn) {
					pause();
				}
				return true;
			}
		});

		Gdx.input.setInputProcessor(stage);


		infoBar = new InfoBar(camera);
		paddle = new PlayerPaddle(camera);
		levelManager = new LevelManager(camera);
		ball = new Ball(camera, levelManager);

		stage.addActor(infoBar);
		stage.addActor(infoBar.pauseBtn);
		stage.addActor(paddle);
		stage.addActor(ball);
		stage.addActor(levelManager.brickGroup);

        advanceLevel();
    }

	@Override
	public void render(float delta) {
//		ScreenUtils.clear(0.52f, 0.73f, 0.94f, 0.6f);
		ScreenUtils.clear(Color.BLACK);

		stage.act(delta);
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

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}
	
	@Override
	public void dispose () {
	}
}
