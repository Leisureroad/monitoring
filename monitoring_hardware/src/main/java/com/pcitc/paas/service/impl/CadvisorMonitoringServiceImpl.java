package com.pcitc.paas.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.pcitc.paas.model.DockerContainerInfo;
import com.pcitc.paas.model.ItemHistory;
import com.pcitc.paas.service.MonitoringService;

public class CadvisorMonitoringServiceImpl implements MonitoringService {
    private HttpClient httpClient = new DefaultHttpClient();;
    private HttpPost httpPost = null;
    
    public Properties prop = new Properties();
    InputStream in;
    
    public CadvisorMonitoringServiceImpl() {
        try {
            in = this.getClass().getResourceAsStream("/conf/cadvisor.properties");
            prop.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public ItemHistory getCurrentCPULoadAverage(List<String> ctnerListParam) {
        Map<String, Map<String, List<String>>> ctnerTimeValueMap = getCPUAverageHistoryRaw(ctnerListParam, null);
        if (ctnerTimeValueMap == null)
            return null;
        ItemHistory result = aggsAllCtnerTimeValue(ctnerTimeValueMap);
        return result;
    }
    
    @Override
    public ItemHistory getCurrentMemoryUsageAverage(List<String> ctnerListParam) {
        Map<String, Map<String, List<String>>> ctnerTimeValueMap = getMemoryAverageHistoryRaw(ctnerListParam, null);
        if (ctnerTimeValueMap == null)
            return null;
        ItemHistory result = aggsAllCtnerTimeValue(ctnerTimeValueMap);
        return result;
    }
    
    @Override
    public List<ItemHistory> getCPULoadAverageHistory(List<String> ctnerListParam) {
        Map<String, List<ItemHistory>> containerListofMap = getCPULoadAverageHistoryMap(ctnerListParam, null);
        if (containerListofMap == null)
            return null;
        return containerListofMap.get(ctnerListParam.get(0));
    }
    
    @Override
    public List<ItemHistory> getMemoryUsageAverageHistory(List<String> ctnerListParam) {
        Map<String, List<ItemHistory>> containerListofMap = getMemoryUsageAverageHistoryMap(ctnerListParam, null);
        if (containerListofMap == null)
            return null;
        return containerListofMap.get(ctnerListParam.get(0));
    }
    
    @Override
    public List<ItemHistory> getCPULoadAverageHistoryByTimeStamp(List<String> ctnerListParam, String timeStamp) {
        Map<String, List<ItemHistory>> containerListofMap = getCPULoadAverageHistoryMap(ctnerListParam, timeStamp);
        if (containerListofMap == null)
            return null;
        return containerListofMap.get(ctnerListParam.get(0));
    }
    
    @Override
    public List<ItemHistory> getMemoryAverageHistoryByTimeStamp(List<String> ctnerListParam, String timeStamp) {
        Map<String, List<ItemHistory>> containerListofMap = getMemoryUsageAverageHistoryMap(ctnerListParam,
                timeStamp);
        if (containerListofMap == null)
            return null;
        return containerListofMap.get(ctnerListParam.get(0));
    }
    
    private Map<String, List<ItemHistory>> getMemoryUsageAverageHistoryMap(List<String> ctnerListParam,
        String timeStamp) {
        Map<String, Map<String, List<String>>> ctnerTimeValueMap = getMemoryAverageHistoryRaw(ctnerListParam, null);
        Map<String, List<ItemHistory>> resultMap = aggsCtnerTimeValue(ctnerTimeValueMap);
        return resultMap;
    }
    
    private Map<String, List<ItemHistory>> getCPULoadAverageHistoryMap(List<String> ctnerListParam,
        String timeStamp) {
        Map<String, Map<String, List<String>>> ctnerTimeValueMap = getCPUAverageHistoryRaw(ctnerListParam, timeStamp);
        Map<String, List<ItemHistory>> resultMap = aggsCtnerTimeValue(ctnerTimeValueMap);
        return resultMap;
    }
    
    /**
     * 获得以container查询名字为key的时间和CPU监控项值列表的Map，如{container1={time1=[value1,value2,value3]}...因为container查询名为模糊搜索，查询结果可能为一个或多个container
     * 
     * @param ctnerListParam
     * @return
     */
    private Map<String, Map<String, List<String>>> getCPUAverageHistoryRaw(List<String> ctnerListParam,
        String timeStamp) {
        String params = queryESData(ctnerListParam, timeStamp);
        String resultRaw = doHttpPost(params);
        Map<String, List<DockerContainerInfo>> ctnerListMap = parseESData(resultRaw);
        if (ctnerListMap == null)
            return null;
        Map<String, List<ItemHistory>> ctnerCPUInfoMap = calculateCPUTotalUsage(ctnerListMap);
        if (ctnerCPUInfoMap == null)
            return null;
        Map<String, Map<String, List<String>>> ctnerTimeValueMap = convertCtnerTimeValueMap(ctnerListParam,
                ctnerCPUInfoMap);
        if (ctnerTimeValueMap == null)
            return null;
        return ctnerTimeValueMap;
    }
    
    /**
     * 获得以container查询名字为key的时间和Memory监控项值列表的Map，如{container1={time1=[value1,value2,value3]}...因为container查询名为模糊搜索，查询结果可能为一个或多个container
     * 
     * @param ctnerListParam
     * @return
     */
    private Map<String, Map<String, List<String>>> getMemoryAverageHistoryRaw(List<String> ctnerListParam,
        String timeStamp) {
        String params = queryESData(ctnerListParam, timeStamp);
        String resultRaw = doHttpPost(params);
        Map<String, List<DockerContainerInfo>> ctnerListMap = parseESData(resultRaw);
        if (ctnerListMap == null)
            return null;
        Map<String, List<ItemHistory>> ctnerMemInfoMap = getMemoryUsageMb(ctnerListMap);
        if (ctnerMemInfoMap == null)
            return null;
        Map<String, Map<String, List<String>>> ctnerTimeValueMap = convertCtnerTimeValueMap(ctnerListParam,
                ctnerMemInfoMap);
        if (ctnerTimeValueMap == null)
            return null;
        return ctnerTimeValueMap;
    }
    
    /**
     * 获得查询ES Restful API的字符串，并替换参数
     * 
     * @param ctnerListParam
     * @return
     */
    private String queryESData(List<String> ctnerListParam, String specificTimeStamp) {
        String queryString = "container_stats.history";
        String paramsRaw = getParams(queryString);
        Map<String, String> vars = new HashMap<String, String>();
        Calendar cal = Calendar.getInstance();
        long now = 0;
        long from = 0;
        if (specificTimeStamp == null) {
            now = cal.getTimeInMillis();
            cal.add(Calendar.MINUTE, -9);
            from = cal.getTimeInMillis();
        }
        if (specificTimeStamp != null) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(specificTimeStamp);
                cal.setTime(date);
                cal.add(Calendar.MINUTE, 5);
                now = cal.getTimeInMillis();
                cal.add(Calendar.MINUTE, -10);
                from = cal.getTimeInMillis();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String ctnerListStr = "";
        for (int i = 0; i < ctnerListParam.size(); i++) {
            ctnerListStr += "container_Name=" + ctnerListParam.get(i) + " OR ";
        }
        String ctnerListVar = ctnerListStr.substring(0, ctnerListStr.length() - 4);
        vars.put("${from}", String.valueOf(from));
        vars.put("${to}", String.valueOf(now));
        vars.put("${containerList}", ctnerListVar); // container_Name=paas_esg*
                                                    // OR
                                                    // container_Name=paas_sbf*
        String params = replaceVars(paramsRaw, vars);
        return params;
    }
    
    /**
     * 根据containerName对监控项进行时间维度汇总
     * 
     * @param ctnerTimeValueMap
     * @return
     */
    private Map<String, List<ItemHistory>>
        aggsCtnerTimeValue(Map<String, Map<String, List<String>>> ctnerTimeValueMap) {
        // 合并相同clock的value值
        Map<String, List<ItemHistory>> resultMap = new HashMap<String, List<ItemHistory>>();
        Iterator<String> keyIter = ctnerTimeValueMap.keySet().iterator();
        while (keyIter.hasNext()) {
            String ctnerName = keyIter.next();
            Map<String, List<String>> timeMap = ctnerTimeValueMap.get(ctnerName);
            Iterator<String> timeIter = timeMap.keySet().iterator();
            List<ItemHistory> itemList = new ArrayList<ItemHistory>();
            while (timeIter.hasNext()) {
                String time = timeIter.next();
                List<String> valueList = timeMap.get(time);
                double sum = 0.00;
                int count = 0;
                double avg = 0.00;
                for (int i = 0; i < valueList.size(); i++) {
                    if (valueList.get(i) == null || valueList.get(i).equals("") || valueList.get(i).equals("0")) {
                        continue;
                    } else {
                        sum += Double.parseDouble(valueList.get(i));
                        count++;
                    }
                }
                avg = sum / count;
                ItemHistory item = new ItemHistory();
                item.setClock(time);
                item.setValue(String.valueOf(avg));
                itemList.add(item);
            }
            if (resultMap.get(ctnerName) == null) {
                resultMap.put(ctnerName, itemList);
            } else {
                resultMap.get(ctnerName).addAll(itemList);
            }
        }
        return resultMap;
    }
    
    /**
     * 对所有container进行时间维度的汇总，返回当前值
     * 
     * @param ctnerTimeValueMap
     * @return
     */
    private ItemHistory aggsAllCtnerTimeValue(Map<String, Map<String, List<String>>> ctnerTimeValueMap) {
        // 合并相同clock的value值
        ItemHistory result = new ItemHistory();
        Map<String, List<Double>> avgTimeValueMap = new HashMap<String, List<Double>>();
        Iterator<String> keyIter = ctnerTimeValueMap.keySet().iterator();
        while (keyIter.hasNext()) {
            String ctnerName = keyIter.next();
            Map<String, List<String>> timeMap = ctnerTimeValueMap.get(ctnerName);
            Iterator<String> timeIter = timeMap.keySet().iterator();
            while (timeIter.hasNext()) {
                String time = timeIter.next();
                List<String> valueList = timeMap.get(time);
                double sum = 0.00;
                int count = 0;
                double avg = 0.00;
                for (int i = 0; i < valueList.size(); i++) {
                    if (valueList.get(i) == null || valueList.get(i).equals("") || valueList.get(i).equals("0")) {
                        continue;
                    } else {
                        sum += Double.parseDouble(valueList.get(i));
                        count++;
                    }
                }
                avg = sum / count;
                List<Double> aggsValueList = new ArrayList<Double>();
                aggsValueList.add(avg);
                if (avgTimeValueMap.get(time) == null || avgTimeValueMap.get(time).size() == 0) {
                    avgTimeValueMap.put(time, aggsValueList);
                } else {
                    avgTimeValueMap.get(time).addAll(aggsValueList);
                }
            }
        }
        List<ItemHistory> itemList = new ArrayList<ItemHistory>();
        Iterator<String> avgTimeIter = avgTimeValueMap.keySet().iterator();
        while (avgTimeIter.hasNext()) {
            String time = avgTimeIter.next();
            List<Double> valueList = avgTimeValueMap.get(time);
            double sum = 0.00;
            int count = 0;
            double avg = 0.00;
            for (int i = 0; i < valueList.size(); i++) {
                if (valueList.get(i) == null || valueList.get(i).equals("") || valueList.get(i).equals("0")) {
                    continue;
                } else {
                    sum += valueList.get(i);
                    count++;
                }
            }
            avg = sum / count;
            ItemHistory item = new ItemHistory();
            item.setClock(time);
            item.setValue(String.valueOf(avg));
            itemList.add(item);
        }
        Collections.sort(itemList); // 按时间顺序排序
        result = itemList.get(itemList.size() - 1); // 获得最后一个值，即时间最大的一个值也就是当前值
        return result;
    }
    
    /**
     * 根据container查询信息，获得container按照时间维度的监控值的列表，但未按照时间合并
     * 
     * @param ctnerParam
     * @param ctnerCPUInfoMap
     * @return
     */
    private Map<String, Map<String, List<String>>> convertCtnerTimeValueMap(List<String> ctnerParam,
        Map<String, List<ItemHistory>> ctnerCPUInfoMap) {
        // 根据实例编码获取所有container的监控信息， {container查询名主键，clock=[value1, value2]}
        Map<String, Map<String, List<String>>> ctnerTimeValueMap = new HashMap<String, Map<String, List<String>>>();
        Iterator<String> ctnerNameIter = ctnerCPUInfoMap.keySet().iterator();
        while (ctnerNameIter.hasNext()) {
            for (int i = 0; i < ctnerParam.size(); i++) {
                String ctnerName = ctnerNameIter.next();
                Pattern p = Pattern.compile(ctnerParam.get(i) + ".*");
                Matcher m = p.matcher(ctnerName);
                if (!m.matches()) {
                    continue;
                } else {
                    List<ItemHistory> itemList = ctnerCPUInfoMap.get(ctnerName);
                    for (int j = 0; j < itemList.size(); j++) {
                        String ctnerNameParam = ctnerParam.get(i);
                        String time = itemList.get(j).getClock();
                        String value = itemList.get(j).getValue();
                        if (ctnerTimeValueMap.get(ctnerNameParam) == null) {
                            List<String> valueList = new ArrayList<String>();
                            valueList.add(value);
                            Map<String, List<String>> timeValueMap = new HashMap<String, List<String>>();
                            timeValueMap.put(time, valueList);
                            ctnerTimeValueMap.put(ctnerNameParam, timeValueMap);
                        } else {
                            if (ctnerTimeValueMap.get(ctnerNameParam).get(time) == null) {
                                List<String> valueList = new ArrayList<String>();
                                valueList.add(value);
                                ctnerTimeValueMap.get(ctnerNameParam).put(time, valueList);
                            } else {
                                List<String> valueList = new ArrayList<String>();
                                valueList.add(value);
                                ctnerTimeValueMap.get(ctnerNameParam).get(time).addAll(valueList);
                            }
                        }
                    }
                }
            }
        }
        return ctnerTimeValueMap;
    }
    
    /**
     * 计算CPU Total Usage
     * 
     * @param ctnerListMap
     * @return
     */
    private Map<String, List<ItemHistory>>
        calculateCPUTotalUsage(Map<String, List<DockerContainerInfo>> ctnerListMap) {
        Iterator<String> iter = ctnerListMap.keySet().iterator();
        Map<String, List<ItemHistory>> ctnerCpuInfoMap = new HashMap<String, List<ItemHistory>>();
        while (iter.hasNext()) {
            String ctnerName = iter.next();
            List<DockerContainerInfo> ctnerList = ctnerListMap.get(ctnerName);
            if (ctnerList.size() < 2) {
                continue;
            }
            for (int i = 1; i < ctnerList.size(); i++) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String timeStamp = format.format(Long.parseLong(ctnerList.get(i).getTimeStamp()) / 1000);
                DockerContainerInfo current = ctnerList.get(i);
                DockerContainerInfo previous = ctnerList.get(i - 1);
                double totalDelta = Double.parseDouble(current.getCpu_usage_total())
                        - Double.parseDouble(previous.getCpu_usage_total());
                double intervalsNs = (Double.parseDouble(current.getTimeStamp())
                        - Double.parseDouble(previous.getTimeStamp())) * 1000000;
                double percentage = totalDelta * 1000 / intervalsNs * 100;
                ItemHistory item = new ItemHistory();
                item.setClock(timeStamp);
                item.setValue(String.valueOf(percentage));
                List<ItemHistory> itemList = new ArrayList<ItemHistory>();
                itemList.add(item);
                if (ctnerCpuInfoMap.get(ctnerName) == null) {
                    ctnerCpuInfoMap.put(ctnerName, itemList);
                } else {
                    ctnerCpuInfoMap.get(ctnerName).addAll(itemList);
                }
            }
        }
        return ctnerCpuInfoMap;
    }
    
    /**
     * 获得Memory Usage
     * 
     * @param ctnerListMap
     * @return
     */
    private Map<String, List<ItemHistory>> getMemoryUsageMb(Map<String, List<DockerContainerInfo>> ctnerListMap) {
        Iterator<String> iter = ctnerListMap.keySet().iterator();
        Map<String, List<ItemHistory>> ctnerMemInfoMap = new HashMap<String, List<ItemHistory>>();
        while (iter.hasNext()) {
            String ctnerName = iter.next();
            List<DockerContainerInfo> ctnerList = ctnerListMap.get(ctnerName);
            for (int i = 0; i < ctnerList.size(); i++) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String timeStamp = format.format(Long.parseLong(ctnerList.get(i).getTimeStamp()) / 1000);
                DockerContainerInfo current = ctnerList.get(i);
                double memUsage = Double.parseDouble(current.getMemeory_usage()) / (1024 * 1024);
                ItemHistory item = new ItemHistory();
                item.setClock(timeStamp);
                item.setValue(String.valueOf(memUsage));
                List<ItemHistory> itemList = new ArrayList<ItemHistory>();
                itemList.add(item);
                if (ctnerMemInfoMap.get(ctnerName) == null) {
                    ctnerMemInfoMap.put(ctnerName, itemList);
                } else {
                    ctnerMemInfoMap.get(ctnerName).addAll(itemList);
                }
            }
        }
        return ctnerMemInfoMap;
    }
    
