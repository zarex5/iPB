package me.ino.tanarias.paintball;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import me.ino.tanarias.paintball.Main;

public class Board implements Listener{
	Main main;
	int i = 0, j = 0;
	int kills = 0, h = 0, b = 0, f = 0, d = 0, bonus = 0, score = 0;
	
	public Board(Main m) {
		this.main = m;
	}
	
	void setupScoreboard(Player p, String min, String sec){	
		d = main.deaths.get(p);
		
		h = main.heads.get(p);
		b = main.bodies.get(p);
		f = main.foots.get(p);
		
		bonus = main.bonus.get(p);
		
		kills = h + b + f;
		
		score = (h*140 + b*100 + f*60 + bonus*5) - d*50;
		
		ScoreboardManager sm = Bukkit.getScoreboardManager();
		Scoreboard infos = sm.getNewScoreboard();
		Objective o = infos.registerNewObjective("dash", "dummy");
		
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		o.setDisplayName("§c§l§o\u03DFPaintBall\u03DF");
		
		Score spacer1 = null;
		Score timeTitle = null;
		Score time = null;
		Score spacer2 = null;
		Score tirTitle = null;
		Score tirTete = null;
		Score tirCorps = null;
		Score tirPieds = null;
		Score spacer3 = null;
		Score mortsTitle = null;
		Score spacer4 = null;
		Score scoreSeparator = null;
		Score scoreTitle = null;
		
		try{
			spacer1 = o.getScore("§a");
			spacer1.setScore(13);

			timeTitle = o.getScore("§7§lTemps:");
			timeTitle.setScore(12);
			
			time = o.getScore("§8       " + min + ":" + sec);
			time.setScore(11);
			
			spacer2 = o.getScore("§b");
			spacer2.setScore(10);
			
			tirTitle = o.getScore("§7§lTirs: §3§l" + kills);
			tirTitle.setScore(9);
			
			tirTete = o.getScore("§7-Tete: §3" + h);
			tirTete.setScore(8);
			
			tirCorps = o.getScore("§7-Corps: §3" + b);
			tirCorps.setScore(7);
			
			tirPieds = o.getScore("§7-Pieds: §3" + f);
			tirPieds.setScore(6);
			
			spacer3 = o.getScore("§c");
			spacer3.setScore(5);
			
			mortsTitle = o.getScore("§7§lMorts: §3§l" + d);
			mortsTitle.setScore(4);
			
			spacer4 = o.getScore("§d");
			spacer4.setScore(3);
			
			scoreSeparator = o.getScore("§7------------");
			scoreSeparator.setScore(2);
			
			scoreTitle = o.getScore("§c§lScore: " + score);
			scoreTitle.setScore(1);
			
			
			p.setScoreboard(infos);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
}
