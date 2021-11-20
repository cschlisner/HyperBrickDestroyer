package com.cschlisner.hbd.actor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.cschlisner.hbd.actor.ui.TextButton;
import com.cschlisner.hbd.screen.GameScreen;
import com.cschlisner.hbd.util.Const;

public class PauseMenu extends Actor {
    private final float SCRH, SCRW, ptextW, ptextH;
    private GameScreen screen;
    public TextButton quitBtn, settingsBtn;
    public Group menuGroup = new Group();
    BitmapFont titlefont;
    FreeTypeFontGenerator fontGenerator;
    FreeTypeFontGenerator.FreeTypeFontParameter fontParameter;

    ShapeRenderer shapeRend = new ShapeRenderer();

    public PauseMenu(GameScreen screen){
        this.screen = screen;

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(Const._FONT_BIG));
        fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size=230;
        titlefont = fontGenerator.generateFont(fontParameter);

        SCRH = screen.UIcamera.viewportHeight;
        SCRW = screen.UIcamera.viewportWidth;

        float w = SCRW*0.9f;
        float h = w*0.5f;
        setBounds((SCRW-w)/2, SCRH/2.0f-(h/2.0f), w, h);

        quitBtn = new TextButton(screen.assManager, Const.TEXT[8],Const.fontr(2, 1),getX()+(getWidth())/4.0f, getY()+(h/3.0f));
        settingsBtn = new TextButton(screen.assManager, Const.TEXT[5],Const.fontr(2, 0), getX()+(getWidth()*0.5f), getY()+(h/3.0f));

        menuGroup.addActor(this);
        menuGroup.addActor(quitBtn);
        menuGroup.addActor(settingsBtn);

        // shape to draw buttons on
        shapeRend.setAutoShapeType(true);
        shapeRend.setColor(0.772f, 0.027f, 0.168f, 0.4f);
        shapeRend.setProjectionMatrix(screen.UIcamera.combined);

        // PAUSED text
        glyphLayout = new GlyphLayout(screen.game.font, Const.TEXT[9]);
        ptextW = glyphLayout.width;
        ptextH = glyphLayout.height;

        this.setZIndex(0);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }
    GlyphLayout glyphLayout;

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRend.begin(ShapeRenderer.ShapeType.Filled);
        shapeRend.rect(getX(), getY(), getWidth(), getHeight());
        shapeRend.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();

        screen.game.font.draw(batch, glyphLayout,screen.UIcamera.position.x-ptextW/2,screen.UIcamera.position.y+ptextH/2);
    }
}
