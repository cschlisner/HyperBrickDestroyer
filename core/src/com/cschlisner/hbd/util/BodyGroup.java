package com.cschlisner.hbd.util;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.SnapshotArray;
import com.cschlisner.hbd.actor.Ball;
import com.cschlisner.hbd.actor.Brick;
import com.cschlisner.hbd.actor.Wall;

public class BodyGroup extends Group {

    public boolean remove(boolean forceRemoveBricks){
        // remove bodies from child actors
        SnapshotArray<Actor> ssa = getChildren();
        Actor[] ch = ssa.begin();
        for (Actor a : ch){
            if (a instanceof Wall)
                ((Wall)a).remove();
            if (a instanceof Ball)
                ((Ball)a).remove();
            if (a instanceof Brick) {
                Brick b = (Brick) a;
                if (!forceRemoveBricks)
                    b.brickBroken(); // waits for break animation to complete before actor removal
                else { // directly remove body and actor
                    b.body.getWorld().destroyBody(b.body);
                    b.remove();
                }
            }
            if (a instanceof BodyGroup)
                ((BodyGroup)a).remove(true);
        }
        ssa.end();
        return super.remove();
    }
    @Override
    public boolean remove() {
        return remove(false);
    }
}
