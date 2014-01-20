package pl.dolecinski.placement.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Combinatorics {

	public static Set<List<Integer>> numberPartition(int n, int maxSize) {

		Set<List<Integer>> ret = new HashSet<List<Integer>>();
		int[] S = new int[n + 1];
		int[] R = new int[n + 1];
		int d, sum, l;

		S[0] = n;
		R[0] = d = 1;

		while (true) {
			// add
			Integer[] solution = new Integer[maxSize];
			Arrays.fill(solution, 0);
			int tmpMaxSizeCount = 0;
			for (int x = 0, i = 0; x < d; x++) {
				for (int y = 0; y < R[x]; y++) {
					if (i < maxSize) {
						solution[i] = S[x];
						i++;
					}
				}
				tmpMaxSizeCount += R[x];
			}
//			System.out.println(Arrays.toString(R));
//			System.out.println(Arrays.toString(S));
//			System.out.println(tmpMaxSizeCount);
//			System.out.println();
			if (tmpMaxSizeCount <= maxSize)
				ret.add(Arrays.asList(solution));

			if (S[0] == 1 || S[0] == 0)
				break;
			sum = 0;
			if (S[d - 1] == 1)
				sum += R[--d];
			sum += S[d - 1];
			R[d - 1]--;
			l = S[d - 1] - 1;
			if (R[d - 1] != 0)
				d++;
			S[d - 1] = l;
			R[d - 1] = sum / l;
			l = sum % l;
			if (l != 0) {
				S[d] = l;
				R[d++] = 1;

			}
		}

		return ret;
	}

}
