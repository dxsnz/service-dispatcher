package com.example.ordercheck;

import com.example.ordercheck.annotation.OrderCheckRule;
import com.example.ordercheck.dao.CpoeOrderCheckRuleDAO;
import com.example.ordercheck.dto.OrderCheckServiceDto;
import com.example.ordercheck.entity.CpoeOrderCheckRule;
import com.example.ordercheck.result.Result;
import com.example.ordercheck.rule.OrderCheckExecutor;
import com.example.ordercheck.rule.PostCheck;
import com.example.ordercheck.rule.PreCheck;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 医嘱校验管理器
 *
 * @author xxChen
 * @since 2024-6-14 11:38
 */
public class OrderCheckManager {

    /**
     * 缓存医嘱校验规则
     * 从数据库获取数据
     * key是opType，value是该opType下的医嘱校验规则集合，按ordinal从小到大排序
     */
    private static HashMap<String, List<CpoeOrderCheckRule>> ruleCache;
    /**
     * 缓存医嘱校验执行器
     * 外层的HashMap：key是opType，value是该opType下的医嘱校验执行器集合
     * 内层的HashMap：key是医嘱校验规则名称，value是相应的医嘱校验执行器
     */
    private static HashMap<String, HashMap<String, OrderCheckExecutor>> executorCache;
    /**
     * 前置医嘱校验执行器，总是第一个执行
     */
    private static final PreCheck preCheck = new PreCheck();
    /**
     * 后置医嘱校验执行器，总是最后一个执行
     */
    private static final PostCheck postCheck = new PostCheck();
    /**
     * 规则DAO
     */
    private static final CpoeOrderCheckRuleDAO cpoeOrderCheckRuleDAO = CpoeOrderCheckRuleDAO.getInstance();
    /**
     * 规则所在的文件路径，用于扫描注解
     */
    private static final String RULE_PACKAGE_NAME = "com.haitaiinc.htmzinterface.service.ordercheckservice.rule";
    /**
     * 是否已启动
     */
    private static boolean isInit = false;

    /**
     * 启动方法
     * 扫描医嘱校验规则注解的类，向数据库中插入医嘱校验规则，缓存医嘱校验规则和执行器
     */
    public static void init() throws Exception {
        Set<Class<?>> classSet = loadRuleClass();
        List<CpoeOrderCheckRule> newRuleList  = createOrderCheckRuleList(classSet);
        insertNewRule(newRuleList);
        List<CpoeOrderCheckRule> ruleList = selectAllAvailableRule();
        ruleCache = createOpTypeAndRuleListMap(ruleList);
        executorCache = createOpTypeAndExecutorMap(ruleCache);
        isInit = true;
    }

    /**
     * 刷新缓存
     * 若数据库的校验规则变更了，则执行该方法
     */
    public synchronized static void refreshCache() throws Exception {
        if (!isInit) {
            throw new Exception("医嘱校验管理器还未初始化，请先调用本类 init 方法");
        }
        List<CpoeOrderCheckRule> ruleList = selectAllAvailableRule();
        ruleCache = createOpTypeAndRuleListMap(ruleList);
        executorCache = createOpTypeAndExecutorMap(ruleCache);
    }

    /**
     * 主入口
     * 根据opType获取缓存的医嘱校验执行器，按顺序执行校验，返回校验结果
     */
    public static Result check(OrderCheckServiceDto paramDto) throws Exception {
        if (!isInit) {
            throw new Exception("医嘱校验管理器还未初始化，请先调用本类 init 方法");
        }
        if (null == paramDto) {
            throw new Exception("参数 paramDto 为空");
        }
        String sourceType = paramDto.sourceType;
        if (null == sourceType || sourceType.length() == 0) {
            throw new Exception("参数 sourceType 为空");
        }
        Map<String, Object> ruleListAndExecutorList = getRuleListAndExecutorListFromCache(sourceType);
        List<CpoeOrderCheckRule> ruleList = (List<CpoeOrderCheckRule>) ruleListAndExecutorList.get("ruleList");
        List<OrderCheckExecutor> executorList = (List<OrderCheckExecutor>) ruleListAndExecutorList.get("executorList");
        return checkInOrder(ruleList, executorList, paramDto);
    }

