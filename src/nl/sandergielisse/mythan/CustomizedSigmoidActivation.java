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

public class CustomizedSigmoidActivation implements ActivationFunction {

	@Override
	public double activate(double x) {
		return 1D / (1D + Math.exp(-4.9 * x));
		//return 2.0 / (1.0 + Math.exp(-4.9 * x)) - 1.0;
	}
}
