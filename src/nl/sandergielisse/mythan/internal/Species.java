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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.sandergielisse.mythan.Setting;
import nl.sandergielisse.mythan.internal.genes.Genome;
import nl.sandergielisse.mythan.internal.genes.Genome.GenomeSorter;

/**
 * A species is a collection of genomes which are genetically close.
 * 
 * The fitness of a genome is divided by the amount of genomes in a species.
 * This way a species can't get too big because then it's fitness gets too small.
 */
public class Species {

	private static int speciesCount = 0;

	private final int id = speciesCount++;
	private Genome representative;
	private final Set<Genome> members = new HashSet<>();
	private double highestFitness = 0;
	private int failedGenerations = 0;

	public Species(Genome representative) {
		this.representative = representative;
		representative.setSpecies(this);
	}

	public int getId() {
		return id;
	}

	public double getHighestFitness() {
		return highestFitness;
	}

	public void setHighestFitness(double highestFitness) {
		this.highestFitness = highestFitness;
		this.failedGenerations = 0;
	}

	public int getFailedGenerations() {
		return failedGenerations;
	}

	public void setFailedGenerations(int failedGenerations) {
		this.failedGenerations = failedGenerations;
	}

	public Genome getRepresentative() {
		return representative;
	}

	public void setRepresentative(Genome representative) {
		this.representative = representative;
	}

	public boolean isCompatible(Genome genome) {
		return Genome.distance(this.representative, genome) <= genome.getCore().getSetting(Setting.SPECIES_COMPATIBILTY_DISTANCE);
	}

	public double getAverageFitness() {
		double total = 0;
		double counter = 0;
		for (Genome g : this.members) {
			total += g.getFitness();
			counter++;
		}
		return total / counter;
	}

	/**
	 * Returns list in decrementing order, so the best come first.
	 */
	public List<Genome> getBestPerforming() {
		List<Genome> bestPerforming = new ArrayList<>();

		for (Genome genome : this.members) {
			bestPerforming.add(genome);
		}
		Collections.sort(bestPerforming, new GenomeSorter());
		return bestPerforming;
	}

	public void remove(Genome g) {
		this.members.remove(g);
	}

	public Set<Genome> getMembers() {
		return members;
	}

	public void update() {
		this.setRepresentative(Random.random(this.getMembers()));
	}
}
