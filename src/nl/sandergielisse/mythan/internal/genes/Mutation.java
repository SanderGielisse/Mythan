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
import java.util.List;

import nl.sandergielisse.mythan.Setting;
import nl.sandergielisse.mythan.internal.Random;

/**
 * There are three types of mutations.
 * 
 * 		1. Add a new node. The new input weight to that node will be 1.
 * 		   The output from the new node will be set to the old connection's weight value.
 * 
 * 		2. Add a new link with a random weight between two existing nodes.
 * 
 * 		3. The weights of an existing connection are changed.
 * 
 */
public class Mutation {

	private final Genome genome;

	public Mutation(Genome genome) {
		this.genome = genome;
	}

	public void mutate() {

		/**
		 * 1. Add a new node. The new input weight to that node will be 1.
		 * 	  The output from the new node will be set to the old connection's weight value.
		 */
		if (Random.success(this.genome.getCore().getSetting(Setting.MUTATION_NEW_NODE_CHANCE))) {
			Gene randomGene = Random.random(new ArrayList<>(this.genome.getGenes()));
			randomGene.setEnabled(false);

			// two new genes
			int from = randomGene.getFrom();
			int to = randomGene.getTo();

			this.genome.getCore().getNextInnovationNumber();

			int newNodeId = this.genome.getHighestNode() + 1;
			this.genome.addGene(new Gene(this.genome.getCore().getNextInnovationNumber(), from, newNodeId, 1D, true), null, null);
			this.genome.addGene(new Gene(this.genome.getCore().getNextInnovationNumber(), newNodeId, to, randomGene.getWeight(), true), null, null);
		}

		/**
		 * 2. Add a new link with a random weight between two existing nodes.
		 *    Start by finding two yet unconnected nodes. One of them must be a hidden node.
		 */
		if (Random.success(this.genome.getCore().getSetting(Setting.MUTATION_NEW_CONNECTION_CHANCE))) {
			try {
				/** 
				 * Instead of looping through all possible connections and choosing one from 
				 * the obtained list, we pick a random connection and hope it doesn't exist
				 * yet. We do this because once the network gets bigger, looping through all
				 * possible connections would be a very intensive task.
				 */
				Collection<? extends Connection> currentConnections = this.genome.getAllConnections();

				int attempts = 0;

				Connection maybeNew = null;
				do {
					{
						if (attempts++ > 40)
							throw new MutationFailedException("New connection could not be created after 40 attempts.");
					}

					int from = Random.random(this.genome.getNodes(true, true, false));

					List<Integer> leftOver = this.genome.getNodes(false, true, true);
					leftOver.remove((Object) from); // cast to Object, otherwise the wrong method remove(int index); will be called

					if (leftOver.isEmpty())
						continue;

					int to = Random.random(leftOver);

					maybeNew = new Connection(from, to);
				} while (maybeNew == null || maybeNew.getFrom() == maybeNew.getTo() || currentConnections.contains(maybeNew) || isRecurrent(maybeNew));

				// add it to the network
				genome.addGene(new Gene(this.genome.getCore().getNextInnovationNumber(), maybeNew.getFrom(), maybeNew.getTo(), Random.random(-1, 1), true), null, null);
			} catch (MutationFailedException e) {
				// System.out.println("Mutation Failed: " + e.getMessage());
			}
		}

		/**
		 * 3. The weights of an existing connection are changed.
		 */
		if (Random.success(this.genome.getCore().getSetting(Setting.MUTATION_WEIGHT_CHANCE))) {
			if (Random.success(this.genome.getCore().getSetting(Setting.MUTATION_WEIGHT_RANDOM_CHANCE))) {
				// assign a random new value
				for (Gene gene : this.genome.getGenes()) {
					double range = this.genome.getCore().getSetting(Setting.MUTATION_WEIGHT_CHANCE_RANDOM_RANGE);
					gene.setWeight(Random.random(-range, range));
				}
			} else {
				// uniformly perturb
				for (Gene gene : this.genome.getGenes()) {
					double disturbance = this.genome.getCore().getSetting(Setting.MUTATION_WEIGHT_MAX_DISTURBANCE);
					double uniform = Random.random(-disturbance, disturbance);
					gene.setWeight(gene.getWeight() + uniform);
				}
			}
		}
	}

	public boolean isRecurrent(Connection with) {
		Genome tmpGenome = this.genome.clone(); // clone so we can change its genes without actually affecting the original genome

		if (with != null) {
			Gene gene = new Gene(tmpGenome.getHighestInnovationNumber() + 1, with.getFrom(), with.getTo(), 0, true);
			tmpGenome.addGene(gene, null, null);
		}

		boolean recc = false;
		for (int hiddenNode : tmpGenome.getHiddenNodes()) {
			if (isRecurrent(new ArrayList<>(), tmpGenome, hiddenNode)) {
				recc = true;
			}
		}
		return recc;
	}

	private boolean isRecurrent(List<Integer> path, Genome genome, int node) {
		if (path.contains(node)) {
			/**
			 * We've been here before, we're in an infinite loop.
			 */
			return true;
		}
		path.add(node);

		boolean recc = false;
		for (int from : this.getInputs(genome, node)) {
			if (!genome.isInputNode(from)) {
				if (this.isRecurrent(path, genome, from)) {
					recc = true;
				}
			}
		}
		return recc;
	}

	private List<Integer> getInputs(Genome genome, int node) {
		List<Integer> froms = new ArrayList<>();
		for (Gene gene : genome.getGenes()) {
			if (gene.getTo() == node) {
				froms.add(gene.getFrom());
			}
		}
		return froms;
	}
}
