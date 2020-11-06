package edu.lewisu.cs.hdondiego.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import edu.lewisu.cs.hdondiego.DiscreteEHCameraEffectsCoorControls;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new DiscreteEHCameraEffectsCoorControls(), config);
	}
}
