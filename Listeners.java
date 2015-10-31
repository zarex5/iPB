package me.ino.tanarias.paintball;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import me.ino.tanarias.paintball.Main;
import me.ino.tanarias.paintball.managers.TitleManager;

public class Listeners implements Listener{
	Main main;
	
	public Listeners(Main m){
		this.main = m;
	}
	
	
	//Event: Connection d'un joueur.
	//Si la partie a déjà commencée ou si le serveur est plein, on n'autorise pas sa connection.
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e){
		if(main.game == 2){
			e.disallow(Result.KICK_OTHER, "Erreur: La partie à déjà commencé.");
		} else if(Bukkit.getOnlinePlayers().size() > 7){
			e.disallow(Result.KICK_OTHER, "Erreur: Le serveur est complet.");
		}
	}
	
	
	//Event: Arrivée d'un joueur.
	//On indique aux joueurs connectés qu'un nouveau joueur les à rejoint, on indique dans quelles conditions la parties pourra
	//démarrer en fonction du nombre de joueurs, et si il y a 4 joueurs connectés on lance le timer
	//On téléporte le joueur au spawn, on le clear, on le met en survie, on met sa nouriture au max et sa saturation de même
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();
		e.setJoinMessage(null);
		
		Bukkit.broadcastMessage("§7" + p.getName() + "§e a rejoint la partie ! (§b" + Bukkit.getOnlinePlayers().size() + "§e/§b8§e)");
		if(Bukkit.getOnlinePlayers().size() < 3 && main.game == 0){
			TitleManager.sendActionBarAllPlayers("§7§oIl manque " + (4-Bukkit.getOnlinePlayers().size()) + " joueurs pour commencer la partie.");
		
		} else if(Bukkit.getOnlinePlayers().size() == 3 && main.game == 0){
			TitleManager.sendActionBarAllPlayers("§7§oIl manque 1 joueur pour commencer la partie.");
		
		} else if(Bukkit.getOnlinePlayers().size() == 4 && main.game == 0){
			TitleManager.sendActionBarAllPlayers("§a§oLa partie va pouvoir commencer...");
			main.start_timer();
		}
		
		main.heads.put(p, 0);
		main.bodies.put(p, 0);
		main.foots.put(p, 0);
		main.deaths.put(p, 0);
		main.bonus.put(p, 0);
		main.killstreak.put(p, 0);
		
		p.removePotionEffect(PotionEffectType.SLOW);
		p.removePotionEffect(PotionEffectType.BLINDNESS);
		
		Location spawn = new Location(e.getPlayer().getWorld(), -492, 129, -373);
		p.teleport(spawn);
		p.setGameMode(GameMode.SURVIVAL);
		
		p.getInventory().clear();
		p.getInventory().setHelmet(null);
		p.getInventory().setChestplate(null);
		p.getInventory().setLeggings(null);
		p.getInventory().setBoots(null);
		
		ItemStack itemA = new ItemStack(Material.NAME_TAG, 1);
		ItemMeta itemAMeta = itemA.getItemMeta();
		itemAMeta.setDisplayName("§7§lChoix de la couleur: §r§8Aléatoire");
		itemAMeta.setLore(Arrays.asList("§8§o> Clic droit pour changer."));
		itemA.setItemMeta(itemAMeta);
		p.getInventory().setItem(0, itemA);
		
		ItemStack itemB = new ItemStack(Material.TRIPWIRE_HOOK, 1);
		ItemMeta itemBMeta = itemB.getItemMeta();
		itemBMeta.setDisplayName("§7§lChoix du bonus préféré: §r§8Aléatoire");
		itemBMeta.setLore(Arrays.asList("§8§o> Clic droit pour changer."));
		itemB.setItemMeta(itemBMeta);
		p.getInventory().setItem(1, itemB);
		
		ItemStack porte = new ItemStack(Material.WOOD_DOOR, 1);
		ItemMeta porteMeta = porte.getItemMeta();
		porteMeta.setDisplayName("§7§lQuitter");
		porte.setItemMeta(porteMeta);
		p.getInventory().setItem(8, porte);
		
		p.setLevel(0);
		p.setExp(0);
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setSaturation(9999);
	}
	
	
	//Event: Déconnection d'un joueur.
	//On affiche simplement un message de départ custom, dans le même type de celui de connection
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e){
		e.setQuitMessage("§7" + e.getPlayer().getName() + "§e a quitté !");
	}
	
	
	//Event: Départ d'un joueur suite à un kick.
	//On n'affiche aucun message de départ
	@EventHandler
	public void onPlayerKick(PlayerKickEvent e){
		e.setLeaveMessage(null);
	}
	
	
	//Event: Clic sur un inventaire.
	//On cancel l'event dans tout les cas pour interdire de déplacer ses items.
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e){
		e.setCancelled(true);
	}
	
	
	//Event: Drop d'un item.
	//On cancel l'event dans tout les cas pour interdire de jeter ses items.
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e){
		e.setCancelled(true);
	}
	
	
	//Event: Interaction d'un joueur.
	//-Si le joueur fait un clic droit (air ou block) avec un stick dans la main, alors si il a de l'xp:
	//on lui fait tirer une boule de neige et on lui enlève un peu d'xp
	//-Si le joueur clique sur un portillon, on cancel l'event (pour l'empecher de l'ouvrir)
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		if((e.getAction() == Action.RIGHT_CLICK_AIR) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)){	
			if(e.getPlayer().getItemInHand().getType() == Material.STICK){
				Player p = e.getPlayer();
	        	if(p.getExp() > 0){
	        		Projectile boule = p.launchProjectile(Snowball.class);
	        		boule.setShooter(p);
	        		
	        		if(main.hasBonus2.contains(p)){
	        			Projectile bouleSupA = p.launchProjectile(Snowball.class);
		        		bouleSupA.setShooter(p);
	        			Projectile bouleSupB = p.launchProjectile(Snowball.class);
		        		bouleSupB.setShooter(p);	
	        		}
	        		
	        		e.getPlayer().setExp(e.getPlayer().getExp() - (float) 0.040);
	        		
	        	} else {
	        		e.getPlayer().sendMessage("§cVous n'avez pas assez d'énergie pour tirer !");
	        	}
	            e.setCancelled(true);
	        }
			if(e.getPlayer().getItemInHand().getType() == Material.WOOD_DOOR){
	        	main.kickPlayer(e.getPlayer(), "Player_quit");
	            e.setCancelled(true);
	        }
			if(e.getPlayer().getItemInHand().getType() == Material.NAME_TAG){
	        	ItemStack itemA = new ItemStack(Material.NAME_TAG, 1);
	    		ItemMeta itemAMeta = itemA.getItemMeta();
	    		itemAMeta.setDisplayName("§7§lChoix de la couleur: §r§5Coming soon !");
	    		itemAMeta.setLore(Arrays.asList("§8§o> Clic droit pour changer."));
	    		itemA.setItemMeta(itemAMeta);
	    		e.getPlayer().getInventory().setItem(0, itemA);
	    		
	            e.setCancelled(true);
	        }
			if(e.getPlayer().getItemInHand().getType() == Material.TRIPWIRE_HOOK){
	        	ItemStack itemB = new ItemStack(Material.TRIPWIRE_HOOK, 1);
	    		ItemMeta itemBMeta = itemB.getItemMeta();
	    		itemBMeta.setDisplayName("§7§lChoix du bonus préféré: §r§5Coming soon !");
	    		itemBMeta.setLore(Arrays.asList("§8§o> Clic droit pour changer."));
	    		itemB.setItemMeta(itemBMeta);
	    		e.getPlayer().getInventory().setItem(1, itemB);
	    		
	            e.setCancelled(true);
	        }
		}
		if(e.getClickedBlock() instanceof Block){
			if(e.getClickedBlock().getType() == Material.DARK_OAK_FENCE_GATE){
				e.setCancelled(true);
			}
		}
	}
	
	
	//Event: Lancement d'un projectile.
	//A recommenter
	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent e){
		if(e.getEntity() instanceof Snowball){
			int i=2;
			final Snowball boule = (Snowball) e.getEntity();
			final World w = Bukkit.getWorld("world");
			main.proj.put(boule, null);
			for(i=2;i<122;i++){
				Bukkit.getServer().getScheduler().runTaskLater(this.main, new Runnable() {
					public void run() {
						if(boule.getCustomName() != "died"){
							Location loc = boule.getLocation();
							for (Player p : Bukkit.getOnlinePlayers()){
								p.spigot().playEffect(loc, Effect.COLOURED_DUST, 0, 0, 0.600F, 0.200F, 0.200F, 1, 0, 25);
								p.spigot().playEffect(loc, Effect.COLOURED_DUST, 0, 0, 0.600F, 0.200F, 0.200F, 1, 0, 25);	
							}
								
							Location old = main.proj.get(boule);
							if(old != null){
								Location mid = new Location(w, ((loc.getX() + old.getX()) / 2), ((loc.getY() + old.getY()) / 2), ((loc.getZ() + old.getZ()) / 2));
								Location mida = new Location(w, (((loc.getX() + old.getX()) / 2) + loc.getX()) / 2, (((loc.getY() + old.getY()) / 2) + loc.getX()) / 2, (((loc.getZ() + old.getZ()) / 2) + loc.getX()) /2);
								Location midb = new Location(w, (((loc.getX() + old.getX()) / 2) + old.getY()) / 2, (((loc.getY() + old.getY()) / 2) + old.getY()) / 2, (((loc.getZ() + old.getZ()) / 2) + old.getY()) / 2);
									
								for (Player p : Bukkit.getOnlinePlayers()){
									p.spigot().playEffect(mid, Effect.COLOURED_DUST, 0, 0, 0.600F, 0.200F, 0.200F, 1, 0, 25);
									p.spigot().playEffect(mid, Effect.COLOURED_DUST, 0, 0, 0.600F, 0.200F, 0.200F, 1, 0, 25);
									
									p.spigot().playEffect(mida, Effect.COLOURED_DUST, 0, 0, 0.600F, 0.200F, 0.200F, 1, 0, 25);
									p.spigot().playEffect(mida, Effect.COLOURED_DUST, 0, 0, 0.600F, 0.200F, 0.200F, 1, 0, 25);	
									
									p.spigot().playEffect(midb, Effect.COLOURED_DUST, 0, 0, 0.600F, 0.200F, 0.200F, 1, 0, 25);
									p.spigot().playEffect(midb, Effect.COLOURED_DUST, 0, 0, 0.600F, 0.200F, 0.200F, 1, 0, 25);
								}
							}
							main.proj.put(boule, boule.getLocation());
						}
					} 
				}, i);
			}	
		}
	}
	
	
	//Event: Projectile hurte quelque chose.
	//Si le projectile est une boule de neige et qu'elle à été lancée par un joueur,
	//et si le bloc théoriquement touché fait partie de la liste de blocs "autorisés à changer"
	//alors on le remplace par un bloc de laine de la couleur du joueur qui a tiré la boule
	//et on programme la remise du bloc original 10 secondes plus tard.
	//On stoque également (sauf si c'est de la laine bien sur pour ne pas causer de bugs) le bloc et la data
	//du bloc au dessus de celui changé, au cas ou il serait cassé par le changement (ex: fleur, herbe, arbuste, etc)
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onProjectileHit(final ProjectileHitEvent e){
		if(e.getEntity() instanceof Snowball){
			Projectile boule = (Projectile) e.getEntity();
			boule.setCustomName("died");
			BlockIterator iterator = new BlockIterator(e.getEntity().getWorld(), e.getEntity().getLocation().toVector(), e.getEntity().getVelocity().normalize(), 0.0D, 4);
			Block hitBlock = null;
			
			while (iterator.hasNext()){
				hitBlock = iterator.next();
				if ((hitBlock.getType() == Material.DIRT) 
						|| (hitBlock.getType() == Material.LEAVES)
						|| (hitBlock.getType() == Material.LEAVES_2)
						|| (hitBlock.getType() == Material.GRAVEL)
						|| (hitBlock.getType() == Material.STAINED_CLAY)
						|| (hitBlock.getType() == Material.CLAY)
						|| (hitBlock.getType() == Material.STONE)
						|| (hitBlock.getType() == Material.GRASS)
						|| (hitBlock.getType() == Material.LOG)
						|| (hitBlock.getType() == Material.WOOL)
						|| (hitBlock.getType() == Material.LOG_2)){

					if (hitBlock.getType() == Material.WOOL){
						break;
					}
					
					final Material oldMaterial = hitBlock.getType();
					final Block oldBlock = hitBlock;
					final byte oldData = hitBlock.getData();
					
					Location ne = oldBlock.getLocation().add(0, 1, 0);
					final Block oldBlockP = ne.getBlock();
					final Material oldMaterialP = oldBlockP.getType();
					final byte oldDataP = oldBlockP.getData();
					
					hitBlock.setType(Material.WOOL);
					hitBlock.setData((byte) 14);
					
					Bukkit.getServer().getScheduler().runTaskLater(this.main, new Runnable(){
						public void run() {
							oldBlock.setType(oldMaterial);
					    	oldBlock.setData(oldData);
	
					    	if((oldMaterialP != Material.WOOL) && (oldMaterialP != Material.STAINED_GLASS) && (oldBlockP.getType() != Material.STAINED_GLASS)){
					    		oldBlockP.setType(oldMaterialP);
					    		oldBlockP.setData(oldDataP);
					    	}
						}
					}, 400);
					break;
				}
			}
		}	
	}
	
	
	//Event: Entitée prend des dégats par une autre entitée.
	//Si le dommage a été causé par une boule de neige et a que l'entitée en question est un joueur
	//alors on calcule, on fonction de la position en y de la boule et la position en y du joueur touché,
	//la position de la boule par rapport au joueur, pour en déduire la partie du corps ou il a été touché.
	//En fonction de la partie touchée, on donne au joueur touché une pièce d'armure du lieu ou il a été touché et de la couleur
	//du joueur qui l'a touché. On affiche également un message dans le chat avec le nom des deux joueurs.
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e){
	    if((e.getDamager() instanceof Snowball) && (e.getEntity() instanceof Player)){
	    	Projectile boule = (Projectile) e.getDamager();
	    	
	    	final Player pdamager  = (Player) boule.getShooter();
	    	Player pdamaged = (Player) e.getEntity();
	    	
	    	boule.setCustomName("died");
	    	
	    	if(pdamager == pdamaged){
	    		e.setCancelled(true);
	    		return;
	    	}
	    	
	    	if(main.isDead.contains(pdamaged)){
	    		e.setCancelled(true);
	  			Vector initVel = boule.getVelocity();
				final Projectile newBoule = pdamaged.launchProjectile(Snowball.class);
			  	newBoule.setVelocity(initVel);
			  	Bukkit.getServer().getScheduler().runTaskLater(this.main, new Runnable(){
					public void run(){
						newBoule.setShooter(pdamager);
					}
				}, 1);
	    		return;
	    	}

			double ye = boule.getLocation().getY() - pdamaged.getLocation().getY();
			
			Random r = new Random();
			Color c = Color.fromRGB(r.nextInt(255), r.nextInt(255), r.nextInt(255));
			
			if(ye > 1.70){
				ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
		    	LeatherArmorMeta mhelmet = (LeatherArmorMeta) helmet.getItemMeta();
		    	mhelmet.setDisplayName("§c§lCasque taché");
		    	mhelmet.setLore(Arrays.asList("§rDernière tache par " + pdamager.getName()));
		    	mhelmet.setColor(c);
		    	helmet.setItemMeta(mhelmet);
		    	pdamaged.getInventory().setHelmet(helmet);
		    	
		    	Bukkit.broadcastMessage("§c" + pdamaged.getName() + " §7a été touché à la tête par §c" + pdamager.getName());
		    	TitleManager.sendActionBarPlayer(pdamaged, "§7Vous avez été touché à la tête par " + pdamager.getName());
		    	main.died(pdamaged, 16);
		    	
		    	main.heads.put(pdamager, main.heads.get(pdamager)+1);
		    	
			}else if(ye > 1.10){
				ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
		    	LeatherArmorMeta mchestplate = (LeatherArmorMeta) chestplate.getItemMeta();
		    	mchestplate.setDisplayName("§c§lPlastron taché");
		    	mchestplate.setLore(Arrays.asList("§rDernière tache par " + pdamager.getName()));
		    	mchestplate.setColor(c);
		    	chestplate.setItemMeta(mchestplate);
		    	pdamaged.getInventory().setChestplate(chestplate);
		    	
		    	Bukkit.broadcastMessage("§c" + pdamaged.getName() + " §7a été touché par §c" + pdamager.getName());
		    	TitleManager.sendActionBarPlayer(pdamaged, "§7Vous avez été touché par " + pdamager.getName());
		    	main.died(pdamaged, 12);
		    	
		    	main.bodies.put(pdamager, main.bodies.get(pdamager)+1);
		    	
			}else if(ye > 0.50){
				ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
		    	LeatherArmorMeta mleggings = (LeatherArmorMeta) leggings.getItemMeta();
		    	mleggings.setDisplayName("§c§lPantalon taché");
		    	mleggings.setLore(Arrays.asList("§rDernière tache par " + pdamager.getName()));
		    	mleggings.setColor(c);
		    	leggings.setItemMeta(mleggings);
		    	pdamaged.getInventory().setLeggings(leggings);
		    	
		    	Bukkit.broadcastMessage("§c" + pdamaged.getName() + " §7a été touché par §c" + pdamager.getName());
		    	TitleManager.sendActionBarPlayer(pdamaged, "§7Vous avez été touché par " + pdamager.getName());
		    	main.died(pdamaged, 12);
		    	
		    	main.bodies.put(pdamager, main.bodies.get(pdamager)+1);
		    	
			}else{
				ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		    	LeatherArmorMeta mboots = (LeatherArmorMeta) boots.getItemMeta();
		    	mboots.setDisplayName("§c§lBottes tachées");
		    	mboots.setLore(Arrays.asList("§rDernière tache par " + pdamager.getName()));
		    	mboots.setColor(c);
		    	boots.setItemMeta(mboots);
		    	pdamaged.getInventory().setBoots(boots);
		    	
		    	Bukkit.broadcastMessage("§c" + pdamaged.getName() + " §7a été touché dans les pieds par §c" + pdamager.getName());
		    	TitleManager.sendActionBarPlayer(pdamaged, "§7Vous avez été touché dans les pieds par " + pdamager.getName());
		    	main.died(pdamaged, 8);
		    	
		    	main.foots.put(pdamager, main.foots.get(pdamager)+1);
			}
	    	main.deaths.put(pdamaged, main.deaths.get(pdamaged)+1);
	    	main.killstreak.put(pdamaged, 0);
	    	main.killstreak.put(pdamager, main.killstreak.get(pdamager)+1);
	    	
	    	
	    	switch(main.killstreak.get(pdamager)){
	    		case 3:
	    			//On lui donne son préféré (pour 30sec)
					pdamager.sendMessage("§9Vous avez gagné un bonus de vitesse grâce à votre série de trois kills à la suite ! (+200% pendant 30s)");
	    			main.giveBonus(pdamager, 0, 30);
	    			break;
	    			
	    		case 5:
	    			//On lui donne son préféré + un aléatoire (pour 60sec)
	    			pdamager.sendMessage("§9Vous avez gagné un bonus de vitesse grâce à votre série de cinq kills à la suite ! (+200% pendant 60s)");
	    			main.giveBonus(pdamager, 0, 60);
	    			main.giveBonus(pdamager, 1, 60);
	    			break;
	    			
	    		case 10:
	    			//On lui donne les trois (pour 90sec)
	    			pdamager.sendMessage("§9Vous avez gagné un bonus de vitesse, d'énergie et de capacité grâce à votre série de dix kills à la suite ! (+20%, +150% et +200% pendant 90s)");
	    			main.giveBonus(pdamager, 0, 90);
	    			main.giveBonus(pdamager, 1, 90);
	    			main.giveBonus(pdamager, 2, 90);
	    			break;
	    	}
	    }
	}
	
	
	//Event: Entitée prend des dégats.
	//Si l'entitée est un joueur, on annule l'event, pour que le joueur ne prenne jamais de dégats
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e){
		if(e.getEntity() instanceof Player){
			e.setCancelled(true);
		}
	}
	
	
	//Event: Mouvement d'un joueur.
	//Si le joueur est sur une échelle et que son mouvement allait vers le haut, alors on lui donne une velocité
	//de 0,5 vers le haut (pour lui permettre de monter l'échelle plus rapidement)
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		double ydistance = (e.getFrom().getY()) - (e.getTo().getY());
		final Player p = e.getPlayer();
		if((new Location(p.getWorld(), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()).getBlock().getType() == Material.LADDER) && !p.isOnGround()){	
			if(ydistance < 0){
				Vector vec = new Vector(0,0.5,0);
				p.setVelocity(vec);
			}
		}
	}
	
	
	//Event: Pose d'un block.
	//On permet uniquement la pose des blocs de verre coloriés, et en dessous d'une certaine limite de hauteur
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e){
		if(e.getBlock().getType() != Material.STAINED_GLASS){
			e.setCancelled(true);
			return;
		}
		if(e.getBlockPlaced().getLocation().getBlockY() > 122){
			e.getPlayer().sendMessage("§cVous ne pouvez pas construire aussi haut.");
			e.setCancelled(true);
		}
	}
	
	//Event: Casse d'un block.
	//On permet uniquement la casse des blocs de verre coloriés
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		if(e.getBlock().getType() != Material.STAINED_GLASS){
			e.setCancelled(true);
		}
	}
	
	
	//Event: Item accroché est cassé.
	//On ne cherche pas à comprendre, on annule l'event, pour interdire de casser peintures, item_frames, etc
	@EventHandler
	public void onHangingBreak(HangingBreakEvent e){
		e.setCancelled(true);
	}
	
	
	//Event: Item est ramassé.
	//On supprime tout les items autour du joueur (comme s'il les avait récupérés) et on cancel l'event
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent e){
		if (e.getItem().getItemStack().getItemMeta().getDisplayName() == "bonus"){
			final Player p = e.getPlayer();
				if(e.getItem().getItemStack().getType() == Material.FEATHER){
					p.sendMessage("§9Vous avez ramassé un bonus de vitesse ! (+20% pendant 15s)");
					main.giveBonus(p, 0, 15);
				}else if(e.getItem().getItemStack().getType() == Material.EXP_BOTTLE){
					p.sendMessage("§9Vous avez ramassé un bonus d'énergie ! (+150% pendant 10s)");
					main.giveBonus(p, 1, 10);	
				} else if(e.getItem().getItemStack().getType() == Material.CHEST){
					p.sendMessage("§9Vous avez ramassé un bonus de capacité ! (+200% pendant 8s)");
					main.giveBonus(p, 2, 8);	
				}	
			
		} else {
			for (Entity ent : e.getPlayer().getNearbyEntities(3, 3, 3)){
				if (ent instanceof Item) {
					ent.remove();
				}
			}
			e.setCancelled(true);
		}
	}
	
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e){
		e.setFormat("§7" + e.getPlayer().getName() + ": §7" + e.getMessage());
	}
}