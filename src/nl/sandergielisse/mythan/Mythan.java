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
package nl.sandergielisse.mythan;

import nl.sandergielisse.mythan.internal.EvolutionCore;

public interface Mythan {

	public static Mythan newInstance(int inputSize, int outputSize, ActivationFunction function, FitnessCalculator calculator) {
		return new EvolutionCore(inputSize, outputSize, function, calculator);
	}

	public int getInputSize();

	public int getOutputSize();

	public double getSetting(Setting setting);

	public void setSetting(Setting setting, double value);

	public void trainToFitness(int populationSize, double targetFitness);

	public FitnessCalculator getFitnessCalculator();

	public ActivationFunction getActivationFunction();
}
