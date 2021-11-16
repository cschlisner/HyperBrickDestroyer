package com.cschlisner.hbd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;

public class Const {   
    
    /* assets */
    public static final String _SOUND = "sound/";
    public static final String _TEXTURE = "texture/";
    
    //sound
    public static final String[] SOUNDS = {
            _SOUND+"ballSpawn.ogg",
            _SOUND+"button.ogg",
            _SOUND+"click.ogg",
            _SOUND+"explosion.ogg",
            _SOUND+"hitMetal.ogg",
            _SOUND+"tnt.ogg",
            _SOUND+"hitHurt.ogg"
    };
    public static String soundp(String s){
        return Const._SOUND+s;
    }
    public static FileHandle soundfh(String s){
        return Gdx.files.internal(soundp(s));
    }
    
    //texture
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
    public static String texp(String t){
        return Const._TEXTURE+t;
    }
    public static FileHandle texfh(String t){
        return Gdx.files.internal(texp(t));
    }

    //sound
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
    };


    /* World information */


    /* Camera information */
    public static final int VIEW_WIDTH = 1300;
    public static final int VIEW_HEIGHT = 2533;


    /* Color information */
    
    /* Level information*/
}
