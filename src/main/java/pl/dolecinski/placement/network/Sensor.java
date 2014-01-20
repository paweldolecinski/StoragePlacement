package pl.dolecinski.placement.network;

public class Sensor {

	public enum SensorType {
		NORMAL, STORAGE,
	};

	private int id;
	private String name;

	private SensorType type;

	public Sensor(int id) {
		this(id, "", SensorType.NORMAL);
	}

	public Sensor(int id, String name) {
		this(id, name, SensorType.NORMAL);
	}

	public Sensor(int id, String name, SensorType type) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public SensorType getType() {
		return type;
	}

	@Override
	public String toString() {
		return id + ":" + name + " (" + type.name().toLowerCase() + ")";
	}

	public void setType(SensorType type) {
		this.type = type;

	}
	
	
}
