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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Car extends JFrame implements Runnable {

	public static void main(String[] args) {
		new Car().run();
	}

	private static final long serialVersionUID = 1L;
	private BufferedImage background;
	{
		try {
			this.background = ImageIO.read(this.getClass().getResource("route.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public BufferedImage getBackgroundImage() {
		return background;
	}

	@Override
	public void run() {
		System.out.println("Enter the preview interval please...");

		Scanner input = new Scanner(System.in);
		while (input.hasNextLine()) {
			String nextLine = input.nextLine();

			input.close();
			int interval = -1;

			try {
				interval = Integer.parseInt(nextLine);
			} catch (NumberFormatException e) {
				System.out.println(nextLine + " is not a number.");
				System.exit(1);
			}

			if (interval == -1)
				throw new AssertionError();

			new MythanTraining(this, getBackgroundImage(), interval).start();
		}
		input.close();
	}
}
