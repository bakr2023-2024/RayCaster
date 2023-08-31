package app;

public class Player {
	int FOV, hFOV;
	double x, y;
	int viewAng;
	double playerSpeed;
	double rotationSpeed;
	boolean fishEyeOn;

	public Player(int fOV, int viewAng, double playerSpeed, double rotationSpeed, boolean fishEyeOn) {
		super();
		FOV = fOV;
		hFOV = FOV / 2;
		this.viewAng = viewAng;
		this.playerSpeed = playerSpeed;
		this.rotationSpeed = rotationSpeed;
		this.fishEyeOn = fishEyeOn;
	}

	void setSpawn(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
