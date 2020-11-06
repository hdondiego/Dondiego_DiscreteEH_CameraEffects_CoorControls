package edu.lewisu.cs.hdondiego;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

abstract class CameraEffect {
    protected OrthographicCamera cam;
    protected int duration, progress;
    protected ShapeRenderer renderer;
    protected SpriteBatch batch;
    public CameraEffect(OrthographicCamera cam, int duration, 
    SpriteBatch batch, ShapeRenderer renderer) {
        this.cam = cam;
        this.duration = duration;
        this.batch = batch;
        this.renderer = renderer;
        progress = duration;
    }
    public boolean isActive() {
        return (progress<duration);
    }
    public abstract void play();
    public void updateCamera() {
        cam.update();
        if (renderer != null) {
            renderer.setProjectionMatrix(cam.combined);
        }
        if (batch != null) {
            batch.setProjectionMatrix(cam.combined);
        }
    }
    public void start() {
        progress = 0;
    }
}

class CameraWave extends CameraEffect {
    private int intensity;
    private int speed;
    public int getIntensity() {
        return intensity;
    }
    public void setIntensity(int intensity) {
        if (intensity < 0) {
            this.intensity = 0;
        } else {
            this.intensity = intensity;
        }
    }
    public int getSpeed() {
        return speed;
    }
    public void setSpeed(int speed) {
        if (speed < 0) {
            speed = 0;
        } else {
            if (speed > duration) {
                speed = duration / 2;
            } else {
                this.speed = speed;
            }
        }
    }
    @Override
    public boolean isActive() {
        return super.isActive() && speed > 0;
    }
    public CameraWave(OrthographicCamera cam, int duration, SpriteBatch batch,
    ShapeRenderer renderer, int intensity, int speed) {
        super(cam,duration,batch,renderer);
        setIntensity(intensity);
        setSpeed(speed);
    }
    
    // progress is initially 0
    // dampenFactor initially starts off as 1
    // as the value of progress increases, the dampenFactor gets smaller
    // this function is used to help change the completeness of the shaking
    // starts shaking at full capacity at the start, and slowly shakes less
    public float dampen() {
    	float dampenFactor = 1 - ((float)progress / duration);
    	return dampenFactor;
    }
    
    @Override
    public void play() {
        if (isActive()) {
            if (progress % speed == 0) {
                intensity = -intensity;
                cam.rotate(3*intensity*dampen());
                System.out.print(3*intensity*dampen());
            }
            progress++;
            if (!isActive()) {
            	cam.rotate(-3*intensity*(1-((float)(progress-1)/duration)));
            }
            updateCamera();
        }
    }
    @Override
    public void start() {
        super.start();
        updateCamera();
    }
}

public class DiscreteEHCameraEffectsCoorControls extends ApplicationAdapter {
	SpriteBatch batch;
    Texture imgLeft, imgRight;
    Texture background;
    float imgX, imgY;
    float bgX, bgY;
    float imgWidth, imgHeight;
    float WIDTH, HEIGHT;
    OrthographicCamera cam;
    float WORLDWIDTH, WORLDHEIGHT;
    LabelStyle labelStyle;
    Label label;
    boolean inJail, movingLeft;
    float jailMinX, jailMinY;
    CameraWave wave;
    
    public void setupLabelStyle() {
    	labelStyle = new LabelStyle();
    	labelStyle.font = new BitmapFont(Gdx.files.internal("fonts/myfont20201030.fnt"));
    }
	
