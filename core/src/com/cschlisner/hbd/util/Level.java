package com.cschlisner.hbd.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
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
public class Level {
    public HyperBrickGame game;
    public LevelManager manager;
    int level_c; // level counter

    Random rng = new Random();

    public Group actorGroup;
    public Group brickGroup;
    public Group spawnedBalls;
    public Group wallGroup;

    public int bricksToClear;
    private int[][] curLevelMap;
    private Hashtable<Vector2, Brick> brickMap = new Hashtable<>();

    /* formatting brick group */
    public float WRLDW;
    public float WRLDH;
    float WRLDWR, WRLDHR;
    float BRKW, BRKH;
    float margin, header;
    public float DRAW_X, DRAW_Y;

    public Level(LevelManager manager, int level){
        this.manager = manager;
        this.game = manager.screen.game;
        this.level_c = level;
        brickGroup = new Group();
        spawnedBalls = new Group();
        wallGroup = new Group();
        actorGroup = new Group();

        float w_scl = (Const.LEVEL_WIDTH_SCALAR * (level_c-1));
        w_scl = (w_scl>0)?w_scl:1;
        float h_scl = (Const.LEVEL_HEIGHT_SCALAR * (level_c-1));
        h_scl = (h_scl>0)?h_scl:1;

        // we will be drawing from lr = (-WRLDW/2,0)
        WRLDW = game.SCRW * w_scl;
        WRLDH = game.SCRH * h_scl;
        WRLDWR = WRLDW*0.5f;
        WRLDHR = WRLDH*0.5f;

        // sample brick for measurements
        Texture brickTex = manager.screen.assManager.get(Const.TEXTURES[3], Texture.class);
        BRKH = brickTex.getHeight() / Const.PPM;
        BRKW = brickTex.getWidth() / Const.PPM;

        // space from top of bricks to top of world
        header = WRLDH / 9;
        // space from left of bricks to left of world
        margin = (WRLDW / BRKW);
        margin = (margin - (int)(margin))/2 + BRKW;
        // drawing coordinates for Brick at (0,0) in brickmap
        DRAW_X = (-WRLDWR)+margin;
        DRAW_Y = WRLDH-header;

        // makes a level and lays out bricks
        createBricks(level_c);

        // create wall bounds
        wallGroup.addActor(new Wall(this, -WRLDWR-0.55f, 0, 1, WRLDH)); //L
        wallGroup.addActor(new Wall(this, WRLDWR-0.55f, 0, 1, WRLDH)); //R
        wallGroup.addActor(new Wall(this, -WRLDWR, WRLDH-0.55f, WRLDW, 1)); //T

        actorGroup.addActor(wallGroup);
        actorGroup.addActor(brickGroup);
        actorGroup.addActor(spawnedBalls);
    }

    public int[][] getCurLevelMap(){
        return curLevelMap;
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

    public void createBricks(int level){
        // precalc probability norms for brick selector
        // normalized probabilities
        float psum = 0;
        for (float[] prob : brickTypeDistribution)
            psum += prob[1];
        for (float[] prob : brickTypeDistribution)
            prob[1]/=psum;

        switch (game.getMode()){
            case ZEN:
                this.curLevelMap = makeLevel(level, true);
                break;
            case CHALLENGE:
                this.curLevelMap = Const.testLevels[level-1];
                break;
        }

        bricksToClear = 0;
        Brick brick;
        for (int i = 0; i < curLevelMap.length; ++i){
            for (int j=0; j< curLevelMap[0].length; ++j) {
                if (curLevelMap[i][j] > 0) {
                    brick = new Brick(this, j, i, curLevelMap[i][j]);
                    brickGroup.addActor(brick);
                    brickMap.put(new Vector2(j,i), brick);
                    if (brick.type != Brick.BrickType.Immune)
                        ++bricksToClear;
                }
            }
        }
    }

    float[][] brickTypeDistribution = {
            {0,0.4f},
            {1,0.4f},
            {2,0.3f},
            {3,0.3f},
            {4,0.06f},
            {5,0.03f},
            {6,0.05f},
            {7,0.03f},
            {8,0.05f},
            {9,0.1f}
    };

    private int selectBrick(int level){
        float p = rng.nextFloat();
        float cumulativeProbability = 0.0f;
        for (float[] brickp : brickTypeDistribution) {
            cumulativeProbability += brickp[1];
            if (p <= cumulativeProbability) {
                return (int)brickp[0];
            }
        }
        return 0;
    }

    public int[][] makeLevel(int level, boolean symmetric){
        int columns = (int)((WRLDW - (2*margin)) / BRKW);
        int rows = (int)((WRLDH - (3*header)) / BRKW);

        int[][] levelmap = new int[rows][columns];

        for (int i = 0; i < rows; ++i){
            for (int j=0; j< (symmetric?(columns/2):columns); ++j){
                int brick = selectBrick(level);
                int mirrorbrick = brick<5||brick==9?brick:1;
                if (symmetric) {
                    boolean coinflip = rng.nextBoolean();
                    levelmap[i][j] = coinflip?brick:mirrorbrick;
                    levelmap[i][columns - 1 - j] = coinflip?mirrorbrick:brick;
                }
                else levelmap[i][j] = brick;
            }
        }

        return levelmap;
    }
}
