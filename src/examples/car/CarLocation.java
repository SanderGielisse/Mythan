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

	public CarLocation(double x, double y, float angle) {
		this.x = x;
		this.y = y;
		this.angle = angle;
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

	public void tick(boolean rightClicked, boolean leftClicked) {
		// update the x and y using angle and speed
		double dx = Car.CAR_SPEED * Math.cos(Math.toRadians(this.angle));
		double dy = Car.CAR_SPEED * Math.sin(Math.toRadians(this.angle));

		this.x += dx;
		this.y += dy;

		if (leftClicked) {
			this.angle -= 3.5;
		}
		if (rightClicked) {
			this.angle += 3.5;
		}
	}

	public boolean isAlive(BufferedImage background) {
		return background.getRGB((int) this.x, (int) this.y) != Color.BLACK.getRGB();
	}
}
