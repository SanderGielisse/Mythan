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
package nl.sandergielisse.mythan.internal.genes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import nl.sandergielisse.mythan.BackTraceTask;
import nl.sandergielisse.mythan.Network;
import nl.sandergielisse.mythan.Setting;
import nl.sandergielisse.mythan.internal.ArrayUtils;
import nl.sandergielisse.mythan.internal.EvolutionCore;
import nl.sandergielisse.mythan.internal.Random;
import nl.sandergielisse.mythan.internal.Species;

public class Genome implements Cloneable, Network {

	private static int counter = 0;
	private final int id = counter++;

	public int getId() {
		return id;
	}

	/**
	 * The TreeMap will make sure the genes are always ordered by increasing innovation number.
	 */
	private Map<Integer, Gene> genes = new TreeMap<>();
	private final EvolutionCore core;

	private List<Integer> inputNodes = new ArrayList<>();
	private List<Integer> outputNodes = new ArrayList<>();

	private Species species;

	public Genome(EvolutionCore core, Species member, Integer[] inputNodes, Integer[] outputNodes) {
		this.core = core;
		this.species = member;

		for (int in : inputNodes)
			this.addInputNode(in);

		for (int out : outputNodes)
			this.addOutputNode(out);
	}

	public void setSpecies(Species sp) {
		if (this.fitness != -1)
			throw new UnsupportedOperationException("setSpecies() must be called before getFitness()");

		this.species = sp;
	}

	public Species getSpecies() {
		return species;
	}

	public Integer[] getInputs() {
		return this.inputNodes.toArray(new Integer[this.inputNodes.size()]);
	}

	public Integer[] getOutputs() {
		return this.outputNodes.toArray(new Integer[this.outputNodes.size()]);
	}

	/**
	 * Please note that the returned list is read-only.
	 */
	public List<Integer> getNodes(boolean includeInput, boolean includeHidden, boolean includeOutput) {
		List<Integer> ids = new ArrayList<>();

		for (int input : this.getAllNodes()) {
			if (this.isInputNode(input) && !includeInput)
				continue;
			if (this.isHiddenNode(input) && !includeHidden)
				continue;
			if (this.isOutputNode(input) && !includeOutput)
				continue;

			ids.add(input);
		}

		return ids;
	}

	public int getHighestNode() {
		List<Integer> its = this.getAllNodes();
		return its.get(its.size() - 1);
	}

	public List<Integer> getAllNodes() {
		List<Integer> ids = new ArrayList<>();
		for (Gene gene : this.getGenes()) {
			if (!ids.contains(gene.getFrom())) {
				ids.add(gene.getFrom());
			}
			if (!ids.contains(gene.getTo())) {
				ids.add(gene.getTo());
			}
		}
		Collections.sort(ids);
		return ids;
	}

	public boolean isHiddenNode(int node) {
		return !this.isInputNode(node) && !this.isOutputNode(node);
	}

	public void addInputNode(int node) {
		if (this.fitness != -1)
			throw new UnsupportedOperationException("addInputNode() must be called before getFitness()");

		if (this.inputNodes.contains(node))
			throw new IllegalArgumentException();

		this.inputNodes.add(node);
	}

	public void addOutputNode(int node) {
		if (this.fitness != -1)
			throw new UnsupportedOperationException("addOutputNode() must be called before getFitness()");

		if (this.outputNodes.contains(node))
			throw new IllegalArgumentException();

		this.outputNodes.add(node);
	}

	public List<Integer> getInputNodes() {
		return inputNodes;
	}

	public List<Integer> getOutputNodes() {
		return outputNodes;
	}

	public List<Integer> getHiddenNodes() {
		List<Integer> its = new ArrayList<>();
		for (int node : this.getAllNodes()) {
			if (!this.isInputNode(node) && !this.isOutputNode(node)) {
				its.add(node);
			}
		}
		return its;
	}

	public boolean isInputNode(int node) {
		return this.inputNodes.contains(node);
	}

	public boolean isOutputNode(int node) {
		return this.outputNodes.contains(node);
	}

	public EvolutionCore getCore() {
		return core;
	}

	public void addGene(Gene gene, Genome parent1, Genome parent2) {

		if (this.fitness != -1)
			throw new UnsupportedOperationException("addGene() must be called before getFitness()");

		if (this.genes.containsKey(gene.getInnovationNumber())) {
			System.out.println(this.toString());
			throw new UnsupportedOperationException("Genome already has gene with innovation number " + gene.getInnovationNumber());
		}

		gene = gene.clone(); // make sure we're working with a cloned instance
		if (parent1 != null && parent2 != null) {
			if (parent1.hasGene(gene.getInnovationNumber()) && parent2.hasGene(gene.getInnovationNumber())) {
				/**
				 * There is a chance that a gene which is disabled in one of the parents is disabled.
				 */
				boolean dis1 = !parent1.getGene(gene.getInnovationNumber()).isEnabled();
				boolean dis2 = !parent2.getGene(gene.getInnovationNumber()).isEnabled();

				// only one of them is disabled
				if ((dis1 && !dis2) || (!dis1 && dis2)) {
					boolean disabled = Random.success(this.getCore().getSetting(Setting.GENE_DISABLE_CHANCE));
					gene.setEnabled(!disabled);
				}
			}
		}

		this.genes.put(gene.getInnovationNumber(), gene); // clone it so we are sure we have a new instance
	}

