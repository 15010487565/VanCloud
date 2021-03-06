/*      						
 * Copyright 2010 Beijing Xinwei, Inc. All rights reserved.
 * 
 * History:
 * ------------------------------------------------------------------------------
 * Date    	|  Who  		|  What  
 * 2015年3月30日	| duanbokan 	| 	create the file                       
 */

package com.vgtech.vancloud.ui.register.country;

import android.graphics.drawable.Drawable;

/**
 *
 * 类简要描述
 *
 * <p>
 * 类详细描述
 * </p>
 *
 */

public class CountryModel
{
	// 国家名称
	public String countryName;

	// 国家代码
	public String countryNumber;

	public String simpleCountryNumber;

	// 国家名称缩写
	public String countrySortKey;

	// 国家图标
	public Drawable contactPhoto;

	public CountryModel(String countryName, String countryNumber, String countrySortKey)
	{
		super();
		this.countryName = countryName;
		this.countryNumber = countryNumber;
		this.countrySortKey = countrySortKey;
		if (countryNumber != null)
		{
			this.simpleCountryNumber = countryNumber.replaceAll("\\-|\\s", "");
		}
	}

}
