package app;

import javax.swing.JFrame;

public class Mainframe extends JFrame {
	public Mainframe(int[][] map) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		int w = 1280, h = 720, scale = 2;
		Player player = new Player(60, 90, 0.2, 10, false);
		int[][] copy = new int[map.length][map[0].length];
		int startX = 1, startY = 1, endX = 1, endY = 1;
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				copy[i][j] = map[i][j];
				if (map[i][j] == -2) {
					startX = j;
					startY = i;
					player.setSpawn(startX, startY);
					copy[i][j] = 0;
				} else if (map[i][j] == -3) {
					endY = i;
					endX = j;
					copy[i][j] = 0;
				}
			}
		}
		Setup setup = new Setup(w, h, copy, player, startX, startY, endX, endY, scale, this);
		this.add(setup);
		this.setResizable(false);
		this.setSize(w, h);
		setup.start();
		this.setVisible(true);
	}

}
