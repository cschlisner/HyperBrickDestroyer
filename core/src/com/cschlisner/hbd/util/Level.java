package com.cschlisner.hbd.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.cschlisner.hbd.HyperBrickGame;
import com.cschlisner.hbd.actor.Ball;
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
    public int level_c; // level counter

    Random rng = new Random();

    public BodyGroup actorGroup;
    public BodyGroup balls = new BodyGroup();
    public BodyGroup bricks = new BodyGroup();
    public BodyGroup walls= new BodyGroup();


    public int bricksToClear;
    private int[][] curLevelMap;
    public Hashtable<Vector2, Brick> brickMap = new Hashtable<>();

    /* formatting brick group */
    public float WRLDW;
    public float WRLDH;
    public float WRLDWR, WRLDHR;
    public float w_SCL, h_SCL;
    public float BRKW;
    public float BRKH;
    float margin, header, footer;
    public float DRAW_X, DRAW_Y;
    int brickC;

    public Level(LevelManager manager, int level){
        this.manager = manager;
        this.game = manager.screen.getGame();
        this.level_c = level;
        actorGroup = new BodyGroup();

        w_SCL = (Const.LEVEL_WIDTH_SCALAR * (level_c-1));
        w_SCL = 0.7f + (w_SCL>0? w_SCL :0);
        h_SCL = (Const.LEVEL_HEIGHT_SCALAR * (level_c-1));
        h_SCL = 0.8f + (h_SCL>0? h_SCL :0);

        brickC = (int)(Const.STARTING_BRICKC * (1.0f+ (((float)level-1) * Const.BRICK_SLALAR)));


        // sample brick for measurements
        Texture brickTex = manager.screen.getAssManager().get(Const.TEXTURES[3], Texture.class);
        BRKH = brickTex.getHeight() / Const.PPM;
        BRKW = brickTex.getWidth() / Const.PPM;

        // we will be drawing from lr = (-WRLDW/2,0)
        switch (game.getMode()){
            case ZEN:
                // we are defining brick map based on world width
                WRLDW = game.SCRW * w_SCL;
                WRLDH = game.SCRH * h_SCL;

                break;
            case CREATE:
                this.curLevelMap = new int[][]{{0,0,0,0,0,0,0},{0,0,0,0,0,0,0},{0,0,0,0,0,0,0}};
                // no break here on purpose...
            case CHALLENGE:
                // we are defining world width based on brick map
                this.curLevelMap = curLevelMap == null ? Const.testLevels[level-1] : curLevelMap;
            default:
                WRLDW = curLevelMap[0].length * BRKW + BRKW*3;
                WRLDW += WRLDW < game.SCRWR ? game.SCRWR - WRLDW:0; // fit to at least 1/2 viewport
                WRLDH = WRLDW * Const.ASPRM;
                break;

        }

        WRLDWR = WRLDW*0.5f;
        WRLDHR = WRLDH*0.5f;
        // space from top of bricks to top of world
        header = WRLDH / Const.BRICK_HEADER;
        footer = 2*header;
        // space from left of bricks to left of world
        margin = (WRLDW / BRKW);
        margin = (margin - (int)(margin))/2 + BRKW;
        // drawing coordinates for Brick at (0,0) in brickmap
        DRAW_X = (-WRLDWR)+margin;
        DRAW_Y = WRLDH-header;

        // precalc probability norms for brick selector
        // normalized probabilities
        float psum = 0;
        for (float[] prob : brickTypeDistribution)
            psum += prob[1];
        for (float[] prob : brickTypeDistribution)
            prob[1]/=psum;

        // get map of brick layout. NxM int grid
        switch (game.getMode()){
            case ZEN:
                this.curLevelMap = makeLevel(level, true);
                break;
            case CHALLENGE:
                break;
            case CREATE:
                break;
        }

        // lays out bricks according to level map
        createBricks(level_c);

        // create wall bounds
        float wallwidth = 1.0f;
        walls.addActor(new Wall(this, -WRLDWR-wallwidth, 0, wallwidth, WRLDH));
        walls.addActor(new Wall(this, WRLDWR, 0, wallwidth, WRLDH)); //R
        walls.addActor(new Wall(this, -WRLDWR, WRLDH, WRLDW, wallwidth)); //T
        actorGroup.addActor(walls);
        actorGroup.addActor(bricks);
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

    // take level defined by level map and create brick actors
    public void createBricks(int level){
        bricksToClear = 0;
        Brick brick;
        for (int i = 0; i < curLevelMap.length; ++i){
            for (int j=0; j< curLevelMap[0].length; ++j) {
                if (curLevelMap[i][j] > 0) {
                    brick = new Brick(this, j, i, Brick.BrickType.values()[curLevelMap[i][j]-1]);
                    bricks.addActor(brick);
                    brickMap.put(new Vector2(j,i), brick);
                    if (brick.type != Brick.BrickType.Immune)
                        ++bricksToClear;
                }
            }
        }
    }

    float[][] brickTypeDistribution = {
            {0,0.05f},
            {1,0.8f},
            {2,0.3f},
            {3,0.3f},
            {4,0.15f},
            {5,0.15f},
            {6,0.25f},
            {7,0.03f},
            {8,0.2f},
            {9,0.333333f}
    };

    float[][] levelBrickProbDist = brickTypeDistribution;

    public float[][] updateProbabilities(int level){
        float[][] newProbDist = new float[brickTypeDistribution.length][brickTypeDistribution[0].length];
        for (int i = 0; i < newProbDist.length; ++i){
            newProbDist[i][0] = i;
            newProbDist[i][1] = (i > level ? 0.001f : 0.1f)*(i+1) * levelBrickProbDist[i][1];
        }
        return newProbDist;
    }

    private int selectBrick(int level){
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

    public int[][] makeLevel(int level, boolean symmetric){

        levelBrickProbDist = updateProbabilities(level);

        int columns = (int)((WRLDW - (2*margin)) / BRKW);
        int rows = (int)((WRLDH - (3*header)) / BRKW);

        int[][] levelmap = new int[rows][columns];

        brickfillloop:
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
                if (brick>0)
                    brickC -= symmetric?2:1;
                if (brickC==0)
                    break brickfillloop;
            }
        }

        return levelmap;
    }
}
