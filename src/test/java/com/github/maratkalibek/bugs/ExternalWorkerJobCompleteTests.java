package com.github.maratkalibek.bugs;

import org.flowable.engine.ManagementService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTest;
import org.flowable.job.api.AcquiredExternalWorkerJob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

@FlowableTest
public class ExternalWorkerJobCompleteTests {

    private ProcessEngine processEngine;

    @BeforeEach
    public final void initializeServices(ProcessEngine processEngine) {
//        processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
        this.processEngine = processEngine;
    }

    @Test
    @Deployment(resources = "processes/ExternalWorkerJobCompleteTests.shouldSuccessfullyCompleteExternalWorkerJob.bpmn20.xml")
    public void shouldSuccessfullyCompleteExternalWorkerJob() {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ManagementService managementService = processEngine.getManagementService();

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("ExternalWorkerJobCompleteTests.shouldSuccessfullyCompleteExternalWorkerJob.Process");

        Assertions.assertFalse(pi.isEnded());

        List<AcquiredExternalWorkerJob> acquiredJobs = managementService.createExternalWorkerJobAcquireBuilder()
                .topic("ExternalWorkerTask1Topic", Duration.ofMinutes(5))
                .acquireAndLock(10, "worker1");

        Assertions.assertEquals(1, acquiredJobs.size());

        AcquiredExternalWorkerJob job = acquiredJobs.get(0);

        managementService.createExternalWorkerCompletionBuilder(job.getId(), "worker1").complete();
        managementService.executeJob(job.getId());

        long runningProcesses = runtimeService.createProcessInstanceQuery().count();

        Assertions.assertEquals(0, runningProcesses);
    }

}
