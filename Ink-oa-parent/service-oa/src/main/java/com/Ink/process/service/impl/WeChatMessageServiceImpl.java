package com.Ink.process.service.impl;

import com.Ink.auth.service.SysUserService;
import com.Ink.custom.LoginUserInfoHelper;
import com.Ink.model.process.Process;
import com.Ink.model.process.ProcessTemplate;
import com.Ink.model.system.SysUser;
import com.Ink.process.service.OaProcessService;
import com.Ink.process.service.OaProcessTemplateService;
import com.Ink.process.service.WeChatMessageService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Map;

@Service
public class WeChatMessageServiceImpl implements WeChatMessageService {

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private OaProcessService processService;

    @Autowired
    private OaProcessTemplateService processTemplateService;

    @Autowired
    private SysUserService sysUserService;

    //发送请求处理信息
    @Override
    public void pushPendingMessage(Long processId, Long userId, String taskId) {
        //查询流程信息
        Process process = processService.getById(processId);
        //查询推送人信息
        SysUser targetSysUser = sysUserService.getById(userId);
        //查询模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        //获取提交审批人信息
        SysUser submitSysUser = sysUserService.getById(process.getUserId());
        String openId = targetSysUser.getOpenId();
        // TODO: 2023/5/28 为了测试方便，添加默认值：自己的openid
        if (StringUtils.isEmpty(openId))
        {
            openId = "o78FD5zcvXTqZQVHlPhf9lP_TjpU";
        }
        //设置消息发送信息
        WxMpTemplateMessage wxMpTemplateMessage=WxMpTemplateMessage.builder()
                .toUser(openId)
                .templateId("L9zIo7ejFH61M2YcWtyAws75uOPbAXnQ00rpkpwSD4w")//模板id
                .url("http://weichat.free.svipss.top/#/show/"+ processId + "/" + taskId)//跳转地址
                .build();
        //设置推送模板
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuilder content = new StringBuilder();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        wxMpTemplateMessage
                .addData(new WxMpTemplateData("first",submitSysUser.getName()+
                        "提交"+processTemplate.getName()+"，请注意查看。","#272727"));
        wxMpTemplateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        wxMpTemplateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        wxMpTemplateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        //发送
        try {
            String sendTemplateMsg  = wxMpService.getTemplateMsgService().sendTemplateMsg(wxMpTemplateMessage);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    //发送处理结果信息
    @Override
    public void pushProcessedMessage(Long processId, Long userId, Integer status) {
        Process process = processService.getById(processId);
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        SysUser sysUser = sysUserService.getById(userId);
        SysUser currentSysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        String openId = sysUser.getOpenId();
        // TODO: 2023/5/28 为了测试方便，添加默认值：自己的openid
        if (StringUtils.isEmpty(openId))
        {
            openId = "o78FD5zcvXTqZQVHlPhf9lP_TjpU";
        }
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openId)//要推送的用户openid
                .templateId("unxOGGpT2p_0NUC7veWg5mDb981aUq-SyhqFmBbdVTM")//模板id
                .url("http://http://weichat.free.svipss.top/#/show/"+processId+"/0")//点击模板消息要访问的网址
                .build();
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formShowData = jsonObject.getJSONObject("formShowData");
        StringBuffer content = new StringBuffer();
        for (Map.Entry entry : formShowData.entrySet()) {
            content.append(entry.getKey()).append("：").append(entry.getValue()).append("\n ");
        }
        templateMessage.addData(new WxMpTemplateData("first", "你发起的"+processTemplate.getName()+"审批申请已经被处理了，请注意查看。", "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword1", process.getProcessCode(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword2", new DateTime(process.getCreateTime()).toString("yyyy-MM-dd HH:mm:ss"), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword3", currentSysUser.getName(), "#272727"));
        templateMessage.addData(new WxMpTemplateData("keyword4", status == 2 ? "审批通过" : "审批拒绝", status == 2 ? "#009966" : "#FF0033"));
        templateMessage.addData(new WxMpTemplateData("content", content.toString(), "#272727"));
        String msg = null;
        try {
            msg = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }
}