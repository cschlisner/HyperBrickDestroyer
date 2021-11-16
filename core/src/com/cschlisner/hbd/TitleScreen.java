package com.cschlisner.hbd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.awt.image.RGBImageFilter;
import java.util.ArrayList;
import java.util.Stack;


public class TitleScreen implements Screen {
    final HyperBrickGame game;



    AssetManager assManager;

    SpriteBatch batch = new SpriteBatch();
    FreeTypeFontGenerator.FreeTypeFontParameter fontParameter;
    FreeTypeFontGenerator fontGenerator;

    BitmapFont titlefont;
    BitmapFont titlefont2;
    BitmapFont titlefont3;

    float SCRW, SCRH;

    OrthographicCamera camera;
    Stage stage;
    final TextButton zenBtn, challengeBtn, createBtn, settingBtn;


    public TitleScreen(final HyperBrickGame game){
        this.game = game;
        this.assManager = game.assetManager;

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("font/VerminVibes1989.ttf"));
        fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size=230;
        titlefont = fontGenerator.generateFont(fontParameter);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Const.VIEW_WIDTH, Const.VIEW_HEIGHT);
        SCRH = camera.viewportHeight;
        SCRW = camera.viewportWidth;
        stage = new Stage(new ScreenViewport(camera));
        stage.getBatch().setProjectionMatrix(camera.combined);
        zenBtn = new TextButton(assManager, Const.TEXT[2],"VerminVibes1989.ttf", 150, 1000, 1300);
        challengeBtn = new TextButton(assManager, Const.TEXT[3],"VerminVibes1989.ttf", 150, 700, zenBtn.texty-200);
        createBtn = new TextButton(assManager, Const.TEXT[4],"VerminVibes1989.ttf", 150, 900, challengeBtn.texty-200);
        settingBtn = new TextButton(assManager, Const.TEXT[5],"VerminVibes1989.ttf", 150, 800, createBtn.texty-200);
        stage.addActor(zenBtn);
        stage.addActor(challengeBtn);
        stage.addActor(createBtn);
        stage.addActor(settingBtn);
    }
    @Override
    public void show() {
        ScreenUtils.clear(0, 0, 0, 0.6f);

        zenBtn.setOnClick(new Runnable() {
            @Override
            public void run() {
                game.setMode(HyperBrickGame.GameMode.ZEN);
                game.setScreen(new GameScreen(game));
                cacheScreen();
                dispose();
            }
        });

        challengeBtn.setOnClick(new Runnable() {
            @Override
            public void run() {
                game.setMode(HyperBrickGame.GameMode.CHALLENGE);
                game.setScreen(new GameScreen(game));
                cacheScreen();
                dispose();
            }
        });
        Gdx.input.setInputProcessor(stage);

    }

    public void cacheScreen(){
        game.screenstack.push(this);
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

    float colorDelta=0.0f;
    float colDir = 0.01f;
    @Override
    public void render(float delta) {
        colorDelta += colDir;
        if (colorDelta>=0.9f||colorDelta<0.01f)
            colDir=-colDir;
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        float H = Interpolation.linear.apply(0.0f, 359.9999f, colorDelta);
        Color newCol = HSBtoColor(Interpolation.linear.apply(0.0f, 359.9999f, colorDelta), 0.5f, 0.8f);
        titlefont.setColor(newCol);
//        System.out.println(String.format("H:%s => %s b:%s f:%s", H, newCol,batch.getColor(), titlefont.getColor()));
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
