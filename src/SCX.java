import java.util.ArrayList;

public class SCX {
	
	static int PLAYER_COUNT = 16; 
	static int MESSAGE_COUNT = 6;
	static int CINEMATIC_COUNT = 4;
	static int BEHAVIOR_COUNT = 3;
	static int VICTORY_CONDITION_COUNT = 10;
	static int DISABLED_TECH_COUNT = 30;
	static int DISABLED_UNIT_COUNT = 30;
	static int DISABLED_BUILDING_COUNT = 20;
	static int RESOURCE_COUNT = 7;
	static int NAME_LENGTH = 256;
	
	String version = new String();
	int briefing_length;
	String briefing = new String();
	int next_unit_id;
	float version2;
	int[] player_count = new int[3];
	Player[] players = new Player[PLAYER_COUNT];
	Player gaia = new Player();
		
	int message_option_0;
	char message_option_1;
	float message_option_2;
	
	String filename = new String();
	
	int[] messages_st = new int[MESSAGE_COUNT];
	String[] messages = new String[MESSAGE_COUNT];
	
	String[] cinematics = new String[CINEMATIC_COUNT];
	
	int[] victories = new int[VICTORY_CONDITION_COUNT];
	
	int[] disability_options = new int[3];
	
	SCX.Bitmap bitmap = new SCX.Bitmap();
	
	Terrain terrain = new Terrain();
	
	ArrayList<Unit> units = new ArrayList<Unit>();
	
	public SCX(){
		for (int i=0; i<PLAYER_COUNT; ++i){
			this.players[i] = new Player();
		}
	}
	
	public int addUnit(float x, float y, float z, short constant, short player){
		Unit u = new Unit();
		u.x = x;
		u.y = y;
		u.z = z;
		u.constant = constant;
		u.player = player;
		u.rotation = (float)Math.random()*8;
		u.id = this.next_unit_id;
		++this.next_unit_id;
		units.add(u);
		if (player == 0)
			++this.gaia.unit_count;
		else
			++this.players[player-1].unit_count;
		
		return u.id;
	}

	public int addUnit(int x, int y, int z, short constant, short player){
		return addUnit(x+.5f, y+.5f, z, constant, player);
	}
	
	public void removeAllUnits(){
		units.clear();
		gaia.unit_count = 0;
		for (int i=0; i<PLAYER_COUNT; ++i){
			if (players[i] != null)
				players[i].unit_count = 0;
		}
		this.next_unit_id = 0;
	}
	
	
	static class Player{
		String name = new String(), ai = new String();
		String[] aic = new String[BEHAVIOR_COUNT];
		int name_st;
		char aitype;
		int bool, machine, profile, unknown;
		float[] resources = new float[RESOURCE_COUNT];
		int[] v_diplomacies = new int[PLAYER_COUNT];
		int alliedvictory, startage;
		int[] disabled_techs = new int[DISABLED_TECH_COUNT];
		int[] disabled_units = new int[DISABLED_UNIT_COUNT];
		int[] disabled_buildings = new int[DISABLED_BUILDING_COUNT];
		int unit_count = 0;
		String subtitle = new String();
		float[] view = new float[2];
		short[] view2 = new short[2];
		char allied;
		int colorid;
		byte[] special = new byte[0], special2 = new byte[0];
		byte[] stance1 = new byte[0], stance2 = new byte[0];
	}
	
	static class Bitmap{
		int bool, width, height;
		short def;
		byte[] bitmap;
	}
		
}
@SuppressWarnings("serial")
class StreamEndException extends Exception{
	public StreamEndException() {  //¸¸Àà·½·¨
		super();
	}
}
