package com.cschlisner.hbd.actor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.cschlisner.hbd.HyperBrickGame;
import com.cschlisner.hbd.actor.ui.TextButton;
import com.cschlisner.hbd.screen.GameScreen;
import com.cschlisner.hbd.screen.GameViewCtx;
import com.cschlisner.hbd.util.Const;

public class PauseMenu extends Actor {
    private final float SCRH, SCRW, OX, OY, ptextW, ptextH;
    private GameViewCtx screen;
    public TextButton quitBtn, settingsBtn;
    public Group menuGroup = new Group();
    BitmapFont font;

    ShapeRenderer shapeRend = new ShapeRenderer();

    public PauseMenu(GameViewCtx screen){
        this.screen = screen;
        HyperBrickGame game = screen.getGame();

        SCRH = screen.getUICamera().viewportHeight;
        SCRW = screen.getUICamera().viewportWidth;
        OX = game.TSCRX;
        OY = game.TSCRY;

        float w = SCRW*0.9f;
        float h = w*0.5f;
        setBounds(OX+(SCRW-w)/2, OY+SCRH/2.0f-(h/2.0f), w, h);

        quitBtn = new TextButton(game.assetManager, Const.TEXT[8],Const.fontr(2, 1),getX()+(getWidth())/4.0f, getY()+(h/3.0f));
        settingsBtn = new TextButton(game.assetManager, Const.TEXT[5],Const.fontr(2, 0), getX()+(getWidth()/2.0f), getY()+(h/3.0f));

        menuGroup.addActor(this);
        menuGroup.addActor(quitBtn);
        menuGroup.addActor(settingsBtn);

        // shape to draw buttons on
        shapeRend.setAutoShapeType(true);
        shapeRend.setColor(0.772f, 0.027f, 0.168f, 0.4f);
        shapeRend.setProjectionMatrix(screen.getUICamera().combined);

        // PAUSED text
        font = game.assetManager.get(Const.fontr(0,4), BitmapFont.class);
        glyphLayout = new GlyphLayout(font, Const.TEXT[9]);
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

        font.draw(batch, glyphLayout,getX()+(getWidth()-ptextW)/2,getTop()-ptextH);
    }
}