    /**
     * 解析ES API返回值，并传化为对象
     * 
     * @param resultRaw
     * @return
     */
    private Map<String, List<DockerContainerInfo>> parseESData(String resultRaw) {
        JSONObject resultJson = new JSONObject(resultRaw);
        if (resultJson == null || resultJson.get("hits") == null)
            return null;
        JSONObject hits1 = (JSONObject) resultJson.get("hits");
        if (hits1 == null || hits1.get("hits") == null)
            return null;
        JSONArray hits2 = (JSONArray) hits1.get("hits");
        if (hits2 == null || hits2.length() == 0)
            return null;
        Map<String, List<DockerContainerInfo>> ctnerListMap = new HashMap<String, List<DockerContainerInfo>>();
        for (int i = 0; i < hits2.length(); i++) {
            JSONObject hitsObj = (JSONObject) hits2.get(i);
            JSONObject _source = (JSONObject) hitsObj.get("_source");
            String ctnerName = _source.get("container_Name").toString();
            JSONObject ctner_stats = (JSONObject) _source.get("container_stats");
            JSONObject cpu = (JSONObject) ctner_stats.get("cpu");
            JSONObject cpu_usage = (JSONObject) cpu.get("usage");
            String total = cpu_usage.get("total").toString();
            JSONObject memory = (JSONObject) ctner_stats.get("memory");
            String memory_usage = memory.get("usage").toString();
            if (!total.equals("0")) {
                DockerContainerInfo container = new DockerContainerInfo();
                container.setTimeStamp(_source.get("timestamp").toString());
                container.setContainerName(ctnerName);
                container.setCpu_usage_total(total);
                container.setMemeory_usage(memory_usage);
                if (ctnerListMap.get(ctnerName) == null) {
                    List<DockerContainerInfo> ctnerInfoList = new ArrayList<DockerContainerInfo>();
                    ctnerInfoList.add(container);
                    ctnerListMap.put(ctnerName, ctnerInfoList);
                } else {
                    ctnerListMap.get(ctnerName).add(container);
                }
            } else {
                continue;
            }
        }
        return ctnerListMap;
    }
    
