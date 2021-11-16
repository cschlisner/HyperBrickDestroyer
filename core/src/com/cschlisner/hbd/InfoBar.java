package com.cschlisner.hbd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class InfoBar extends Actor implements Collision {
    Screen screen;
    int lives;
    int score;
    int level;
    BitmapFont font;
    ShapeRenderer shapeRend = new ShapeRenderer();
    float scrw;
    float scrh;

    TextButton pauseBtn;

    public InfoBar(GameScreen screen){
        this.screen = screen;
        setName("Info");
        scrw = screen.camera.viewportWidth;
        scrh = screen.camera.viewportHeight;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/alagard.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size=60;
        p.color = Color.RED;
        font = generator.generateFont(p);

        float height = scrh/38;

        // drawing area
        setBounds(0,scrh-height,scrw,height);
        boundingBox = new Rectangle(0,scrh-height,scrw,height);

        pauseBtn = new TextButton(screen.assManager, Const.TEXT[6], "VerminVibes1989.ttf", 60, getX()+10, getY()+font.getLineHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        shapeRend.setColor(Color.BLACK);
        shapeRend.begin(ShapeRenderer.ShapeType.Filled);
        shapeRend.rect(getX(), getY(), getWidth(), getHeight());
        shapeRend.end();
        batch.begin();
        font.draw(batch, String.format("Level:%s",level), getX()+(scrw/4),getY()+font.getLineHeight());
        font.draw(batch, String.format("Lives:%s",lives), getX()+(2*scrw/4),getY()+font.getLineHeight());
        font.draw(batch, String.format("Score:%s",score), getX()+(3*scrw/4),getY()+font.getLineHeight());
    }

    private Rectangle boundingBox;
    @Override
    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    @Override
    public void handleCollision() {
        //ignore
    }

    public void dispose(){
        shapeRend.dispose();
        font.dispose();
    }
}