    /**
     * 从缓存中获取执行器集合
     */
    private synchronized static Map<String, Object> getRuleListAndExecutorListFromCache(String sourceType) throws Exception {
        List<CpoeOrderCheckRule> ruleList = ruleCache.get(sourceType);
        if (null == ruleList || ruleList.isEmpty()) {
            throw new Exception("sourceType=" + sourceType + " 的医嘱校验规则不存在");
        }
        HashMap<String, OrderCheckExecutor> ruleNameAndExecutorMap = executorCache.get(sourceType);
        if (null == ruleNameAndExecutorMap || ruleNameAndExecutorMap.isEmpty()) {
            throw new Exception("sourceType=" + sourceType + " 的医嘱校验执行器不存在");
        }
        List<OrderCheckExecutor> executorList = new ArrayList<>(ruleList.size() + 2);
        for (CpoeOrderCheckRule rule : ruleList) {
            String ruleName = rule.getRuleName();
            OrderCheckExecutor executor = ruleNameAndExecutorMap.get(ruleName);
            if (null == executor) {
                throw new Exception("sourceType=" + sourceType + ",ruleName=" + ruleName + " 的医嘱校验执行器不存在");
            }
            executorList.add(executor);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("ruleList", ruleList);
        map.put("executorList", executorList);
        return map;
    }

    /**
     * 按顺序执行校验，返回校验结果
     */
    private static Result checkInOrder(List<CpoeOrderCheckRule> ruleList, List<OrderCheckExecutor> executorList, OrderCheckServiceDto paramDto) throws Exception {
        Result result = new Result("1", "成功");
        for (int i = 0; i < ruleList.size(); i++) {
            CpoeOrderCheckRule rule = ruleList.get(i);
            OrderCheckExecutor executor = executorList.get(i);
            executor.check(paramDto, result);
            if (!result.isSuccess()) {
                String limitLevel = rule.getLimitLevel();
                if (null != limitLevel && limitLevel.length() > 0) {
                    result.setCode(limitLevel);
                }
                return result;
            }
        }
        return result;
    }

    /**
     * 从数据库中获取可用的医嘱校验规则
     */
    private static List<CpoeOrderCheckRule> selectAllRule() throws Exception {
        return cpoeOrderCheckRuleDAO.selectAllRule();
    }

    /**
     * 从数据库中获取可用的医嘱校验规则
     */
    private static List<CpoeOrderCheckRule> selectAllAvailableRule() throws Exception {
        return cpoeOrderCheckRuleDAO.selectAllAvailableRule();
    }

    /**
     * 根据opType组织医嘱校验规则，根据ordinal从小到大排序
     */
    private static HashMap<String, List<CpoeOrderCheckRule>> createOpTypeAndRuleListMap(List<CpoeOrderCheckRule> ruleList) {
        if (null == ruleList || ruleList.isEmpty()) {
            return new HashMap<>(0);
        }
        HashMap<String, List<CpoeOrderCheckRule>> opTypeAndRuleListMap = new HashMap<>();
        for (CpoeOrderCheckRule rule : ruleList) {
            String opType = rule.getOpType();
            if (null == opType || opType.length() == 0) {
                continue;
            }
            String[] opTypeList = opType.split(",");
            for (String ot : opTypeList) {
                if (!opTypeAndRuleListMap.containsKey(ot)) {
                    opTypeAndRuleListMap.put(ot, new ArrayList<>());
                }
                opTypeAndRuleListMap.get(ot).add(rule);
            }
        }
        for (List<CpoeOrderCheckRule> list : opTypeAndRuleListMap.values()) {
            list.stream().sorted(Comparator.comparing(CpoeOrderCheckRule::getOrdinal));
        }
        return opTypeAndRuleListMap;
    }

    /**
     * 根据医嘱校验规则创建医嘱校验执行器实例
     */
    private static HashMap<String, HashMap<String, OrderCheckExecutor>> createOpTypeAndExecutorMap(HashMap<String, List<CpoeOrderCheckRule>> opTypeAndRuleListMap) throws Exception {
        if (null == opTypeAndRuleListMap || opTypeAndRuleListMap.isEmpty()) {
            return new HashMap<>(0);
        }
        HashMap<String, HashMap<String, OrderCheckExecutor>> opTypeAndExecutorMap = new HashMap<>(opTypeAndRuleListMap.size());
        for (Map.Entry<String, List<CpoeOrderCheckRule>> entry : opTypeAndRuleListMap.entrySet()) {
            List<CpoeOrderCheckRule> ruleList = entry.getValue();
            HashMap<String, OrderCheckExecutor> executorMap = new HashMap<>(ruleList.size());
            for (CpoeOrderCheckRule rule : ruleList) {
                String className = rule.getClassName();
                Class<?> aClass = Class.forName(className);
                OrderCheckExecutor executor = (OrderCheckExecutor) aClass.newInstance();
                String ruleName = rule.getRuleName();
                if (executorMap.containsKey(ruleName)) {
                    throw new Exception("ruleName=" + ruleName + " 重复存在，请删除至唯一");
                }
                executorMap.put(ruleName, executor);
            }
            String opType = entry.getKey();
            opTypeAndExecutorMap.put(opType, executorMap);
        }
        return opTypeAndExecutorMap;
    }

    /**
     * 扫描医嘱校验执行器
     */
    private static Set<Class<?>> loadRuleClass() {
        Reflections reflections = new Reflections(RULE_PACKAGE_NAME);
        return reflections.getTypesAnnotatedWith(OrderCheckRule.class);
    }

    /**
     * 组织待插入的医嘱校验规则对象集合
     */
    private static List<CpoeOrderCheckRule> createOrderCheckRuleList(Set<Class<?>> classSet) {
        if (null == classSet || classSet.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<CpoeOrderCheckRule> ruleList = new ArrayList<>();
        for (Class<?> aClass : classSet) {
            OrderCheckRule annotation = aClass.getDeclaredAnnotation(OrderCheckRule.class);
            CpoeOrderCheckRule rule = new CpoeOrderCheckRule();
            rule.setRuleId(UUID.randomUUID().toString().trim().replaceAll("-", " "));
            rule.setRuleName(annotation.ruleName());
            rule.setClassName(aClass.getName());
            rule.setOpType(annotation.opType());
            rule.setOrdinal(annotation.ordinal());
            rule.setInoutFlag(annotation.inoutFlag());
            ruleList.add(rule);
        }
        return ruleList;
    }

    /**
     * 插入新的医嘱校验规则
     * 假如 ruleName 在数据库中已存在，则不插入。
     */
    private static void insertNewRule(List<CpoeOrderCheckRule> newRuleList) throws Exception {
        if (null == newRuleList || newRuleList.isEmpty()) {
            return;
        }
        List<CpoeOrderCheckRule> oldRuleList = selectAllRule();
        Set<String> ruleNameSet = oldRuleList.stream().map(CpoeOrderCheckRule::getRuleName).collect(Collectors.toSet());
        List<CpoeOrderCheckRule> insertRuleList = new ArrayList<>();
        for (CpoeOrderCheckRule rule : newRuleList) {
            if (!ruleNameSet.contains(rule.getRuleName())) {
                insertRuleList.add(rule);
            }
        }
        if (!insertRuleList.isEmpty()) {
            cpoeOrderCheckRuleDAO.insert(insertRuleList);
        }
    }

}
