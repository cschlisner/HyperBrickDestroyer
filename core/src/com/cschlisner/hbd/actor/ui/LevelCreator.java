package com.cschlisner.hbd.actor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Scaling;
import com.cschlisner.hbd.HyperBrickGame;
import com.cschlisner.hbd.actor.Brick;
import com.cschlisner.hbd.screen.CreateScreen;
import com.cschlisner.hbd.util.Const;
import com.cschlisner.hbd.util.Level;

// menu overlay for level creation screen
public class LevelCreator extends Actor {
    HyperBrickGame game;
    CreateScreen screen;
    public Level _level; // level we are editing
    int columns, rows;
    Json json = new Json();
    ShapeRenderer shapeRend = new ShapeRenderer();
    public Container<Table> uiContainer;

    public CheckBox gridCB;
    TextButton OKBtn;
    public SelectBox<Brick.BrickType> brickSelct;

    EventListener inputListenerCanceller = new EventListener(){
        @Override
        public boolean handle(Event event) {
            return true;
        }
    };

    ClickListener clickListener = new ClickListener(){
        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            screen.pauseMenu.quitBtn.clickSound.play();
        }
    };

    ClickListener saveListener = new ClickListener(){
        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            saveLevel();
        }
    };

    public LevelCreator(CreateScreen screen, Level level){
        this.setName("LevelCreator");
        this.screen = screen;
        this.game = screen.game;
        this._level = level;

        this.columns = level.levelMap[0].length;
        this.rows = level.levelMap.length;

        this.addListener(inputListenerCanceller);

        // shape to draw buttons on
        setBounds(0, 0, game.TSCRW, game.TSCRH / 8);


        uiContainer = new Container<Table>();

        uiContainer.setSize(getWidth(), getHeight());
        uiContainer.setPosition(getX(), getY());
        uiContainer.fillX();
        Skin skin = new Skin(Gdx.files.internal("skin/tracer/tracer-ui.json"));
        skin.getFont("font").getData().setScale(2.0f);
        Table table = new Table(skin);

        gridCB = new CheckBox("Grid", skin);
        gridCB.addListener(this.clickListener);
        gridCB.setTransform(true);
        gridCB.getImage().setScaling(Scaling.fit);
        gridCB.getImageCell().size(getWidth()/18);

        brickSelct = new SelectBox<Brick.BrickType>(skin);
        brickSelct.setItems(Brick.BrickType.values());

        OKBtn = new TextButton("OK", skin);
        OKBtn.addListener(clickListener);
        OKBtn.addListener(saveListener);
//        com.badlogic.gdx.scenes.scene2d.ui.


        table.row().grow();
        table.add(gridCB).grow();
        table.add(brickSelct).grow();
        table.add(OKBtn).grow();

        uiContainer.setActor(table);
//        uiContainer.setDebug(true);
//        table.setDebug(true);

        shapeRend.setAutoShapeType(true);

    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRend.setProjectionMatrix(game.textCamera.combined);
        shapeRend.begin(ShapeRenderer.ShapeType.Filled);
        shapeRend.setColor(0.772f, 0.027f, 0.168f, 0.4f);
        // draw a panel for menu
        shapeRend.rect(getX(), getY(), getWidth(), getHeight());

        // draw grid if enabled
        if (gridCB.isChecked()) {
            shapeRend.setProjectionMatrix(game.camera.combined);
            shapeRend.set(ShapeRenderer.ShapeType.Line);
            shapeRend.setColor(Color.WHITE);

            // render grid for our level
            for (int i = 1; i < rows+1; ++i) {
                for (int j = 0; j < columns; ++j) {
                    shapeRend.rect(-_level.WRLDWR + (j * Const.BRICK_WIDTH), _level.WRLDH - (i * Const.BRICK_HEIGHT), Const.BRICK_WIDTH, Const.BRICK_HEIGHT);
                }
            }
        }

        shapeRend.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();
    }

    public void saveLevel(){
        FileHandle file = Gdx.files.local("created_level.json");
        String lvl = json.prettyPrint(this._level);
        file.writeString(lvl, false);
        System.out.println(lvl);
        int[][] save = new int[_level.levelMap.length][_level.levelMap[0].length];
        for (int i =0;i<save.length;++i)
            for (int j=0;j<save[0].length; ++j)
                save[i][j]=_level.levelMap[i][j];
        Const.createdlevels.add(save);
    }


}
