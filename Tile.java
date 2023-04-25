import java.io.Serializable;

public abstract class Tile implements Comparable<Tile>, Serializable{
	//Variables
	private String type;
	protected Boolean isMatch = false;
	
	//Default Constructor
	public Tile() {
		type = "Black";
	}
	
	//Parametrized Constructor
	public Tile(String t) {
		type = t;
	}

	/** 
	 * @return String
	 */
	public String getType() {
		return type;
	}

	/** 
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/** 
	 * @param other
	 * @return int
	 */
	public int compareTo(Tile other) {
		
		if(this.type == other.type)
			return 0;
		else 
			return 1;
	}
	
	/** 
	 * @return String
	 */
	public String toString() {
		return type.substring(0, Math.min(type.length(), 1));

	}

}