package com.pcitc.paas.service.impl;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.pcitc.paas.model.ItemHistory;

public class CadvisorMonitoringServiceImplTest {
	CadvisorMonitoringServiceImpl cadvisor;
	List<String> containerList;
	@Before
	public void setUp() {
		cadvisor = new CadvisorMonitoringServiceImpl();
		containerList = new ArrayList<String>();
		containerList.add("monitoringhardware_cadvisor_1");
//		containerList.add("monitoringhardware_elasticsearch_1");
	}
	@Test
	public void testGetCurrentCPUTotalUsageAverage() {
		List<ItemHistory> results = cadvisor.getCPUTotalUsageAverageHistory(containerList);
		System.out.println(results);
	}

	@Test
	public void testGetCurrentMemoryUsageAverage() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCPUTotalUsageAverageHistory() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMemoryUsageAverageHistory() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCPUTotalUsageAverageHistoryByTimeStamp() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMemoryAverageHistoryByTimeStamp() {
		fail("Not yet implemented");
	}

}
