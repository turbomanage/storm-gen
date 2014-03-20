package com.turbomanage.storm.entity;

import com.turbomanage.storm.api.Entity;

@Entity
public class ValueEntity {

	private long id;
	private int intValue;
	
	public ValueEntity() {
		// TODO Auto-generated constructor stub
	}
	
	public ValueEntity(int val) {
		this.intValue = val;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getIntValue() {
		return intValue;
	}
	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}
	
}
