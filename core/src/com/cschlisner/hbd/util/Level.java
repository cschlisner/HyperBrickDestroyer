package com.cschlisner.hbd.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cschlisner.hbd.HyperBrickGame;
import com.cschlisner.hbd.actor.Brick;
import com.cschlisner.hbd.actor.Wall;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

/**
 * Holds data about current level's brick group, walls, dimensions etc
 */
public class Level implements Json.Serializable {
    public boolean initialized;
    public HyperBrickGame game;
    public LevelManager manager;

    Random rng = new Random();

    public BodyGroup actorGroup = new BodyGroup();
    public BodyGroup balls = new BodyGroup();
    public BodyGroup bricks = new BodyGroup();
    public BodyGroup walls= new BodyGroup();


    public int bricksToClear;
    public int[][] levelMap;
    public Hashtable<Vector2, Brick> brickMap = new Hashtable<>();

    /* formatting brick group */
    public float WRLDW;
    public float WRLDH;
    public float WRLDWR, WRLDHR;
    int margin, header, footer;
    int brickC;

    public Level(){
        // created from json probably
        // levelmap is initialized but need to call init() to create bricks,walls,add actors, draw
    }

    // initialize with a level map
    public Level(int[][] map){
        this.levelMap = map;
    }

    // create a (ROWxCOL)xBricksize size level
    public Level(LevelManager manager, int bCol, int bRow){
        this.manager = manager;
        this.game = manager.screen.getGame();

        this.WRLDW = bCol * Const.BRICK_WIDTH;
        this.WRLDH = bRow * Const.BRICK_HEIGHT;
        this.WRLDWR = WRLDW * 0.5f;
        this.WRLDHR = WRLDH * 0.5f;

        // make a level map based on scaling from manager level
        this.levelMap = createLevelMap(manager.level_c, true);
        // lays out bricks according to level map
        createBricks();
        // creates level walls
        createWalls();

        // levelmap is initialized but need to call addActors() to add actors, draw
        this.initialized = true;
    }

    public void clearActors(){
        bricks.remove();
        walls.remove();
        balls.remove();
        bricks.clear();
        walls.clear();
        balls.clear();
    }

    public void addActors(Stage stage){
        actorGroup.clear();
        actorGroup.addActor(walls);
        actorGroup.addActor(bricks);
        actorGroup.addActor(balls);
        stage.addActor(actorGroup);
    }

    public void createWalls(){
        // create wall bounds
        walls.addActor(new Wall(this, -WRLDWR-Const.WALL_WIDTH, 0, Const.WALL_WIDTH, WRLDH));
        walls.addActor(new Wall(this, WRLDWR, 0, Const.WALL_WIDTH, WRLDH)); //R
        walls.addActor(new Wall(this, -WRLDWR, WRLDH, WRLDW, Const.WALL_WIDTH)); //T
    }

    // take level defined by level map and create brick actors
    public void createBricks(){
        bricksToClear = 0;
        Brick brick;
        for (int i = 0; i < levelMap.length; ++i){
            for (int j = 0; j< levelMap[0].length; ++j) {
                if (levelMap[i][j] > 0) {
                    brick = new Brick(this, j, i, Brick.BrickType.values()[levelMap[i][j]-1]);
                    bricks.addActor(brick);
                    brickMap.put(new Vector2(j,i), brick);
                    if (brick.type != Brick.BrickType.Immune)
                        ++bricksToClear;
                }
            }
        }
    }
    float[][] brickTypeDistribution = {
            {0,0.7f},
            {1,0.8f},
            {2,0.6f},
            {3,0.3f},
            {4,0.15f},
            {5,0.03f},
            {6,0.03f},
            {7,0.03f},
            {8,0.03f},
            {9,0.03f}
    };

    // normalize probability distribution

    private void normProbDist(float[][] dist){
        float psum = 0;
        for (float[] prob : dist)
            psum += prob[1];
        for (float[] prob : dist)
            prob[1]/=psum;
    }
    float[][] levelBrickProbDist = brickTypeDistribution;

    public float[][] updateProbabilities(int level){
        float[][] newProbDist = new float[brickTypeDistribution.length][brickTypeDistribution[0].length];
        for (int i = 0; i < newProbDist.length; ++i){
            newProbDist[i][0] = i;
            newProbDist[i][1] = (i > level ? 0.01f : 1f)*(i+1) * levelBrickProbDist[i][1];
        }
        normProbDist(newProbDist);
        return newProbDist;
    }

