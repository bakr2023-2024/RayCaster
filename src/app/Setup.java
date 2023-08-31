package app;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Setup extends JPanel {
	final static int delay = 40;
	final static int precision = 64;
	Player player;
	Screen screen;
	int scale;
	Projector projector;
	Timer timer;
	int startX, startY;
	int endX, endY;
	BufferedImage buffer = null;
	Graphics2D g2d = null;
	Color ceilingColor = Color.pink;
	Color wallColor = Color.blue;
	Color floorColor = Color.yellow;
	Mainframe reference;
	int[][] map;

	class Projector {
		int pW, pH, hpW, hpH;
		double incrementAngle;

		Projector(Screen screen, Player player) {
			this.pW = screen.sW / screen.scale;
			this.pH = screen.sH / screen.scale;
			this.hpW = pW / 2;
			this.hpH = pH / 2;
			this.incrementAngle = (double) player.FOV / (double) this.pW;
		}
	}

	class Screen {
		int scale;
		int sW, sH, hsW, hsH;
		AffineTransform at;

		Screen(int w, int h, int scale) {
			sW = w;
			sH = h;
			hsW = sW / 2;
			hsH = sH / 2;
			this.scale = scale;
			at = AffineTransform.getScaleInstance(scale, scale);
		}

	}

	Setup(int width, int height, int[][] map, Player player, int sX, int sY, int eX, int eY, int scale,
			Mainframe reference) {
		this.reference = reference;
		resetVariables(width, height, map, player, sX, sY, eX, eY, scale);
		timer = new Timer(delay, e -> {
			rayCasting();
		});

		this.addKeyListener(controls);
		this.setBackground(Color.black);
	}

	void resetVariables(int width, int height, int[][] map, Player player, int sX, int sY, int eX, int eY, int scale) {
		this.scale = scale;
		screen = new Screen(width, height, scale);
		this.player = player;
		projector = new Projector(screen, player);
		this.map = map;
		this.startX = sX;
		this.startY = sY;
		this.endX = eX;
		this.endY = eY;
		buffer = new BufferedImage(projector.pW, projector.pH, BufferedImage.TYPE_INT_RGB);
		g2d = buffer.createGraphics();
		g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
	}

	void start() {
		timer.restart();
		this.setFocusable(true);
		this.requestFocus();
	}

	void stop() {
		timer.stop();
		this.setFocusable(false);
	}

	void drawLine(int x1, int y1, int x2, int y2, Color color) {
		GeneralPath shape = new GeneralPath();
		shape.moveTo(x1, y1);
		shape.lineTo(x2, y2);
		g2d.setColor(color);
		g2d.draw(shape);
	}

	void rayCasting() {
		double rayAngle = player.viewAng - player.hFOV;
		for (int i = 0; i < projector.pW; i++, rayAngle += projector.incrementAngle) {
			double dx = Math.cos(Math.toRadians(rayAngle)) / precision,
					dy = Math.sin(Math.toRadians(rayAngle)) / precision;
			double rayX = player.x, rayY = player.y;
			do {
				rayX += dx;
				rayY += dy;
			} while (map[(int) (rayY)][(int) (rayX)] != 1);
			double distance = Math.sqrt(Math.pow(player.x - rayX, 2) + Math.pow(player.y - rayY, 2));
			if (!player.fishEyeOn)
				distance *= Math.cos(Math.toRadians(player.viewAng - rayAngle));
			int wallHeight = (int) (projector.hpH / distance);
			drawLine(i, 0, i, projector.hpH - wallHeight, ceilingColor);
			drawLine(i, projector.hpH - wallHeight, i, projector.hpH + wallHeight, wallColor);
			drawLine(i, projector.hpH + wallHeight, i, projector.pH, floorColor);
		}
		drawStats();
		g2d.setColor(Color.black);
		repaint();
	}

	void drawStats() {
		g2d.setColor(Color.white);
		g2d.setFont(new Font("Monospaced", Font.PLAIN, 20 / screen.scale));
		String text = String.format("Player coordinates: (%.2f, %.2f) \nPlayer view angle: %d°", (float) player.x,
				(float) player.y, (int) player.viewAng);
		int x = 10 / screen.scale;
		int y = 20 / screen.scale;
		int lineHeight = 20 / screen.scale;
		for (String line : text.split("\n")) {
			g2d.drawString(line, x, y);
			y += lineHeight;
		}
		double distance = Math.sqrt(Math.pow(player.x - endX, 2) + Math.pow(player.y - endY, 2));
		String distanceText = String.format("Distance to end: %.2f units", distance);
		g2d.drawString(distanceText, x, y);

		int miniMapSize = projector.pW / 4;
		int tileSize = miniMapSize / Math.max(map.length, map[0].length);
		int playerTileX = (int) (player.x * tileSize);
		int playerTileY = (int) (player.y * tileSize);
		int miniMapX = projector.pW - miniMapSize;
		int miniMapY = 0;
		g2d.setColor(Color.white);
		g2d.fillRect(miniMapX, miniMapY, miniMapSize, miniMapSize);
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				if (map[i][j] == 1) {
					g2d.setColor(Color.black);
					g2d.fillRect(miniMapX + j * tileSize, miniMapY + i * tileSize, tileSize, tileSize);
				}
			}
		}
		g2d.setColor(Color.blue);
		g2d.fillRect(miniMapX + startX * tileSize, miniMapY + startY * tileSize, tileSize, tileSize);
		g2d.setColor(Color.pink);
		g2d.fillRect(miniMapX + endX * tileSize, miniMapY + endY * tileSize, tileSize, tileSize);
		g2d.setColor(Color.green);
		g2d.fillRect(miniMapX + playerTileX, miniMapY + playerTileY, tileSize, tileSize);
		g2d.setColor(Color.red);
		int lineLength = tileSize / 2;
		double radianAngle = Math.toRadians(player.viewAng);
		int lineX = (int) (miniMapX + playerTileX + tileSize / 2 + Math.cos(radianAngle) * lineLength);
		int lineY = (int) (miniMapY + playerTileY + tileSize / 2 + Math.sin(radianAngle) * lineLength);

		g2d.drawLine(miniMapX + playerTileX + tileSize / 2, miniMapY + playerTileY + tileSize / 2, lineX, lineY);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.transform(screen.at);
		if (buffer != null)
			g2d.drawImage(buffer, 0, 0, null);

	}

	private boolean ePressed = false;
	private boolean qPressed = false;
	private boolean wPressed = false;
	private boolean sPressed = false;
	private boolean aPressed = false;
	private boolean dPressed = false;
	KeyListener controls = new KeyListener() {
		@Override
		public void keyTyped(KeyEvent e) {
			if (e.getKeyChar() == 'p') {
				Setup.this.stop();
				int result = JOptionPane.showConfirmDialog(null,
						"Game is paused, click YES to continue\nor NO to return to Maze Generator&Solver", "Pause",
						JOptionPane.YES_OPTION);
				if (result == JOptionPane.YES_OPTION)
					Setup.this.start();
				else
					Setup.this.reference.dispose();
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			switch (keyCode) {
			case KeyEvent.VK_W:
				wPressed = true;
				break;

			case KeyEvent.VK_S:
				sPressed = true;
				break;

			case KeyEvent.VK_Q:
				qPressed = true;
				break;

			case KeyEvent.VK_E:
				ePressed = true;
				break;

			case KeyEvent.VK_A:
				aPressed = true;
				break;

			case KeyEvent.VK_D:
				dPressed = true;
				break;

			default:
			}
			if (wPressed) {
				move(0);
			}
			if (qPressed) {
				turn(true);
			}
			if (sPressed) {
				move(2);
			}
			if (aPressed) {
				move(3);
			}
			if (dPressed) {
				move(1);
			}
			if (ePressed) {
				turn(false);
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			int keyCode = e.getKeyCode();
			switch (keyCode) {
			case KeyEvent.VK_W:
				wPressed = false;
				break;

			case KeyEvent.VK_S:
				sPressed = false;
				break;

			case KeyEvent.VK_Q:
				qPressed = false;
				break;

			case KeyEvent.VK_E:
				ePressed = false;
				break;
			case KeyEvent.VK_A:
				aPressed = false;
				break;

			case KeyEvent.VK_D:
				dPressed = false;
				break;
			default:
			}
		}

	};

	void move(int direction) {
		double newX, newY;
		switch (direction) {
		case 0:
			newX = player.x + player.playerSpeed * Math.cos(Math.toRadians(player.viewAng));
			newY = player.y + player.playerSpeed * Math.sin(Math.toRadians(player.viewAng));
			break;
		case 1:
			newX = player.x + player.playerSpeed * Math.cos(Math.toRadians(player.viewAng + 90));
			newY = player.y + player.playerSpeed * Math.sin(Math.toRadians(player.viewAng + 90));
			break;
		case 2:
			newX = player.x - player.playerSpeed * Math.cos(Math.toRadians(player.viewAng));
			newY = player.y - player.playerSpeed * Math.sin(Math.toRadians(player.viewAng));
			break;
		case 3:
			newX = player.x - player.playerSpeed * Math.cos(Math.toRadians(player.viewAng + 90));
			newY = player.y - player.playerSpeed * Math.sin(Math.toRadians(player.viewAng + 90));
			break;
		default:
			return;
		}
		if (map[(int) newY][(int) newX] == 0) {
			player.x = newX;
			player.y = newY;
			if ((int) player.y == endY && (int) player.x == endX) {
				int result = JOptionPane.showConfirmDialog(null,
						"You reached the goal tile, press YES to restart\noe NO to return to the maze generator and solver",
						"End", JOptionPane.YES_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					player.x = startX;
					player.y = startY;
					Setup.this.start();
				} else
					Setup.this.reference.dispose();
			}
		}
	}

	void turn(boolean left) {
		player.viewAng += (left) ? -player.rotationSpeed : player.rotationSpeed;
		player.viewAng = (((player.viewAng % 360) + 360) % 360);
	}
}
