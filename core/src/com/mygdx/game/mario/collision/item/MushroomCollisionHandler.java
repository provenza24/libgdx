package com.mygdx.game.mario.collision.item;

import com.mygdx.game.mario.camera.GameCamera;
import com.mygdx.game.mario.sprite.AbstractSprite;
import com.mygdx.game.mario.sprite.tileobject.mario.Mario;

public class MushroomCollisionHandler extends AbstractItemCollisionHandler {

	public MushroomCollisionHandler() {		
	}

	@Override
	public void collide(Mario mario, AbstractSprite item, GameCamera camera) {		
		super.collide(mario, item, camera);
		if (mario.getSizeState()==0) {
			mario.changeSizeState(1);
		}
	}

}
