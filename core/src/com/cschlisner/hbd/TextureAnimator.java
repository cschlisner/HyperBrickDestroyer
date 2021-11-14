package com.cschlisner.hbd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class TextureAnimator {
    TextureRegion still;
    ArrayList<Animation<TextureRegion>> animations = new ArrayList<>();
    private int anim_mode = 0;
    public float duration = 0;
    public TextureAnimator(Texture texture, int frames, int modes, float frameDuration){
        duration = frames*frameDuration;
        int w = texture.getWidth() / frames;
        int h = texture.getHeight() / modes;
        TextureRegion[][] texturemap = TextureRegion.split(texture, w, h);
        TextureRegion[] mode_frames = new TextureRegion[frames];
        for (int m = 0; m < modes;  ++m) {
            for (int i = 0, frame = 0; i < frames; ++i, ++frame)
                    mode_frames[frame] = texturemap[m][i];
            animations.add(new Animation<>(frameDuration, mode_frames));
        }
        still = texturemap[0][0];
    }


    public TextureRegion getFrame(float state, boolean loop) {
        return animations.get(anim_mode).getKeyFrame(state, loop);
    }
}
