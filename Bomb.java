public class Bomb implements PowerUp{
	//Default Constructor
    public Bomb(){
    }

	/** 
	 * @param aT
	 * This will re roll all tiles in a 4x4 at the center of the grid 
	 */
	public void Use(Tile[][] aT) {
		for(int r = 2; r < 6; r++) {
			for(int c = 2; c< 6; c++) {
				aT[r][c].isMatch = true;
			}
		}
	}
}
