/**
 * Copyright 2016 Alexander Gielisse
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package examples.car;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Frame extends JPanel {

	private final Car carObject;

	public Frame(Car car) {
		this.carObject = car;

		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_LEFT)
					leftPressed = true;
				if (key == KeyEvent.VK_RIGHT)
					rightPressed = true;
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_LEFT)
					leftPressed = false;
				if (key == KeyEvent.VK_RIGHT)
					rightPressed = false;
			}
		});
		setBackground(Color.PINK);
		setFocusable(true);
	}

	private static final long serialVersionUID = 1L;
	private double startX = 400;
	private double startY = 460;
	private final CarLocation location = new CarLocation(startX, startY, 0);

	private BufferedImage car;
	{
		try {
			this.car = ImageIO.read(this.getClass().getResource("car.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean leftPressed = false;
	private boolean rightPressed = false;

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawStar(g);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(800, 800);
	}

	private void drawStar(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		g2d.drawImage(this.getBackgroundImage(), 0, 0, null);

		AffineTransform transform = new AffineTransform();
		transform.rotate(Math.toRadians(this.location.getAngle()), this.location.getX(), this.location.getY());
		AffineTransform old = g2d.getTransform();
		g2d.transform(transform);

		g2d.drawImage(this.car, (int) this.location.getX() - this.car.getWidth(null) / 2, (int) this.location.getY() - this.car.getHeight(null) / 2, null);
		g2d.setTransform(old);
	}

	public double getStartX() {
		return startX;
	}

	public void setStartX(double startX) {
		this.startX = startX;
	}

	public double getStartY() {
		return startY;
	}

	public void setStartY(double startY) {
		this.startY = startY;
	}

	public BufferedImage getBackgroundImage() {
		return this.carObject.getBackgroundImage();
	}

	public BufferedImage getCar() {
		return car;
	}

	public void setCar(BufferedImage car) {
		this.car = car;
	}

	public boolean isLeftPressed() {
		return leftPressed;
	}

	public void setLeftPressed(boolean leftPressed) {
		this.leftPressed = leftPressed;
	}

	public boolean isRightPressed() {
		return rightPressed;
	}

	public void setRightPressed(boolean rightPressed) {
		this.rightPressed = rightPressed;
	}

	public CarLocation getCarLocation() {
		return location;
	}
}