	public Collection<Gene> getGenes() {
		return genes.values();
	}

	public int getHighestInnovationNumber() {
		if (this.genes.isEmpty()) {
			throw new UnsupportedOperationException("Genes may not be empty");
		}
		Iterator<Gene> it = this.genes.values().iterator();
		Gene last = null;
		while (it.hasNext()) {
			last = it.next();
		}
		// can't be null otherwise UnsupportedOperationException has already been thrown
		if (last == null)
			throw new AssertionError();

		return last.getInnovationNumber();
	}

	private boolean hasGene(int innovationNumber) {
		return this.genes.containsKey(innovationNumber);
	}

	private Gene getGene(int innovationNumber) {
		return this.genes.get(innovationNumber);
	}

	/**
	 * ArrayList
	 */
	public List<Connection> getAllConnections() {
		List<Connection> conns = new ArrayList<>();
		for (Gene gene : this.getGenes()) {
			conns.add(new Connection(gene.getFrom(), gene.getTo()));
		}
		return conns;
	}

	/**
	 * HashSet
	 */
	public Collection<? extends Connection> getActiveConnections() {
		Set<Connection> conns = new HashSet<>();
		for (Gene gene : this.getGenes()) {
			if (gene.isEnabled()) {
				conns.add(new Connection(gene.getFrom(), gene.getTo()));
			}
		}
		return conns;
	}

	/**
	 * Cloned object has cloned maps and lists, the genes are also cloned.
	 * But the contents of the other maps and lists are not cloned.
	 */
	@Override
	public Genome clone() {
		Genome newGenome = new Genome(this.core, this.getSpecies(), this.getInputs(), this.getOutputs());

		// clone the values of the genes map
		newGenome.genes = new TreeMap<>();
		for (Entry<Integer, Gene> s : this.genes.entrySet()) {
			newGenome.genes.put(s.getKey(), s.getValue().clone());
		}

		newGenome.inputNodes = new ArrayList<>(this.inputNodes);
		newGenome.outputNodes = new ArrayList<>(this.outputNodes);
		return newGenome;
	}

	/**
	 * If a genome has exactly the same genes as an already existing genome but has different
	 * innovation numbers, we replace it.
	 */
	public void fixDuplicates() {

		if (this.fitness != -1)
			throw new UnsupportedOperationException("fixDuplicates() must be called before getFitness()");

		for (Species sp : this.getCore().getPopulationManager().getPopulation().getSpecies()) {
			for (Genome genome : sp.getMembers()) {
				List<Connection> conA = this.getAllConnections();
				List<Connection> conB = genome.getAllConnections();

				if (ArrayUtils.equals(conB, conA)) {
					Iterator<Gene> toCloneFrom = new ArrayList<>(genome.genes.values()).iterator();
					Iterator<Gene> toReplace = new ArrayList<>(this.genes.values()).iterator();

					while (toCloneFrom.hasNext() && toReplace.hasNext()) {
						Gene from = toCloneFrom.next();
						Gene to = toReplace.next();

						int oldInno = to.getInnovationNumber();
						int changeTo = from.getInnovationNumber();

						Gene old = this.genes.remove(oldInno);
						old.setInnovationNumber(changeTo);
						this.genes.put(old.getInnovationNumber(), old);
					}
					if (toCloneFrom.hasNext() || toReplace.hasNext())
						throw new AssertionError();
					return;
				}
			}
		}
	}

	/**
	 * Make sure calculateFitness() has been called already.
	 */
	public static void crossAndAdd(Genome a, Genome b) {

		if (!a.getSpecies().equals(b.getSpecies()))
			throw new UnsupportedOperationException("Species must match when crossing");

		double aFitness = a.getFitness();
		double bFitness = b.getFitness();

		Genome strongest;
		Genome weakest;
		if (aFitness > bFitness) {
			strongest = a;
			weakest = b;
		} else {
			strongest = b;
			weakest = a;
		}
		Genome child = crossDominant(strongest, weakest);
		a.getCore().getPopulationManager().getPopulation().addGenome(child);
	}

