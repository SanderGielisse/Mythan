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

public class Gene implements Cloneable {

	private int innovationNumber;
	private final int from;
	private final int to;
	private double weight;
	private boolean enabled;

	public Gene(int innovationNumber, int from, int to, double weight, boolean enabled) {
		this.innovationNumber = innovationNumber;
		this.from = from;
		this.to = to;
		this.weight = weight;
		this.enabled = enabled;
	}

	public int getInnovationNumber() {
		return innovationNumber;
	}

	public void setInnovationNumber(int innovationNumber) {
		this.innovationNumber = innovationNumber;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	protected Gene clone() {
		return new Gene(innovationNumber, from, to, weight, enabled);
	}

	@Override
	public String toString() {
		return "Gene [innovationNumber=" + innovationNumber + ", from=" + from + ", to=" + to + ", weight=" + weight + ", enabled=" + enabled + "]";
	}
}
