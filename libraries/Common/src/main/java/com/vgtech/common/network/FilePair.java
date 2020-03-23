package com.vgtech.common.network;

import java.io.File;

public class FilePair {

	private String key;
	private File file;

	public FilePair(String key, File file) {
		this.key = key;
		this.file = file;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}
