import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class Imagenario {
	
	public static final String VERSION = "0.2";
	
	static int[] PLAYER_PALETTE = {
		0xffffff, 0x0000ff, 0xff0000, 0x00ff00, 0xffff00, 
		0x00ffff, 0xff00ff, 0x7f7f7f, 0xff7f00, 0x000000};
	
	static Palette colormap;
	static Palette drawing_colormap;
	
	static File srcfile, srcfile2, dstfile;
	static SCX scx;
	
	static BufferedImage srcimg;
	static BufferedImage srcimg2;
	static BufferedImage mapimg; 
	static BufferedImage mapimg2;
	
	static final String RAW = "raw.scx";
	
	static final String SETTING_FILE = "settings.ini";
	
	static final String COLORMAP_ARTIST = "palette/artist.plt";
	static final String COLORMAP_PRACTICAL = "palette/practical.plt";
	static final String COLORMAP_DRAWING = "palette/drawing.plt";
	
	/**
	 * palette : the palette filename for generating terrain tiles
	 * mode : 0-trim, 1-stretch
	 * plants : 0-no, 1-yes
	 * max_height : maximum height of hill, also the amount of levels of hill 
	 * open_dict : dictionary of opening
	 * save_dict : dictionary of saving
	 */
	static HashMap<String, Object> settings = new HashMap<String, Object>();	
	
	static public void main(String[] args){
		
		loadSettings();
		
		final MainFrame mainframe = new MainFrame();
		mainframe.setVisible(true);
		mainframe.repaint();
		
		//put settings
		colormap = new Palette((String) settings.get("palette"));
		drawing_colormap = new Palette(COLORMAP_DRAWING);
		
		final SettingDialog settingdialog = new SettingDialog(mainframe);
		settingdialog.center();
		
		
		mainframe.button_open.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (mainframe.chooser1.showOpenDialog(mainframe) != JFileChooser.APPROVE_OPTION)
					return;
				
				if ((srcfile = mainframe.chooser1.getSelectedFile())!=null){
					if (!srcfile.exists()){
						JOptionPane.showMessageDialog(mainframe,"文件无效！");
						return;
					}
					try {
						mainframe.setDirectory(srcfile, 1);
						settings.put("open_dict", srcfile.getPath());
						BufferedImage img = ImageIO.read(srcfile);
						if (img == null){
							JOptionPane.showMessageDialog(mainframe,"这不是有效的图像文件！");
							return;
						}
						mainframe.setImage(img);
						srcimg = img;
						
						if (srcimg2 != null && JOptionPane.showConfirmDialog(mainframe, "您要弃置高度图吗？") == 0)
							srcimg2 = null;
						
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(mainframe,"这不是有效的图像文件！");
						e1.printStackTrace();
					}
				}
			}
		});
		
		mainframe.button_open2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (mainframe.chooser1.showOpenDialog(mainframe) != JFileChooser.APPROVE_OPTION)
					return;
				if ((srcfile2 = mainframe.chooser1.getSelectedFile())!=null){
					if (!srcfile2.exists()){
						JOptionPane.showMessageDialog(mainframe,"文件无效！");
						return;
					}
					try {
						mainframe.setDirectory(srcfile2, 1);
						settings.put("open_dict", srcfile2.getPath());
						BufferedImage img = ImageIO.read(srcfile2);
						if (img == null){
							JOptionPane.showMessageDialog(mainframe,"这不是有效的图像文件！");
							return;
						}
						mainframe.setImage(img);
						srcimg2 = img;
						
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(mainframe,"这不是有效的图像文件！");
						e1.printStackTrace();
					}
				}
			}
		});
		
		mainframe.button_process.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					scx = SCXManager.load(RAW);
				}catch (FileNotFoundException exception){
					JOptionPane.showMessageDialog(mainframe,"无法打开"+RAW+"文件，因此无法生成。");
					return;
				}
				
				Terrain terr = scx.terrain;
				
				ImageProcessor.setColormap(colormap);
				
				if (srcimg != null){
					ImageProcessor.setTerrainSizeByImage(terr, srcimg, (int)settings.get("mode"));
					scx.removeAllUnits();
					ImageProcessor.convertImageToMap(srcimg, terr);
					if ((int)settings.get("plants") == 1)
						ImageProcessor.createObjects(srcimg, scx);
				}else if(srcimg2 != null){
					ImageProcessor.setTerrainSizeByImage(terr, srcimg2, (int)settings.get("mode"));
					scx.removeAllUnits();
				}else{
					return;
				}
				
				if (srcimg2 != null){
					ImageProcessor.convertImageToMapHill(srcimg2, terr);
					mapimg2 = ImageProcessor.getMapImageHill(terr);
				}
				
				mapimg = ImageProcessor.getMapImage(terr);
				mainframe.setImage(mapimg);
				
				
			}
		});
		
		mainframe.button_save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (scx == null){
					JOptionPane.showMessageDialog(mainframe,"尚未生成地图！");
					return;
				}
				mainframe.chooser2.showSaveDialog(mainframe);
				if ((dstfile = mainframe.chooser2.getSelectedFile())!=null){
					String path = dstfile.getPath();
					mainframe.setDirectory(dstfile, 2);
					settings.put("save_dict", path);
					if (path.endsWith(".scx") || path.endsWith(".scn"))
						SCXManager.save(path, scx);
					else
						SCXManager.save(path+".scx", scx);
				}
			}});
		
		//tiles image
		mainframe.buttons_control[0].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (srcimg != null){
					mainframe.setImage(srcimg);
				}
			}});
		
		//tiles map
		mainframe.buttons_control[1].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (mapimg != null){
					mainframe.setImage(mapimg);
				}
			}
		});
		
		//hill image
		mainframe.buttons_control[2].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (srcimg2 != null){
					mainframe.setImage(srcimg2);
				}
			}});
		
		//hill map
		mainframe.buttons_control[3].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (mapimg2 != null){
					mainframe.setImage(mapimg2);
				}
			}});

		//rotate ccw
		mainframe.buttons_control[4].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (mainframe.image == mapimg){
					ImageProcessor.rotateTerrain(scx.terrain, false);
					ImageProcessor.rotateImage(mapimg, false);
					mainframe.repaintCanvas();
				} else if (mainframe.image == mapimg2){
					ImageProcessor.rotateTerrainHill(scx.terrain, false);
					ImageProcessor.rotateImage(mapimg2, false);
					mainframe.repaintCanvas();
				}}});
		
		//trans
		mainframe.buttons_control[5].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (mainframe.image == mapimg){
					ImageProcessor.transTerrain(scx.terrain);
					ImageProcessor.transImage(mapimg);
					mainframe.repaintCanvas();
				} else if (mainframe.image == mapimg2){
					ImageProcessor.transTerrainHill(scx.terrain);
					ImageProcessor.transImage(mapimg2);
					mainframe.repaintCanvas();
				}}});
		
		//rotate cw
		mainframe.buttons_control[6].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (mainframe.image == mapimg){
					ImageProcessor.rotateTerrain(scx.terrain, true);
					ImageProcessor.rotateImage(mapimg, true);
					mainframe.repaintCanvas();
				} else if (mainframe.image == mapimg2){
					ImageProcessor.rotateTerrainHill(scx.terrain, true);
					ImageProcessor.rotateImage(mapimg2, true);
					mainframe.repaintCanvas();
				}}});
		
		
		mainframe.button_about.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				JOptionPane.showMessageDialog(mainframe, "Imagenario v"+VERSION+"\n这是一个简单而实用的工具，你只要选择图片，就可以按照它的颜色，生成相似的地图！\nBy 我是谁004(WAIFor)");
			}
		});
		
		mainframe.button_setting.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				settingdialog.setVisible(true);
				settingdialog.fill();
			}
		});
		
		
		//While closing, save settings.
		mainframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainframe.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				saveSettings();
				System.exit(0);
			}});
		
		settingdialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		settingdialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				settingdialog.submit();
				if (!settingdialog.newpalette.equals((String) settings.get("palette"))){
					settings.put("palette", settingdialog.newpalette);
					colormap = new Palette(settingdialog.newpalette);
				}
				settingdialog.setVisible(false);
			}});
	}
	
	static public void loadSettings(){
		File file = new File(SETTING_FILE);
		
		settings.put("palette", COLORMAP_ARTIST);
		settings.put("mode", ImageProcessor.SIZE_MODE_STRETCH);
		settings.put("plants", 1);
		settings.put("max_height", 7);
		settings.put("open_dict", "./");
		settings.put("save_dict", "./");
			
		if (file.exists()){
			try {
				BufferedReader br = new BufferedReader(new FileReader(SETTING_FILE));
				int category = 0; // 0-none, 1-file, 2-generate
				String line;
				while ((line=br.readLine()) != null){
					if (line.matches("\\s*\\[file\\]\\s*"))
						category = 1;
					else if (line.matches("\\s*\\[generate\\]\\s*"))
						category = 2;
					else if (line.matches(".+=.+")){
						line = line.split("\\s*;\\s*")[0];
						String[] set = line.split("\\s*=\\s*");
						if (category == 1){
							if (set[0].equals("open"))
								settings.put("open_dict", set[1]);
							else if (set[0].equals("save"))
								settings.put("save_dict", set[1]);
						}else if (category == 2){
							if (set[0].equals("mode"))
								settings.put("mode", Integer.parseInt(set[1]));
							else if (set[0].equals("plants"))
								settings.put("plants", Integer.parseInt(set[1]));
							else if (set[0].equals("palette"))
								settings.put("palette", set[1]);
							else if (set[0].equals("max_height"))
								settings.put("max_height", Integer.parseInt(set[1]));
						}
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	static public void saveSettings(){
		FileWriter fw;
		try {
			fw = new FileWriter(SETTING_FILE);
			fw.write("[file]\r\n");
			fw.write("open="+settings.get("open_dict")+"\r\n");
			fw.write("save="+settings.get("save_dict")+"\r\n");
			fw.write("\r\n[generate]\r\n");
			fw.write("palette="+settings.get("palette")+"\r\n");
			fw.write("mode="+settings.get("mode")+"\r\n");
			fw.write("plants="+settings.get("plants")+"\r\n");
			fw.write("max_height="+settings.get("max_height")+"\r\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
