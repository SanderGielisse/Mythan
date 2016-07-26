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
package nl.sandergielisse.mythan.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.sandergielisse.mythan.Setting;
import nl.sandergielisse.mythan.internal.genes.Gene;
import nl.sandergielisse.mythan.internal.genes.Genome;

public class PopulationManager {

	private int currentGeneration = 1;
	private final EvolutionCore evolutionCore;
	private final Population currentPopulation;
	private int populationSize = 500;
	private Genome latestFitness;

	public PopulationManager(EvolutionCore evolutionCore) {
		this.evolutionCore = evolutionCore;
		this.currentPopulation = new Population(this.evolutionCore);
	}

	public Population getPopulation() {
		return currentPopulation;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public EvolutionCore getCore() {
		return evolutionCore;
	}

	public List<Species> getSpecies() {
		return this.currentPopulation.getSpecies();
	}

	public Genome getLatestFitness() {
		return latestFitness;
	}

	public void newGeneration() {
		this.currentGeneration++;

		// start with calling getBestPerforming() for every species so that calculateFitness() is executed
		Map<Species, List<Genome>> bestPerforming = new HashMap<>();
		for (Species sp : this.getSpecies()) {
			bestPerforming.put(sp, sp.getBestPerforming());
		}

		// calculate the total average
		double sum = 0;
		for (Species sp : this.getSpecies()) {
			sum += sp.getAverageFitness();
		}

		HashMap<Species, Genome> vips = new HashMap<>();
		Iterator<Species> it = this.getSpecies().iterator();
		while (it.hasNext()) {
			Species sp = it.next();

			/**
			 * We start by eliminating the worst performing genome's from every species.
			 */
			List<Genome> best = bestPerforming.get(sp);
			if (best == null)
				throw new AssertionError();

			double remove = Math.ceil(best.size() * this.getCore().getSetting(Setting.GENERATION_ELIMINATION_PERCENTAGE));
			int start = (int) (Math.floor(best.size() - remove) + 1);

			for (int i = start; i < best.size(); i++) {
				Genome bad = best.get(i);
				sp.remove(bad);
			}

			/**
			 * Remove all species who's fitness has not reached the max for 15 generations.
			 */
			sp.setFailedGenerations(sp.getFailedGenerations() + 1);

			if (sp.getFailedGenerations() > 15) {
				System.out.println("Species was removed, because it failed for 15 generations.");
				it.remove();
				continue;
			}

			/**
			 * Remove all species which don't get any breeding spots in the next generation.
			 */

			double totalSize = this.getPopulationSize();
			double breedsAllowed = Math.floor(sp.getAverageFitness() / sum * totalSize) - 1.0;

			if (breedsAllowed < 1) {
				// System.out.println("Species was removed, breeds allowed < 1.");
				it.remove();
				continue;
			}

			/**
			 * Copy the best of every species directly into the next generation.
			 */
			Genome bestOfSpecies = best.get(0);
			// vips.put(sp, bestOfSpecies);
		}

		{
			int size = 0;
			for (Species sp : this.getSpecies()) {
				size += sp.getMembers().size();
			}
			System.out.println("Building generation " + this.currentGeneration + "... Now " + this.getSpecies().size() + " species active (with a total size of " + size + ").");
		}

		if (this.getSpecies().isEmpty()) {
			throw new RuntimeException("All species died");
		}

		int populationSize = 0;

		Map<Species, Set<Genome>> oldMembers = new HashMap<>();
		for (Species sp : this.getSpecies()) {
			oldMembers.put(sp, new HashSet<>(sp.getMembers()));

			sp.getMembers().clear();

			Genome vip = vips.get(sp);
			if (vip != null) {
				sp.getMembers().add(vip);
				populationSize++;
			}
		}

		/**
		 * Fill the population with new children.
		 */
		while (populationSize < this.populationSize) {
			Species randomSpecies = Random.random(this.getSpecies());
			Set<Genome> oldMems = oldMembers.get(randomSpecies);

			if (oldMems != null) {
				if (Random.success(this.getCore().getSetting(Setting.BREED_CROSS_CHANCE))) {
					// cross
					Genome father = Random.random(oldMems);
					Genome mother = Random.random(oldMems);

					Genome.crossAndAdd(father, mother);
				} else {
					// don't cross just copy
					Genome g = Random.random(oldMems).clone();
					g.mutate();
					randomSpecies.getMembers().add(g);
				}
				populationSize++;
			}
		}

		Iterator<Species> its = this.getSpecies().iterator();
		while (its.hasNext()) {
			Species sp = its.next();
			if (sp.getMembers().isEmpty()) {
				its.remove();
			}
		}

		for (Species sp : this.getSpecies()) {
			sp.update();
		}

		/**
		 * Display how the new population performed.
		 */
		this.latestFitness = this.currentPopulation.getBestPerforming();

		System.out.println("Best performing genome [" + this.latestFitness.getId() + "] had fitness of " + this.latestFitness.getFitness() + " and was part of species " + this.latestFitness.getSpecies().getId() + " which has " + this.latestFitness.getSpecies().getMembers().size() + " members");
		System.out.println(this.latestFitness.toString());
	}

	public void initialize(int populationSize) {
		this.populationSize = populationSize;

		if (this.currentGeneration != 1)
			throw new UnsupportedOperationException("The initialize() method should only be called for the first generation");

		Genome init = this.initial();

		for (int i = 0; i < this.getPopulationSize(); i++) {
			// new genome, choose random weights
			Genome genome = init.clone();
			for (Gene gene : genome.getGenes()) { // genes are cloned as well
				double dist = this.getCore().getSetting(Setting.MUTATION_WEIGHT_CHANCE_RANDOM_RANGE);
				gene.setWeight(Random.random(-dist, dist));
			}
			// System.out.println("GENOME " + genome.toString());
			this.getCore().getPopulationManager().getPopulation().addGenome(genome);
		}
	}

	private Genome initial() {
		Integer[] inputs = new Integer[this.getCore().getInputSize()];
		for (int i = 0; i < inputs.length; i++)
			inputs[i] = i + 1;

		Integer[] outputs = new Integer[this.getCore().getOutputSize()];
		for (int i = 0; i < outputs.length; i++)
			outputs[i] = inputs.length + i + 1;

		double dist = this.getCore().getSetting(Setting.MUTATION_WEIGHT_CHANCE_RANDOM_RANGE);
		Genome gen = new Genome(this.getCore(), null, inputs, outputs);
		for (int in = 1; in <= this.getCore().getInputSize(); in++) {
			for (int out = 1; out <= this.getCore().getOutputSize(); out++) {
				gen.addGene(new Gene(this.getCore().getNextInnovationNumber(), in, this.getCore().getInputSize() + out, Random.random(-dist, dist), true), null, null);
			}
		}
		return gen;
	}

	public int getGeneration() {
		return this.currentGeneration;
	}
}
