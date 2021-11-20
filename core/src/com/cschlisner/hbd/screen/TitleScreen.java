package com.cschlisner.hbd.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cschlisner.hbd.HyperBrickGame;
import com.cschlisner.hbd.actor.ui.TextButton;
import com.cschlisner.hbd.screen.GameScreen;
import com.cschlisner.hbd.util.Const;


public class TitleScreen implements Screen {
    final HyperBrickGame game;
    public AssetManager assManager;


    SpriteBatch batch = new SpriteBatch();

    BitmapFont titlefont;

    Stage stage;
    final TextButton zenBtn, challengeBtn, createBtn, settingBtn;

    public TitleScreen(final HyperBrickGame game){
        this.game = game;
        this.assManager = game.assetManager;

        titlefont = game.font;

        stage = new Stage(game.textVP);
        stage.getBatch().setProjectionMatrix(game.textCamera.combined);

        float space = game.textCamera.viewportHeight/11;
        zenBtn = new TextButton(assManager, Const.TEXT[2],Const.fontr(2, 3), game.TSCRX + game.TSCRW*0.5f,game.TSCRY+ game.TSCRH*0.35f);
        challengeBtn = new TextButton(assManager, Const.TEXT[3],Const.fontr(2, 3), game.TSCRX+game.TSCRW*0.2f, zenBtn.texty-space);
        createBtn = new TextButton(assManager, Const.TEXT[4],Const.fontr(2, 3), game.TSCRX+game.TSCRW*0.3f, challengeBtn.texty-space);
        settingBtn = new TextButton(assManager, Const.TEXT[5],Const.fontr(2, 3), game.TSCRX+game.TSCRW*0.2f, createBtn.texty-space);
        stage.addActor(zenBtn);
        stage.addActor(challengeBtn);
        stage.addActor(createBtn);
        stage.addActor(settingBtn);

        game.updateCamera();
    }
    @Override
    public void show() {
        ScreenUtils.clear(0, 0, 0, 0.6f);

        zenBtn.setOnClick(new Runnable() {
            @Override
            public void run() {
                game.setMode(HyperBrickGame.GameMode.ZEN);
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });

        challengeBtn.setOnClick(new Runnable() {
            @Override
            public void run() {
                game.setMode(HyperBrickGame.GameMode.CHALLENGE);
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });
        Gdx.input.setInputProcessor(stage);

    }

    float colorDelta=0.0f;
    float colDir = 0.01f;
    @Override
    public void render(float delta) {
        colorDelta += colDir;
        if (colorDelta>=0.9f||colorDelta<0.01f)
            colDir=-colDir;

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(game.textCamera.combined);

        batch.begin();

        float H = Interpolation.linear.apply(0.0f, 359.9999f, colorDelta);
        Color newCol = HSBtoColor(H, 0.5f, 0.8f);
        titlefont.setColor(newCol);

        titlefont.draw(batch, Const.TEXT[0], game.TSCRX, game.TSCRY+game.TSCRH*0.9f);

        batch.end();

        stage.act(delta);
        stage.draw();
    }

    // for animating Title color (can't use java.awt)
    private Color HSBtoColor(float h, float s, float l){
        float C = (1-Math.abs(2*l-1))*s;
        float X = C * (1-Math.abs(((h/60.0f)%2)-1));
        float m = l-C/2;
        float[] Cx0 = {C, X, 0.0f};
        float[] RGBprime = {};
        if (h < 60)
            RGBprime = Cx0;
        else if (h < 120)
            RGBprime = new float[]{Cx0[1], Cx0[0], Cx0[2]};
        else if (h < 180)
            RGBprime = new float[]{Cx0[2], Cx0[0], Cx0[1]};
        else if (h < 240)
            RGBprime = new float[]{Cx0[2], Cx0[1], Cx0[0]};
        else if (h < 300)
            RGBprime = new float[]{Cx0[1], Cx0[2], Cx0[0]};
        else if (h < 360)
            RGBprime = new float[]{Cx0[0], Cx0[2], Cx0[1]};

        float[] RGB = new float[3];
        int i=0;
        for (float v : RGBprime)
            RGB[i] = (RGBprime[i++]+m);

        return new Color(RGB[0],RGB[1],RGB[2],1);
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
