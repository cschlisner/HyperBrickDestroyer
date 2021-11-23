package com.cschlisner.hbd.actor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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

        Container<Table> tableContainer = new Container<Table>();
        tableContainer.setSize(getWidth(), getHeight());
        tableContainer.setPosition(getX(), getY());
        tableContainer.fillX();
        Skin skin = new Skin();
//        skin.load();
//        Table table = new Table(skin);
//
//        Label topLabel = new Label("A LABEL", skin);
//        topLabel.setAlignment(Align.center);
//        Slider slider = new Slider(0, 100, 1, false, skin);
//        Label anotherLabel = new Label("ANOTHER LABEL", skin);
//        anotherLabel.setAlignment(Align.center);
//
//        CheckBox checkBoxA = new CheckBox("Checkbox Left", skin);
//        CheckBox checkBoxB = new CheckBox("Checkbox Center", skin);
//        CheckBox checkBoxC = new CheckBox("Checkbox Right", skin);
//
//        Table buttonTable = new Table(skin);
//
//        TextButton buttonA = new TextButton("LEFT", skin);
//        TextButton buttonB = new TextButton("RIGHT", skin);
//
//        table.row().colspan(3).expandX().fillX();
//        table.add(topLabel).fillX();
//        table.row().colspan(3).expandX().fillX();
//        table.add(slider).fillX();
//        table.row().colspan(3).expandX().fillX();
//        table.add(anotherLabel).fillX();
//        table.row().expandX().fillX();
//
//        table.add(checkBoxA).expandX().fillX();
//        table.add(checkBoxB).expandX().fillX();
//        table.add(checkBoxC).expandX().fillX();
//        table.row().expandX().fillX();;
//
//        table.add(buttonTable).colspan(3);
//
//        buttonTable.pad(16);
//        buttonTable.row().fillX().expandX();
//        buttonTable.add(buttonA).width(cw/3.0f);
//        buttonTable.add(buttonB).width(cw/3.0f);
//
//        tableContainer.setActor(table);

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