	/**
	 * Also calls the mutations.
	 */
	private static Genome crossDominant(Genome dominant, Genome other) {
		if (!dominant.getSpecies().equals(other.getSpecies()))
			throw new UnsupportedOperationException("Species must match when crossing");

		if (dominant.getGenes().isEmpty() || other.getGenes().isEmpty())
			throw new UnsupportedOperationException("Genes may not be empty");

		// find out how far they match
		int sharedLength = -1;
		for (int i = 1;; i++) {
			if (i > 100000)
				throw new RuntimeException();

			if (dominant.hasGene(i) && other.hasGene(i)) {
				sharedLength = i;
			} else {
				break;
			}
		}
		if (sharedLength == -1)
			throw new AssertionError();

		Genome newGenome = new Genome(dominant.getCore(), null, dominant.getInputs(), dominant.getOutputs()); // inputs/outputs should match so it doesn't matter where we get it from
		for (int i = 1; i <= dominant.getHighestInnovationNumber(); i++) {
			if (dominant.hasGene(i)) {
				// the following should also be random if both parents have the gene
				if (other.hasGene(i)) {
					newGenome.addGene(Random.random(new Gene[] { dominant.getGene(i), other.getGene(i) }), dominant, other);
				} else {
					newGenome.addGene(dominant.getGene(i), dominant, other);
				}
			}
		}

		// make sure there are no duplicates
		newGenome.fixDuplicates();

		// do mutations
		newGenome.mutate();

		return newGenome;
	}

	public void mutate() {
		Mutation mutation = new Mutation(this);
		mutation.mutate();
	}

	/**
	 * Returns the distance between two existing genomes using the following formula.
	 * d = (c1 * E) / N + (c2 * D) / N + c3 * W
	 */
	public static double distance(Genome a, Genome b) {
		// find the longest
		int aLength = a.getHighestInnovationNumber();
		int bLength = b.getHighestInnovationNumber();

		Genome longest;
		Genome shortest;

		if (aLength > bLength) {
			longest = a;
			shortest = b;
		} else {
			longest = b;
			shortest = a;
		}

		int shortestLength = shortest.getHighestInnovationNumber();
		int longestLength = longest.getHighestInnovationNumber();

		double disjoint = 0; // use double so it won't be used as an int in the formula
		double excess = 0; // use double so it won't be used as an int in the formula

		List<Double> weights = new ArrayList<>();
		for (int i = 1; i <= longestLength; i++) {
			Gene aa = longest.getGene(i);
			Gene bb = shortest.getGene(i);

			if ((aa == null && bb != null) || (aa != null && bb == null)) {
				// only present in one of them

				if (i <= shortestLength) {
					disjoint++;
				} else if (i > shortestLength) {
					excess++;
				}
			}
			if (aa != null && bb != null) {
				// matching gene
				double distance = Math.abs(aa.getWeight() - bb.getWeight());
				weights.add(distance);
			}
		}

		double total = 0;
		double size = 0;

		for (double w : weights) {
			total += w;
			size++;
		}

		double averageWeightDistance = total / size;
		double n = longest.getGenes().size();
		double c1 = a.getCore().getSetting(Setting.DISTANCE_EXCESS_WEIGHT);
		double c2 = a.getCore().getSetting(Setting.DISTANCE_DISJOINT_WEIGHT);
		double c3 = a.getCore().getSetting(Setting.DISTANCE_WEIGHTS_WEIGHT);

		// formula: d = (c1 * E) / N + (c2 * D) / N + c3 * W
		double d = ((c1 * excess) / n) + ((c2 * disjoint) / n) + (c3 * averageWeightDistance);
		return d;
	}

	@Override
	public double[] calculate(double[] input) {
		return new BackTraceTask(this, this.core.getActivationFunction(), input).calculateOutput();
	}

	private double fitness = -1;

	private double calculateFitness() {
		this.fitness = this.core.getFitnessCalculator().getFitness(this);

		if (this.fitness > this.getSpecies().getHighestFitness()) {
			this.getSpecies().setHighestFitness(this.fitness);
		}
		return this.fitness;
	}

	/**
	 * Returns the same value as the most recent call of calculateFitness()
	 * or -1 if calculateFitness() hasn't been called yet.
	 */
	public double getFitness() {
		if (this.fitness == -1)
			return calculateFitness();

		return this.fitness;
	}

	/**
	 * Class sorts by descending order, so best comes first.
	 * Make sure calculateFitness() has been called already.
	 */
	public static class GenomeSorter implements Comparator<Genome> {

		@Override
		public int compare(Genome o1, Genome o2) {
			double a1 = o1.getFitness();
			double a2 = o2.getFitness();

			if (a1 > a2)
				return -1;
			if (a1 < a2)
				return 1;
			return 0;
		}
	}

	@Override
	public String toString() {
		StringBuilder genes = new StringBuilder();
		for (Entry<Integer, Gene> gen : this.genes.entrySet()) {
			Gene gene = gen.getValue();
			genes.append("[ " + gen.getKey() + "=" + gene.getInnovationNumber() + " , " + gene.getFrom() + " , " + gene.getTo() + " , " + gene.getWeight() + " " + gene.isEnabled() + " ] ");
		}
		return genes.toString();
	}
}
