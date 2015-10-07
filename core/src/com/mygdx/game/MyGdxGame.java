package com.mygdx.game;

import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.mygdx.game.mario.background.IScrollingBackground;
import com.mygdx.game.mario.background.impl.LeftScrollingBackground;
import com.mygdx.game.mario.collision.CollisionHandler;
import com.mygdx.game.mario.enums.DirectionEnum;
import com.mygdx.game.mario.enums.MarioStateEnum;
import com.mygdx.game.mario.sprite.AbstractGameSprite;
import com.mygdx.game.mario.sprite.impl.Mario;
import com.mygdx.game.mario.sprite.impl.MysteryBlock;
import com.mygdx.game.mario.tilemap.TmxMap;

public class MyGdxGame extends ApplicationAdapter {

	private boolean textDebugMode = false;
	
	private boolean boxDebugMode = false;		

	private TmxMap tileMap;

	private OrthogonalTiledMapRenderer renderer;

	private OrthographicCamera camera;

	private Batch batch;

	private Mario mario;

	private float cameraOffset = 0;

	private BitmapFont font;

	private SpriteBatch spriteBatch;

	private ShapeRenderer shapeRenderer;

	private int jumpTimerMax = 20;

	private IScrollingBackground scrollingBackground;

	@Override
	public void create() {

		shapeRenderer = new ShapeRenderer();

		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
		// font.setColor(0.5f,0.4f,0,1);
		font.setColor(0, 0, 1, 1);

		// load the map, set the unit scale to 1/32 (1 unit == 32 pixels)
		tileMap = new TmxMap("tilemaps/level_1_1.tmx");
		renderer = new OrthogonalTiledMapRenderer(tileMap.getMap(), 1 / 32f);

		mario = tileMap.getMario();

		// create an orthographic camera, shows us 30x20 units of the world
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 16, 15);
		camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
		camera.update();

