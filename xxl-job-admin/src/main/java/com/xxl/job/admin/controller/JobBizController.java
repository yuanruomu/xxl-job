package com.xxl.job.admin.controller;


import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.model.XxlJobBiz;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.util.IgnorePropertiesUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 业务用处理JOB
 *
 * @author XUQ
 */
@RestController
@RequestMapping("/job")
public class JobBizController {

    @Resource
    private XxlJobService xxlJobService;
    @Resource
    private XxlJobGroupDao xxlJobGroupDao;

    @RequestMapping("/add")
    @PermissionLimit(limit = false)
    public ReturnT<String> add(@RequestBody XxlJobBiz jobBiz) {
        XxlJobGroup group = xxlJobGroupDao.selectByName(jobBiz.getJobGroupName());
        if (group == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "执行器未定义或无效");
        }
        XxlJobInfo jobInfo = new XxlJobInfo();
        BeanUtils.copyProperties(jobBiz, jobInfo);
        jobInfo.setJobGroup(group.getId());
        ReturnT resultT = xxlJobService.add(jobInfo);
        if (resultT.getCode() == ReturnT.SUCCESS_CODE) {
            int jobId = Integer.parseInt((String) resultT.getContent());
            ReturnT startRes = xxlJobService.start(jobId);
            if (startRes.getCode() == ReturnT.SUCCESS_CODE) {
                return ReturnT.SUCCESS;
            } else {
                xxlJobService.remove(jobId);
                return ReturnT.FAIL;
            }
        }
        return resultT;
    }

    @RequestMapping("/update")
    @PermissionLimit(limit = false)
    public ReturnT<String> update(@RequestBody XxlJobBiz jobBiz) {
        ReturnT<XxlJobInfo> returnT = xxlJobService.selectByName(jobBiz.getJobName());
        if (returnT.getCode() == ReturnT.SUCCESS_CODE && returnT.getContent() != null) {
            XxlJobInfo jobInfo = returnT.getContent();
            BeanUtils.copyProperties(jobBiz, jobInfo, IgnorePropertiesUtil.getNullPropertyNames(jobBiz));
            // GROUP
            if (jobBiz.getJobGroupName() != null) {
                // 查询执行器
                XxlJobGroup group = xxlJobGroupDao.selectByName(jobBiz.getJobGroupName());
                if (group == null) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "执行器未定义或无效");
                }
                jobInfo.setJobGroup(group.getId());
            }
            return xxlJobService.update(jobInfo);
        }
        return ReturnT.FAIL;
    }

    @RequestMapping("/addOrUpdate")
    @PermissionLimit(limit = false)
    public ReturnT<String> addOrUpdate(@RequestBody XxlJobBiz jobBiz) {
        XxlJobGroup group = xxlJobGroupDao.selectByName(jobBiz.getJobGroupName());
        if (group == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "执行器未定义或无效");
        }
        ReturnT<XxlJobInfo> returnT = xxlJobService.selectByName(jobBiz.getJobName());
        if (returnT.getCode() == ReturnT.SUCCESS_CODE) {
            XxlJobInfo jobInfo = returnT.getContent();
            BeanUtils.copyProperties(jobBiz, jobInfo);
            if (jobInfo != null) {
                // 执行更新JOB
                jobInfo.setJobGroup(group.getId());
                BeanUtils.copyProperties(jobBiz, jobInfo);
                return xxlJobService.update(jobInfo);
            } else {
                // 添加JOB
                jobInfo = new XxlJobInfo();
                BeanUtils.copyProperties(jobBiz, jobInfo);
                jobInfo.setJobGroup(group.getId());
                ReturnT resultT = xxlJobService.add(jobInfo);
                if (resultT.getCode() == ReturnT.SUCCESS_CODE) {
                    int jobId = Integer.parseInt((String) resultT.getContent());
                    ReturnT startRes = xxlJobService.start(jobId);
                    if (startRes.getCode() == ReturnT.SUCCESS_CODE) {
                        return ReturnT.SUCCESS;
                    }
                }
            }
        }
        return ReturnT.FAIL;
    }

    @RequestMapping("/stop")
    @PermissionLimit(limit = false)
    public ReturnT<String> stop(String jobName) {
        return xxlJobService.stopByName(jobName);
    }

    @RequestMapping("/remove")
    @PermissionLimit(limit = false)
    public ReturnT<String> remove(String jobName) {
        return xxlJobService.removeByName(jobName);
    }
}