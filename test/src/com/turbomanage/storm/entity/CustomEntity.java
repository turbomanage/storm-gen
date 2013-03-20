package com.turbomanage.storm.entity;

import com.turbomanage.storm.api.Entity;
import com.turbomanage.storm.api.Id;

@Entity(name="[order]")
public class CustomEntity {
	
	private long id;
	@Id
	private long customId;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getCustomId() {
		return customId;
	}

	public void setCustomId(long customId) {
		this.customId = customId;
	}

}
