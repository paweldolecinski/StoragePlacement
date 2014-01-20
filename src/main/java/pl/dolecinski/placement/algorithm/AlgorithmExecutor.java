package pl.dolecinski.placement.algorithm;

import pl.dolecinski.placement.network.SensorNetwork;

public interface AlgorithmExecutor {

	public interface Results {

	}

	void execute(SensorNetwork network, int k, boolean blackRoot);

	Results getResults();

}