    public void addBrick(Level curLevel, int i, int j, Brick.BrickType selected) {
        Brick newB = new Brick(curLevel, i, j, selected);
        bricks.addActor(newB);
        levelMap[j][i] = selected.ordinal()+1;
        brickMap.put(new Vector2(j,i), newB);
        this.actorGroup.getStage().addActor(newB);
    }

    private int selectBrick(int level, int bricksLeft, int spaceLeft){
        // if brick quota supplied, modify chance of 0 by bricks left to place
        if (bricksLeft >= 0)
            levelBrickProbDist[0][1] = 1.0f - ((float)bricksLeft / (float)spaceLeft);
        float p = rng.nextFloat();
        float cumulativeProbability = 0.0f;
        for (float[] brickp : levelBrickProbDist) {
            cumulativeProbability += brickp[1];
            if (p <= cumulativeProbability) {
                return (int)brickp[0];
            }
        }
        return 0;
    }

    public int[][] createLevelMap(int level, boolean symmetric){
        // formatting params for auto-gen levels
        // space from top of bricks to top of world, in brick units
        this.header = (int)((WRLDH / Const.BRICK_HEADER) / Const.BRICK_HEIGHT);
        // space from bottom of world to bottom of bricks
        this.footer = (int)((WRLDH / Const.BRICK_FOOTER) / Const.BRICK_HEIGHT);
        // space from left of bricks to left of world
        this.margin = (int)((WRLDW / Const.BRICK_MARGIN) / Const.BRICK_WIDTH);

        // normalize default probability distribution
        normProbDist(brickTypeDistribution);

//        levelBrickProbDist = updateProbabilities(level);
        levelBrickProbDist = brickTypeDistribution;


        // full level width incl. margins
        int lColumns = (int)(WRLDW / Const.BRICK_WIDTH);
        int lRows = (int)(WRLDH / Const.BRICK_HEIGHT);

        // amount of cols/rows occupied by bricks
        int bColumns = (int)(WRLDW  / Const.BRICK_WIDTH) - (2*margin);
        int bRows = (int)(WRLDH  / Const.BRICK_HEIGHT) - (footer+header);

        float density = 0.75f;
        this.brickC = bRows * bColumns; // max brick count
        int brkplaced = (int)((float)bRows * (float)bColumns * density); // less dense brick count

        int[][] levelmap = new int[lRows][lColumns];

        brickfillloop:
        for (int i = 0; i < bRows; ++i){
            for (int j = 0; j< (symmetric?(bColumns/2):bColumns); ++j){
                int brick = selectBrick(level, brkplaced, brickC - (i*j));
                int mirrorbrick = brick<5||brick==9?brick:1;
                if (symmetric) {
                    boolean coinflip = rng.nextBoolean();
                    levelmap[i+header][j+margin] = coinflip?brick:mirrorbrick;
                    levelmap[i+header][(lColumns - 1) - j-margin] = coinflip?mirrorbrick:brick;
                }
                else levelmap[i][j] = brick;
                if (brick>0)
                    brkplaced -= symmetric?2:1;
                if (brkplaced == 0)
                    break brickfillloop;
            }
        }

        return levelmap;
    }

    @Override
    public void write(Json json) {
        json.writeValue("LEVELMAP", this.levelMap);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        JsonValue map = jsonData.get("LEVELMAP");
        this.levelMap = new int[map.size][map.child().size];
        JsonValue row = map.child();
        int i = 0;
        do {
            this.levelMap[i++] = row.asIntArray();
            row = row.next();
        } while (row != null);
    }

    // initalizes a loaded level with members
    public void init(LevelManager manager){
        this.manager = manager;
        this.game = manager.screen.getGame();
        this.WRLDW = this.levelMap[0].length * Const.BRICK_WIDTH;
        this.WRLDH = this.levelMap.length * Const.BRICK_HEIGHT;
        this.WRLDWR = WRLDW * 0.5f;
        this.WRLDHR = WRLDH * 0.5f;
        createWalls();
        createBricks();
        addActors(manager.screen.getGameStage());
        initialized = true;
    }

    // get surrounding bricks within r units of (x,y) in map
    public List<Brick> getNeighbors(int x, int y, int r){
        ArrayList<Brick> neighbors = new ArrayList<>();
        for (int i=x-r; i <= x+r; ++i){
            for (int j = y-r; j <= y+r; ++j){
                if (i==x&&j==y) continue;
                Brick n = brickMap.get(new Vector2(i,j));
                if (n != null)
                    neighbors.add(n);
            }
        }
        return neighbors;
    }
}
