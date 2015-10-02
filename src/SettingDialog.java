import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


public class SettingDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4813174274419388887L;
	
	ButtonGroup mode, plants, palette;
	JRadioButton[] radio_mode, radio_plants, radio_palette;
	
	JTextField max_height;
	
	String newpalette;
	
	JButton nullbutton = new JButton();
	
	public SettingDialog(MainFrame mainframe){
		super(mainframe);
		
		this.setSize(400, 400);
		this.setResizable(false);
		
		addLabel("地图尺寸", 100, 24, 20, 20);
		mode = new ButtonGroup();
		radio_mode = new JRadioButton[2];
		radio_mode[0] = addRadio("裁剪", 80, 24, 150, 20);
		radio_mode[1] = addRadio("延伸", 80, 24, 250, 20);
		mode.add(radio_mode[0]);
		mode.add(radio_mode[1]);
		
		addLabel("生成植物", 100, 24, 20, 90);
		plants = new ButtonGroup();
		radio_plants = new JRadioButton[2];
		radio_plants[0] = addRadio("不生成", 80, 24, 150, 90);
		radio_plants[1] = addRadio("生成", 80, 24, 250, 90);
		plants.add(radio_plants[0]);
		plants.add(radio_plants[1]);
		
		addLabel("生成调色板", 100, 24, 20, 160);
		palette = new ButtonGroup();
		radio_palette = new JRadioButton[3];
		radio_palette[0] = addRadio("艺术", 80, 24, 150, 160);
		radio_palette[1] = addRadio("实用", 80, 24, 250, 160);
		palette.add(radio_palette[0]);
		palette.add(radio_palette[1]);
		
		addLabel("海拔等级", 100, 24, 20, 230);
		max_height = addTextField(100, 24, 150, 230);
		

		
		add(nullbutton);
		nullbutton.setVisible(false);
	}
	
	public void center(){
		Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
		int width = sd.width;
		int height = sd.height;
		
		setLocation((width - this.getWidth())/2, (height-this.getHeight())/2);
	}
	
	public JRadioButton addRadio(String label, int w, int h, int x, int y){
		JRadioButton button = new JRadioButton(label);
		button.setSize(w, h);
		button.setLocation(x, y);
		this.add(button);
		return button;
	}
	
	public JLabel addLabel(String label, int w, int h, int x, int y){
		JLabel button = new JLabel(label);
		button.setSize(w, h);
		button.setLocation(x, y);
		this.add(button);
		return button;
	}
	
	public JTextField addTextField(int w, int h, int x, int y){
		JTextField button = new JTextField();
		button.setSize(w, h);
		button.setLocation(x, y);
		this.add(button);
		return button;
	}

	public void fill(){
		if ((int)Imagenario.settings.get("mode") == ImageProcessor.SIZE_MODE_TRIM)
			radio_mode[0].setSelected(true);
		else
			radio_mode[1].setSelected(true);
		
		if ((int)Imagenario.settings.get("plants") == ImageProcessor.PLANTS_NO)
			radio_plants[0].setSelected(true);
		else
			radio_plants[1].setSelected(true);
		
		String palette = (String)Imagenario.settings.get("palette");
		if (palette.equals(Imagenario.COLORMAP_ARTIST))
			radio_palette[0].setSelected(true);
		else if (palette.equals(Imagenario.COLORMAP_PRACTICAL))
			radio_palette[1].setSelected(true);
		
		max_height.setText(""+(int)Imagenario.settings.get("max_height"));

	}
	
	public void submit(){
		Imagenario.settings.put("mode", radio_mode[0].isSelected() ?
				ImageProcessor.SIZE_MODE_TRIM : ImageProcessor.SIZE_MODE_STRETCH);
		Imagenario.settings.put("plants", radio_plants[0].isSelected() ?
				ImageProcessor.PLANTS_NO : ImageProcessor.PLANTS_YES);
		
		if (radio_palette[0].isSelected())
			newpalette = Imagenario.COLORMAP_ARTIST;
		else if (radio_palette[1].isSelected())
			newpalette = Imagenario.COLORMAP_PRACTICAL;
		
		if (max_height.getText().matches("^\\d+$")){
			Imagenario.settings.put("max_height", Integer.parseInt(max_height.getText()));
		}
	}
}
