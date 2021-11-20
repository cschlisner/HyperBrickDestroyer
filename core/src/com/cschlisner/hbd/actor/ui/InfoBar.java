package com.cschlisner.hbd.actor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.cschlisner.hbd.actor.ui.TextButton;
import com.cschlisner.hbd.screen.GameScreen;
import com.cschlisner.hbd.util.Const;

public class InfoBar extends Actor {
    GameScreen screen;
    public int lives;
    public int score;
    public int level;
    BitmapFont font;
    BitmapFont markerFont;
    ShapeRenderer shapeRend = new ShapeRenderer();
    float scrw, mgx;
    float scrh, mgy;

    public TextButton pauseBtn;

    public InfoBar(GameScreen screen){
        this.screen = screen;
        setName("Info");
        scrw = screen.UIcamera.viewportWidth;
        scrh = screen.UIcamera.viewportHeight;

        font = screen.assManager.get(Const.fontr(2,1), BitmapFont.class);
        font.setColor(Color.GRAY);

        // we are drawing to pixel space
        mgx = 0; // margins
        mgy = font.getLineHeight();
        float height = Const._STATUS_HEIGHT + mgy/2;

        // drawing area
        setBounds(0,scrh-height,scrw,height);
        shapeRend.setProjectionMatrix(screen.UIcamera.combined);

        pauseBtn = new TextButton(screen.assManager, Const.TEXT[6], Const.fontr(2,1), getX(), getY()+mgy);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRend.setProjectionMatrix(getStage().getCamera().combined);
        shapeRend.setColor(0.772f, 0.027f, 0.168f, 0.4f);
        shapeRend.begin(ShapeRenderer.ShapeType.Filled);
        shapeRend.rect(getX(), getY(), getWidth(), getHeight());
        shapeRend.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();
        batch.setProjectionMatrix(getStage().getCamera().combined);


        font.draw(batch, String.format("Level:%s",level), getX()+scrw/4,getY()+mgy);
        font.draw(batch, String.format("Lives:%s",lives), getX()+(2*scrw/4),getY()+mgy);
        font.draw(batch, String.format("Score:%s",score), getX()+(3*scrw/4),getY()+mgy);


    }

    public void dispose(){
        shapeRend.dispose();
    }
}
