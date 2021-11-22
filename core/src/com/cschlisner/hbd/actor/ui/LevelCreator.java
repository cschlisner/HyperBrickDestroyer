package com.cschlisner.hbd.actor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Json;
import com.cschlisner.hbd.HyperBrickGame;
import com.cschlisner.hbd.screen.CreateScreen;
import com.cschlisner.hbd.util.LevelManager;

// menu overlay for level creation screen
public class LevelCreator extends Actor {
    HyperBrickGame game;
    CreateScreen screen;
    Json json = new Json();
    ShapeRenderer shapeRend = new ShapeRenderer();

    public LevelCreator(CreateScreen screen){
        this.screen = screen;
        this.game = screen.game;


        // shape to draw buttons on
        setBounds(0, 0, game.TSCRW, game.TSCRH / 8);
        shapeRend.setAutoShapeType(true);
        shapeRend.setColor(0.772f, 0.027f, 0.168f, 0.4f);
        shapeRend.setProjectionMatrix(game.textCamera.combined);

    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        batch.end();

        // draw a panel
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRend.begin(ShapeRenderer.ShapeType.Filled);
        shapeRend.rect(getX(), getY(), getWidth(), getHeight());
        shapeRend.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
    }
}
