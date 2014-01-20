package pl.dolecinski.placement.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Permutations {
	private int[] key;
	private List<Integer> list, pWord;
	private int n, i = 1;

	public Permutations(List<Integer> list) {
		this.list = list;

		n = list.size();
		key = new int[n + 1];

		for (int i = 1; i <= n; i++)
			key[i] = i;
	}

	public boolean next() {
		if (i == 1) {
			build();
		} else if (i == fact(n) + 1) {
			return false;
		} else if (i % 2 == 0) {
			swap(n, n - 1);
			build();
		} else if (i % 2 == 1) {
			int j, k;

			for (j = n - 1; j >= 0; j--) {
				if ((i - 1) % fact(j) == 0) {
					add(n - j);
					for (k = 1; k <= n - j - 1; k++) {
						if (key[k] == key[n - j]) {
							add(n - j);
							k = 0;
						}
					}
				}
			}

			build();
		}

		i++;
		return true;
	}

	private long fact(long f) {
		return f == 0 ? 1 : f * fact(f - 1);
	}

	private void swap(int a, int b) {
		int t = key[a];
		key[a] = key[b];
		key[b] = t;
	}

	private void build() {
		Integer[] s = new Integer[list.size()];

		for (int i = 1; i <= n; i++)
			s[i - 1] = (list.get(key[i] - 1));

		pWord = Arrays.asList(s);
	}

	private void add(int i) {
		key[i]++;
		if (key[i] == n + 1)
			key[i] = 1;
	}

	public List<Integer> getPermutation() {
		return pWord;
	}

	static public Set<List<Integer>> getPermutations(int m, int length) {
		if (m < 0) {
			m = 0;
		}
		Set<List<Integer>> numberPartition = Combinatorics.numberPartition(m,
				length);
		Set<List<Integer>> permutations = new HashSet<List<Integer>>();

		for (List<Integer> list : numberPartition) {
			Permutations p = new Permutations(list);
			while (p.next()) {
				permutations.add(p.getPermutation());
			}
		}
		return permutations;
	}
	
}
