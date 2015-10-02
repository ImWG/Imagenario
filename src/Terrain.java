
public class Terrain {
	int sizex, sizey;
	char[][] tiles;
	char[][] hills;
	
	void initializeTiles(){
		tiles = new char[sizex][sizey];
		hills = new char[sizex][sizey];
	}
}
