package com.base.bot.model;

public class PlayerSwitch {
	
	public PlayerSwitch(){
		
	}
	
	public PlayerSwitch(String playerNumber, String oldSlot, String newSlot){
		this.playerId = playerNumber;
		this.oldSlotId = oldSlot;
		this.newSlotId = newSlot;
	}
	
	public String toString(){
		return "1_" + this.playerId + "_" + this.oldSlotId + "_" + this.newSlotId;
	}

	public String playerId;
	public String oldSlotId;
	public String newSlotId;

}
