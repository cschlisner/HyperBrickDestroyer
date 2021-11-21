package com.cschlisner.hbd.util;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.cschlisner.hbd.actor.Ball;
import com.cschlisner.hbd.actor.Brick;
import com.cschlisner.hbd.actor.PlayerPaddle;

public class CollisionHandler implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fxa = contact.getFixtureA();
        Fixture fxb = contact.getFixtureB();

        int contactFlags = fxa.getFilterData().categoryBits | fxb.getFilterData().categoryBits;
        switch (contactFlags){
            case Const.BALL_FLAG | Const.PADDLE_FLAG:
                Fixture p = (fxa.getFilterData().categoryBits == Const.BALL_FLAG) ? fxb : fxa;
                ((PlayerPaddle)p.getUserData()).handleCollision();
                ((Ball)(p==fxa?fxb:fxa).getUserData()).onContact();
                break;
            case Const.BALL_FLAG | Const.BRICK_FLAG:
                Fixture br = (fxa.getFilterData().categoryBits == Const.BALL_FLAG) ? fxb : fxa;
                ((Ball)(br==fxa?fxb:fxa).getUserData()).onContact();
                break;
            case Const.BALL_FLAG | Const.WALL_FLAG:
                Fixture ba = (fxa.getFilterData().categoryBits == Const.BALL_FLAG) ? fxa : fxb;
                ((Ball)ba.getUserData()).onContact();
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fxa = contact.getFixtureA();
        Fixture fxb = contact.getFixtureB();

        int contactFlags = fxa.getFilterData().categoryBits | fxb.getFilterData().categoryBits;
        switch (contactFlags){
            case Const.BALL_FLAG | Const.PADDLE_FLAG:
                break;
            case Const.BALL_FLAG | Const.BRICK_FLAG:
                Fixture br = (fxa.getFilterData().categoryBits == Const.BALL_FLAG) ? fxb : fxa;
                ((Brick)br.getUserData()).takeDamage();
                break;
            case Const.BALL_FLAG | Const.WALL_FLAG:
                break;
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
