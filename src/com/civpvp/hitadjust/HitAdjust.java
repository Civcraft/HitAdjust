package com.civpvp.hitadjust;


import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.reflect.FieldAccessException;

/**
 * Changes the Minecraft hit limiter from the player to the hitter
 * to allow for more realistic combat
 * @author Squeenix
 *
 */
public class HitAdjust extends JavaPlugin implements CommandExecutor {
	private ProtocolManager protocolManager;
	private ConcurrentHashMap<Player, Long> hitTime;
	private int hitDelay = this.getConfig().getInt("hitdelay", 1000);

	@Override
	public void onEnable() {
		registerPacketListeners();
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
	
	private void registerPacketListeners(){		
		protocolManager = ProtocolLibrary.getProtocolManager();
		
	    /*
	     * Sets the invulnerable ticks of a player to zero every time it is attacked. 
	     * Will require recompilation every version update because of the usage of 
	     * Minecraft classes instead of just the Bukkit API.
	     */
	    protocolManager.addPacketListener(new PacketAdapter(this, PacketType.Play.Client.USE_ENTITY){
	    	@Override
	    	public void onPacketReceiving(PacketEvent e){
	    		Player reciever = e.getPlayer();
	    		boolean inList = hitTime.containsKey(reciever);
	    		try{
	    			if(!inList||(hitTime.get(reciever)<(System.currentTimeMillis()-hitDelay))){
    					World w = reciever.getWorld();
    	    			PacketContainer p = e.getPacket();
    	    			int i = p.getIntegers().read(0);
    	    			Entity hitEntity = null;
    	    			
    	    			//A bit silly that this method isn't already in the Bukkit API
    	    			for(Entity q : w.getEntities()){
    	    				if(q.getEntityId()==i){
    	    					hitEntity = q;
    	    					break;
    	    				}
    	    			}
    	    			
    	    			if(hitEntity!=null){
    	    				net.minecraft.server.v1_7_R2.Entity mcEntity = ((CraftEntity) hitEntity).getHandle();
    	    				mcEntity.noDamageTicks=0;
    	    			}
    	    			hitTime.put(reciever, System.currentTimeMillis());
    	    			return;
    				}else{
    					e.setCancelled(true);
    				}
	    			
	    		} catch (FieldAccessException exception){ //Should catch if the packet is the wrong type
	    			exception.printStackTrace();
	    		}
	    	}
	    });
	}
}
