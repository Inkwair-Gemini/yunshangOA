package com.Ink.auth.process;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

@SpringBootTest
public class processInstanceTest {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;


    // 单个文件的部署
    @Test
    public void deployProcess() {
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("process/qingjia.bpmn20.xml")
                .addClasspathResource("process/qingjia.png")
                .name("请假申请流程")
                .deploy();

        System.out.println("deploy.getId() = " + deploy.getId());
        System.out.println("deploy.getName() = " + deploy.getName());
    }

    //启动流程实例
    @Test
    public void startProcess(){
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey("qingjia");
        System.out.println("processInstance.getProcessDefinitionId() = " + processInstance.getProcessDefinitionId());//qingjia:1:9680e0eb-f07d-11ed-910b-005056c00008
        System.out.println("processInstance.getId() = " + processInstance.getId());//2e3796f3-f09d-11ed-a639-005056c00008
        System.out.println("processInstance.getActivityId() = " + processInstance.getActivityId());//null
    }

    //查询个人的代办任务 zhangsan
    @Test
    public void findTaskList(){
        String assign="zhangsan";
        List<Task> list=taskService.createTaskQuery()
                .taskAssignee(assign).list();
        for (Task task : list) {
            System.out.println("task.getProcessInstanceId() = " + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    //处理当前任务
    @Test
    public void completeTask(){
        //先查寻
        Task task=taskService.createTaskQuery()
                .taskAssignee("zhangsan")
                .singleResult();
        //后完成
        taskService.complete(task.getId());
    }

    //查询已经处理的任务
    @Test
    public void findCompleteTaskList(){
        List<HistoricTaskInstance> list=historyService.createHistoricTaskInstanceQuery()
                .taskAssignee("zhangsan")
                .finished().list();
        for (HistoricTaskInstance historicTaskInstance : list) {
            System.out.println("流程实例id：" + historicTaskInstance.getProcessInstanceId());
            System.out.println("任务id：" + historicTaskInstance.getId());
            System.out.println("任务负责人：" + historicTaskInstance.getAssignee());
            System.out.println("任务名称：" + historicTaskInstance.getName());
        }
    }

    //删除流程定义
    @Test
    public void deleteDeployment() {
        //部署id
        String deploymentId = "qingjia:1:9680e0eb-f07d-11ed-910b-005056c00008";
        //删除流程定义，如果该流程定义已有流程实例启动则删除时出错
        repositoryService.deleteDeployment(deploymentId);
        //设置true 级联删除流程定义，即使该流程有流程实例启动也可以删除，设置为false非级别删除方式
//        repositoryService.deleteDeployment(deploymentId, true);
    }

    //查询流程定义
    @Test
    public void findProcessDefinitionLost(){
        List<ProcessDefinition> definitionList=repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion()
                .desc()
                .list();
        //输出流程定义信息
        for (ProcessDefinition processDefinition : definitionList) {
            System.out.println("流程定义 id="+processDefinition.getId());
            System.out.println("流程定义 name="+processDefinition.getName());
            System.out.println("流程定义 key="+processDefinition.getKey());
            System.out.println("流程定义 Version="+processDefinition.getVersion());
            System.out.println("流程部署ID ="+processDefinition.getDeploymentId());
        }
    }

    // 启动流程实例，添加businessKey
    @Test
    public void startUpProcessAddBusinessKey(){
        // 启动流程实例，指定业务标识businessKey，也就是请假申请单id
        ProcessInstance processInstance = runtimeService.
                startProcessInstanceByKey("qingjia","1001");
        // 输出
        System.out.println("业务id:"+processInstance.getBusinessKey()); //1001
        System.out.println("processInstance.getId() = " + processInstance.getId()); //8a9f78ff-f09a-11ed-9f57-005056c00008
    }
    
    //全部流程实例挂起
    @Test
    public void suspendProcessInstance(){
        // 1. 获取流程定义对象
        ProcessDefinition qingjia=repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("qingjia")
                .singleResult();
        // 2、调用流程定义对象的方法判断当前状态：挂起   激活
        boolean suspended = qingjia.isSuspended();
        if (suspended) {
            // 暂定,那就可以激活
            // 参数1:流程定义的id  参数2:是否激活    参数3:时间点
            repositoryService.activateProcessDefinitionById(qingjia.getId(), true, null);
            System.out.println("流程定义:" + qingjia.getId() + " 激活");
        } else {
            repositoryService.suspendProcessDefinitionById(qingjia.getId(), true, null);
            System.out.println("流程定义:" + qingjia.getId() + " 挂起");
        }
    }

    // 单个流程实例挂起
    @Test
    public void SingleSuspendProcessInstance() {
        String processInstanceId = "8a9f78ff-f09a-11ed-9f57-005056c00008";
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        //获取到当前流程定义是否为暂停状态   suspended方法为true代表为暂停   false就是运行的
        boolean suspended = processInstance.isSuspended();
        if (suspended) {
            runtimeService.activateProcessInstanceById(processInstanceId);
            System.out.println("流程实例:" + processInstanceId + "激活");
        } else {
            runtimeService.suspendProcessInstanceById(processInstanceId);
            System.out.println("流程实例:" + processInstanceId + "挂起");
        }
    }
}
