package com.example.uwenglabs;

public class LabInfo {
	private String labname;
	private String location;
	private String occupancy;
	
	public LabInfo(String lab, String loc, String occ) {
		labname = lab;
		location = loc;
		occupancy = occ;
	}
	
	public String getName() {
		return labname;
	}
	
	public String getLoc() {
		return location;
	}
	
	public String getOcc() {
		return occupancy;
	}
}
