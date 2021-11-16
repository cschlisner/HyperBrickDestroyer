package com.cschlisner.hbd;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Stack;

public class HyperBrickGame extends Game {

    // stack of screens so we can go back and forth
    public Stack<Screen> screenstack = new Stack<>();

    public enum GameMode {
        ZEN, CHALLENGE
    }
    private GameMode mode;
    public SpriteBatch spriteBatch;
    public BitmapFont font, loadingfont;
    AssetManager assetManager;
    FreeTypeFontGenerator fontGenerator;
    private int SCRH, SCRW;
    TitleScreen titleScreen;

    @Override
    public void create() {
        assetManager = new AssetManager();

        for (String asset : Const.SOUNDS)
            assetManager.load(asset, Sound.class);
        for (String asset : Const.TEXTURES)
            assetManager.load(asset, Texture.class);

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("font/VerminVibes1989.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size=230;
        font = fontGenerator.generateFont(fontParameter);
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("font/alagard.ttf"));
        fontParameter.size=100;
        loadingfont = fontGenerator.generateFont(fontParameter);

        SCRH = Gdx.graphics.getHeight();
        SCRW = Gdx.graphics.getWidth();

        spriteBatch = new SpriteBatch();

    }

    public void setMode(GameMode mode){
        this.mode = mode;
    }

    public GameMode getMode() {
        return mode;
    }

    // for animating Title color (can't use java.awt)
    private Color HSLtoColor(float h, float s, float l){
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

    boolean blinking=true, bw=true;
    int blink_i=3;
    public void render(){
        if (titleScreen != null){
            getScreen().render(Gdx.graphics.getDeltaTime());
            return;
        }
        super.render();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(assetManager.update()) {
            blinking = blink_i-- > 0;
            ScreenUtils.clear(blinking&bw?Color.WHITE:Color.BLACK);
            if (blinking) {
                try {
                    Thread.sleep(150);
                    bw = !bw;
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            else {
                assetManager.finishLoading();
                titleScreen = new TitleScreen(this);
                this.setScreen(titleScreen);
            }
        }
        try {
            Thread.sleep(5);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        float progress = assetManager.getProgress();
        float H = Interpolation.fastSlow.apply(0.0f, 359.9999f, progress);
        float S = Interpolation.fade.apply(0.2f, 1f, progress);
        float L = Interpolation.slowFast.apply(1f, 0.5f, progress);
        Color newCol = HSLtoColor(H, S, L);
        font.setColor(newCol);
        spriteBatch.begin();
        font.draw(spriteBatch, "Hyper\nBrick\nDestroyer", SCRW/10.0f,SCRH*0.65f);
        loadingfont.draw(spriteBatch, String.format("loading.... %.2f%%", progress*100), 200,SCRH*0.1f);
        spriteBatch.end();
    }
    public void dispose() {
        super.dispose();
        spriteBatch.dispose();
        font.dispose();
        loadingfont.dispose();
    }
}
