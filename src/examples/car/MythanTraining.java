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

import javax.swing.JFrame;

import nl.sandergielisse.mythan.CustomizedSigmoidActivation;
import nl.sandergielisse.mythan.FitnessCalculator;
import nl.sandergielisse.mythan.Mythan;
import nl.sandergielisse.mythan.Network;
import nl.sandergielisse.mythan.Setting;

public class MythanTraining {

	private final BufferedImage background;
	private final Car car;
	private Frame board;

	public MythanTraining(Car car, BufferedImage background) {
		this.background = background;
		this.car = car;

		car.add(this.board = new Frame(MythanTraining.this.car));
		car.pack();
		car.setLocationRelativeTo(null);
		car.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		car.setVisible(true);
		car.setTitle("Mythan Driving Car Example (AI Powered)");
	}

	public void setTitle(String title) {
		this.car.setTitle(title);
	}

	public void start() {
		/**
		 * The inputs are the X antenna's. When the antenna hits something,
		 * the input is 0, or 1 for an antenna not hitting something.
		 * 
		 * There are 3 output types.
		 * 
		 * 0.0 - 0.3 = steer left
		 * 0.3 - 0.7 = don't steer
		 * 0.7 - 1.0 = steer right
		 */

		Mythan mythan = Mythan.newInstance(new CarLocation().getAntennas().size(), 1, new CustomizedSigmoidActivation(), new FitnessCalculator() {

			@Override
			public double getFitness(Network network) {
				CarLocation carLocation = new CarLocation();

				long ticksLived = 0;

				do {
					boolean rightClicked = false;
					boolean leftClicked = false;

					double[] inputs = new double[carLocation.getAntennas().size()];
					for (int i = 0; i < carLocation.getAntennas().size(); i++) {
						inputs[i] = (carLocation.getAntennas().get(i).isOnRoad(background) ? 1.0 : 0.0);
					}

					double output = network.calculate(inputs)[0];

					if (output >= 0 && output <= 0.3)
						leftClicked = true;

					if (output >= 0.7 && output <= 1)
						rightClicked = true;

					carLocation.tick(rightClicked, leftClicked);
					ticksLived++;
				} while (carLocation.isAlive(background) && !carLocation.isOnFinish(background));

				// worst case 45 seconds

				double secondsLived = ticksLived / 30D; // 30 ticks per second
				double fitness = 45 - secondsLived;
				if (!carLocation.isAlive(background)) {
					fitness = 0;
				}

				return fitness * fitness;
			}

			private int generation = 1;

			@Override
			public void generationFinished(Network bestPerforming) {
				this.generation++;

				//if (this.generation++ < 25)
				//	return;

				setTitle("Mythan Driving Car Example (AI Powered) - Generation " + this.generation + " - Fitness " + bestPerforming.getFitness());

				board.setLocation(new CarLocation());
				while (true) {
					try {
						Thread.sleep((long) (1000D / 30D)); // 30 FPS

						boolean rightClicked = false;
						boolean leftClicked = false;

						double[] inputs = new double[board.getCarLocation().getAntennas().size()];
						for (int i = 0; i < board.getCarLocation().getAntennas().size(); i++) {
							inputs[i] = (board.getCarLocation().getAntennas().get(i).isOnRoad(background) ? 1.0 : 0.0);
						}

						double output = bestPerforming.calculate(inputs)[0];

						if (output >= 0 && output <= 0.3)
							leftClicked = true;

						if (output >= 0.7 && output <= 1)
							rightClicked = true;

						board.getCarLocation().tick(rightClicked, leftClicked);

						if (!board.getCarLocation().isAlive(board.getBackgroundImage()) || board.getCarLocation().isOnFinish(board.getBackgroundImage())) {
							break;
						}

						car.repaint();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		mythan.setSetting(Setting.GENE_DISABLE_CHANCE, 0.75);
		mythan.setSetting(Setting.MUTATION_WEIGHT_CHANCE, 0.7);
		mythan.setSetting(Setting.MUTATION_WEIGHT_RANDOM_CHANCE, 0.10);
		mythan.setSetting(Setting.MUTATION_WEIGHT_MAX_DISTURBANCE, 0.1);

		mythan.setSetting(Setting.MUTATION_NEW_CONNECTION_CHANCE, 0.03);
		mythan.setSetting(Setting.MUTATION_NEW_NODE_CHANCE, 0.05);

		mythan.setSetting(Setting.DISTANCE_EXCESS_WEIGHT, 1.0);
		mythan.setSetting(Setting.DISTANCE_DISJOINT_WEIGHT, 1.0);
		mythan.setSetting(Setting.DISTANCE_WEIGHTS_WEIGHT, 0.4);

		mythan.setSetting(Setting.SPECIES_COMPATIBILTY_DISTANCE, 0.75); // the bigger the less species
		mythan.setSetting(Setting.MUTATION_WEIGHT_CHANCE_RANDOM_RANGE, 3); // -2.0 - 2.0

		mythan.setSetting(Setting.GENERATION_ELIMINATION_PERCENTAGE, 0.85);
		mythan.setSetting(Setting.BREED_CROSS_CHANCE, 0.75);

		mythan.trainToFitness(1000, Double.MAX_VALUE);
	}
}
