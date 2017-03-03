package com.pcitc.paas.service;

import java.util.List;

import com.pcitc.paas.model.ItemHistory;

public interface MonitoringService {
    // 取得当前监控指标的平均值
    ItemHistory getCurrentCPULoadAverage(List<String> instanceCodeList);
    
    ItemHistory getCurrentMemoryUsageAverage(List<String> instanceCodeList);
    
    ItemHistory getCurrentURLHealthPercentage(List<String> instanceCodeList);
    
    ItemHistory countCurrentFailURL(List<String> instanceCodeList);
    
    ItemHistory countCurrentSuccessURL(List<String> instanceCodeList);
    
    // 取得监控指标的历史平均值趋势
    List<ItemHistory> getCPULoadAverageHistory(List<String> instanceCodeList);
    
    List<ItemHistory> getMemoryUsageAverageHistory(List<String> instanceCodeList);
    
    List<ItemHistory> getURLHealthAverageHistory(List<String> instanceCodeList);
    
    List<ItemHistory> getFileSysUsedRateHistoryByDay(List<String> instanceCodeList, int numOfDay);
    
    // 取得指定时间点前后的监控指标的历史平均值趋势
    List<ItemHistory> getCPULoadAverageHistoryByTimeStamp(List<String> instanceCodeList, String timeStamp);
    
    List<ItemHistory> getMemoryAverageHistoryByTimeStamp(List<String> instanceCodeList, String timeStamp);
    
    List<ItemHistory> getURLHealthAverageHistoryByTimeStamp(List<String> instanceCodeList, String timeStamp);
    
}
