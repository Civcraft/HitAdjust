package com.civpvp.hitadjust;


import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Changes the Minecraft hit limiter from the player to the hitter
 * to allow for more realistic combat
 * @author Squeenix
 *
 */
public class HitAdjust extends JavaPlugin implements Listener, CommandExecutor{
	private ConcurrentHashMap<Player, Long> offTime;
	private ConcurrentHashMap<Player, Long> defTime;
	private int hitDelay = this.getConfig().getInt("hitdelay", 500);
	private int getHitDelay = this.getConfig().getInt("invincibilitylength", 1);
	private boolean meleeEnabled = this.getConfig().getBoolean("meleeEnabled", true);
	private boolean bowEnabled = this.getConfig().getBoolean("bowEnabled", true);
	
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		offTime = new ConcurrentHashMap<Player,Long>();
		defTime = new ConcurrentHashMap<Player,Long>();
		this.saveDefaultConfig();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("hitdelay")) {
				hitDelay = Integer.valueOf(args[1]);
				sender.sendMessage("Hit delay set to "+args[1]+"ms");
			}
			else if(args[0].equalsIgnoreCase("invincibilitylength")){
				getHitDelay = Integer.valueOf(args[1]);
				sender.sendMessage("Invincibility length set to "+args[1]+"ms");
			}
			else if(args[0].equalsIgnoreCase("enable")){
				if(args[1].equalsIgnoreCase("melee")){
					this.meleeEnabled = true;
					sender.sendMessage("Hit delay enabled for melee attacks");
				}
				else if(args[1].equalsIgnoreCase("bow")){
					this.bowEnabled = true;
					sender.sendMessage("Hit delay enabled for bow attacks");
				}
			}
			else if(args[0].equalsIgnoreCase("disable")){
				if(args[1].equalsIgnoreCase("melee")){
					this.meleeEnabled = false;
					sender.sendMessage("Hit delay disabled for melee attacks");
				}
				else if(args[1].equalsIgnoreCase("bow")){
					this.bowEnabled = false;
					sender.sendMessage("Hit delay disabled for bow attacks");
				}
			}
		}
		
		return true;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerHit(EntityDamageByEntityEvent e){		
		Entity damager = e.getDamager();
		Entity hitEntity = e.getEntity();
		
		/*
		 * Handle sword hits
		 */
		if(meleeEnabled && damager instanceof Player && hitEntity instanceof Player){
			boolean canAttack = !offTime.containsKey((Player)damager) || offTime.get(damager) < System.currentTimeMillis()-hitDelay;
			boolean canBeAttacked = !defTime.containsKey(hitEntity) || defTime.get(hitEntity) < System.currentTimeMillis()-getHitDelay;
			if(canAttack && canBeAttacked){
    			if(hitEntity!=null){
    				net.minecraft.server.v1_7_R1.Entity mcEntity = ((CraftEntity) hitEntity).getHandle();
    				mcEntity.noDamageTicks=0;
    			}
    			if(!e.isCancelled()){ //EventPriority set to 'lowest', should happen after NCP check
    				offTime.put((Player)damager, System.currentTimeMillis());
    				defTime.put((Player)hitEntity, System.currentTimeMillis());
    			}
    			return;
			}else{
				e.setCancelled(true);
			}	
		}
		
		/*
		 * Handle Arrow Hits
		 */
		if(bowEnabled && damager instanceof Arrow && hitEntity instanceof Player){
			Arrow hitArrow = (Arrow)damager;
			ProjectileSource shooter = hitArrow.getShooter();
			if(shooter instanceof Player){
				boolean canAttack = !offTime.containsKey((Player)shooter) || offTime.get(shooter) < System.currentTimeMillis()-hitDelay;
				boolean canBeAttacked = !defTime.containsKey((Player)hitEntity) || defTime.get(hitEntity) < System.currentTimeMillis()-getHitDelay;
				if(canAttack && canBeAttacked){
	    			if(hitEntity!=null){
	    				net.minecraft.server.v1_7_R1.Entity mcEntity = ((CraftEntity) hitEntity).getHandle();
	    				mcEntity.noDamageTicks=0;
	    			}
	    			if(!e.isCancelled()){ //EventPriority set to 'lowest', should happen after NCP check
	    				offTime.put((Player)shooter, System.currentTimeMillis());
	    				defTime.put((Player)hitEntity, System.currentTimeMillis());
	    			}
	    			return;
				}else{
					e.setCancelled(true);
				}	
			}
		}
	}
}

