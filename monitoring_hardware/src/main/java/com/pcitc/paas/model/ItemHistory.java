package com.pcitc.paas.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ItemHistory implements Serializable,Comparable<ItemHistory> {
	private static final long serialVersionUID = 1L;
	private String clock;
	private String value;
	public String getClock() {
		return clock;
	}
	public void setClock(String clock) {
		this.clock = clock;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public int compareTo(ItemHistory a) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		try {
			cal1.setTime(format.parse(this.getClock()));
			cal2.setTime(format.parse(a.getClock()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return cal1.compareTo(cal2);
	}
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NodeItemHistory [clock=");
        builder.append(clock);
        builder.append(", value=");
        builder.append(value);
        builder.append("]");
        return builder.toString();
    }
}