	@Override
	public void create () {
		batch = new SpriteBatch();
        imgRight = new Texture("mj_move_right.png");
        imgLeft = new Texture("mj_move_left.png");
        background = new Texture("the_earth_wraps_dance_club_society.png");
        WIDTH = Gdx.graphics.getWidth();
        HEIGHT = Gdx.graphics.getHeight();   //viewport or screen        
		WORLDWIDTH = background.getWidth()/2;
		WORLDHEIGHT = background.getHeight()/2;              //of world
		System.out.printf("WORLDWIDTH: %f\nWORLDHEIGHT: %f\n", WORLDWIDTH, WORLDHEIGHT);
        imgX = 0;
        imgY = 0;
        bgX = -1*(background.getWidth()/2);
        bgY = -1*(background.getHeight()/2);
        imgWidth = imgRight.getWidth();
        imgHeight = imgRight.getHeight();
        cam = new OrthographicCamera(WIDTH,HEIGHT);
        cam.translate(WIDTH/2,HEIGHT/2);
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        System.out.println(cam.position.x + " " + cam.position.y);
        setupLabelStyle();
        // now create the label
        label = new Label("Welcome!", labelStyle);
        label.setPosition(20,400); // world coordinate == screen coordinates at the beginning
        inJail = false; // determines if the character will be locked within a region
        movingLeft = false; // determines if the character is going to move left or right - changing which image to show
        wave = new CameraWave(cam, 100, batch, null, 10, 2); // helps create wave motion of camera
	}
	
	public void handleInput() {
        if (Gdx.input.isKeyPressed(Keys.A)) {
        	movingLeft = true; // the character should face to the right - moonwalking to the left
            imgX-=10;
        }
        if (Gdx.input.isKeyPressed(Keys.D)) {
        	movingLeft = false; // the character should face to the left - moonwalking to the right
            imgX+=10;
        }
        if (Gdx.input.isKeyPressed(Keys.W)) {
            imgY+=10;
        }
        if (Gdx.input.isKeyPressed(Keys.S)) {
            imgY-=10; 
        }
        if (Gdx.input.isKeyPressed(Keys.J)) {
        	jailMinX = imgX; // save current imgX coordinate for the minimum jail x coordinate
        	jailMinY = imgY; // save current imgY coordinate for the minimum jail y coordinate
        	inJail = true; // will enforce the restricted region to move in
        }
        if (Gdx.input.isKeyPressed(Keys.U)) {
        	inJail = false; // will remove the restricted region
        }
	}
	public Vector2 getViewPortOrigin() {
		return new Vector2(cam.position.x-WIDTH/2, cam.position.y - HEIGHT/2);
	}
	public Vector2 getScreenCoordinates() {
		Vector2 viewportOrigin = getViewPortOrigin();
		return new Vector2(imgX-viewportOrigin.x, imgY-viewportOrigin.y);
	}
    public void panCoordinates(float border) {
        Vector2 screenPos = getScreenCoordinates();
        int quadrantLength = background.getWidth()/2;
        int quadrantHeight = background.getHeight()/2;
        
        // the world is placed at (-1000, -825), so the center of the world can be at origin (0,0)
        // for the x-axis, it must be split in half (1000 to the right, -1000 to the left)
        if (screenPos.x > WIDTH - imgWidth - border) { // in the right direction
        	if (imgX + imgWidth > quadrantLength + imgWidth + border) { // about to wrap around map from right to left side
        		wrapCoordinates(quadrantLength, quadrantHeight);
        		cam.position.x = -1 * cam.position.x + imgWidth;
                System.out.println(cam.position.x);
                cam.update();
                batch.setProjectionMatrix(cam.combined);
        	} else { // only exploring the right side of the world
        		cam.position.x = cam.position.x + screenPos.x - WIDTH + imgWidth + border;
                System.out.println(cam.position.x);
                cam.update();
                batch.setProjectionMatrix(cam.combined);
        	}
        }
        
        if (screenPos.x < 0) { // about to leave the viewport on the left side
        	if (imgX < (-1*quadrantLength) - border) { // about to wrap around map from left to right side
            	wrapCoordinates(quadrantLength, quadrantHeight);
            } else {
            	cam.position.x = cam.position.x - border - screenPos.x;
                System.out.println(cam.position.x);
                cam.update();
                batch.setProjectionMatrix(cam.combined);
            }
        }
        
        if (screenPos.y > HEIGHT - imgHeight - border) {  // go off viewport vertically
            if (imgY + imgHeight >= WORLDHEIGHT - border) {  // out of real estate in y direction
            	lockCoordinates((-1*quadrantLength) - border - imgWidth, -quadrantHeight, quadrantLength + imgWidth + border, quadrantHeight);
            } else { // keep panning we have more room
                cam.position.y = cam.position.y + screenPos.y - HEIGHT + imgHeight + border;
                System.out.println(cam.position.y);
                cam.update();
                batch.setProjectionMatrix(cam.combined);
            }
        }
        
        // the viewport is moving down the screen
        if (screenPos.y < 0) {
        	if (imgY <= -quadrantHeight) {
        		lockCoordinates((-1*quadrantLength) - border - imgWidth, -quadrantHeight, quadrantLength + imgWidth + border, quadrantHeight);
        	} else {
        		cam.position.y = cam.position.y - (border + screenPos.y);
                System.out.println(cam.position.y);
                cam.update();
                batch.setProjectionMatrix(cam.combined);
        	}
        }
    }
    
