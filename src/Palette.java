import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class Palette {
	/**
	 * 0-tile, 1-r, 2-g, 3-b, 4-unit, 5-percentage density
	 */
	int[][] palette;
	
	int length;
	
	private static int MAX_COLORS = 256;
	
	public Palette(String filename){
		int[][] tpalette = new int[MAX_COLORS][6];
		try {
			FileReader fr = new FileReader(filename);
			BufferedReader br = new BufferedReader(fr);
			String line;
			int i = 0;
			while((line=br.readLine())!=null){
				String tline = line.replaceFirst("\\s*\\;.+", "");
				if (tline.length() == 0)
					continue;
				if (!tline.matches("[0-9 ]+"))
					continue;
				String[] values = tline.split("\\s+");
				switch (values.length){
				default: break;
				case 5: case 6:
					tpalette[i][5] = values.length==6 ? Integer.parseInt(values[5]) : 100; 
				case 4:
					tpalette[i][4] = values.length>=5 ? Integer.parseInt(values[4]) : -1;
					tpalette[i][0] = Integer.parseInt(values[0]);
					tpalette[i][1] = Integer.parseInt(values[1]);
					tpalette[i][2] = Integer.parseInt(values[2]);
					tpalette[i][3] = Integer.parseInt(values[3]);
					++i;
				}
			}
			
			palette = new int[i][6];
			for (int j=0; j<i; ++j){
				for (int k=0; k<6; ++k){
					palette[j][k] = tpalette[j][k];
				}
			}
			
			this.length = i;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
