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
	private final int interval;

	public MythanTraining(Car car, BufferedImage background, int interval) {
		this.background = background;
		this.car = car;
		this.interval = interval;

		car.add(this.board = new Frame(MythanTraining.this.car));
		car.pack();
		car.setLocationRelativeTo(null);
		car.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		car.setVisible(true);
		car.setTitle("Mythan Driving Car Example (AI Powered)");
		car.setResizable(false);
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

		Mythan mythan = Mythan.newInstance(new CarLocation().getAntennas().size() + 1, 2, new CustomizedSigmoidActivation(), new FitnessCalculator() {

			@Override
			public double getFitness(Network network) {

				CarLocation carLocation = new CarLocation();

				long ticksLived = 0;

				while (carLocation.isAlive(background) && !carLocation.isOnFinish(background)) {
					boolean rightClicked = false;
					boolean leftClicked = false;

					double[] inputs = new double[carLocation.getAntennas().size() + 1];

					for (int i = 0; i < carLocation.getAntennas().size(); i++) {
						Antenna ant = carLocation.getAntennas().get(i);
						double len = ant.getFreeDistance(background);
						if (len > 200)
							len = 200;
						inputs[i] = len / 200D;
					}
					inputs[inputs.length - 1] = carLocation.getCurrentSpeed();

					double[] ans = network.calculate(inputs);
					double output = ans[0];
					double speed = ans[1];

					if (output >= 0 && output <= 0.3)
						leftClicked = true;

					if (output >= 0.7 && output <= 1)
						rightClicked = true;

					carLocation.tick(rightClicked, leftClicked, speed);
					ticksLived++;
				}

				/**
				 * First 50 fitness is for actually making it (0-100%), rest is for speed.
				 */
				double fitness = 0;
				double secondsLived = ticksLived / 30D; // 30 ticks per second

				if (secondsLived > 45)
					throw new RuntimeException();

				if (carLocation.isOnFinish(background)) {
					// we finished
					fitness = (45 - secondsLived);
				}

				return fitness * fitness;
			}

			private int generation = 1;

			@Override
			public void generationFinished(Network bestPerforming) {
				this.generation++;

				if (this.getFitness(bestPerforming) != bestPerforming.getFitness())
					throw new AssertionError();

				if (interval != 0 && !(this.generation % interval == 0)) {
					return;
				}

				setTitle("Mythan Driving Car Example (AI Powered) - Generation " + this.generation + " - Fitness " + bestPerforming.getFitness());

				board.setLocation(new CarLocation());
				while (true) {
					try {
						Thread.sleep((long) (1000D / 30D)); // 30 FPS

						boolean rightClicked = false;
						boolean leftClicked = false;

						double[] inputs = new double[board.getCarLocation().getAntennas().size() + 1];
						for (int i = 0; i < board.getCarLocation().getAntennas().size(); i++) {
							Antenna ant = board.getCarLocation().getAntennas().get(i);
							double len = ant.getFreeDistance(background);
							if (len > 200)
								len = 200;
							inputs[i] = len / 200D;
						}
						inputs[inputs.length - 1] = board.getCarLocation().getCurrentSpeed();

						double[] ans = bestPerforming.calculate(inputs);
						double output = ans[0];
						double speed = ans[1];

						if (output >= 0 && output <= 0.3)
							leftClicked = true;

						if (output >= 0.7 && output <= 1)
							rightClicked = true;

						board.getCarLocation().tick(rightClicked, leftClicked, speed);

						if (!board.getCarLocation().isAlive(board.getBackgroundImage())) {
							// restart
							board.setLocation(new CarLocation());
							continue;
						}

						if (board.getCarLocation().isOnFinish(board.getBackgroundImage())) {
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

		mythan.setSetting(Setting.SPECIES_COMPATIBILTY_DISTANCE, 0.8); // the bigger the less species
		mythan.setSetting(Setting.MUTATION_WEIGHT_CHANCE_RANDOM_RANGE, 3);

		mythan.setSetting(Setting.GENERATION_ELIMINATION_PERCENTAGE, 0.85);
		mythan.setSetting(Setting.BREED_CROSS_CHANCE, 0.75);

		mythan.trainToFitness(1000, Double.MAX_VALUE);
	}
}