    /**
     * 用HTTPClient 发送ES RESTFul API 请求
     * 
     * @param params
     * @return
     */
    private String doHttpPost(String params) {
        httpPost = new HttpPost(getParams("cadvisor_api_url"));
        String result = null;
        int status = -1;
        
        try {
            httpPost.setHeader("Content-type", "application/json-rpc; charset=utf-8");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new StringEntity(params, Charset.forName("UTF-8")));
            HttpResponse response = httpClient.execute(httpPost);
            status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                System.out.println("Method failed:" + response.getStatusLine());
            }
            result = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 根据查询Key读取配置文件中的查询信息
     * 
     * @param queryKey
     * @return
     */
    private String getParams(String queryKey) {
        return prop.getProperty(queryKey);
    }
    
    /**
     * 替换配置文件中的变量
     * 
     * @param paramsRaw
     * @param vars
     * @return
     */
    private String replaceVars(String paramsRaw, Map<String, String> vars) {
        String raw = paramsRaw;
        Iterator<String> var = vars.keySet().iterator();
        while (var.hasNext()) {
            String key = var.next();
            raw = raw.replace(key, vars.get(key).toString());
        }
        return raw;
    }

    @Override
    public ItemHistory getCurrentURLHealthPercentage(List<String> instanceCodeList) {
        return null;
    }

    @Override
    public ItemHistory countCurrentFailURL(List<String> instanceCodeList) {
        return null;
    }

    @Override
    public ItemHistory countCurrentSuccessURL(List<String> instanceCodeList) {
        return null;
    }

    @Override
    public List<ItemHistory> getURLHealthAverageHistory(List<String> instanceCodeList) {
        return null;
    }

    @Override
    public List<ItemHistory> getFileSysUsedRateHistoryByDay(List<String> instanceCodeList, int numOfDay) {
        return null;
    }

    @Override
    public List<ItemHistory> getURLHealthAverageHistoryByTimeStamp(List<String> instanceCodeList, String timeStamp) {
        return null;
    }
}
