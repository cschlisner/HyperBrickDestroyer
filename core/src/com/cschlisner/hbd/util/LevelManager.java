package com.cschlisner.hbd.util;


import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.cschlisner.hbd.actor.Brick;
import com.cschlisner.hbd.screen.GameScreen;
import com.cschlisner.hbd.screen.GameViewCtx;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

public class LevelManager {
    GameViewCtx screen;
    public int level_c;
    public Level curLevel, lastLevel;
    public ArrayList<Body> garbage = new ArrayList<>();
    public boolean levelInitialized = false;

    public LevelManager(GameViewCtx screen){
        this.screen = screen;
    }

    public void newLevel(int level_index){
        lastLevel = curLevel;
        curLevel = new Level(this, level_index);
    }

    public void incScore() {
        if (screen instanceof GameScreen)
            ((GameScreen)screen).infoBar.score += 10;
    }
}
