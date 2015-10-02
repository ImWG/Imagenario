import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


public class SCXManager {
	
	static SCX load (String scfile) throws FileNotFoundException{
		
		SCX scx = new SCX();
		File scxfile = new File(scfile);
		int flen = (int) scxfile.length();  
		
		try {
			FileInputStream fis;
			fis = new FileInputStream(scxfile);
			
			byte[] byte16 = new byte[16];
			byte[] byte4 = new byte[4];
			byte[] byte2 = new byte[2];
			byte[] byte1 = new byte[1];
			
			// general / version / 1
			fis.read(byte4);
			scx.version = new String(byte4);
			//System.out.println(scx.version);
			
			//general / header
			fis.read(byte4);
			
			//general / unknown / 1
			fis.read(byte4);
			
			//general / timestamp
			fis.read(byte4);
			//System.currentTimeMillis();
			
			//message / briefing
			fis.read(byte4);
			int briefing_length = $(byte4);
			//System.out.println(briefing_length);
			byte[] briefing = new byte[briefing_length];
			scx.briefing_length = briefing_length;
			
			fis.read(briefing);
			scx.briefing = new String(briefing);
			//System.out.println(scx.briefing);
			
			//general / unknown / 2
			fis.read(byte4);
			
			//players / count / 1
			fis.read(byte4);
			scx.player_count[0] = $(byte4);
			//System.out.println("Player Count:"+scx.player_count[0]);

	
			/* ************** BODY PART *************** */
			Inflater inf = getBody(fis, flen);
			
			fis.close();
			
			//objects / increment
			inf.inflate(byte4);
			scx.next_unit_id = $(byte4);
			//System.out.println("Next Object ID:"+objects);
			
			//version / 2
			scx.version2 = $F(inf, byte4);
			//System.out.println(new String(byte4));
			
			//players / name
			byte[] pname = new byte[SCX.NAME_LENGTH];
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				inf.inflate(pname);
				scx.players[i].name = new String(pname).trim();
				//System.out.println("Player #"+(i+1)+":"+scx.players[i].name);
			}
			
			//players / string
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				scx.players[i].name_st = $(inf, byte4);
			}
			
