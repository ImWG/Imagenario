import java.awt.image.BufferedImage;


public class ImageProcessor {
	static Palette COLORMAP;
	
	static void setColormap(Palette COLORMAP){
		ImageProcessor.COLORMAP = COLORMAP;
	}
	
	static final int SIZE_MODE_TRIM = 0;
	static final int SIZE_MODE_STRETCH = 1;
	static final int PLANTS_NO = 0;
	static final int PLANTS_YES = 1;
	
	static void setTerrainSizeByImage(Terrain terr, BufferedImage img, int mode){
		int size;
		if (mode == 0){
			size = Math.min(img.getWidth(), img.getHeight());
		}else{
			size = Math.max(img.getWidth(), img.getHeight());
		}if (size > 255){
			size = 255;
		}
		terr.sizex = size;
		terr.sizey = size;
		terr.initializeTiles();
	}
	
	static int getPaletteId(int rgb){
	 	int id = -1;
  		int distance = 0x10000*3;
		int r = (rgb & 0xff0000)>> 16;
		int g = (rgb & 0x00ff00)>> 8;
		int b = (rgb & 0x0000ff);
		for (int k=0; k<COLORMAP.length; ++k){
			int dist = (r - COLORMAP.palette[k][1])*(r - COLORMAP.palette[k][1])
					+ (g - COLORMAP.palette[k][2])*(g - COLORMAP.palette[k][2])
					+ (b - COLORMAP.palette[k][3])*(b - COLORMAP.palette[k][3]);
			if (dist < distance){
				distance = dist;
				id = k;
			}
		}
		return id;
	}
	
	static void convertImageToMap(BufferedImage img, Terrain terr){
		 for(int i=0; i<terr.sizex; ++i){
			 for(int j=0; j<terr.sizey; ++j){
		  		if (i < img.getWidth() && j < img.getHeight()){
		  			int rgb = img.getRGB(i, j);
					terr.tiles[i][j] = (char) COLORMAP.palette[getPaletteId(rgb)][0];
		  		}else{
		  			terr.tiles[i][j] = '\0';
		  		}
				  
			}
		}
	}
	
	static void convertImageToMapHill(BufferedImage img, Terrain terr){
		
		for(int i=0; i<terr.sizex; ++i){
			int x = i*img.getWidth() / terr.sizex;
			for(int j=0; j<terr.sizey; ++j){
				int y = j*img.getHeight() / terr.sizey;
				int rgb = img.getRGB(x, y);
				int r = (rgb & 0xff0000)>> 16;
				int g = (rgb & 0x00ff00)>> 8;
				int b = (rgb & 0x0000ff);
				terr.hills[i][j] = (char)((r+g+b) * ((int)Imagenario.settings.get("max_height")) / 3 / 0xFF);
			}
		}
	}
	
	static void createObjects(BufferedImage img, SCX scx){
		Terrain terr = scx.terrain;
		for(int i=0; i<terr.sizex; ++i){
			for(int j=0; j<terr.sizey; ++j){
		  		if (i < img.getWidth() && j < img.getHeight()){
		  			int id = getPaletteId(img.getRGB(i, j));
					short unitId = (short)COLORMAP.palette[id][4];
					if (unitId < 0)
						continue;
					else if (Math.random()*100 < COLORMAP.palette[id][5]){
						scx.addUnit(i, j, 0, unitId, (short)0);
					}
		  		}
			}
		}
	}
	
	static BufferedImage getMapImage(Terrain terr){
		BufferedImage img = new BufferedImage(terr.sizex, terr.sizey, BufferedImage.TYPE_INT_RGB);
		
		Palette palette = Imagenario.drawing_colormap;
		int[] colormap = new int[palette.palette.length];
		for (int k=0; k<palette.palette.length; ++k){
			colormap[k] = (palette.palette[k][1]<<16) | (palette.palette[k][2]<<8) | (palette.palette[k][3]);
		}
		
		for(int i=0; i<terr.sizex; ++i){
			for(int j=0; j<terr.sizey; ++j){
				for (int k=0; k<palette.palette.length; ++k){
					if (palette.palette[k][0] == terr.tiles[i][j]){ 
						img.setRGB(i, j, colormap[k]);
						break;
					}
				}
			 }
		 }
		return img;
	}
	
