package com.cschlisner.hbd;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class LevelManager {
    static int[][][] testLevels = {
            {
                    {9,4,4,9,9,9,9,4},
                    {9,4,4,9,4,4,4,4},
                    {9,9,9,9,9,9,9,4},
                    {4,4,4,9,4,4,9,4},
                    {9,9,9,9,4,4,9,4},
            },
            {
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {1,9,1,1,9,1,1,9},
                {1,1,1,1,1,1,1,1},
                {1,2,3,4,5,6,7,8},
            },
            {
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {1,9,1,1,9,1,1,9},
                {1,1,1,1,1,1,1,1},
                {1,9,9,1,1,9,9,1},
            },
            {
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {1,8,1,1,8,1,1,8},
                {1,1,1,1,1,1,1,1},
                {1,8,1,1,1,1,8,1},
            },
    };



    Random rng = new Random();

    Camera camera;
    Game game;
    Group brickGroup;
    Group spawnedBalls;

    int levelBrickCount = 0;
    private int[][] curLevelMap;
    private Hashtable<Vector2,Brick> brickMap = new Hashtable<>();

    /* formatting brick group */
    float SCRW, SCRH;
    int BRKW, BRKH;
    float margin, header;

    public float DRAW_X, DRAW_Y;


    public LevelManager(Camera camera){
        this.camera = camera;
        brickGroup = new Group();
        spawnedBalls = new Group();
        SCRH = camera.viewportHeight;
        SCRW = camera.viewportWidth;



        Brick b = new Brick(this,0,0,1);
        BRKH = (int)b.getHeight();
        BRKW = (int)b.getWidth();

        header = SCRH/9;
        margin = BRKW;
        DRAW_X = margin;
        DRAW_Y = SCRH-header;

        createBricks(1);
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

        this.curLevelMap = makeLevel(level, true);
//        this.curLevelMap = testLevels[level-1];

        for (Actor b : brickGroup.getChildren())
            b.remove();
        brickGroup.clear();
        brickMap.clear();

        int col = curLevelMap[0].length;

        levelBrickCount = 0;
        Brick brick;
        for (int i = 0; i < curLevelMap.length; ++i){
            for (int j=0; j< curLevelMap[0].length; ++j) {
                if (curLevelMap[i][j] > 0) {
                    brick = new Brick(this, j, i, curLevelMap[i][j]);
                    brickGroup.addActor(brick);
                    brickMap.put(new Vector2(j,i), brick);
                    if (brick.type != Brick.BrickType.Immune)
                        ++levelBrickCount;
                }
            }
        }
    }

    float[][] brickTypeDistribution = {
            {0,0.4f},
            {1,0.4f},
            {2,0.3f},
            {3,0.3f},
            {4,0.15f},
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
        int columns = (int)((SCRW - (2*margin)) / BRKW);
        int rows = (int)((SCRH - (3*header)) / BRKW);

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
