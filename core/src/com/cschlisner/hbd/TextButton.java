package com.cschlisner.hbd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class TextButton extends Actor {
    float textx, texty;

    BitmapFont font, fontClick;
    GlyphLayout glyphLayout;
    boolean clicked = false;
    Sound clickSound;


    InputListener btnIL = new InputListener(){
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            clicked = true;
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            clicked = false;
            clickSound.play();
        }
    };

    public TextButton(String text, String fontref, int size, float xpos, float ypos){
        textx = xpos;
        texty = ypos;
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(String.format("font/%s",fontref)));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size=size;
        p.color = Color.WHITE;
        font = generator.generateFont(p);
        p.color = Color.RED;
        fontClick = generator.generateFont(p);
        glyphLayout = new GlyphLayout(font, text);
        setBounds(xpos,ypos- glyphLayout.height,glyphLayout.width, glyphLayout.height);

        clickSound = Gdx.audio.newSound(Gdx.files.internal("sound/button.wav"));

        this.addListener(btnIL);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        (clicked? fontClick:font).draw(batch, glyphLayout, textx, texty);
    }
}
