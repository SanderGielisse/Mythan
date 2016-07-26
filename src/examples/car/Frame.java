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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Frame extends JPanel {

	private final Car carObject;

	public Frame(Car car) {
		this.carObject = car;
		setBackground(Color.BLACK);
		setFocusable(true);
	}

	private static final long serialVersionUID = 1L;
	private CarLocation location = new CarLocation();

	public void setLocation(CarLocation location) {
		this.location = location;
	}

	private BufferedImage car;
	{
		try {
			this.car = ImageIO.read(this.getClass().getResource("car.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(800, 800);
	}

	private void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		g2d.drawImage(this.getBackgroundImage(), 0, 0, null);

		AffineTransform transform = new AffineTransform();
		transform.rotate(Math.toRadians(this.location.getAngle()), this.location.getX(), this.location.getY());
		AffineTransform old = g2d.getTransform();
		g2d.transform(transform);

		g2d.drawImage(this.car, (int) this.location.getX() - this.car.getWidth(null) / 2, (int) this.location.getY() - this.car.getHeight(null) / 2, null);
		g2d.setTransform(old);

		for (Antenna ant : this.getCarLocation().getAntennas()) {
			ant.draw(this.getBackgroundImage(), g2d);
		}

		g2d.setColor(Color.WHITE);

		g2d.setFont(g2d.getFont().deriveFont(20F));
		g2d.drawString("Speed " + round(this.getCarLocation().getCurrentSpeed() * 100, 3) + "%", 10, 25);
	}

	public static double round(double value, int places) {
		return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
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

	public CarLocation getCarLocation() {
		return location;
	}
}