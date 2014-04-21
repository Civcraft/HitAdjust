package com.civpvp.hitadjust;


import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Changes the Minecraft hit limiter from the player to the hitter
 * to allow for more realistic combat
 * @author Squeenix
 *
 */
public class HitAdjust extends JavaPlugin implements Listener, CommandExecutor{
	private ConcurrentHashMap<Player, Long> hitTime;
	private int hitDelay = this.getConfig().getInt("hitdelay", 1000);
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		hitTime = new ConcurrentHashMap<Player,Long>();
		this.saveDefaultConfig();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("delay")) {
				hitDelay = Integer.valueOf(args[1]);
				sender.sendMessage("HitDelay set to "+args[1]+"ms");
			}
		}
		
		return true;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerHit(EntityDamageByEntityEvent e){		
		Entity damager = e.getDamager();
		Entity hitEntity = e.getEntity();
		if(damager instanceof Player && hitEntity instanceof Player){
			boolean inList = hitTime.containsKey((Player)damager);
			if(!inList||(hitTime.get(damager)<(System.currentTimeMillis()-hitDelay))){
    			if(hitEntity!=null){
    				net.minecraft.server.v1_7_R1.Entity mcEntity = ((CraftEntity) hitEntity).getHandle();
    				mcEntity.noDamageTicks=0;
    			}
    			if(!e.isCancelled()){ //EventPriority set to 'lowest', should happen after NCP check
    				hitTime.put((Player)damager, System.currentTimeMillis());
    			}
    			return;
			}else{
				e.setCancelled(true);
			}	
		}
	}
}