		scrollingBackground = new LeftScrollingBackground(mario, spriteBatch, "background.gif", 16);
	}

	@Override
	public void render() {

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float delta = Gdx.graphics.getDeltaTime();

		// Listen to keyboard actions and update Mario status
		handleInput();
		mario.move(delta);
		mario.collideWithTilemap(tileMap);
		mario.updateAnimation(delta);

		// Move camera
		moveCamera();

		// Move scrolling background
		if (Math.floor(cameraOffset) == 8) {
			scrollingBackground.update();
		}
		scrollingBackground.render();

		// Render tilemap
		renderer.setView(camera);
		renderer.render();
		// Render mystery blocks
		renderMysteryBlocks(delta);
		// Render enemies
		handleEnemies(delta);
		// Render Mario
		mario.render(renderer.getBatch());
		// Render debug mode (press F1 to display/hide debug)
		renderDebugMode();

	}

	private void renderDebugMode() {
		
		if (textDebugMode) {
			// Mario information
			spriteBatch.begin();
			font.draw(spriteBatch,
					"mario.position=" + String.format("%.1f", mario.getX()) + "," + String.format("%.1f", mario.getY()),
					10, 460);
			font.draw(spriteBatch, "mario.acceleration=" + String.format("%.1f", mario.getAcceleration().x) + ","
					+ String.format("%.1f", mario.getAcceleration().y), 10, 440);
			font.draw(spriteBatch, "state=" + mario.getState().toString(), 10, 420);
			font.draw(spriteBatch, "direction=" + mario.getDirection().toString(), 10, 400);
			font.draw(spriteBatch, "jumptimer=" + mario.getJumpTimer(), 10, 380);
			font.draw(spriteBatch, "isOnFloor=" + mario.isOnFloor(), 10, 360);
			font.draw(spriteBatch, "camera.x=" + String.format("%.1f", camera.position.x) + " camera.offset="
					+ String.format("%.1f", cameraOffset), 10, 340);
			font.draw(spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond(), 450, 460);
			font.draw(spriteBatch,
					"tile-collision:  (right=" + mario.getMapCollisionEvent().isCollidingRight() + ", left="
							+ mario.getMapCollisionEvent().isCollidingLeft() + ", top="
							+ mario.getMapCollisionEvent().isCollidingTop() + ", bottom="
							+ mario.getMapCollisionEvent().isCollidingBottom() + ")",
					10, 320);
			font.draw(spriteBatch, "Mysteryblocks: " + tileMap.getMysteryBlocks().size(), 10, 300);
			font.draw(spriteBatch, "Enemies: " + tileMap.getEnemies().size(), 10, 280);
			spriteBatch.end();
		}
		
		if (boxDebugMode) {
			// Green rectangle around Mario
			batch = renderer.getBatch();
			batch.begin();
			shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(0, 0, 1, 1);
			shapeRenderer.rect(mario.getX()+mario.getOffset().x, mario.getY(), mario.getWidth(), mario.getHeight());
			
			for (AbstractGameSprite sprite : tileMap.getEnemies()) {
				shapeRenderer.rect(sprite.getX()+sprite.getOffset().x, sprite.getY(), sprite.getWidth(), sprite.getHeight());
			}
			
			shapeRenderer.end();
			batch.end();
		}
	}

	private void moveCamera() {

		float move = mario.getX() - mario.getOldPosition().x;
		if (cameraOffset < 8) {
			cameraOffset = cameraOffset + move;
		} else {
			if (move > 0) {
				camera.position.x = camera.position.x + move;
			} else {
				cameraOffset = cameraOffset + move;
			}
		}
		if (mario.getX() < camera.position.x - 8) {
			mario.setX(mario.getOldPosition().x);
			mario.getAcceleration().x = 0;
			cameraOffset = cameraOffset - move;
		}
		camera.update();
	}

	private void handleEnemies(float deltaTime) {

		List<AbstractGameSprite> enemies = tileMap.getEnemies();
		for (int i = 0; i < enemies.size(); i++) {
			AbstractGameSprite enemy = enemies.get(i);
			enemy.update(tileMap, camera, deltaTime);
			// Draw it
			if (enemy.isAlive()) {
				for (int j = i + 1; j < enemies.size(); j++) {
					// Check collision with other enemies					
					CollisionHandler.getCollisionHandler().collideEnemies(enemy, enemies.get(j));
				}				
			}			
			if (enemy.isDeletable()) {
				enemies.remove(i--);
			} else if (enemy.isVisible()) {
				enemy.render(renderer.getBatch());
			}

		}
	}

	private void renderMysteryBlocks(float delta) {

		// Get blocks from tilemap
		List<MysteryBlock> blocks = tileMap.getMysteryBlocks();
		if (blocks.size() > 0) {
			batch = renderer.getBatch();
			batch.begin();
			// All blocks have the same animation, running in phasis
			MysteryBlock.updateAnimation(delta);
			for (int i = 0; i < blocks.size(); i++) {
				// For each block
				MysteryBlock block = blocks.get(i);
				if (block.isVisible()) {
					// Block is drawn only if visible
					if (block.getX() < camera.position.x - 9) {
						// Block not visible anymore, delete it from list
						blocks.remove(i--);
					} else {
						// Block is still visible, draw it
						batch.draw(block.getCurrentFrame(), block.getX(), block.getY(), 1, 1);
					}
				} else if (block.getX() < camera.position.x + 8) {
					// Block was not visible, now will be visible and drawable
					block.setVisible(true);
				}
			}
			batch.end();
		}
	}

	private void handleInput() {

		if (Gdx.input.isKeyJustPressed(Keys.F1)) {
			textDebugMode = !textDebugMode;
		}
		
		if (Gdx.input.isKeyJustPressed(Keys.F2)) {
			boxDebugMode = !boxDebugMode;
		}		
		
		if (Gdx.input.isKeyJustPressed(Keys.F3)) {
			mario.getAcceleration().y = 0;
			mario.setY(mario.getY() + 5);
		}

		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			if (mario.getDirection() == DirectionEnum.LEFT) {
				// Sliding
				mario.setStateIfNotJumping(MarioStateEnum.SLIDING_LEFT);
				mario.decelerate();
				if (mario.getAcceleration().x <= 0) {
					mario.getAcceleration().x = 0;
					mario.setDirection(DirectionEnum.RIGHT);
				}
			} else {
				mario.accelerate();
				mario.setDirection(DirectionEnum.RIGHT);
				mario.setStateIfNotJumping(MarioStateEnum.RUNNING_RIGHT);
			}
		} else if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			if (mario.getDirection() == DirectionEnum.RIGHT) {
				// Sliding
				mario.setStateIfNotJumping(MarioStateEnum.SLIDING_RIGHT);
				mario.decelerate();
				if (mario.getAcceleration().x <= 0) {
					mario.getAcceleration().x = 0;
					mario.setDirection(DirectionEnum.LEFT);
				}
			} else {
				mario.accelerate();
				mario.setDirection(DirectionEnum.LEFT);
				mario.setStateIfNotJumping(MarioStateEnum.RUNNING_LEFT);
			}
		} else {
			mario.decelerate();
		}

		if (Gdx.input.isKeyPressed(Keys.UP) && mario.canInitiateJump()
				&& !(mario.getState() == MarioStateEnum.JUMPING || mario.getState() == MarioStateEnum.FALLING)) {
			// player is on the ground, so he is allowed to start a jump
			mario.setJumpTimer(1);
			mario.setState(MarioStateEnum.JUMPING);
			mario.getAcceleration().y = 0.16f;
			mario.setCanJumpHigher(true);
			jumpTimerMax = 24 + (int) mario.getAcceleration().x / 5;
		} else if (Gdx.input.isKeyPressed(Keys.UP) && mario.getState() == MarioStateEnum.JUMPING
				&& mario.canJumpHigher()) {
			if (mario.getJumpTimer() < jumpTimerMax) { //
				mario.incJumpTimer();
				mario.getAcceleration().y += 0.009;
			} else {
				mario.setCanJumpHigher(false);
				mario.setJumpTimer(0);
				mario.setState(MarioStateEnum.FALLING);
			}
		} else {
			mario.setCanJumpHigher(false);
			mario.setJumpTimer(0);
		}

		mario.setCanInitiateJump(!Gdx.input.isKeyPressed(Keys.UP) && mario.isOnFloor());

	}

}
