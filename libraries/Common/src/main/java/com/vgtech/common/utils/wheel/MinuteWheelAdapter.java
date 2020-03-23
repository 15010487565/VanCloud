/*
 *  Copyright 2010 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.vgtech.common.utils.wheel;


import java.util.List;

/**
 * Numeric Wheel adapter.
 */
public class MinuteWheelAdapter implements WheelAdapter {
	private List<Integer> minuteList;

	private String format;
	public MinuteWheelAdapter(List<Integer> minuteList, String format) {
		this.minuteList = minuteList;
		this.format = format;
	}

	@Override
	public String getItem(int index) {
		if (index >= 0 && index < getItemsCount()) {
			int value = minuteList.get(index);
			return format != null ? String.format(format, value) : Integer.toString(value);
		}
		return null;
	}

	@Override
	public int getItemsCount() {
		return minuteList.size();
	}
	
	@Override
	public int getMaximumLength() {
		return minuteList.size();
	}
}
