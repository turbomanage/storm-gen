package com.turbomanage.storm.entity;

import javax.persistence.Entity;

@Entity
public class JpaEntity {
	
	private long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
