package me.ino.tanarias.paintball;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import de.slikey.effectlib.EffectManager;
import me.ino.tanarias.paintball.Listeners;
import me.ino.tanarias.paintball.managers.TitleManager;
import me.ino.tanarias.paintball.Board;
import me.ino.tanarias.paintball.End;

public class Main extends JavaPlugin implements Listener{
	Logger logger = Logger.getLogger("Minecraft");
	EffectManager em;
	public Listeners listeners;
	public Board board;
	public End end;
	int game = 0;
	
	ArrayList<Player> isDead = new ArrayList<Player>();
	ArrayList<Player> hasBonus0 = new ArrayList<Player>();
	ArrayList<Player> hasBonus1 = new ArrayList<Player>();
	ArrayList<Player> hasBonus2 = new ArrayList<Player>();
	 
	Map<Player,Integer> heads = new HashMap<Player,Integer>();
	Map<Player,Integer> bodies = new HashMap<Player,Integer>();
	Map<Player,Integer> foots = new HashMap<Player,Integer>();
	
	Map<Player,Integer> deaths = new HashMap<Player,Integer>();
	Map<Player,Integer> bonus = new HashMap<Player,Integer>();
	
	Map<Player,Integer> killstreak = new HashMap<Player,Integer>();
	
	Map<Snowball,Location> proj = new HashMap<Snowball,Location>();
	
	public void onLoad(){
		RegeneWorld("world", "map", "world");
	}
	  
	public void onEnable(){
		long timea = System.currentTimeMillis();
		logger.info("[PB] Demarrage du plugin");
		
		this.listeners = new Listeners(this);
		this.board = new Board(this);
		this.end = new End(this);
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(listeners, this);
		getServer().getPluginManager().registerEvents(board, this);
		getServer().getPluginManager().registerEvents(end, this);
		em = new EffectManager(this);
		
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		
		long timeb = System.currentTimeMillis();
		long ms = timeb - timea;
		logger.info("[PB] Plugin demarre en " + ms + " ms");
	}
	
	public void onDisable(){
		em.dispose();
		logger.info("[PB] Arret du plugin");
	}
	
	public static void RegeneWorld(String target, String source, String worldname){
		Bukkit.unloadWorld(worldname, false);
		File targetF = new File(target);
		File sourceF = new File(source);
		deleteWorld(targetF);
		copyWorld(sourceF, targetF);
		new WorldCreator(worldname).environment(Environment.NORMAL).generateStructures(false).seed(0L);
	}
	
	public static void copyWorld(File source, File target){
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ArrayList ignore = new ArrayList(Arrays.asList(new String[] { "uid.dat", "session.dat" }));
		if (!ignore.contains(source.getName()))
			if (source.isDirectory()){
				if (!target.exists()){
		        	target.mkdirs();
		        }
		        String[] files = source.list();
		        for (String file : files){
		        	File srcFile = new File(source, file);
		        	File destFile = new File(target, file);
		        	copyWorld(srcFile, destFile);
		        }
			} else {
				try {
					InputStream in = new FileInputStream(source);
					OutputStream out = new FileOutputStream(target);
					byte[] buffer = new byte[1024];
		         	int length;
		         	while ((length = in.read(buffer)) > 0){
		         		out.write(buffer, 0, length);
		         	}
		         	in.close();
		         	out.close();
		        } catch(FileNotFoundException e){
		        	e.printStackTrace();
		    } catch(IOException e){
		        	e.printStackTrace();
		    }
		}
	}

