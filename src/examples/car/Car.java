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

	public static final double CAR_SPEED = 4;
	private static final long serialVersionUID = 1L;
	private Frame board;

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
		System.out.println("What would you like to do? Use /play to play yourself or use /ai if you'd like Mythan to train.");

		Scanner input = new Scanner(System.in);
		while (input.hasNextLine()) {
			String nextLine = input.nextLine();

			/**
			 * User wants to play him/herself.
			 */
			if (nextLine.equalsIgnoreCase("/play")) {
				this.add(this.board = new Frame(Car.this));
				this.pack();
				this.setLocationRelativeTo(null);
				this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				this.setVisible(true);
				this.setTitle("Mythan Driving Car Example");

				new Thread(new Runnable() {

					@Override
					public void run() {
						while (true) {
							try {
								Thread.sleep((long) (1000D / 30D)); // 30 FPS

								board.getCarLocation().tick(board.isRightPressed(), board.isLeftPressed());

								if (!board.getCarLocation().isAlive(board.getBackgroundImage())) {
									System.exit(0);
								}

								repaint();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
			} else if (nextLine.equalsIgnoreCase("/ai")) {
				new MythanTraining(this, getBackgroundImage()).start();
			} else {
				System.out.println("Unknown command " + nextLine + "...");
				System.exit(0);
			}
		}
		input.close();
	}
}
