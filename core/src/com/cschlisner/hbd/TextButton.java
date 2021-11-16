package com.cschlisner.hbd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class TextButton extends Actor {
    float textx, texty;
    float marginx, marginy;

    BitmapFont font, fontClick;
    GlyphLayout glyphLayout;
    boolean clicked = false;
    Sound clickSound;
    Runnable onclick;


    InputListener btnIL = new InputListener(){
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            clicked = true;
            clickSound.play();
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            clicked = false;
            if (onclick != null) onclick.run();
        }
    };

    public TextButton(AssetManager manager, String text, String fontref, int size, float xpos, float ypos){
        this.setName(text);
        textx = xpos;
        texty = ypos;
        marginx = marginy = 100;
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(String.format("font/%s",fontref)));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size=size;
        p.color = Color.WHITE;
        font = generator.generateFont(p);
        p.color = Color.RED;
        fontClick = generator.generateFont(p);
        glyphLayout = new GlyphLayout(font, text);
        setBounds(textx-marginx/2,texty- glyphLayout.height-(marginy/2),glyphLayout.width+marginx, glyphLayout.height+marginy);

        clickSound = manager.get(Const.SOUNDS[1], Sound.class);

        this.addListener(btnIL);


    }


    public void drawBounds(Camera camera){
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setColor(Color.LIME);
    }

    public void setOnClick(Runnable onClick){
        this.onclick = onClick;
    }

    public void setText(String text){
        this.setName(text);
        glyphLayout = new GlyphLayout(font, text);
        setBounds(textx-marginx/2,texty- glyphLayout.height-(marginy/2),glyphLayout.width+marginx, glyphLayout.height+marginy);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }
    ShapeRenderer shapeRenderer;
    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shapeRenderer != null) {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
            shapeRenderer.end();
            batch.begin();
        }
        (clicked? fontClick:font).draw(batch, glyphLayout, textx, texty);
    }
}