    // this helps with having the character wrap around the world when they reach past either the left side or right side of the world
    public void wrapCoordinates(float targetWidth, float targetHeight) {
        if (imgX > targetWidth) {
            imgX= -targetWidth;
        } else if (imgX <= (-targetWidth - imgWidth)) {
            imgX=targetWidth;
        }
        if (imgY > targetHeight) {
            imgY = -imgHeight;
        } else if (imgY < -targetHeight) {
            imgY = targetHeight;
        }
    }
    public void wrapCoordinates() {
        wrapCoordinates(WIDTH, HEIGHT);
    }
    
    // this helps with enforcing the lockCoordinates for the y axis and the jail region
    public void lockCoordinates(float minX, float minY, float maxX, float maxY) {
    	if (imgX < minX) {
    		imgX = minX;
    	} else if (imgX > maxX - imgWidth) {
    		imgX = maxX - imgWidth;
    	}
    	
    	if (imgY > maxY - imgHeight) {
    		imgY = maxY - imgHeight;
    	} else if (imgY < minY) {
    		imgY = minY;
    	}
    }
    
    public void lockCoordinates(float targetWidth, float targetHeight) {
        if (imgX > targetWidth - imgWidth) {
            imgX = targetWidth - imgWidth;
        } else if (imgX < 0) {
            imgX = 0;
        }
        
        if (imgY > targetHeight - imgHeight) {
            imgY = targetHeight - imgHeight;
        } else if (imgY < 0) {
            imgY = -targetHeight;
        }   
    }
    public void lockCoordinates() {
        lockCoordinates(WIDTH, HEIGHT);
    }

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        handleInput();
        panCoordinates(20);
        label.setText("X = " + imgX + ", Y = " + imgY);
        // update the label position to ensure that it stays at the same place on
        // the screen as the camera moves.
        label.setPosition(20+(cam.position.x-WIDTH/2), 400+(cam.position.y-HEIGHT/2));
        batch.begin();
        batch.draw(background,bgX,bgY);
        
        // enforce restricted region if inJail is true - J was pressed
        if (inJail) {
        	lockCoordinates(jailMinX, jailMinY, jailMinX + (imgWidth*1.5f), jailMinY + (imgHeight*1.5f));
        }
        
        // this is for the camera wave effect
        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
        	wave.start();
        }
        wave.play();
        
        // determining which image to draw depending on where character will moonwalk
        if (movingLeft) {
            batch.draw(imgLeft, imgX, imgY);
        } else {
            batch.draw(imgRight, imgX, imgY);
        }
        label.draw(batch, 1); // appear bold on the screen - actually be visible
        batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		imgLeft.dispose();
		imgRight.dispose();
	}
}
