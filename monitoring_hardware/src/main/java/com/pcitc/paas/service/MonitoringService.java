package com.pcitc.paas.service;

import java.util.List;

import com.pcitc.paas.model.ItemHistory;

public interface MonitoringService {
    // 取得当前监控指标的平均值
    ItemHistory getCurrentCPULoadAverage(List<String> containerList);
    
    ItemHistory getCurrentMemoryUsageAverage(List<String> containerList);
    
    ItemHistory getCurrentURLHealthPercentage(List<String> containerList);
    
    ItemHistory countCurrentFailURL(List<String> containerList);
    
    ItemHistory countCurrentSuccessURL(List<String> containerList);
    
    // 取得监控指标的历史平均值趋势
    List<ItemHistory> getCPULoadAverageHistory(List<String> containerList);
    
    List<ItemHistory> getMemoryUsageAverageHistory(List<String> containerList);
    
    List<ItemHistory> getURLHealthAverageHistory(List<String> containerList);
    
    List<ItemHistory> getFileSysUsedRateHistoryByDay(List<String> containerList, int numOfDay);
    
    // 取得指定时间点前后的监控指标的历史平均值趋势
    List<ItemHistory> getCPULoadAverageHistoryByTimeStamp(List<String> containerList, String timeStamp);
    
    List<ItemHistory> getMemoryAverageHistoryByTimeStamp(List<String> containerList, String timeStamp);
    
    List<ItemHistory> getURLHealthAverageHistoryByTimeStamp(List<String> containerList, String timeStamp);
    
}
