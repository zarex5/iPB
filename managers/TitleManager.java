package me.ino.tanarias.paintball.managers;

import net.minecraft.server.v1_8_R2.IChatBaseComponent;
import net.minecraft.server.v1_8_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R2.PacketPlayOutChat;
import net.minecraft.server.v1_8_R2.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R2.PacketPlayOutTitle.EnumTitleAction;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

 
public class TitleManager {
	public static void sendTitle(Player player, String msgTitle, String msgSubTitle, int ticks){
		IChatBaseComponent chatTitle = ChatSerializer.a("{\"text\": \"" + msgTitle + "\"}");
		IChatBaseComponent chatSubTitle = ChatSerializer.a("{\"text\": \"" + msgSubTitle + "\"}");
		PacketPlayOutTitle p1 = new PacketPlayOutTitle(EnumTitleAction.TITLE, chatTitle);
		PacketPlayOutTitle p2 = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, chatSubTitle);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(p1);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(p2);
                
		PacketPlayOutTitle pt = new PacketPlayOutTitle(EnumTitleAction.TIMES, null, 20, ticks, 20);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(pt);
	}
       
	public static void sendActionBarPlayer(Player player, String message){
		IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
		PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(ppoc);
	}
        
	public static void sendActionBarAllPlayers(String message){
		for (Player p : Bukkit.getOnlinePlayers()){
			sendActionBarPlayer(p, message);
		}
	}
}