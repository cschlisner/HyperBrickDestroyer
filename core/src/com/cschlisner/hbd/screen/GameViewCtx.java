package com.cschlisner.hbd.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.cschlisner.hbd.HyperBrickGame;

public interface GameViewCtx {
    public Camera getCamera();
    public Camera getUICamera();
    public HyperBrickGame getGame();
    public AssetManager getAssManager();
}
