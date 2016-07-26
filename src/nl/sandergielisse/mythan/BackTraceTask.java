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

import java.util.HashMap;
import java.util.Map;

import nl.sandergielisse.mythan.internal.genes.Gene;
import nl.sandergielisse.mythan.internal.genes.Genome;

/**
 * Please note that this is slower than feed forward. But, this
 * is the only option because we don't know the genome's phenotype.
 */
public class BackTraceTask {

	private final Genome genome;
	private final ActivationFunction function;

	private Map<Integer, Double> nodeInputValues = new HashMap<>();

	public BackTraceTask(Genome genome, ActivationFunction function, double[] input) {
		this.genome = genome;
		this.function = function;

		if (genome.getInputNodes().size() != input.length) {
			throw new IllegalArgumentException("Input size " + input.length + " was not equal to the specified length " + genome.getInputNodes().size());
		}

		int c = 0;
		for (int inputNode : this.genome.getInputNodes()) {
			this.nodeInputValues.put(inputNode, input[c++]);
		}
	}

	public double[] calculateOutput() {
		/**
		 * This is a feed backward so we start at the output nodes and trace back.
		 */
		Map<Integer, Double> cache = new HashMap<>();
		int c = 0;
		double[] out = new double[this.genome.getOutputNodes().size()];
		for (int output : this.genome.getOutputNodes()) {
			out[c++] = this.getOutput(output, cache);
		}
		return out;
	}

	private double getOutput(int node, Map<Integer, Double> cache) {

		Double val = cache.get(node);
		if (val != null)
			return val;

		/**
		 * Start by getting the summed input.
		 */
		double sum = 0;

		for (Gene gene : this.genome.getGenes()) {
			if (gene.getTo() == node && gene.isEnabled()) {
				if (this.genome.isInputNode(gene.getFrom())) {
					// we have this value
					sum += this.nodeInputValues.get(gene.getFrom()) * gene.getWeight();
				} else {
					// we have to dig deeper to find the source
					sum += this.getOutput(gene.getFrom(), cache) * gene.getWeight();
				}
			}
		}

		/**
		 * Apply our activation function.
		 */
		double d = this.function.activate(sum);
		cache.put(node, d);
		return d;
	}
}
