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

	public Antenna(CarLocation carLocation, float angle) {
		this.carLocation = carLocation;
		this.angle = angle;
	}

	public float getAngle() {
		return angle;
	}

	public CarLocation getCarLocation() {
		return carLocation;
	}

	public double getPreviewLength() {
		return 200;
	}

	public double getEndX(BufferedImage background) {
		double startX = this.getCarLocation().getX();
		double totalAngle = this.carLocation.getAngle() + this.angle;
		double dx = getFreeDistance(background) * Math.cos(Math.toRadians(totalAngle));
		return startX + dx;
	}

	public double getEndY(BufferedImage background) {
		double startY = this.getCarLocation().getY();
		double totalAngle = this.carLocation.getAngle() + this.angle;
		double dy = getFreeDistance(background) * Math.sin(Math.toRadians(totalAngle));
		return startY + dy;
	}

	private final static double STEP_SIZE = 3.0;
	public static final Color ROAD_COLOR = new Color(255, 174, 0);

	public double getFreeDistance(BufferedImage background) {
		double totalAngle = this.carLocation.getAngle() + this.angle;
		double startX = this.getCarLocation().getX();
		double startY = this.getCarLocation().getY();

		double dx = STEP_SIZE * Math.cos(Math.toRadians(totalAngle));
		double dy = STEP_SIZE * Math.sin(Math.toRadians(totalAngle));

		while (true) {
			startX += dx;
			startY += dy;

			if (background.getRGB((int) startX, (int) startY) != Antenna.ROAD_COLOR.getRGB() && background.getRGB((int) startX, (int) startY) != Color.RED.getRGB()) {
				// we're off the road

				double distX = this.getCarLocation().getX() - startX;
				double distY = this.getCarLocation().getY() - startY;

				return Math.sqrt(distX * distX + distY * distY);
			}
		}
	}

	public void draw(BufferedImage background, Graphics2D g2d) {
		g2d.setColor(Color.BLUE);

		g2d.drawLine((int) this.getCarLocation().getX(), (int) this.getCarLocation().getY(), (int) this.getEndX(background), (int) this.getEndY(background));
	}
}
