package me.ino.tanarias.paintball;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import de.slikey.effectlib.effect.DiscoBallEffect;
import de.slikey.effectlib.effect.TextEffect;
import me.ino.tanarias.paintball.Main;

public class End implements Listener{
	Main main;
	Board board;
	
	public End(Main m){
		this.main = m;
	}
	
	public void end_game(){
		main.logger.info("[PB] Fin de la partie");
		Bukkit.getScheduler().cancelAllTasks();
		
		final World w = Bukkit.getWorld("world");
		
		for (Entity ent : w.getEntities()){
			if (ent instanceof Item) {
				ent.remove();
			}
		}
		
		int i = 11000, it = 0;
		for(i=11000;i<14500;i+=250){
			final int time = i;
			Bukkit.getServer().getScheduler().runTaskLater(this.main, new Runnable() {
				public void run() {
					w.setTime(time);
				}
			}, it*4);
			it++;
		}
		
		for (Player p : Bukkit.getOnlinePlayers()){
			p.setExp(0);
			p.getInventory().clear();
			
			p.removePotionEffect(PotionEffectType.SLOW);
			p.removePotionEffect(PotionEffectType.BLINDNESS);
			
			for (Player autre : Bukkit.getOnlinePlayers()){
				p.showPlayer(autre);
				
				if((p != autre) && (getScore(p) == getScore(autre))){
					if(main.deaths.get(p) > main.deaths.get(autre)){
						main.bonus.put(p, 1);
						main.board.setupScoreboard(p, "00", "00");
					}else{
						main.bonus.put(autre, 1);
						main.board.setupScoreboard(autre, "00", "00");
					}
				}
			}
		}
		
		Bukkit.broadcastMessage("§2§lLa partie est terminée.");
		Bukkit.broadcastMessage("§6Calcul des résultats...");
		
		Bukkit.getServer().getScheduler().runTaskLater(this.main, new Runnable(){
			public void run(){
				anim();
				
				Bukkit.broadcastMessage("§7-------------------------------------");
				if(Bukkit.getOnlinePlayers().size() >= 3){
					for (Player p : Bukkit.getOnlinePlayers()){
						switch(getRank(p)){
							case 1:
								p.sendMessage("§7 1. §l" + getPlayerAtRank(1).getName() + " : " + getScore(getPlayerAtRank(1)) + " points");
								p.sendMessage("§7 2. " + getPlayerAtRank(2).getName() + " : " + getScore(getPlayerAtRank(2)) + " points");
								p.sendMessage("§7 3. " + getPlayerAtRank(3).getName() + " : " + getScore(getPlayerAtRank(3)) + " points");
								break;
							case 2:
								p.sendMessage("§7 1. " + getPlayerAtRank(1).getName() + " : " + getScore(getPlayerAtRank(1)) + " points");
								p.sendMessage("§7 2. §l" + getPlayerAtRank(2).getName() + " : " + getScore(getPlayerAtRank(2)) + " points");
								p.sendMessage("§7 3. " + getPlayerAtRank(3).getName() + " : " + getScore(getPlayerAtRank(3)) + " points");
								break;
							case 3:
								p.sendMessage("§7 1. " + getPlayerAtRank(1).getName() + " : " + getScore(getPlayerAtRank(1)) + " points");
								p.sendMessage("§7 2. " + getPlayerAtRank(2).getName() + " : " + getScore(getPlayerAtRank(2)) + " points");
								p.sendMessage("§7 3. §l" + getPlayerAtRank(3).getName() + " : " + getScore(getPlayerAtRank(3)) + " points");
								break;
							default:
								p.sendMessage("§7 1. " + getPlayerAtRank(1) + " : " + getScore(getPlayerAtRank(1)) + " points");
								p.sendMessage("§7 2. " + getPlayerAtRank(2) + " : " + getScore(getPlayerAtRank(2)) + " points");
								p.sendMessage("§7 3. " + getPlayerAtRank(3) + " : " + getScore(getPlayerAtRank(3)) + " points");
								p.sendMessage("§7 " + getRank(p) + ". §l" + p.getName() + " : " + getScore(p) + " points");
								break;
						}
					}
				}else if(Bukkit.getOnlinePlayers().size() == 2){
					for (Player p : Bukkit.getOnlinePlayers()){
						switch(getRank(p)){
							case 1:
								p.sendMessage("§7 1. §l" + getPlayerAtRank(1).getName() + " : " + getScore(getPlayerAtRank(1)) + " points");
								p.sendMessage("§7 2. " + getPlayerAtRank(2).getName() + " : " + getScore(getPlayerAtRank(2)) + " points");
								break;
							case 2:
								p.sendMessage("§7 1. " + getPlayerAtRank(1).getName() + " : " + getScore(getPlayerAtRank(1)) + " points");
								p.sendMessage("§7 2. §l" + getPlayerAtRank(2).getName() + " : " + getScore(getPlayerAtRank(2)) + " points");
								break;
						}
					}
				}else if(Bukkit.getOnlinePlayers().size() == 1){
					for (Player p : Bukkit.getOnlinePlayers()){
						p.sendMessage("§7 1. §l" + p.getName() + " : " + getScore(p) + " points");
					}
				}
				Bukkit.broadcastMessage("§7-------------------------------------");
			}
		}, 50);
	
		Bukkit.getServer().getScheduler().runTaskLater(this.main, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage("§c§oFélicitations à tous !");
			}
		}, 110);
	
		Bukkit.getServer().getScheduler().runTaskLater(this.main, new Runnable() {
			public void run() {
				Bukkit.broadcastMessage("§c§oFermeture du serveur dans quelques secondes...");
			}
		}, 120);
		
		Bukkit.getServer().getScheduler().runTaskLater(this.main, new Runnable() {
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()){
					main.kickPlayer(p, "Game_finished");
				}
				Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "stop");
			}
		}, 440);
	}
	
	
	public void anim(){
		World w = Bukkit.getWorld("world");
		int i = 0;
		
		for(i=0;i<Bukkit.getOnlinePlayers().size();i++){
			final int pos = i+1;
			Bukkit.getServer().getScheduler().runTaskLater(this.main, new Runnable() {
				public void run() {
					Location spawna = new Location(Bukkit.getWorld("world"), -492, 124, -349, 0, 0);
					Location spawnb = new Location(Bukkit.getWorld("world"), -492, 124, -402, 180, 0);
					
					TextEffect effecta = new TextEffect(main.em);
					effecta.iterations = 1;
					effecta.visibleRange = 999;
					effecta.text = pos + ". " + getPlayerAtRank(pos).getName() + " : " + getScore(getPlayerAtRank(pos));
					effecta.setLocation(spawna);
					effecta.speed = 0.01F;
					effecta.start();
			
					TextEffect effectb = new TextEffect(main.em);
					effectb.iterations = 1;
					effectb.visibleRange = 999;
					effectb.text = pos + ". " + getPlayerAtRank(pos).getName() + " : " + getScore(getPlayerAtRank(pos));
					effectb.setLocation(spawnb);
					effectb.speed = 0.01F;
					effectb.start();
				}
			}, i*55);
		}
		
		final Location[] spawns = new Location[]{
				new Location(w, -492, 126, -373), 
				new Location(w, -506, 126, -384), 
				new Location(w, -512, 126, -362), 
				new Location(w, -473, 126, -353), 
				new Location(w, -469, 126, -390)
		};
		
		for(i=0;i<150;i++){
			Bukkit.getServer().getScheduler().runTaskLater(this.main, new Runnable() {
				public void run() {
					Random r = new Random();
					Color c = Color.fromRGB(r.nextInt(255), r.nextInt(255), r.nextInt(255));
					int j = 0;
					
					for(j=0;j<5;j++){
						DiscoBallEffect effect = new DiscoBallEffect(main.em);
						effect.setLocation(spawns[j]);
						effect.lineColor = c;
						effect.sphereRadius = 0;
						effect.sphereParticles = 0;
						effect.visibleRange = 12;
						effect.iterations = 1;
						effect.start();
					}
				}
			}, i*4);
		}
	}

	
	public int getScore(Player p){
		int h = 0, b = 0, f = 0, d = 0, bonus = 0, score = 0;
		h = main.heads.get(p);
		b = main.bodies.get(p);
		f = main.foots.get(p);
		d = main.deaths.get(p);
		bonus = main.bonus.get(p);
		
		score = (h*140 + b*100 + f*60 + bonus*5) - d*50;
		return score;
	}
	
	
	public int getRank(Player p){
		int rank = 1;
		for (Player autre : Bukkit.getOnlinePlayers()){
			if(getScore(p) < getScore(autre)){
				rank++;
			}
		}
		return rank;
	}
	
	
	public Player getPlayerAtRank(int rank){
		Player p = null;
		for (Player autre : Bukkit.getOnlinePlayers()){
			if(getRank(autre) == rank){
				p = autre;
			}
		}
		return p;
	}
}
