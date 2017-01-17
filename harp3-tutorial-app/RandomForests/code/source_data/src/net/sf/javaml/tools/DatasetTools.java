/**
 * This file is part of the Java Machine Learning Library
 * 
 * The Java Machine Learning Library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * The Java Machine Learning Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Java Machine Learning Library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Copyright (c) 2006-2009, Thomas Abeel
 * 
 * Project: http://java-ml.sourceforge.net/
 * 
 */
package net.sf.javaml.tools;

import java.util.List;
import java.util.Random;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;
import net.sf.javaml.tools.sampling.SamplingMethod;

import org.apache.commons.math.stat.StatUtils;

import be.abeel.util.Pair;

/**
 * This class provides utility methods on data sets.
 * 
 * @see Dataset
 * @see DefaultDataset
 * 
 * @version 0.1.5
 * 
 * @author Thomas Abeel
 * 
 */
final public class DatasetTools {

	/**
	 * All data will be merged together in the first supplied data set.
	 * 
	 * @param datasets
	 *            a number of data sets
	 * 
	 */
	public static void merge(Dataset... datasets) {
		Dataset out = null;
		for (Dataset data : datasets) {
			if (out == null)
				out = data;
			else
				out.addAll(data);
		}
	}

	/**
	 * Generate a bootstrap sample from the data set with a particular size,
	 * using the given random generator.
	 * 
	 * This is done by sampling with replacement.
	 * 
	 * @param data
	 *            data set to bootstrap from
	 * @param size
	 *            number of instances in the output data
	 * @param rg
	 *            random generator to use for the bootstrapping
	 * @return bootstrap of the supplied data
	 */
	@Deprecated
	public static Dataset bootstrap(Dataset data, int size, Random rg) {
		Dataset out = new DefaultDataset();
		while (out.size() < size) {
			out.add(data.instance(rg.nextInt(data.size())).copy());
		}
		return out;
	}

	/**
	 * Create an instance that contains all the maximum values for the
	 * attributes.
	 * 
	 * @param data
	 *            data set to find minimum attribute values for
	 * @return Instance representing the minimum values for each attribute
	 */
	public static Instance maxAttributes(Dataset data) {
		Instance max = new SparseInstance();
		for (Instance i : data) {
			for (Integer index : i.keySet()) {
				double val = i.value(index);
				if (!max.containsKey(index))
					max.put(index, val);
				else if (max.get(index) < val)
					max.put(index, val);

			}

		}
		return max;
	}

	/**
	 * Create an instance that contains all the minimum values for the
	 * attributes.
	 * 
	 * @param data
	 *            data set to calculate minimum attribute values for
	 * @return Instance representing all minimum attribute values
	 */
	public static Instance minAttributes(Dataset data) {
		Instance min = new SparseInstance();
		for (Instance i : data) {
			for (Integer index : i.keySet()) {
				double val = i.value(index);
				if (!min.containsKey(index))
					min.put(index, val);
				else if (min.get(index) > val)
					min.put(index, val);
			}
		}
		return min;
	}

	/**
	 * Creates an instance that contains the standard deviation of the values
	 * for each attribute.
	 * 
	 * @param data
	 *            data set to calculate attribute value standard deviations for
	 * @param avg
	 *            the average instance for the data set
	 * @return Instance representing the standard deviation of the values for
	 *         each attribute
	 */
	public static Instance standardDeviation(Dataset data, Instance avg) {
		Instance sum = new DenseInstance(new double[avg.noAttributes()]);
		for (Instance i : data) {
			Instance diff = i.minus(avg);
			sum = sum.add(diff.multiply(diff));
		}
		sum = sum.divide(data.size());
		return sum.sqrt();

	}

	/**
	 * Creates an instance that contains the average values for the attributes.
	 * 
	 * @param data
	 *            data set to calculate average attribute values for
	 * @return Instance representing the average attribute values
	 */
	public static Instance average(Dataset data) {
		double[] tmpOut = new double[data.noAttributes()];
		double sum=0;
		for (int i = 0; i < data.noAttributes(); i++) {
			for (int j = 0; j < data.size(); j++) {
				sum+= data.get(j).value(i);
			}
			tmpOut[i] = sum/data.size();

		}
		return new DenseInstance(tmpOut);
	}

	/**
	 * Calculates the percentile hinge for a given percentile.
	 * 
	 * @param data
	 *            data set to calculate percentile for
	 * @param perc
	 *            percentile to calculate, Q1=25, Q2=median=50,Q3=75
	 * @return
	 */
	public static Instance percentile(Dataset data, double perc) {
		double[] tmpOut = new double[data.noAttributes()];
		for (int i = 0; i < data.noAttributes(); i++) {
			double[] vals = new double[data.size()];
			for (int j = 0; j < data.size(); j++) {
				vals[j] = data.get(j).value(i);
			}
			tmpOut[i] = StatUtils.percentile(vals, perc);

		}
		return new DenseInstance(tmpOut);
	}

	/**
	 * Creates an Instance from the class labels over all Instances in a data
	 * set.
	 * 
	 * The indices of the class labels are used because the class labels can be
	 * any Object.
	 * 
	 * @param data
	 *            data set to create class label instance for
	 * @return instance with class label indices as values.
	 */
	public static Instance createInstanceFromClass(Dataset data) {
		Instance out = new DenseInstance(data.size());
		int index = 0;
		for (Instance inst : data)
			out.put(index++, (double) data.classIndex(inst.classValue()));
		return out;
	}

	/**
	 * Creates an Instance from the values of one particular attribute over all
	 * Instances in a data set.
	 * 
	 * @param data
	 * @param i
	 * @return
	 */
	public static Instance createInstanceFromAttribute(Dataset data, int i) {
		Instance out = new DenseInstance(data.size());
		int index = 0;
		for (Instance inst : data)
			out.put(index++, inst.value(i));
		return out;
	}


	/**
	 * 
	 * @param inputData
	 * @param s
	 * @param size
	 * @return
	 */
	/*
	public static Pair<Dataset, Dataset> sample(Dataset inputData,
			SamplingMethod s, int size, long seed) {
		List<Integer> ixs = ListTools.incfill(inputData.size());
		List<Integer> sampledIxs = s.sample(ixs, size, seed);
		ixs.removeAll(sampledIxs);
		Dataset in = new DefaultDataset();
		Dataset out = new DefaultDataset();
		for (int i : sampledIxs)
			in.add(inputData.get(i).copy());
		for (int i : ixs)
			out.add(inputData.get(i).copy());
		return new Pair<Dataset, Dataset>(in,out);

	}
	*/

	public static Dataset sampleTrain(Dataset inputData,
			SamplingMethod s, int size, long seed) {
		List<Integer> sampledIxs = s.sample(null, size, seed);
		Dataset train = new DefaultDataset();
		for (int i : sampledIxs)
			train.add(inputData.get(i));
		return train;

	}

	/*
	public static Pair<Dataset, Dataset> sample(Dataset inputData,
			SamplingMethod s, int size) {
		return sample(inputData, s, size, System.currentTimeMillis());

	}
	*/

}
