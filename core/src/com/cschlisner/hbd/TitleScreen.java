package com.cschlisner.hbd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import android.graphics.Color;

public class TitleScreen implements Screen {
    final HyperBrickGame game;

    SpriteBatch batch = new SpriteBatch();
    BitmapFont titlefont;
    BitmapFont titlefont2;
    BitmapFont titlefont3;

    int SCRW, SCRH;

    OrthographicCamera camera;
    Stage stage;
    final TextButton zenBtn, challengeBtn, createBtn, settingBtn;


    public TitleScreen(final HyperBrickGame game){
        this.game = game;
        SCRH = Gdx.graphics.getHeight();
        SCRW = Gdx.graphics.getWidth();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/VerminVibes1989.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size=230;
        titlefont = generator.generateFont(p);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1200, 2533);
        stage = new Stage(new ScreenViewport(camera));
        stage.getBatch().setProjectionMatrix(camera.combined);
        zenBtn = new TextButton("Zen","VerminVibes1989.ttf", 150, 1000, 1300);
        challengeBtn = new TextButton("Challenge","VerminVibes1989.ttf", 150, 700, zenBtn.texty-200);
        createBtn = new TextButton("Create","VerminVibes1989.ttf", 150, 900, challengeBtn.texty-200);
        settingBtn = new TextButton("Settings","VerminVibes1989.ttf", 150, 800, createBtn.texty-200);
        stage.addActor(zenBtn);
        stage.addActor(challengeBtn);
        stage.addActor(createBtn);
        stage.addActor(settingBtn);
    }
    @Override
    public void show() {
        ScreenUtils.clear(0, 0, 0, 0.6f);
        stage.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (((InputEvent) event).getType() != InputEvent.Type.exit)
                    return true;
                if (event.getTarget() == zenBtn) {
                    game.setScreen(new GameScreen(game));
                    dispose();
                }
                return true;
            }
        });
        Gdx.input.setInputProcessor(stage);

    }

    float colorDelta=0;
    @Override
    public void render(float delta) {
        colorDelta += delta;
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        titlefont.setColor(new com.badlogic.gdx.graphics.Color( android.graphics.Color.HSBtoRGB(Interpolation.linear.apply(0.0f, 0.9999f, delta), 0.5f, 0.5f)));
        batch.begin();
        titlefont.draw(batch, "Hyper\nBrick\nDestroyer", SCRW/11.0f,SCRH*0.95f);
        batch.end();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

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
    public void dispose() {

    }
}
