package com.pcitc.paas.model;

public class DockerContainerInfo {
	private String timeStamp;
	private String containerName;
	private String cpu_usage_total;
	private String cpu_usage_system;
	private String cpu_usage_user;
	private String memeory_usage;
	public String getMemeory_usage() {
		return memeory_usage;
	}
	public void setMemeory_usage(String memeory_usage) {
		this.memeory_usage = memeory_usage;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getContainerName() {
		return containerName;
	}
	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}
	public String getCpu_usage_total() {
		return cpu_usage_total;
	}
	public void setCpu_usage_total(String cpu_usage_total) {
		this.cpu_usage_total = cpu_usage_total;
	}
	public String getCpu_usage_system() {
		return cpu_usage_system;
	}
	public void setCpu_usage_system(String cpu_usage_system) {
		this.cpu_usage_system = cpu_usage_system;
	}
	public String getCpu_usage_user() {
		return cpu_usage_user;
	}
	public void setCpu_usage_user(String cpu_usage_user) {
		this.cpu_usage_user = cpu_usage_user;
	}
}
