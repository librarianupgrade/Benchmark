/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.opensymphony.xwork2.util;

import com.opensymphony.xwork2.conversion.annotations.ConversionRule;
import com.opensymphony.xwork2.conversion.annotations.ConversionType;
import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

import java.util.*;

/**
 * @author <a href="mailto:plightbo@cisco.com">Pat Lightbody</a>
 * @author $Author$
 * @version $Revision$
 */
public class Foo {

	Bar bar;
	Date birthday;
	Date event;
	Date meeting;
	Foo child;
	List cats;
	List annotatedCats;
	List moreCats;
	List strings;
	Collection barCollection;
	Collection annotatedBarCollection;
	Map catMap;
	Map anotherCatMap;
	String title;
	long[] points;
	Foo[] relatives;
	boolean useful;
	int number;
	long aLong;
	Calendar calendar;
	BarJunior barJunior;
	Map<MyNumber, Animal> animalMap;

	public BarJunior getBarJunior() {
		return barJunior;
	}

	public void setBarJunior(BarJunior barJunior) {
		this.barJunior = barJunior;
	}

	public void setALong(long aLong) {
		this.aLong = aLong;
	}

	public long getALong() {
		return aLong;
	}

	public void setBar(Bar bar) {
		this.bar = bar;
	}

	public Bar getBar() {
		return bar;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setCatMap(Map catMap) {
		this.catMap = catMap;
	}

	public Map getCatMap() {
		return catMap;
	}

	public void setCats(List cats) {
		this.cats = cats;
	}

	public List getCats() {
		return cats;
	}

	public void setAnnotatedCats(List annotatedCats) {
		this.annotatedCats = annotatedCats;
	}

	@TypeConversion(rule = ConversionRule.ELEMENT, converterClass = Cat.class)
	public List getAnnotatedCats() {
		return annotatedCats;
	}

	public void setChild(Foo child) {
		this.child = child;
	}

	public Foo getChild() {
		return child;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	/**
	 * @return Returns the anotherCatMap.
	 */
	public Map getAnotherCatMap() {
		return anotherCatMap;
	}

	/**
	 * @param anotherCatMap The anotherCatMap to set.
	 */
	public void setAnotherCatMap(Map anotherCatMap) {
		this.anotherCatMap = anotherCatMap;
	}

	/**
	 * @return Returns the moreCats.
	 */
	public List getMoreCats() {
		return moreCats;
	}

	/**
	 * @param moreCats The moreCats to set.
	 */
	public void setMoreCats(List moreCats) {
		this.moreCats = moreCats;
	}

	/**
	 * @return Returns the catSet.
	 */
	public Collection getBarCollection() {
		return barCollection;
	}

	/**
	 * @param barCollection The barCollection to set.
	 */
	public void setBarCollection(Collection barCollection) {
		this.barCollection = barCollection;
	}

	@TypeConversion(rule = ConversionRule.KEY_PROPERTY, value = "id")
	public void setAnnotatedBarCollection(Collection annotatedBarCollection) {
		this.annotatedBarCollection = annotatedBarCollection;
	}

	@TypeConversion(rule = ConversionRule.ELEMENT, converter = "com.opensymphony.xwork2.util.Bar")
	public Collection getAnnotatedBarCollection() {
		return annotatedBarCollection;
	}

	public void setPoints(long[] points) {
		this.points = points;
	}

	public long[] getPoints() {
		return points;
	}

	public void setRelatives(Foo[] relatives) {
		this.relatives = relatives;
	}

	public Foo[] getRelatives() {
		return relatives;
	}

	public void setStrings(List strings) {
		this.strings = strings;
	}

	public List getStrings() {
		return strings;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setUseful(boolean useful) {
		this.useful = useful;
	}

	public boolean isUseful() {
		return useful;
	}

	public Date getEvent() {
		return event;
	}

	public void setEvent(Date event) {
		this.event = event;
	}

	public Date getMeeting() {
		return meeting;
	}

	public void setMeeting(Date meeting) {
		this.meeting = meeting;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	public Map<MyNumber, Animal> getAnimalMap() {
		return animalMap;
	}

	public void setAnimalMap(Map<MyNumber, Animal> animalMap) {
		this.animalMap = animalMap;
	}

	@Override
	public String toString() {
		return "Foo";
	}
}
