package com.Ink.process.service.impl;
import com.Ink.auth.service.SysUserService;
import com.Ink.custom.LoginUserInfoHelper;
import com.Ink.model.process.Process;
import com.Ink.model.process.ProcessRecord;
import com.Ink.model.process.ProcessTemplate;
import com.Ink.model.system.SysUser;
import com.Ink.process.mapper.OaProcessMapper;
import com.Ink.process.service.OaProcessRecordService;
import com.Ink.process.service.OaProcessService;
import com.Ink.process.service.OaProcessTemplateService;
import com.Ink.process.service.WeChatMessageService;
import com.Ink.vo.process.ApprovalVo;
import com.Ink.vo.process.ProcessFormVo;
import com.Ink.vo.process.ProcessQueryVo;
import com.Ink.vo.process.ProcessVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipInputStream;

@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, Process> implements OaProcessService {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private SysUserService userService;
    @Autowired
    private OaProcessTemplateService templateService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private OaProcessRecordService processRecordService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private WeChatMessageService weChatMessageService;

    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageInfo, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> page= baseMapper.selectPage(pageInfo, processQueryVo);
        return page;
    }

    //流程部署
    public void deployByZip(String processDefinitionPath) {
        InputStream inputStream=
                this.getClass().getClassLoader().getResourceAsStream(processDefinitionPath);
        ZipInputStream zipInputStream=new ZipInputStream(inputStream);
        //部署
        Deployment deployment=repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .deploy();
        System.out.println(deployment.getId());
        System.out.println(deployment.getName());
    }

    //启动流程
    @Override
    public void startUp(ProcessFormVo processFormVo) {
        //1 根据当前用户id获取用户信息
        SysUser sysUser=userService.getById(LoginUserInfoHelper.getUserId());
        //2 根据模板信息id查询模板信息
        ProcessTemplate template = templateService.getById(processFormVo.getProcessTemplateId());
        //3 保存提交的审批信息到业务表: oa_process
        Process process=new Process();
        BeanUtils.copyProperties(processFormVo,process);
        process.setStatus(1);//审核中
        String workNo=System.currentTimeMillis()+"";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName()+"发起"+template.getName()+"申请");
        baseMapper.insert(process);
        //4 启动流程实例
         //流程定义KEY
        String processDefinitionKey = template.getProcessDefinitionKey();
         //流程业务key processId
        String businessKey = String.valueOf(process.getId());
        //流程参数 form表单json数据 转换成map集合
        String formValues = processFormVo.getFormValues();
        JSONObject jsonObject = JSON.parseObject(formValues);
        JSONObject formDate = jsonObject.getJSONObject("formData");
         //遍历formData
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : formDate.entrySet()) {
            map.put(entry.getKey(),entry.getValue());
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("data",map);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
        //5 查询审批人 审批人可能有多个
        List<Task> taskList = this.getCurrentTaskList(processInstance.getId());
        if (!CollectionUtils.isEmpty(taskList)) {
            List<String> assigneeList = new ArrayList<>();
            for(Task task : taskList) {
                SysUser user = userService.getUserByUserName(task.getAssignee());
                assigneeList.add(user.getName());
                //6 推送消息给下一个审批人
                weChatMessageService.pushPendingMessage(process.getId(),user.getId(), task.getId());
            }
            process.setProcessInstanceId(processInstance.getId());
            process.setDescription("等待" + StringUtils.join(assigneeList.toArray(), ",") + "审批");
        }
        //7 业务和流程关联更新数据
        baseMapper.updateById(process);
        //8 记录
        processRecordService.record(process.getId(),1,"发起申请");
    }

    //查询待处理
    @Override
    public IPage<ProcessVo> findPending(Page<Process> pageParam) {
        TaskQuery query = taskService.createTaskQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .orderByTaskCreateTime()
                .desc();
        int begin= (int) ((pageParam.getCurrent()-1)*pageParam.getSize());
        int size= (int) pageParam.getSize();
        long count = query.count();
        List<Task> tasks = query.listPage(begin, size);//参数1：开始位置，参数2：每页记录数
        List<ProcessVo> processVoList=new ArrayList<>();
        for (Task task : tasks) {
            //从task获取流程实例id
            String processInstanceId = task.getProcessInstanceId();
            //根据id获取实例对象
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            //从对象获取业务key
            String businessKey = processInstance.getBusinessKey();
            //根据业务key获取process对象
            Process process =  this.getById(businessKey);
            if (process == null) {
                continue;
            }
            ProcessVo processVo=new ProcessVo();
            BeanUtils.copyProperties(process,processVo);
            processVo.setTaskId(task.getId());
            processVoList.add(processVo);
        }
        IPage<ProcessVo> page=new Page<>(pageParam.getCurrent(), pageParam.getSize(),count);
        page.setRecords(processVoList);
        return page;
    }

    //查询已完成审批
    @Override
    public Object findProcessed(Page<Process> pageParam) {
        //封装查询条件
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .finished()
                .orderByTaskCreateTime()
                .desc();
        //调用方法查询
        int begin= (int) ((pageParam.getCurrent()-1)*pageParam.getSize());
        int size= (int) pageParam.getSize();
        List<HistoricTaskInstance> list = query.listPage(begin, size);
        long count = query.count();
        //返回
        List<ProcessVo> processVoList=new ArrayList<>();
        for (HistoricTaskInstance item : list) {
            String processInstanceId = item.getProcessInstanceId();
            LambdaQueryWrapper<Process> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(Process::getProcessInstanceId,processInstanceId);
            Process process=baseMapper.selectOne(wrapper);
            if (process == null) {
                continue;
            }
            //process->processVo
            ProcessVo processVo=new ProcessVo();
            BeanUtils.copyProperties(process,processVo);
            processVo.setTaskId(item.getId());
            processVoList.add(processVo);
        }
        IPage<ProcessVo> page=new Page<>(pageParam.getCurrent(),size,count);
        page.setRecords(processVoList);
        return page;
    }

    //已发起
    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo=new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> page = baseMapper.selectPage(pageParam, processQueryVo);
        return page;
    }

    //查询审批详情信息
    @Override
    public Map<String, Object> show(Long id) {
        //1.根据流程id获取流程信息process
        Process process = baseMapper.selectById(id);
        //2.根据流程id获取流程记录信息
        LambdaQueryWrapper<ProcessRecord> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(ProcessRecord::getProcessId,id);
        List<ProcessRecord> recordList = processRecordService.list(wrapper);
        //3.根据模板id查询模板信息
        ProcessTemplate processTemplate = templateService.getById(process.getProcessTemplateId());
        //4.判断当前用户是否可以审批
        boolean isApprove=false;
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        for (Task task : taskList) {
            //判断审批人是不是当前用户
            if(task.getAssignee().equals(LoginUserInfoHelper.getUsername())){
                isApprove=true;
            }
        }
        //返回
        Map<String,Object>map=new HashMap<>();
        map.put("process",process);
        map.put("processRecordList",recordList);
        map.put("processTemplate",processTemplate);
        map.put("isApprove",isApprove);
        return map;
    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        //1 从approveVo获取任务id，根据id获取流程变量
        String taskId=approvalVo.getTaskId();
        Map<String, Object> variables = taskService.getVariables(taskId);
        //2 状态值=1，通过; 状态值=-1 ，驳回，流程结束
        if(approvalVo.getStatus()==1){
            taskService.complete(taskId);
        }else{
            this.endTask(taskId);
        }
        //3 记录
        String description = approvalVo.getStatus().intValue() == 1 ? "已通过" : "驳回";
        processRecordService.record(approvalVo.getProcessId(),
                approvalVo.getStatus(),description);
        //4 查询下一个审批人
        Process process = baseMapper.selectById(approvalVo.getProcessId());
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if(!CollectionUtils.isEmpty(taskList)){
            List<String> assignRealNameList = new ArrayList<>();
            for (Task task : taskList) {
                String assignee=task.getAssignee();
                SysUser sysUser=userService.getUserByUserName(assignee);
                assignRealNameList.add(sysUser.getName());
                //消息推送
                weChatMessageService.pushPendingMessage(process.getId(),sysUser.getId(), task.getId());
            }
            //更新process流程信息
            process.setDescription("等待"+StringUtils.join(assignRealNameList.toArray(),",")+"审批");
            process.setStatus(1);
        }else{
            //当前审批人
            SysUser sysUser=userService.getById(process.getUserId());
            if(approvalVo.getStatus().intValue()==1){
                process.setDescription("审批完成(通过)");
                process.setStatus(2);
                //推送最后结果
                weChatMessageService.pushProcessedMessage(process.getId(),sysUser.getId(),process.getStatus());
            }else{
                process.setDescription("审批完成(驳回)");
                process.setStatus(-1);
                weChatMessageService.pushProcessedMessage(process.getId(),sysUser.getId(),process.getStatus());
            }
        }
        baseMapper.updateById(process);
    }

    private void endTask(String taskId) {
        //1 根据任务id获取任务对象task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //2 获取流程定义模型 bpmnModel
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        //3 获取结束流向节点
        List<EndEvent> endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        if(CollectionUtils.isEmpty(endEventList)){
            return;
        }
        FlowNode endFlowNode =endEventList.get(0);
        //4 获取当前流向节点
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
        //临时保存当前活动的原始方向
        List originalSequenceFlowList=new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        //5 清理当前流向方向
        currentFlowNode.getOutgoingFlows().clear();
        //6 创建新流向
        SequenceFlow newSequenceFlow=new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlow");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        //7 当前节点指向新方向
        List newSequenceFlowList=new ArrayList<>();
        newSequenceFlowList.add(newSequenceFlow);
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);
        //8 完成任务
        taskService.complete(taskId);
    }

    //查询当前任务列表
    private List<Task> getCurrentTaskList(String id){
        List<Task> list = taskService.createTaskQuery()
                .processInstanceId(id)
                .list();
        return list;
    }
}
