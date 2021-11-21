package com.cschlisner.hbd.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Const {

    /**
     * ASSETS
     */
    public static final String _SOUND = "sound/";
    public static final String _TEXTURE = "texture/";
    public static final String _FONT = "font/";

    // sound files
    public static final String[] SOUNDS = {
            _SOUND+"ballSpawn.ogg",
            _SOUND+"button.ogg",
            _SOUND+"click.ogg",
            _SOUND+"explosion.ogg",
            _SOUND+"hitMetal.ogg",
            _SOUND+"tnt.ogg",
            _SOUND+"hitHurt.ogg"
    };

    // texture files
    public static final String[] TEXTURES = {
            _TEXTURE + "paddleSprite.png",
            _TEXTURE + "Ball2.png",
            _TEXTURE + "brick/BrickDamage2.png",
            _TEXTURE + "brick/Normal.png",
            _TEXTURE + "brick/Weak.png",
            _TEXTURE + "brick/Tough.png",
            _TEXTURE + "brick/Immune.png",
            _TEXTURE + "brick/PaddleSmall.png",
            _TEXTURE + "brick/BallSpeed.png",
            _TEXTURE + "brick/PaddleBig.png",
            _TEXTURE + "brick/BallSpawn.png",
            _TEXTURE + "brick/Explosive.png",
    };

    // font files
    public static final String[] FONTS = {
            _FONT+"doomed.ttf",
            _FONT+"IsWasted.ttf",
            _FONT+"pixel-bit-advanced.ttf",
    };

    public static String soundp(String s){
        return Const._SOUND+s;
    }
    public static FileHandle soundfh(String s){
        return Gdx.files.internal(soundp(s));
    }

    public static String texp(String t){
        return Const._TEXTURE+t;
    }

    public static FileHandle texfh(String t){
        return Gdx.files.internal(texp(t));
    }
    // internal font name (in assetmanager) with size
    public static String fontr(int font, int size){
        String[] sizes = new String[]{"XXS","XS","SM","MD","LG"};
        String[] fp = FONTS[font].split("/");
        String[] fn = fp[fp.length-1].split("\\.");
        return String.format("%s%s.%s",fn[0],sizes[size],fn[1]);
    }
    public static String fontp(String t){
        return Const._FONT+t;
    }
    public static FileHandle fontfh(String t){
        return Gdx.files.internal(texp(t));
    }

    public static final String _FONT_BIG = FONTS[0];
    public static final String _FONT_MD = FONTS[1];
    public static final String _FONT_SM = FONTS[2];

    public static final String[] TEXT = {
            "HYPER\nBRICK\nDESTROYER",
            "Hyper\n Brick\n Destroyer",
            "ZEN",
            "CHALLENGE",
            "CREATE",
            "SETTINGS",
            "PAUSE",
            "RESUME",
            "QUIT",
            "PAUSED",
    };


    /**
     * World params
     */
    public static final float PPM = 100;
    public static final float FRAMERATE = 60.0f;

    /* Camera information */
    public static final int VIEW_WIDTH = Gdx.graphics.getWidth();
    public static final int VIEW_HEIGHT = Gdx.graphics.getHeight();
    public static final float ASPR = VIEW_HEIGHT / VIEW_WIDTH;
    public static final float VIEW_WIDTHM = 15; // meters for box2d
    public static final float VIEW_HEIGHTM = 30; // meters for box2d

    public static final float REFDISP_x = 1440;
    public static final float REFDISP_y = 2533;

    // text sizes
    public static final int _TEXT_LG = (int)(200.0f * ((float)VIEW_WIDTH/REFDISP_x));
    public static final int _TEXT_MD = (int)((float)_TEXT_LG*0.75f);
    public static final int _TEXT_SM = _TEXT_LG/2;
    public static final int _TEXT_XS = _TEXT_LG/4;
    public static final int _TEXT_XXS = _TEXT_LG/8;

    public static final float TEXTVIEW_WIDTHM = VIEW_WIDTH / PPM; // meters for box2d
    public static final float TEXTVIEW_HEIGHTM= TEXTVIEW_WIDTHM * (VIEW_HEIGHTM/VIEW_WIDTHM); // meters for box2d
    public static final float _STATUS_HEIGHT = VIEW_HEIGHT / 48.0f;


    //Box2D Collision Bits
    public static final short NOTHING_FLAG = 0;
    public static final short BALL_FLAG = 1;
    public static final short WALL_FLAG = 2;
    public static final short PADDLE_FLAG = 4;
    public static final short BRICK_FLAG = 8;
    public static final short DESTROYED_FLAG = 16;
    public static final short _COLLISION_MASK = NOTHING_FLAG|BALL_FLAG|WALL_FLAG|PADDLE_FLAG|BRICK_FLAG|DESTROYED_FLAG;

    // Physics params
    public static final float BALL_SPEED = 15.0f;
    public static final float BALL_FRICTION = 0.1f;
    public static final float BALL_RESTITUTION = 0.9f;
    public static final float BALL_DENSITY = 0.3f;

    public static final float PADDLE_SPEED = 40f;
    public static final float PADDLE_FRICTION = 0.4f;
    public static final float PADDLE_RESTITUTION = 0.0f;
    public static final float PADDLE_DENSITY = 1.0f;

    public static final float BRICK_FRICTION = 0.1f;
    public static final float BRICK_RESTITUTION = 0.1f;
    public static final float BRICK_DENSITY = 1f;

    public static final float WALL_FRICTION = 0.0f;
    public static final float WALL_RESTITUTION = 0.3f;
    public static final float WALL_DENSITY = 1f;

    /* Color information */
    
    /* Level information*/
    public static final int START_LEVEL = 0;
    public static float LEVEL_WIDTH_SCALAR=1.2f;
    public static float LEVEL_HEIGHT_SCALAR=1f;

    public static final int[][][] testLevels = {
            {
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,2,0,0,0}
            },
            {
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,2,0,0,0}
            },
            {
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,9,0,9,0,9,0},
                {0,0,0,0,0,0,0,0},
                {0,5,0,6,0,7,0,8}
            },
            {
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,9,0,9,0,9,0},
                {0,0,0,0,0,0,0,0},
                {1,2,3,4,5,6,7,8}
            },
    };

    //
}