	public static boolean deleteWorld(File file){
		if (file.exists()){
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++){
				if (files[i].isDirectory()){
					files[i].isDirectory();
					files[i].isDirectory();
		        }
			}
		}
	return file.delete();
	}
	
	public void kickPlayer(Player p, String motif){
		p.kickPlayer("[BungeeCord] Retour au lobby (" + motif + ")");
		/*
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF("lobby");
		player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
		*/
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){	
		Player p = null;
		p = Bukkit.getPlayer(sender.getName());
		if(commandLabel.equals("start") && p.isOp()){
			start_timer();
		} else if(commandLabel.equals("test") && p.isOp()){
			died(p,10);
		}
		return true;
	}
	
	public void start_timer(){
		logger.info("[PB] Lancement du timer");
		game = 1;
		Bukkit.broadcastMessage("§eLa partie commence dans §630 §esecondes !");
		
		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage("§eLa partie commence dans §620 §esecondes !");
			}
		}, 200);
		
		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage("§eLa partie commence dans §610 §esecondes !");
			}
		}, 400);
		
		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage("§eLa partie commence dans §c5 §esecondes !");
				for (Player p : Bukkit.getOnlinePlayers()){
					p.playNote(p.getLocation(), Instrument.BASS_DRUM, Note.flat(0, Tone.G));
				}
			}
		}, 500);
		
		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage("§eLa partie commence dans §c4 §esecondes !");
				for (Player p : Bukkit.getOnlinePlayers()){
					p.playNote(p.getLocation(), Instrument.BASS_DRUM, Note.flat(0, Tone.G));
				}
			}
		}, 520);
		
		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage("§eLa partie commence dans §c3 §esecondes !");
				for (Player p : Bukkit.getOnlinePlayers()){
					p.playNote(p.getLocation(), Instrument.BASS_DRUM, Note.flat(0, Tone.G));
				}
			}
		}, 540);
		
		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage("§eLa partie commence dans §c2 §esecondes !");
				for (Player p : Bukkit.getOnlinePlayers()){
					p.playNote(p.getLocation(), Instrument.BASS_DRUM, Note.flat(0, Tone.G));
				}
			}
		}, 560);
		
		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage("§eLa partie commence dans §c1 §eseconde !");
				for (Player p : Bukkit.getOnlinePlayers()){
					p.playNote(p.getLocation(), Instrument.BASS_DRUM, Note.flat(0, Tone.G));
				}
			}
		}, 580);
		
		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				init();
			}
		}, 600);
	}
	
	public void init(){
		logger.info("[PB] Debut de la partie");
		int i = 0;
		game = 2;
		
		Bukkit.broadcastMessage("§2§lLa partie a commencé, bonne chance !");
		Bukkit.broadcastMessage("§aObjectif: Toucher un maximum de joueurs en un temps imparti. Evitez cependant de vous faire toucher car cela vous fera perdre un temps précieux et vous empechera de profiter des bonus de killstreak.");
		
		final World w = Bukkit.getWorld("world");
		Location[] spawn = new Location[]{
				new Location(w, -524, 106, -349, -144, 1), 
				new Location(w, -461, 107, -413, 35, 3),
				new Location(w, -534, 106, -388, -59, 2),
				new Location(w, -487, 106, -338, -167, 4),
				new Location(w, -502, 119, -343, -175, 9),
				new Location(w, -505, 116, -405, -23, 6),
				new Location(w, -458, 121, -350, 114, 2), 
				new Location(w, -533, 121, -369, -93, 9)
		};
		
		for (Player p : Bukkit.getOnlinePlayers()){
			p.playNote(p.getLocation(), Instrument.PIANO, Note.flat(0, Tone.G));
			
			board.setupScoreboard(p, "05", "00");
			
			p.teleport(spawn[i]);
			p.setGameMode(GameMode.SURVIVAL);
			p.setExp((float) 0.5);
			
			p.getInventory().clear();
			giveStuff(p);
			
			Color c = Color.fromRGB(153, 51, 51);
			
			ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
	    	LeatherArmorMeta mhelmet = (LeatherArmorMeta) helmet.getItemMeta();
	    	mhelmet.setDisplayName("§c§lCasque neuf");
	    	mhelmet.setColor(c);
	    	helmet.setItemMeta(mhelmet);
	    	p.getInventory().setHelmet(helmet);
	    	
	    	ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
	    	LeatherArmorMeta mchestplate = (LeatherArmorMeta) chestplate.getItemMeta();
	    	mchestplate.setDisplayName("§c§lPlastron neuf");
	    	mchestplate.setColor(c);
	    	chestplate.setItemMeta(mchestplate);
	    	p.getInventory().setChestplate(chestplate);
	    	
	    	ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
	    	LeatherArmorMeta mleggings = (LeatherArmorMeta) leggings.getItemMeta();
	    	mleggings.setDisplayName("§c§lPantalon neuf");
	    	mleggings.setColor(c);
	    	leggings.setItemMeta(mleggings);
	    	p.getInventory().setLeggings(leggings);
	    	
	    	ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
	    	LeatherArmorMeta mboots = (LeatherArmorMeta) boots.getItemMeta();
	    	mboots.setDisplayName("§c§lBottes neuves");
	    	mboots.setColor(c);
	    	boots.setItemMeta(mboots);
	    	p.getInventory().setBoots(boots);
	    	
			i++;
		}
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			@SuppressWarnings("deprecation")
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()){
					if(!isDead.contains(p)){
						if(p.getExp() < 1){
							if(!hasBonus1.contains(p)){
								p.setExp((float) 0.0040 + p.getExp());
							}else{
								p.setExp((float) 0.0099 + p.getExp());
							}
						}
					}
					if(hasBonus0.contains(p)){
						p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12, 0));
					}
				}
				Random r = new Random();
				int data = r.nextInt(6);
				
				w.getBlockAt(-471, 101, -395).setTypeId(126);
				w.getBlockAt(-471, 101, -395).setData((byte) data);
				
				w.getBlockAt(-504, 101, -369).setTypeId(126);
				w.getBlockAt(-504, 101, -369).setData((byte) data);
				
				w.getBlockAt(-502, 96, -372).setTypeId(126);
				w.getBlockAt(-502, 96, -372).setData((byte) data);
				
				w.getBlockAt(-502, 97, -372).setTypeId(0);
			}
		}, 0, 3);
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run() {
				int m = 0, s = 0;
				
				m = (300 - board.i)/60;
				String min = "00";
				if(m>=0){
					min = "0" + m;
				}
				
				s = (300 - board.i)%60;
				String sec = "00";
				if(s>=0){
					if(s>9){sec = ""+s;} else {sec = "0"+s;};
				}
				
				for (Player p : Bukkit.getOnlinePlayers()){
					board.setupScoreboard(p, min, sec);
				}
				
				if(board.i == 300){
					end.end_game();
				}
				board.i++;
			}
		}, 0, 20);
		
		spawnBonus(0);
		spawnBonus(1);
		spawnBonus(2);
	}

	public void died(final Player p, final int sec){
		isDead.add(p);
		hasBonus0.remove(p);
		hasBonus1.remove(p);
		hasBonus2.remove(p);
		
		p.getInventory().clear();
		
		for (Player autres : Bukkit.getOnlinePlayers()){
			autres.hidePlayer(p);
			p.hidePlayer(autres);
		}
		
		if(p.getExp() > 0.5){
			int j = 0;
			final double dif = (p.getExp()-0.5)/12;
			for(j=0; j<12; j++){
				Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						p.setExp((float) (p.getExp()-dif));
					}
				}, 3*j);
			}
		} else {
			int j = 0;
			final double dif = (0.5-p.getExp())/10;
			for(j=0; j<10; j++){
				Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						p.setExp((float) (p.getExp()+dif));
					}
				}, 3*j);
			}
		}
		
		p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 99999, 1));
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99999, 1));
		
		int i = 2;
		for(i=2;i<sec;i++){
			final int j = (sec-i);
			Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					if(j != 1){
						TitleManager.sendActionBarPlayer(p, "§7Respawn dans " + j + " secondes");
					} else {
						TitleManager.sendActionBarPlayer(p, "§7Respawn dans 1 seconde");
					}
				}
			}, i*20);
		}	
		
		
		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				Random r = new Random();
				p.setVelocity(new Vector(r.nextInt(3),0.9,r.nextInt(3)));
				
				for (Player autres : Bukkit.getOnlinePlayers()){
					p.showPlayer(autres);
				}
			}
		}, (sec*20)-30);
		
		
		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				int i=0;
				for(i=0;i<6;i++){
					p.getWorld().playEffect(p.getLocation(), Effect.EXPLOSION, 10);
					p.getWorld().playEffect(p.getLocation().add(0, 1, 0), Effect.EXPLOSION, 10);
					p.getWorld().playEffect(p.getLocation().add(1, 0, -1), Effect.EXPLOSION, 10);
					p.getWorld().playEffect(p.getLocation().add(1, 0, 1), Effect.EXPLOSION, 10);
					p.getWorld().playEffect(p.getLocation().add(-1, 0, -1), Effect.EXPLOSION, 10);
					p.getWorld().playEffect(p.getLocation().add(-1, 0, 1), Effect.EXPLOSION, 10);
				}
			}
		}, (sec*20)-5);
		
		Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				giveStuff(p);
				for (Player autres : Bukkit.getOnlinePlayers()){
					autres.showPlayer(p);
				}
				p.removePotionEffect(PotionEffectType.SLOW);
				p.removePotionEffect(PotionEffectType.BLINDNESS);
				p.removePotionEffect(PotionEffectType.INVISIBILITY);
				TitleManager.sendActionBarPlayer(p, "");
				isDead.remove(p);
			}
		}, sec*20);
	}
	
	public void giveStuff(Player p){
		ItemStack stick = new ItemStack(Material.STICK, 1);
		ItemMeta stickMeta = stick.getItemMeta();
		stickMeta.setDisplayName("§c§lBaguette");
		stickMeta.addEnchant(Enchantment.ARROW_DAMAGE, 10, true);
		stick.setItemMeta(stickMeta);
		
		ItemStack blocs = new ItemStack(Material.STAINED_GLASS, 24, (byte) 14);
		ItemMeta blocsMeta = blocs.getItemMeta();
		blocsMeta.setDisplayName("§c§lBlocs");
		blocs.setItemMeta(blocsMeta);
		
		ItemStack bonus0 = new ItemStack(Material.INK_SACK, 1, (byte) 8);
		ItemMeta bonus0Meta = bonus0.getItemMeta();
		bonus0Meta.setDisplayName("§8§lBonus de vitesse");
		bonus0Meta.setLore(new ArrayList<String>(Arrays.asList(new String[]{"§8Etat: §7Non-Actif", "§8Description: §7+20% de vitesse, qui vous permet", "§7d'être bien plus rapide que vos adversaires,","§7et donc de pouvoir les surprendre facilement."})));
		bonus0.setItemMeta(bonus0Meta);
		
		ItemStack bonus1 = new ItemStack(Material.INK_SACK, 1, (byte) 8);
		ItemMeta bonus1Meta = bonus1.getItemMeta();
		bonus1Meta.setDisplayName("§8§lBonus d'énergie");
		bonus1Meta.setLore(new ArrayList<String>(Arrays.asList(new String[]{"§8Etat: §7Non-Actif", "§8Description: §7+150% de régénération d'énergie,", "§7qui passe donc de 2.4 points/seconde à 6,","§7et vous permet ainsi de tirer plus rapidement."})));
		bonus1.setItemMeta(bonus1Meta);
		
		ItemStack bonus2 = new ItemStack(Material.INK_SACK, 1, (byte) 8);
		ItemMeta bonus2Meta = bonus2.getItemMeta();
		bonus2Meta.setDisplayName("§8§lBonus de capacité");
		bonus2Meta.setLore(new ArrayList<String>(Arrays.asList(new String[]{"§8Etat: §7Non-Actif", "§8Description: §7+200% de capacité de chargeur,", "§7qui vous offre la possibilité de tirer trois", "§7boules à la place d'une seule en temps normal."})));
		bonus2.setItemMeta(bonus2Meta);
		
	
		p.getInventory().setItem(0, stick);
		p.getInventory().setItem(1, blocs);
		p.getInventory().setItem(6, bonus0);
		p.getInventory().setItem(7, bonus1);
		p.getInventory().setItem(8, bonus2);
	}
	
	public void spawnBonus(int id){
		World w = Bukkit.getWorld("world");
		
		switch (id){
			case 0:
				Location bonus0Loc = new Location(w, -470.5, 101.6, -394.5);
				
				ItemStack bonus0Item = new ItemStack(Material.FEATHER);
				ItemMeta bonus0Meta = bonus0Item.getItemMeta();
				bonus0Meta.setDisplayName("bonus");
				bonus0Item.setItemMeta(bonus0Meta);

				w.dropItem(bonus0Loc, bonus0Item);
				break;
			
			case 1:
				Location bonus1Loc = new Location(w, -503.5, 101.6, -368.5);
				
				ItemStack bonus1Item = new ItemStack(Material.EXP_BOTTLE);
				ItemMeta bonus1Meta = bonus1Item.getItemMeta();
				bonus1Meta.setDisplayName("bonus");
				bonus1Item.setItemMeta(bonus1Meta);

				w.dropItem(bonus1Loc, bonus1Item);
				break;
				
			case 2:
				Location bonus2Loc = new Location(w, -501.5, 96.6, -371.5);
				
				ItemStack bonus2Item = new ItemStack(Material.CHEST);
				ItemMeta bonus2Meta = bonus2Item.getItemMeta();
				bonus2Meta.setDisplayName("bonus");
				bonus2Item.setItemMeta(bonus2Meta);

				w.dropItem(bonus2Loc, bonus2Item);
				break;	
		}
	}
	
	public void giveBonus(final Player p, int id, int sec){
		switch (id){
			case 0:
				hasBonus0.add(p);

				ItemStack bonus0 = new ItemStack(Material.INK_SACK, 1, (byte) 12);
				ItemMeta bonus0Meta = bonus0.getItemMeta();
				bonus0Meta.setDisplayName("§9§lBonus de vitesse");
				bonus0Meta.setLore(new ArrayList<String>(Arrays.asList(new String[]{"§8Etat: §7Actif", "§8Description: §7+20% de vitesse, qui vous permet", "§7d'être bien plus rapide que vos adversaires,","§7et donc de pouvoir les surprendre facilement."})));
				bonus0.setItemMeta(bonus0Meta);
				p.getInventory().setItem(6, bonus0);
		
				Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						p.getInventory().remove(Material.FEATHER);
					} 
				}, 1);
				
				Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						hasBonus0.remove(p);
						if(!hasBonus0.contains(p) && !isDead.contains(p)){
							ItemStack bonus0 = new ItemStack(Material.INK_SACK, 1, (byte) 8);
							ItemMeta bonus0Meta = bonus0.getItemMeta();
							bonus0Meta.setDisplayName("§8§lBonus de vitesse");
							bonus0Meta.setLore(new ArrayList<String>(Arrays.asList(new String[]{"§8Etat: §7Non-Actif", "§8Description: §7+20% de vitesse, qui vous permet", "§7d'être bien plus rapide que vos adversaires,","§7et donc de pouvoir les surprendre facilement."})));
							bonus0.setItemMeta(bonus0Meta);
							p.getInventory().setItem(6, bonus0);
						}
					} 
				}, sec*20);
		
				Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						spawnBonus(0);
					} 
				}, 600);
			break;
			
			case 1:
				hasBonus1.add(p);
				
				ItemStack bonus1 = new ItemStack(Material.INK_SACK, 1, (byte) 10);
				ItemMeta bonus1Meta = bonus1.getItemMeta();
				bonus1Meta.setDisplayName("§a§lBonus d'énergie");
				bonus1Meta.setLore(new ArrayList<String>(Arrays.asList(new String[]{"§8Etat: §7Actif", "§8Description: §7+150% de régénération d'énergie,", "§7qui passe donc de 2.4 points/seconde à 6,","§7et vous permet ainsi de tirer plus rapidement."})));
				bonus1.setItemMeta(bonus1Meta);
				p.getInventory().setItem(7, bonus1);
				
				Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						p.getInventory().remove(Material.EXP_BOTTLE);
					} 
				}, 1);
				
				Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						hasBonus1.remove(p);
						if(!hasBonus1.contains(p) && !isDead.contains(p)){
							ItemStack bonus1 = new ItemStack(Material.INK_SACK, 1, (byte) 8);
							ItemMeta bonus1Meta = bonus1.getItemMeta();
							bonus1Meta.setDisplayName("§8§lBonus d'énergie");
							bonus1Meta.setLore(new ArrayList<String>(Arrays.asList(new String[]{"§8Etat: §7Non-Actif", "§8Description: §7+150% de régénération d'énergie,", "§7qui passe donc de 2.4 points/seconde à 6,","§7et vous permet ainsi de tirer plus rapidement."})));
							bonus1.setItemMeta(bonus1Meta);
							p.getInventory().setItem(7, bonus1);
						}
					} 
				}, sec*20);
				
				Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						spawnBonus(1);
					} 
				}, 600);
			break;
			
			case 2:
				hasBonus2.add(p);
				
				ItemStack bonus2 = new ItemStack(Material.INK_SACK, 1, (byte) 5);
				ItemMeta bonus2Meta = bonus2.getItemMeta();
				bonus2Meta.setDisplayName("§5§lBonus de capacité");
				bonus2Meta.setLore(new ArrayList<String>(Arrays.asList(new String[]{"§8Etat: §7Actif", "§8Description: §7+200% de capacité de chargeur,", "§7qui vous offre la possibilité de tirer trois", "§7boules à la place d'une seule en temps normal."})));
				bonus2.setItemMeta(bonus2Meta);
				p.getInventory().setItem(8, bonus2);
				
				Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						p.getInventory().remove(Material.CHEST);
					} 
				}, 1);
				
				Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						hasBonus2.remove(p);
						if(!hasBonus2.contains(p) && !isDead.contains(p)){
							ItemStack bonus2 = new ItemStack(Material.INK_SACK, 1, (byte) 8);
							ItemMeta bonus2Meta = bonus2.getItemMeta();
							bonus2Meta.setDisplayName("§8§lBonus de capacité");
							bonus2Meta.setLore(new ArrayList<String>(Arrays.asList(new String[]{"§8Etat: §7Non-Actif", "§8Description: §7+200% de capacité de chargeur,", "§7qui vous offre la possibilité de tirer trois", "§7boules à la place d'une seule en temps normal."})));
							bonus2.setItemMeta(bonus2Meta);
							p.getInventory().setItem(8, bonus2);
						}
					}
				}, sec*20);
				
				Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						spawnBonus(2);
					} 
				}, 600);
			break;
		}
	}
}