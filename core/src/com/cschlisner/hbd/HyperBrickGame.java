package com.cschlisner.hbd;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Stack;

public class HyperBrickGame extends Game {
    Viewport gameVP;
    OrthographicCamera camera; // used for drawing textures, game world
    Viewport textVP;
    OrthographicCamera textCamera; // use for drawing UI


    // Game Camera Data
    public float SCRH, SCRW; // width/height of viewport / world
    public float SCRHR, SCRWR; // 0.5 * SCRW/SCRH
    public float CAMX, CAMY; // X, Y in game units of camera center
    public float CAMOX, CAMOY; // X, Y of camera bottom left corner
    public float ASPR; // aspect ratio
    // Text Camera Data
    public float TSCRH;
    public float TSCRW;
    public float TSCRX, TSCRY;

    TitleScreen titleScreen; // entry point to rest of game
    // For drawing splash screen
    public SpriteBatch spriteBatch;
    public BitmapFont font, loadingfont;
    AssetManager assetManager;
    FreeTypeFontGenerator fontGenerator;

    // curent game mode
    public enum GameMode {
        ZEN, CHALLENGE
    }
    private GameMode mode;

    /**
     * Box2d stuff
     */
    // world to use, initialize as much stuff as possible while we are on a loading screen
    private World world;
    private Vector2 gravity;
    Box2DDebugRenderer debugRenderer;
    public boolean debug = true;

    @Override
    public void create() {

        // Global camera for game drawing
        camera = new OrthographicCamera(Const.VIEW_WIDTHM, Const.VIEW_HEIGHTM);
        gameVP = new ExtendViewport(camera.viewportWidth, camera.viewportHeight, camera);
        this.SCRH = this.camera.viewportHeight;
        this.SCRW = this.camera.viewportWidth;
        this.SCRHR = SCRH*0.5f;
        this.SCRWR = SCRW*0.5f;
        this.ASPR = (float)Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth();

        textCamera = new OrthographicCamera(Const.VIEW_WIDTH, Const.VIEW_HEIGHT);
//        textVP = new ExtendViewport(textCamera.viewportWidth, textCamera.viewportHeight, textCamera);
//        textVP = new FitViewport(textCamera.viewportWidth, textCamera.viewportHeight, textCamera);
//        textVP = new StretchViewport(textCamera.viewportWidth, textCamera.viewportHeight, textCamera);
        textVP = new ScreenViewport(textCamera);
        this.TSCRH = this.textCamera.viewportHeight;
        this.TSCRW = this.textCamera.viewportWidth;
        this.TSCRX = this.textCamera.position.x - (this.TSCRW/2);
        this.TSCRY = this.textCamera.position.y - (this.TSCRH/2);
        // set camera default coordingates to center on x-axis
        // camera bottom left is now (0,0)
        updateCamera(SCRWR, SCRHR);

//        System.out.println(String.format("CAM: %s, (%0.2f,%0.2f) vW:0.2f %vH:0.2f | VIEW: %s, ", splashView.));
        // FOr drawing title and progress etc
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(Const._FONT_BIG));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size= Const._TEXT_LG;


        font = fontGenerator.generateFont(fontParameter);
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(Const._FONT_SM));
        fontParameter.size=Const._TEXT_SM;
        loadingfont = fontGenerator.generateFont(fontParameter);
//        loadingfont.getData().setLineHeight(Const._TEXT_LH);

        spriteBatch = new SpriteBatch();

        // load assets
        assetManager = new AssetManager();
        for (String asset : Const.SOUNDS)
            assetManager.load(asset, Sound.class);
        for (String asset : Const.TEXTURES)
            assetManager.load(asset, Texture.class);

        //fonts
        FileHandleResolver resolver = new InternalFileHandleResolver();
        assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
        String assetName;
        for (String asset : Const.FONTS) {
            // Generate small, medium, large fonts for each font in Const
            assetName = asset.split("/")[1].split("\\.")[0];

            FreetypeFontLoader.FreeTypeFontLoaderParameter lpxxs = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
            lpxxs.fontFileName = asset;
            lpxxs.fontParameters.size = Const._TEXT_XS;
            assetManager.load(assetName+"XXS.ttf", BitmapFont.class, lpxxs);

            FreetypeFontLoader.FreeTypeFontLoaderParameter lpxs = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
            lpxs.fontFileName = asset;
            lpxs.fontParameters.size = Const._TEXT_XS;
            assetManager.load(assetName+"XS.ttf", BitmapFont.class, lpxs);

            FreetypeFontLoader.FreeTypeFontLoaderParameter lpsm = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
            lpsm.fontFileName = asset;
            lpsm.fontParameters.size = Const._TEXT_SM;
            assetManager.load(assetName+"SM.ttf", BitmapFont.class, lpsm);

            FreetypeFontLoader.FreeTypeFontLoaderParameter lpmd = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
            lpmd.fontFileName = asset;
            lpmd.fontParameters.size = Const._TEXT_MD;
            assetManager.load(assetName+"MD.ttf", BitmapFont.class, lpmd);

            FreetypeFontLoader.FreeTypeFontLoaderParameter lplg = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
            lplg.fontFileName = asset;
            lplg.fontParameters.size = Const._TEXT_LG;
            assetManager.load(assetName+"LG.ttf", BitmapFont.class, lplg);
        }

        // create a world to use for the rest of the games. Should be mutable
        this.gravity = new Vector2(0.0f,-1);
        this.world = new World(gravity, true);
        this.debugRenderer = new Box2DDebugRenderer();

        //debugging...
