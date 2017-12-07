/**
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
package tools.descartes.petsupplystore.recommender.algorithm.impl.cf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tools.descartes.petsupplystore.recommender.algorithm.AbstractRecommender;

/**
 * Recommender based on item-based collaborative filtering with the slope one
 * algorithm.
 * 
 * @author Johannes Grohmann
 *
 */
public class SlopeOneRecommender extends AbstractRecommender {
	
	/**
	 * Represents a matrix, assigning each itemid an average difference (in
	 * rating/buying) to any other itemid.
	 */
	private Map<Long, Map<Long, Double>> differences = new HashMap<>();

	/**
	 * Represents a matrix, counting the frequencies of each combination (i.e. users
	 * rating/buying both items).
	 */
	private Map<Long, Map<Long, Integer>> frequencies = new HashMap<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * tools.descartes.petsupplystore.recommender.algorithm.AbstractRecommender#
	 * execute(java.util.List)
	 */
	@Override
	protected List<Long> execute(Long userid, List<Long> currentItems) {
		if (userid == null) {
			throw new UnsupportedOperationException(this.getClass().getName()
					+ " does not support null userids. Use a pseudouser or switch to another approach.");
		}
		// This could be further optimized by moving this part into the pre-processing
		// step, but we want to have nicer performance behavior
		
		return null;
		
		
	}

	private double calculateScoreForItem(long userid, long itemid) {
		double score = 0;
		double cumWeights = 0;

		// normalize
		return score / cumWeights;
	}

	@Override
	protected void executePreprocessing() {
		// The buying matrix is considered to be the rating
		// i.e. the more buys, the higher the rating
		buildDifferencesMatrices(getUserBuyingMatrix());
	}

	/**
	 * Based on the available data, calculate the relationships between the items
	 * and number of occurrences. Fill the difference and frequencies matrix.
	 * 
	 * @param data
	 *            The user rating matrix
	 */
	private void buildDifferencesMatrices(Map<Long, Map<Long, Double>> userRatingMatrix) {
		for (Map<Long, Double> uservalues : userRatingMatrix.values()) {
			for (Entry<Long, Double> singleRating : uservalues.entrySet()) {
				// if not present -> create
				if (!frequencies.containsKey(singleRating.getKey())) {
					frequencies.put(singleRating.getKey(), new HashMap<Long, Integer>());
					differences.put(singleRating.getKey(), new HashMap<Long, Double>());
				}
				// for all other ratings of that user
				for (Entry<Long, Double> otherRating : uservalues.entrySet()) {
					int currCount = 0;
					Integer count = frequencies.get(singleRating.getKey()).get(otherRating.getKey());
					if (count != null) {
						// count is != null, if the key is actually found
						// if so, we use the known count value as count, otherwise the count until now
						// is 0
						currCount = count.intValue();
					}

					double currDiff = 0;
					Double diff = differences.get(singleRating.getKey()).get(otherRating.getKey());
					if (diff != null) {
						// diff is != null, if the key is actually found
						// if so, we use the known difference value as currDiff, otherwise the diff
						// until now is 0.0
						currDiff = diff.doubleValue();
					}

					// get the diff value of this user
					double userdiff = singleRating.getValue() - otherRating.getValue();
					frequencies.get(singleRating.getKey()).put(otherRating.getKey(), currCount + 1);
					differences.get(singleRating.getKey()).put(otherRating.getKey(), currDiff + userdiff);
				}
			}
		}

		// now, transform the differences matrix into real differences (not just the sum
		// of all found differences)
		for (Long i : differences.keySet()) {
			for (Long j : differences.get(i).keySet()) {
				// for all matrix entries divide the differences by the sum of occurences
				double diffval = differences.get(i).get(j);
				double freq = frequencies.get(i).get(j);
				differences.get(i).put(j, diffval / freq);
			}
		}
	}

}