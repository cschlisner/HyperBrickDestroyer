package com.cschlisner.hbd.util;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Json;
import com.cschlisner.hbd.actor.Brick;
import com.cschlisner.hbd.screen.GameScreen;
import com.cschlisner.hbd.screen.GameViewCtx;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

public class LevelManager {
    GameViewCtx screen;
    public int level_c = 1;
    public Level curLevel, lastLevel;
    public ArrayList<Body> garbage = new ArrayList<>();
    public boolean levelInitialized = false;
    Json json = new Json();

    public LevelManager(GameViewCtx screen){
        this.screen = screen;
    }

    // generate new level based on level index
    public void genLevel(int level_index){
        level_c = level_index;
        lastLevel = curLevel;

        // get scaled level dimensions based on level index
        int columns = (int)((screen.getGame().SCRW*(1.1 + ((level_index-1)*Const.LEVEL_WIDTH_SCALAR))) / Const.BRICK_WIDTH);
        int rows = (int)((screen.getGame().SCRH*(1.1 + ((level_index-1)*Const.LEVEL_HEIGHT_SCALAR))) / Const.BRICK_HEIGHT);

        curLevel = new Level(this, columns, rows);
    }

    public void readLevel(String filename){
        FileHandle file =  Gdx.files.local(filename);
        this.curLevel = json.fromJson(Level.class, file);
        this.curLevel.init(this);
    }

    public void readMap(int[][] map){
        this.curLevel = new Level(map);
        this.curLevel.init(this);
    }

    public void blankLevel(){
        int c = (int)(screen.getGame().SCRW / Const.BRICK_WIDTH);
        int r = (int)(screen.getGame().SCRH / Const.BRICK_HEIGHT);
        this.curLevel = new Level(new int[r][c]);
        this.curLevel.init(this);
    }

    public void incScore() {
        if (screen instanceof GameScreen)
            ((GameScreen)screen).infoBar.score += 10;
    }
}
