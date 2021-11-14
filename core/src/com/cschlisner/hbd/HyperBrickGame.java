package com.cschlisner.hbd;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class HyperBrickGame extends Game {
    public SpriteBatch spriteBatch;
    public BitmapFont font;

    @Override
    public void create() {
        this.setScreen(new TitleScreen(this));
    }

    public void render(){
        super.render();
    }

    public void dispose() {
        spriteBatch.dispose();
        font.dispose();
    }
}