	static BufferedImage getMapImageHill(Terrain terr){
		BufferedImage img = new BufferedImage(terr.sizex, terr.sizey, BufferedImage.TYPE_INT_RGB);
		
		for(int i=0; i<terr.sizex; ++i){
			for(int j=0; j<terr.sizey; ++j){
				int h = terr.hills[i][j];
				int rgb = Math.min(h*16, 0xff);
				img.setRGB(i, j, rgb | (rgb << 8) | (rgb << 16));
			 }
		 }
		return img;
	}
	
	static void transImage(BufferedImage img){
		int size = Math.min(img.getWidth(), img.getHeight());
		for(int i=1; i<size; ++i){
			for(int j=0; j<i; ++j){
				int rgb = img.getRGB(i, j);
				img.setRGB(i, j, img.getRGB(j, i));
				img.setRGB(j, i, rgb);
			 }
		 }
	}
	
	private static void trans(char[][] array, int size){
		for(int i=1; i<size; ++i){
			for(int j=0; j<i; ++j){
				char t = array[i][j];
				array[i][j] = array[j][i];
				array[j][i] = t;
			 }
		 }
	}
	
	static void transTerrain(Terrain terr){
		int size = Math.min(terr.sizex, terr.sizey);
		trans(terr.tiles, size);
	}
	
	static void transTerrainHill(Terrain terr){
		int size = Math.min(terr.sizex, terr.sizey);
		trans(terr.hills, size);
	}
	
	static void rotateImage(BufferedImage img, boolean clockwise){
		int size = Math.min(img.getWidth(), img.getHeight());
		int size1 = size - 1;
		int rangex = (size+1)/2;
		int rangey = size/2;
		if (clockwise){
			for(int i=0; i<rangex; ++i){
				int si = size1 - i;
				for(int j=0; j<rangey; ++j){
					int rgb = img.getRGB(i, j);
					img.setRGB(i, j, img.getRGB(j, si));
					img.setRGB(j, si, img.getRGB(si, size1-j));
					img.setRGB(si, size1-j, img.getRGB(size1-j, i));
					img.setRGB(size1-j, i, rgb);
				 }
			 }
		}else{
			for(int i=0; i<rangex; ++i){
				int si = size1 - i;
				for(int j=0; j<rangey; ++j){
					int rgb = img.getRGB(i, j);
					img.setRGB(i, j, img.getRGB(size1-j, i));
					img.setRGB(size1-j, i, img.getRGB(si, size1-j));
					img.setRGB(si, size1-j, img.getRGB(j, si));
					img.setRGB(j, si, rgb);
				 }
			 }
		}
	}
	private static void rotate(char[][] array, int size, boolean clockwise){
		int size1 = size - 1;
		int rangex = (size+1)/2;
		int rangey = size/2;
		if (clockwise){
			for(int i=0; i<rangex; ++i){
				int si = size1 - i;
				for(int j=0; j<rangey; ++j){
					char t = array[i][j];
					array[i][j] = array[j][si];
					array[j][si] = array[si][size1-j];
					array[si][size1-j] = array[size1-j][i];
					array[size1-j][i] = t;
				 }
			 }
		}else{
			for(int i=0; i<rangex; ++i){
				int si = size1 - i;
				for(int j=0; j<rangey; ++j){
					char t = array[i][j];
					array[i][j] = array[size1-j][i];
					array[size1-j][i] = array[si][size1-j];
					array[si][size1-j] = array[j][si];
					array[j][si] = t;
				 }
			 }
		}
	}
	
	static void rotateTerrain(Terrain terr, boolean clockwise){
		int size = Math.min(terr.sizex, terr.sizey);
		rotate(terr.tiles, size, clockwise);
	}
	static void rotateTerrainHill(Terrain terr, boolean clockwise){
		int size = Math.min(terr.sizex, terr.sizey);
		rotate(terr.hills, size, clockwise);
	}
	
	static void transUnits(SCX scx){
		for (Unit u: scx.units){
			float t = u.x;
			u.x = u.y;
			u.y = t;
		}
	}
	static void rotateUnits(SCX scx, boolean clockwise){
		int size = scx.terrain.sizex; 
		if (clockwise){
			for (Unit u: scx.units){
				float x = u.x;
				u.x = u.y;
				u.y = size - x;
			}
		}else{
			for (Unit u: scx.units){
				float y = u.y;
				u.y = u.x;
				u.x = size - y;
			}
		}
	}
}
