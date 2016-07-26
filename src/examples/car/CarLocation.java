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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CarLocation {

	private double x;
	private double y;
	private float angle;
	private final List<Antenna> antennas = new ArrayList<>();

	private final static double startX = 400;
	private final static double startY = 460;

	public CarLocation() {
		this(startX, startY, 0);
	}

	public CarLocation(double x, double y, float angle) {
		this.x = x;
		this.y = y;
		this.angle = angle;

		this.antennas.add(new Antenna(this, 0));
		for (int i = 1; i < 9; i++) {
			this.antennas.add(new Antenna(this, i * 9));
			this.antennas.add(new Antenna(this, -i * 9));
		}
	}

	public List<Antenna> getAntennas() {
		return antennas;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	private static final double MAX_CAR_SPEED = 10;
	private double currentSpeed = 4;

	public void tick(boolean rightClicked, boolean leftClicked, double gasPercentage /*0 = no has, 0.5 = same speed , 1 = full gas*/) {

		if (gasPercentage < 0 || gasPercentage > 1)
			throw new IllegalArgumentException();

		if (leftClicked) {
			this.angle -= 3.5;
		}
		if (rightClicked) {
			this.angle += 3.5;
		}

		double gasChange = (gasPercentage - 0.5) * 0.3D;
		this.currentSpeed += gasChange;

		if (this.currentSpeed < 4) {
			this.currentSpeed = 4;
		}

		if (this.currentSpeed > MAX_CAR_SPEED)
			this.currentSpeed = MAX_CAR_SPEED;

		// update the x and y using angle and speed
		double dx = this.currentSpeed * Math.cos(Math.toRadians(this.angle));
		double dy = this.currentSpeed * Math.sin(Math.toRadians(this.angle));

		this.x += dx;
		this.y += dy;

		// System.out.println("X " + x);
		// System.out.println("Y " + y);
	}

	public boolean isAlive(BufferedImage background) {
		return background.getRGB((int) this.x, (int) this.y) == Antenna.ROAD_COLOR.getRGB() || background.getRGB((int) this.x, (int) this.y) == Color.RED.getRGB();
	}

	public boolean isOnFinish(BufferedImage background) {
		return background.getRGB((int) this.x, (int) this.y) == Color.RED.getRGB();
	}

	public double getCurrentSpeed() {
		return this.currentSpeed / MAX_CAR_SPEED; // scale
	}
}
