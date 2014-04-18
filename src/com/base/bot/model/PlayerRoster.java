package com.base.bot.model;

public class PlayerRoster {
	
	public PlayerRoster(){
		slot = new Slot();
	}
	
	public String id;
	
	public Slot slot;
	
	public String name;
	public String team;
	public String position;
	public String opponent;
	public String status;
	public String pr7;
	public String pr15;	
	public String pr30;
	public String prYear;
	public Integer positionRank;
	public String avgDraftPosition;
	public String owned;
	public String ownedChange;
	public boolean disabled;
}
