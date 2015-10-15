package com.mygdx.game.mario.background.impl;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.mygdx.game.mario.enums.BackgroundTypeEnum;
import com.mygdx.game.mario.sprite.AbstractSprite;

public class LeftScrollingBackground extends ScrollingBackground {

	public LeftScrollingBackground(AbstractSprite followedSprite, Batch batch, BackgroundTypeEnum backgroundType, float pvelocity) {
		super(followedSprite, batch, backgroundType);		
		setX(width);
		this.velocity = -pvelocity;
	}

}