//        drawBounds();
    }

    public void setGravity(Vector2 gravity) {
        this.gravity = gravity;
    }

    public Vector2 getGravity() {
        return gravity;
    }

    public void setWorld(World world){
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public void setMode(GameMode mode){
        this.mode = mode;
    }

    public GameMode getMode() {
        return mode;
    }

    public void updateCamera(){
        updateCamera(this.CAMX, this.CAMY);
    }

    public void updateCamera(float x, float y){
        camera.position.set(x,y,0);
        camera.update();
        this.CAMX = camera.position.x;
        this.CAMY = camera.position.y;
        this.CAMOX = CAMX-SCRWR;
        this.CAMOY = CAMY-SCRHR;

        // set text camera origin (bottom-left) at (0,0) -- NOT CENTERED ON CAMX,CAMY
        // whenever rendering text, pixel units can be used and drawn to the text camera
        // which is the size of the client screen and starts at (0,0)
        textCamera.position.set(x+(TSCRW/2)-SCRWR,x+(TSCRH/2)-SCRHR,0);
        textCamera.update();
        this.TSCRX = textCamera.position.x - TSCRW/2;
        this.TSCRY = textCamera.position.y - TSCRH/2;
    }

    // for animating Title color (can't use java.awt)
    public Color HSLtoColor(float h, float s, float l){
        float C = (1-Math.abs(2*l-1))*s;
        float X = C * (1-Math.abs(((h/60.0f)%2)-1));
        float m = l-C/2;
        float[] Cx0 = {C, X, 0.0f};
        float[] RGBprime = {};
        if (h < 60)
            RGBprime = Cx0;
        else if (h < 120)
            RGBprime = new float[]{Cx0[1], Cx0[0], Cx0[2]};
        else if (h < 180)
            RGBprime = new float[]{Cx0[2], Cx0[0], Cx0[1]};
        else if (h < 240)
            RGBprime = new float[]{Cx0[2], Cx0[1], Cx0[0]};
        else if (h < 300)
            RGBprime = new float[]{Cx0[1], Cx0[2], Cx0[0]};
        else if (h < 360)
            RGBprime = new float[]{Cx0[0], Cx0[2], Cx0[1]};

        float[] RGB = new float[3];
        int i=0;
        for (float v : RGBprime)
            RGB[i] = (RGBprime[i++]+m);

        return new Color(RGB[0],RGB[1],RGB[2],1);
    }

    boolean blinking=true, bw=true;
    int blink_i=4;
    public void render(){

        if (titleScreen != null){
            getScreen().render(Gdx.graphics.getDeltaTime());
            return;
        }
        super.render();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(assetManager.update()) {
            blinking = blink_i-- > 0;
            ScreenUtils.clear(blinking&bw?Color.RED:Color.BLACK);
            if (blinking) {
                try {
                    Thread.sleep(150);
                    bw = !bw;
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            else {
                assetManager.finishLoading();
                if (assetManager.getProgress() == 1)
                    // done loading. load up title and wait a sec
                    titleScreen = new TitleScreen(this);
                    try {
                        Thread.sleep(256);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    this.setScreen(titleScreen);
            }
        }
        float progress = assetManager.getProgress();
        float H = Interpolation.fastSlow.apply(0.0f, 359.9999f, progress);
        float S = Interpolation.fade.apply(0.2f, 1f, progress);
        float L = Interpolation.slowFast.apply(1f, 0.5f, progress);
        Color newCol = HSLtoColor(H, S, L);
        font.setColor(newCol);

        spriteBatch.setProjectionMatrix(this.textCamera.combined);

        spriteBatch.begin();

        font.draw(spriteBatch, Const.TEXT[0], TSCRX,TSCRY+TSCRH*0.9f);
        loadingfont.draw(spriteBatch, String.format("loading.... %.0f%%", progress*100), TSCRX,TSCRY+TSCRH*0.1f);

        spriteBatch.end();

        // debugging....
//        Gdx.gl.glEnable(GL20.GL_BLEND);
//        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//        renderCameraData(camera, Color.LIME, 3);
//        renderCameraData(textCamera, Color.BLUE, 2);
//        Gdx.gl.glDisable(GL20.GL_BLEND);

    }

    // debugging....
    ShapeRenderer shapeRenderer;
    public void drawBounds(){
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setProjectionMatrix(this.textCamera.combined);
        shapeRenderer.setColor(Color.LIME);
    }
    private void renderCameraData(Camera cam, Color color, int r){

        if (shapeRenderer != null) {

            shapeRenderer.setColor(new Color(color.r, color.b, color.g, 0.3f));
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            //VIEWPORT
            shapeRenderer.rect(cam.position.x-cam.viewportWidth/4, cam.position.y-cam.viewportHeight/4, SCRWR,SCRHR);

            //ORIGIN
            shapeRenderer.circle(0, 0, r,200);

            //CAMERA POS
            shapeRenderer.circle(cam.position.x, cam.position.y, r,200);

            shapeRenderer.end();
        }
    }


    public void dispose() {
        super.dispose();
        assetManager.dispose();
        spriteBatch.dispose();
        font.dispose();
        loadingfont.dispose();
    }
}
