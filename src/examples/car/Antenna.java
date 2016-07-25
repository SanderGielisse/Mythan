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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Antenna {

	private final CarLocation carLocation;
	private final float angle;
	private final int length;

	public Antenna(CarLocation carLocation, float angle, int length) {

		if (length <= 0)
			throw new IllegalArgumentException("Length must be positive");

		this.carLocation = carLocation;
		this.angle = angle;
		this.length = length;
	}

	public float getAngle() {
		return angle;
	}

	public double getLength() {
		return length;
	}

	public CarLocation getCarLocation() {
		return carLocation;
	}

	public double getEndX() {
		double startX = this.getCarLocation().getX();
		double dx = this.getLength() * Math.cos(Math.toRadians(this.getAngle() + this.getCarLocation().getAngle()));
		return startX + dx;
	}

	public double getEndY() {
		double startY = this.getCarLocation().getY();
		double dy = this.getLength() * Math.sin(Math.toRadians(this.getAngle() + this.getCarLocation().getAngle()));
		return startY + dy;
	}

	public boolean isOnRoad(BufferedImage background) {
		int x = (int) this.getEndX();
		int y = (int) this.getEndY();

		if (x < 0 || y < 0 || x >= background.getWidth() || y >= background.getHeight())
			return false;

		return background.getRGB(x, y) != Color.BLACK.getRGB();
	}

	public void draw(BufferedImage background, Graphics2D g2d) {
		g2d.setColor(this.isOnRoad(background) ? Color.RED : Color.BLUE);
		g2d.drawLine((int) this.getCarLocation().getX(), (int) this.getCarLocation().getY(), (int) this.getEndX(), (int) this.getEndY());
	}
}
