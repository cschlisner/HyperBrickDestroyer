package com.cschlisner.hbd.actor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.cschlisner.hbd.util.Const;
import com.cschlisner.hbd.util.Level;

public class Wall extends Actor {
    public Level level;
    public Body body;

    public Wall(Level level, float x, float y, float w, float h){
        setBounds(x, y, w, h);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x+w/2,y+h/2);

        this.level = level;
        this.body = level.game.getWorld().createBody(bodyDef);

        PolygonShape wallShape = new PolygonShape();
        wallShape.setAsBox(w/2, h/2);

        FixtureDef fDef = new FixtureDef();
        fDef.filter.categoryBits = Const.WALL_FLAG;
        fDef.filter.maskBits = Const._COLLISION_MASK;
        fDef.shape = wallShape;
        fDef.density = Const.WALL_DENSITY;
        fDef.friction = Const.WALL_FRICTION;
        fDef.restitution = Const.WALL_RESTITUTION;
        Fixture fixture = body.createFixture(fDef);
        fixture.setUserData(this);

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(level.game.camera.combined);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.setAutoShapeType(true);
    }

    ShapeRenderer shapeRenderer;
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.begin();
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(level.game.camera.combined);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();
        batch.begin();
    }

    public void destroy(){
        this.body.getWorld().destroyBody(this.body);
        this.level.wallGroup.removeActor(this);
        remove();
    }
}
