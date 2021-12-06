package com.cschlisner.hbd.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.cschlisner.hbd.HyperBrickGame;

public interface GameViewCtx {
    public Camera getCamera();
    public Camera getUICamera();
    public HyperBrickGame getGame();
    public AssetManager getAssManager();
    public Stage getGameStage();
    public Stage getUIStage();
}
