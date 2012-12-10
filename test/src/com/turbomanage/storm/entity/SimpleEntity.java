/*******************************************************************************
 * Copyright 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.turbomanage.storm.entity;

import java.util.Date;

import com.turbomanage.storm.api.Entity;
import com.turbomanage.storm.api.Persistable;
import com.turbomanage.storm.converter.Latitude;

@Entity
public class SimpleEntity implements Persistable {

	public enum EnumType {VALUE1, VALUE2};

	private int privateField;
	transient int transientField;
	long id;
	boolean booleanField;
	byte byteField;
	byte[] blobField;
	char charField;
	short shortField;
	int intField;
	long longField;
	float floatField;
	double doubleField;
	Boolean wBooleanField;
	Byte wByteField;
	Character wCharacterField;
	Date wDateField;
	EnumType enumField;
	Short wShortField;
	Integer wIntegerField;
	Long wLongField;
	Float wFloatField;
	Double wDoubleField;
	String wStringField;
	Latitude wLatitudeField; // test custom converter

	@Override
	public long getId() {
		return id;
	}
	@Override
	public void setId(long id) {
		this.id = id;
	}
	public int getPrivateField() {
		return privateField;
	}
	public void setPrivateField(int privateField) {
		this.privateField = privateField;
	}
	public int getTransientField() {
		return transientField;
	}
	public void setTransientField(int transientField) {
		this.transientField = transientField;
	}
	public boolean isBooleanField() {
		return booleanField;
	}
	public void setBooleanField(boolean booleanField) {
		this.booleanField = booleanField;
	}
	public byte getByteField() {
		return byteField;
	}
	public void setByteField(byte byteField) {
		this.byteField = byteField;
	}
	public byte[] getBlobField() {
		return blobField;
	}
	public void setBlobField(byte[] blobField) {
		this.blobField = blobField;
	}
	public char getCharField() {
		return charField;
	}
	public void setCharField(char charField) {
		this.charField = charField;
	}
	public short getShortField() {
		return shortField;
	}
	public void setShortField(short tinyField) {
		this.shortField = tinyField;
	}
	public int getIntField() {
		return intField;
	}
	public void setIntField(int intField) {
		this.intField = intField;
	}
	public long getLongField() {
		return longField;
	}
	public void setLongField(long longField) {
		this.longField = longField;
	}
	public float getFloatField() {
		return floatField;
	}
	public void setFloatField(float floatField) {
		this.floatField = floatField;
	}
	public double getDoubleField() {
		return doubleField;
	}
	public void setDoubleField(double doubleField) {
		this.doubleField = doubleField;
	}
	public Boolean getwBooleanField() {
		return wBooleanField;
	}
	public void setwBooleanField(Boolean wBooleanField) {
		this.wBooleanField = wBooleanField;
	}
	public Byte getwByteField() {
		return wByteField;
	}
	public void setwByteField(Byte wByteField) {
		this.wByteField = wByteField;
	}
	public Character getwCharacterField() {
		return wCharacterField;
	}
	public void setwCharacterField(Character wCharacterField) {
		this.wCharacterField = wCharacterField;
	}
	public Date getwDateField() {
		return wDateField;
	}
	public void setwDateField(Date wDateField) {
		this.wDateField = wDateField;
	}
	public EnumType getEnumField() {
		return enumField;
	}
	public void setEnumField(EnumType enumField) {
		this.enumField = enumField;
	}
	public Short getwShortField() {
		return wShortField;
	}
	public void setwShortField(Short wShortField) {
		this.wShortField = wShortField;
	}
	public Integer getwIntegerField() {
		return wIntegerField;
	}
	public void setwIntegerField(Integer wIntegerField) {
		this.wIntegerField = wIntegerField;
	}
	public Long getwLongField() {
		return wLongField;
	}
	public void setwLongField(Long wLongField) {
		this.wLongField = wLongField;
	}
	public Float getwFloatField() {
		return wFloatField;
	}
	public void setwFloatField(Float wFloatField) {
		this.wFloatField = wFloatField;
	}
	public Double getwDoubleField() {
		return wDoubleField;
	}
	public void setwDoubleField(Double wDoubleField) {
		this.wDoubleField = wDoubleField;
	}
	public String getwStringField() {
		return wStringField;
	}
	public void setwStringField(String wStringField) {
		this.wStringField = wStringField;
	}
	public Latitude getwLatitudeField() {
		return wLatitudeField;
	}
	public void setwLatitudeField(Latitude wLatitudeField) {
		this.wLatitudeField = wLatitudeField;
	}

}
