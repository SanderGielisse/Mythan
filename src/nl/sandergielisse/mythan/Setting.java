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

public enum Setting {

	/**
	 * There is a chance that a gene which is disabled in one of the parents is disabled in the child.
	 */
	GENE_DISABLE_CHANCE(0.75),

	/**
	 * There is a chance that a gene is mutated when copied.
	 */
	MUTATION_WEIGHT_CHANCE(0.80),

	/**
	 * There is a chance that a weight mutation get's an entire new random weight value.
	 */
	MUTATION_WEIGHT_RANDOM_CHANCE(0.10),

	/**
	 * Weight will be disturbed by a random value between -0.25 and 0.25.
	 */
	MUTATION_WEIGHT_MAX_DISTURBANCE(0.25),

	/**
	 * The chance a new connection is made as result of a mutation.
	 */
	MUTATION_NEW_CONNECTION_CHANCE(0.05),

	/**
	 * The chance a new node is made as result of a mutation.
	 */
	MUTATION_NEW_NODE_CHANCE(0.03),

	/**
	 * Constant in distance formula.
	 */
	DISTANCE_EXCESS_WEIGHT(1.0),

	/**
	 * Constant in distance formula.
	 */
	DISTANCE_DISJOINT_WEIGHT(1.0),

	/**
	 * Constant in distance formula.
	 */
	DISTANCE_WEIGHTS_WEIGHT(0.4),

	/**
	 * The distance allowed between two genomes in the same species.
	 */
	SPECIES_COMPATIBILTY_DISTANCE(0.8),

	/**
	 * The percentage of genomes that will eliminated for the new generation
	 */
	GENERATION_ELIMINATION_PERCENTAGE(0.90),

	/**
	 * The chance a gene will be bred, instead of directly copied.
	 */
	BREED_CROSS_CHANCE(0.75),

	MUTATION_WEIGHT_CHANCE_RANDOM_RANGE(5.0),;

	private final double defaultSetting;

	private Setting(double defaultSetting) {
		this.defaultSetting = defaultSetting;
	}

	public double getDefaultValue() {
		return defaultSetting;
	}
}
