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
package examples;

import nl.sandergielisse.mythan.CustomizedSigmoidActivation;
import nl.sandergielisse.mythan.FitnessCalculator;
import nl.sandergielisse.mythan.Mythan;
import nl.sandergielisse.mythan.Network;
import nl.sandergielisse.mythan.Setting;

public class XOR implements Runnable {

	public static void main(String[] args) {
		new XOR().run();
	}

	private final double[][] inputs = new double[4][];
	{
		// first element is bias
		inputs[0] = new double[] { 1, 0, 0 };
		inputs[1] = new double[] { 1, 1, 1 };
		inputs[2] = new double[] { 1, 0, 1 };
		inputs[3] = new double[] { 1, 1, 0 };
	}

	private final double[][] outputs = new double[4][];
	{
		outputs[0] = new double[] { 0 };
		outputs[1] = new double[] { 0 };
		outputs[2] = new double[] { 1 };
		outputs[3] = new double[] { 1 };
	}

	@Override
	public void run() {

		int inputSize = this.inputs[0].length;
		int outputSize = this.outputs[0].length;

		Mythan instance = Mythan.newInstance(inputSize, outputSize, new CustomizedSigmoidActivation(), new FitnessCalculator() {

			@Override
			public double getFitness(Network network) {

				double off = 0;
				for (int i = 0; i < 4; i++) {
					double[] in = inputs[i];

					double expectedOut = outputs[i][0];
					double actualOut = network.calculate(in)[0];

					off += Math.abs(actualOut - expectedOut);
				}
				// subtract from 4 and square to increase fitness proportionally
				double fitness = 4 - off;

				if (fitness < 0)
					fitness = 0;

				return fitness * fitness;
			}
		});

		instance.setSetting(Setting.GENE_DISABLE_CHANCE, 0.75);
		instance.setSetting(Setting.MUTATION_WEIGHT_CHANCE, 0.7);
		instance.setSetting(Setting.MUTATION_WEIGHT_RANDOM_CHANCE, 0.10);
		instance.setSetting(Setting.MUTATION_WEIGHT_MAX_DISTURBANCE, 0.1);

		instance.setSetting(Setting.MUTATION_NEW_CONNECTION_CHANCE, 0.3);
		instance.setSetting(Setting.MUTATION_NEW_NODE_CHANCE, 0.003);

		instance.setSetting(Setting.DISTANCE_EXCESS_WEIGHT, 1.0);
		instance.setSetting(Setting.DISTANCE_DISJOINT_WEIGHT, 1.0);
		instance.setSetting(Setting.DISTANCE_WEIGHTS_WEIGHT, 0.4);

		instance.setSetting(Setting.SPECIES_COMPATIBILTY_DISTANCE, 1.25); // the bigger the less species
		instance.setSetting(Setting.MUTATION_WEIGHT_CHANCE_RANDOM_RANGE, 3); // -2.0 - 2.0

		instance.setSetting(Setting.GENERATION_ELIMINATION_PERCENTAGE, 0.85);
		instance.setSetting(Setting.BREED_CROSS_CHANCE, 0.75);

		/**
		 * Train to fitness of 15.5, best is 16 (4 * 4)
		 * sqrt(15.5) = 3.94 so 4 - 3.94 = 0.06 total off
		 * 0.06 off from total of 4 gives 1.5% error
		 */
		instance.trainToFitness(1000, 15.5);
	}
}