			//players / config
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				inf.inflate(byte4);
				scx.players[i].bool = $(byte4);
				inf.inflate(byte4);
				scx.players[i].machine = $(byte4);
				inf.inflate(byte4);
				scx.players[i].profile = $(byte4);
				inf.inflate(byte4);
				scx.players[i].unknown = $(byte4);
//				//System.out.printf("Player #%d: %d %d %d %d\n", i, 
//						scx.players[i].bool, scx.players[i].machine, scx.players[i].profile, scx.players[i].unknown);
			}
			
			//message / unknowns
			inf.inflate(byte4);
			scx.message_option_0 = $(byte4);
			inf.inflate(byte1);
			scx.message_option_1 = (char) byte1[0];
			inf.inflate(byte4);
			scx.message_option_2 = ByteConverter.getFloat(byte4, 0);
			
			//message / filename
			inf.inflate(byte2);
			short filename_length = ByteConverter.getShort(byte2, 0);
			byte[] filename = new byte[filename_length];
			inf.inflate(filename);
			scx.filename = new String(filename);
			//System.out.println(scx.filename);
			
			//# message / strings
			//# 0x01 = objective
			//# 0x02 = hints
			//# 0x03 = victory
			//# 0x04 = failure
			//# 0x05 = history
			//# 0x06 = scouts
			for (int i=0; i<SCX.MESSAGE_COUNT; ++i){
				scx.messages_st[i] = $(inf, byte4);
				//System.out.println(scx.messages_st[i]);
			}
			
			// message / scripts
			readStrings(inf, SCX.MESSAGE_COUNT, scx.messages);
			
			//message / cinematics
			readStrings(inf, SCX.CINEMATIC_COUNT, scx.cinematics);
			
			
			//message / bitmap
			{
				scx.bitmap.bool = $(inf, byte4);
				scx.bitmap.width = $(inf, byte4);
				scx.bitmap.height = $(inf, byte4);
				inf.inflate(byte2);
				scx.bitmap.def = ByteConverter.getShort(byte2, 0);
				if (scx.bitmap.bool > 0){
					byte[] bitmap = new byte[40+1024+scx.bitmap.width*scx.bitmap.height];
					inf.inflate(bitmap);
					scx.bitmap.bitmap = bitmap;
					//System.out.println("Bitmap Get");
				}else{
					scx.bitmap.bitmap = new byte[0];
					//System.out.println("No Bitmap");
				}
			}
			
			//behavior / names
			inf.inflate(new byte[SCX.PLAYER_COUNT*(SCX.BEHAVIOR_COUNT - 1)*2]); // SKIP ALL AOE1 PROPS
			//for (int i=0; i<3; ++i){
				for (int j=0; j<SCX.PLAYER_COUNT; ++j){
					inf.inflate(byte2);
					int length = ByteConverter.getShort(byte2, 0);
					if (length > 0){
						byte[] message = new byte[length];
						inf.inflate(message);
						scx.players[j].ai = new String(message);
					}
				}
			//}
				
			//behavior / size & data
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				int[] lengths = new int[SCX.BEHAVIOR_COUNT];
				for (int j=0; j<SCX.BEHAVIOR_COUNT; ++j){
					lengths[j] = $(inf, byte4);
				}for (int j=0; j<SCX.BEHAVIOR_COUNT; ++j){
					byte[] message = new byte[lengths[j]];
					inf.inflate(message);
					scx.players[i].aic[j] = new String(message);
				}
			}
			
			//behavior / type
			inf.inflate(byte16);
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				scx.players[i].aitype = (char)byte16[i];
			}
			
			//general / separator / 1
			inf.inflate(byte4);
			
			//player / config (2)
			inf.inflate(new byte[24*SCX.PLAYER_COUNT]);
			
			//general / separator / 2
			inf.inflate(byte4);
			
			//victory / globals
			//# 0x01 = conquest
			//# 0x02 = ruins
			//# 0x03 = artifacts
			//# 0x04 = discoveries
			//# 0x05 = explored
			//# 0x06 = gold count
			//# 0x07 = required
			//# 0x08 = condition
			//# 0x09 = score
			//# 0x0A = time limit
			for (int i=0; i<SCX.VICTORY_CONDITION_COUNT; ++i){
				scx.victories[i] = $(inf, byte4);
			}
			
			//victory / diplomacy / player / stance
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				for (int j=0; j<SCX.PLAYER_COUNT; ++j){
					scx.players[i].v_diplomacies[j] = $(inf, byte4);
				}
			}
			
			//victory / individual-victory (12 triggers per players) 
			//(they are unused in AoK/AoC once the new trigger system was introduced)
			inf.inflate(new byte[SCX.PLAYER_COUNT * 15 * 12 * 4]);
			
			//general / separator / 3
			inf.inflate(byte4);
			//System.out.println($(byte4));
			
			//victory / diplomacy / player / allied
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				scx.players[i].alliedvictory = $(inf, byte4);
			}//System.out.printf("\n");
			
			//disability / techtree
			int[] disables = {SCX.DISABLED_TECH_COUNT, SCX.DISABLED_UNIT_COUNT, SCX.DISABLED_BUILDING_COUNT};
			for (int i=0; i<3; ++i){
				inf.inflate(new byte[64]);
				for (int j=0; j<SCX.PLAYER_COUNT; ++j){
					for (int k=0; k<disables[i]; ++k){
						switch (i){
							case 0:scx.players[j].disabled_techs[k] = $(inf, byte4); break;
							case 1:scx.players[j].disabled_units[k] = $(inf, byte4); break;
							case 2:scx.players[j].disabled_buildings[k] = $(inf, byte4); break;
						}
						//System.out.printf("%d ", scx.players[j].disabled_techs[k]);
					}
					//System.out.printf("\n");
				}
			}
			
			//disability / options
			for (int i=0; i<3; ++i){
				scx.disability_options[i] = $(inf, byte4);
			}
			
			//disability / starting age
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				scx.players[i].startage = $(inf, byte4);
			}
			
			//general / separator / 4
			inf.inflate(byte4);
			//System.out.println($(byte4));
			
			//terrain / view
			inf.inflate(byte4);
			inf.inflate(byte4);
			
			//terrain / type
			inf.inflate(byte4);
			
			//terrain size
			scx.terrain.sizex = $(inf, byte4);
			scx.terrain.sizey = $(inf, byte4);
			
			//System.out.printf("%d ", scx.terrain.sizex);
			
			//terrain / data @TERRAIN
			byte[] byte3 = new byte[3];
			scx.terrain.initializeTiles();
			for (int i=0; i<scx.terrain.sizey; ++i){
				for (int j=0; j<scx.terrain.sizex; ++j){
					inf.inflate(byte3);
					scx.terrain.tiles[j][i] = (char) byte3[0];
					scx.terrain.hills[j][i] = (char) byte3[1];
				}
			}
			
			//players / count / 2
			// GAIA included
			int playercount = $(inf, byte4);
			scx.player_count[1] = playercount;
			//System.out.println(scx.player_count[0]);
			
			//player / sources & config
			for (int i=0; i<playercount-1; ++i){
				for (int j=0; j<SCX.RESOURCE_COUNT; ++j){
					scx.players[i].resources[j] = $F(inf, byte4); 
					//System.out.printf("%5d ", (int)scx.players[i].resources[j]);
				}
				//System.out.printf("\n");
			}
			
			//objects / players
			for (int i=0; i<playercount; ++i){
				int count = $(inf, byte4);
				//System.out.println("Player #"+i+" Units:"+count);
				if (i == 0)
					scx.gaia.unit_count = count;
				else
					scx.players[i-1].unit_count = count;
				for (int j=0; j<count; ++j){
					Unit u = new Unit();
					u.x = $F(inf, byte4);
					u.y = $F(inf, byte4);
					u.z = $F(inf, byte4);
					u.id = $(inf, byte4);
					inf.inflate(byte2);
					u.constant = ByteConverter.getShort(byte2, 0);
					inf.inflate(byte1);
					u.progress = (char) byte1[0];
					u.rotation = $F(inf, byte4);
					inf.inflate(byte2);
					u.frame = ByteConverter.getShort(byte2, 0);
					u.garrison = $(inf, byte4);
					u.player = (short) i;
					//System.out.printf("%.0f %.0f %.0f %d %d\n", u.x, u.y, u.z, u.id, u.constant);
					scx.units.add(u);
				}
			}
			//System.out.println("Total Units:"+scx.units.size());
			
			//players / count / 3 : Should be 9
			scx.player_count[2] = $(inf, byte4);
			//System.out.println("Players Count 3:"+scx.player_count[2]);
			
			//
			for (int i=1; i<playercount; ++i){
				SCX.Player player = scx.players[i-1]; 
				
				//player / script
				short len = $S(inf, byte2);
				//System.out.println(len);
				byte[] subtitle = new byte[len];
				inf.inflate(subtitle);
				player.subtitle = new String(subtitle);
				//System.out.println(player.subtitle);
				
				//player / views
				player.view[0] = $F(inf, byte4);
				player.view[1] = $F(inf, byte4);
				//System.out.println(player.view[0]+", "+player.view[1]);
				
				player.view2[0] = $S(inf, byte2);
				player.view2[1] = $S(inf, byte2);
				//System.out.println(player.view2[0]+", "+player.view2[1]);
				
				//player / diplomacy
				player.allied = $C(inf, byte1);
				int alliedcount = $S(inf ,byte2);
				//diplomacy / stance / 1
				player.stance1 = new byte[alliedcount];
				inf.inflate(player.stance1);
				//System.out.println(new String(stance1));
				//diplomacy / stance / 2
				player.stance2 = new byte[alliedcount << 2];
				inf.inflate(player.stance2);
				//System.out.println(new String(stance2));
				
				//player / color
				player.colorid = $(inf, byte4); 
				//System.out.println(player.colorid);
				
				//player / victory / version
				float number = $F(inf, byte4); 
				
				//player / victory / triggers / count
				short triggers = $S(inf, byte2);
				
				//player / victory / values
				if ((int)number == 2){
					player.special = new byte[8];
					inf.inflate(player.special);
				}
				
				//player / triggers / trigger (the ancient triggers
				inf.inflate(new byte[triggers*11*4]);
				
				//player / unknown
				player.special2 = new byte[7];
				inf.inflate(player.special2);
				
				//player / victory / unknown
				$(inf, byte4); 
				
			}
			
			inf.end();
			
			return scx;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DataFormatException e) {
			e.printStackTrace();
		} catch (StreamEndException e) {
			return scx;
		}
			
		return null;
	}
	
	static public int save(String scfile, SCX scx){
		try {
			FileOutputStream fos = new FileOutputStream(scfile);
			
			//general / version / 1
			fos.write(scx.version.getBytes());
			
			//general / header
			int header = 0;
			int briefing_length = length(scx.briefing);
			if (briefing_length != 0){
				header += briefing_length;
			}
			//System.out.println(header);
			fos.write($$(header + 20));
			
			//general / unknown / 1
			fos.write($$(2));
			
			//general / timestamp
			fos.write($$((int) System.currentTimeMillis()));
			
			//message / briefing
			fos.write($$(briefing_length));
			fos.write(scx.briefing.getBytes());
			
			//general / unknown / 2
			fos.write($$(1));
			
			//players / count / 1
			fos.write($$(scx.player_count[0]));
			
			/* ********* BODY ********* */
			FileOutputStream fos2 = new FileOutputStream("TEMP.tmp");
			
			//objects / increment
			fos2.write($$(scx.next_unit_id));
			
			//version / 2
			fos2.write($$(scx.version2));
			
			//players / name
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				fos2.write(scx.players[i].name.getBytes());
				fos2.write(new byte[SCX.NAME_LENGTH - length(scx.players[i].name)]);
			}
			
			//players / string
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				fos2.write($$(scx.players[i].name_st));}
			
			//players / config
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				SCX.Player player = scx.players[i];
				fos2.write($$(player.bool));
				fos2.write($$(player.machine));
				fos2.write($$(player.profile));
				fos2.write($$(player.unknown));
			}
			
			//message / unknowns
			fos2.write($$(scx.message_option_0));
			fos2.write($$(scx.message_option_1));
			fos2.write($$(scx.message_option_2));
			
			//message / filename
			writeString(fos2, scx.filename, true);
			
			//message / strings
			for (int i=0; i<SCX.MESSAGE_COUNT; ++i){
				fos2.write($$(scx.messages_st[i]));
			}
			
			//message / scripts
			for (int i=0; i<SCX.MESSAGE_COUNT; ++i){
				//System.out.println(length(scx.messages[i]));
				writeString(fos2, scx.messages[i], true);
			}
			
			//message / cinematics
			for (int i=0; i<SCX.CINEMATIC_COUNT; ++i){
				writeString(fos2, scx.cinematics[i], true);
			}
			
			//message / bitmap
			fos2.write($$(scx.bitmap.bool));
			fos2.write($$(scx.bitmap.width));
			fos2.write($$(scx.bitmap.height));
			fos2.write($$(scx.bitmap.def));
			fos2.write(scx.bitmap.bitmap);
			
			//behavior / names
			// SKIP ALL AOE1 PROPS
			fos2.write(new byte[SCX.PLAYER_COUNT*(SCX.BEHAVIOR_COUNT - 1)*2]);
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				writeString(fos2, scx.players[i].ai, true);}
			
			//behavior / size & data
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				for (int j=0; j<SCX.BEHAVIOR_COUNT; ++j){
					fos2.write($$(length(scx.players[i].aic[j])));
				}for (int j=0; j<SCX.BEHAVIOR_COUNT; ++j){
					fos2.write(scx.players[i].aic[j].getBytes());
				}
			}
			
			//behavior / type
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				fos2.write($$(scx.players[i].aitype));}
			
			//general / separator / 1
			fos2.write($$(-99));
			
			//player / config (2)
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				for (int j=0; j<6; ++j){
					fos2.write($$((int)scx.players[i].resources[j]));
				}
			}
			
			//general / separator / 2
			fos2.write($$(-99));
			
			//victory / globals
			for (int i=0; i<SCX.VICTORY_CONDITION_COUNT; ++i){
				fos2.write($$(scx.victories[i]));}
			
			//victory / diplomacy / player / stance
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				for (int j=0; j<SCX.PLAYER_COUNT; ++j){
					fos2.write($$(scx.players[i].v_diplomacies[j]));}
			}
			
			//victory / individual-victory
			fos2.write(new byte[SCX.PLAYER_COUNT * 15 * 12 * 4]);
			
			//general / separator / 3
			fos2.write($$(-99));
			
			//victory / diplomacy / player / allied
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				fos2.write($$(scx.players[i].alliedvictory));}
			
			//disability / techtree
			int[] disables = {SCX.DISABLED_TECH_COUNT, SCX.DISABLED_UNIT_COUNT, SCX.DISABLED_BUILDING_COUNT};
			for (int i=0; i<3; ++i){
				fos2.write(new byte[64]);
				for (int j=0; j<SCX.PLAYER_COUNT; ++j){
					for (int k=0; k<disables[i]; ++k){
						switch (i){
							case 0:fos2.write($$(scx.players[j].disabled_techs[k])); break;
							case 1:fos2.write($$(scx.players[j].disabled_units[k])); break;
							case 2:fos2.write($$(scx.players[j].disabled_buildings[k])); break;
						}
					}
				}
			}
			
			//disability / options
			for (int i=0; i<3; ++i){
				fos2.write($$(scx.disability_options[i]));}
			
			//disability / starting age
			for (int i=0; i<SCX.PLAYER_COUNT; ++i){
				fos2.write($$(scx.players[i].startage));
			}
			
			//general / separator / 4
			fos2.write($$(-99));
			
			//terrain / view
			fos2.write($$((int)scx.players[0].view[1]));
			fos2.write($$((int)scx.players[0].view[0]));
			
			//terrain / type
			fos2.write($$(0x0));
			
			//terrain size
			fos2.write($$(scx.terrain.sizex));
			fos2.write($$(scx.terrain.sizey));
			
			//terrain / data @TERRAIN
			for (int i=0; i<scx.terrain.sizey; ++i){
				for (int j=0; j<scx.terrain.sizex; ++j){
					fos2.write(new byte[]{(byte) scx.terrain.tiles[j][i], (byte) scx.terrain.hills[j][i], 0});}
			}
			
			//players / count / 2
			// GAIA included
			fos2.write($$(scx.player_count[1]));
			
			//player / sources & config
			for (int i=0; i<scx.player_count[1]-1; ++i){
				for (int j=0; j<SCX.RESOURCE_COUNT; ++j){
					fos2.write($$(scx.players[i].resources[j])); }
			}
			
			//objects / players
			for (int i=0; i<scx.player_count[1]; ++i){
				int count;
				if (i == 0)
					count = scx.gaia.unit_count;
				else
					count = scx.players[i-1].unit_count;
				//System.out.println("Player #"+i+" Units:"+count);
				fos2.write($$(count));
				int processed = 0;
				for (Unit u : scx.units){
					if (u.player == i){
						++processed;
						fos2.write($$(u.x));
						fos2.write($$(u.y));
						fos2.write($$(u.z));
						fos2.write($$(u.id));
						fos2.write($$(u.constant));
						fos2.write($$(u.progress));
						fos2.write($$(u.rotation));
						fos2.write($$(u.frame));
						fos2.write($$(u.garrison));
					}
					if (processed >= count)
						break;
				}
			}
			
			//players / count / 3 : Should be 9
			fos2.write($$(scx.player_count[2]));
			
			//
			for (int i=1; i<scx.player_count[1]; ++i){
				SCX.Player player = scx.players[i-1]; 
				
				//player / script
				writeString(fos2, player.subtitle, true);
				
				//player / views
				fos2.write($$(player.view[0]));
				fos2.write($$(player.view[1]));
				
				fos2.write($$(player.view2[0]));
				fos2.write($$(player.view2[1]));
				
				//player / diplomacy
				fos2.write($$(player.allied));
				//diplomacy / count
				fos2.write($$((short)player.stance1.length));
				//diplomacy / stance / 1
				fos2.write(player.stance1);
				//diplomacy / stance / 2
				fos2.write(player.stance2);
				
				//player / color
				fos2.write($$(player.colorid));
				
				//player / victory / version
				fos2.write($$(2.0f));
				
				//player / victory / triggers / count
				fos2.write($$((short)0));
				
				//player / victory / values
				fos2.write(player.special);
				
				//player / triggers / trigger (the ancient triggers
				
				//player / unknown
				fos2.write(player.special2);
				
				//player / victory / unknown
				fos2.write($$(-1));
			}
			
			
			/* ********* FOOT ********* */
			//double scx_version = 1.3;
			//fos2.write($$(scx_version));
			fos2.write(new byte[]{
					(byte) 0x9A, (byte) 0x99, (byte) 0x99, (byte) 0x99, 
					(byte) 0x99, (byte) 0x99, (byte) 0xF9, (byte) 0x3F});
			
			//if (scx_version >= 1.4){
			//	fos2.write(new byte[]{'\0'});}
			
			//number of triggers
			fos2.write($$(0));
			//Write the end
			fos2.write($$(0));
			fos2.write($$(0));
			
			fos2.close();
			
			FileInputStream fis2 = new FileInputStream("Temp.tmp");
			int bodylen = (int) new File("Temp.tmp").length();
			//System.out.println("length="+bodylen);
			byte[] body = new byte[bodylen];
			fis2.read(body);
			
			Deflater def = new Deflater(5, true);
			def.setInput(body);
			def.finish();
			byte[] buf = new byte[1024];
			int l;
			while((l = def.deflate(buf)) > 0){
				fos.write(buf, 0, l);
			}
			def.end();
			
			fos.close();
			
			
			return 1;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	static Inflater getBody(InputStream fis, int length){
		try {

			byte[] filecontent = new byte[length];
			
			fis.read(filecontent);
			
			Inflater inf = new Inflater(true);
			
			inf.setInput(filecontent, 0, length);
				
			return inf;
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	static private int $(byte[] bytes){
		return ByteConverter.byteArray2int(bytes, true);
	}
	static private float $F(byte[] bytes){
		return ByteConverter.getFloat(bytes, 0);
	}
	static private short $S(byte[] bytes){
		return ByteConverter.getShort(bytes, 0);
	}
	
	static private int $(Inflater inf, byte[] byte4) throws DataFormatException, StreamEndException{
		//System.out.println(inf.getBytesRead());
		if (inf.inflate(byte4) == 0)
			throw new StreamEndException();
		return $(byte4);
	}
	static private float $F(Inflater inf, byte[] byte4) throws DataFormatException, StreamEndException{
		if (inf.inflate(byte4) == 0)
			throw new StreamEndException();
		return $F(byte4);
	}
	static private short $S(Inflater inf, byte[] byte2) throws DataFormatException, StreamEndException{
		if (inf.inflate(byte2) == 0)
			throw new StreamEndException();
		return $S(byte2);
	}
	static private void readStrings(Inflater inf, int count, String[] target) throws DataFormatException, StreamEndException{
		byte[] byte2 = new byte[2];
		for (int i=0; i<count; ++i){
			if (inf.inflate(byte2) == 0)
				throw new StreamEndException();
			int length = ByteConverter.getShort(byte2, 0);
			byte[] message = new byte[length];
			inf.inflate(message);
			target[i] = new String(message);
		}
	}
	static private char $C(Inflater inf, byte[] byte1) throws DataFormatException, StreamEndException{
		if (inf.inflate(byte1) == 0)
			throw new StreamEndException();
		return (char)byte1[0];
	}
	
	static private byte[] $$(int num){
		return ByteConverter.int2byteArray(num, true);
	}
	static private byte[] $$(float num){
		byte[] byte4 = new byte[4];
		ByteConverter.putFloat(byte4, num, 0);
		return byte4;
	}
	@SuppressWarnings("unused")
	static private byte[] $$(double num){
		byte[] byte8 = new byte[8];
		ByteConverter.putDouble(byte8, num, 0);
		return byte8;
	}
	static private byte[] $$(short num){
		byte[] byte2 = new byte[2];
		ByteConverter.putShort(byte2, num, 0);
		return byte2;
	}
	static private byte[] $$(char num){
		byte[] byte1 = new byte[1];
		byte1[0] = (byte)num;
		return byte1;
	}
	
	
	
	static private int length(String str){
		if (str.getBytes().length <= 1)
			return 0;
		else
			return str.getBytes().length;
	}
	
	static private void writeString(FileOutputStream os, String str, boolean isShort) throws IOException{
		if (isShort)
			os.write($$((short)length(str)));
		else
			os.write($$(length(str)));
		if (length(str) == 0)
			return;
		os.write(str.getBytes());
	}
	
	
	
	
	static public void pack(String headfile, String bodyfile, String destfile){
		try {
			FileOutputStream fos = new FileOutputStream(destfile);
			
			File head = new File(headfile);
			FileInputStream fis = new FileInputStream(head);
			int headlength = (int) head.length();
			byte[] headbytes = new byte[headlength];
			fis.read(headbytes);
			fos.write(headbytes);
			
			
			Deflater def = new Deflater(9, true);
			
			File body = new File(bodyfile);
			FileInputStream fis2 = new FileInputStream(body);
			
			int bodylength = (int) body.length();
			byte[] bodybytes = new byte[bodylength];
			fis2.read(bodybytes);
			
			def.setInput(bodybytes);
			def.finish();
			
			byte[] buf = new byte[1024];
			int l;
			while((l = def.deflate(buf)) > 0){
				fos.write(buf, 0, l);
			}
			def.end();
			
			fos.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
