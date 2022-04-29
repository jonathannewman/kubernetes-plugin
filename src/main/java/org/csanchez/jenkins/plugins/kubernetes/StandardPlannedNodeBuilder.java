package org.csanchez.jenkins.plugins.kubernetes;

import hudson.Util;
import hudson.model.Descriptor;
import hudson.slaves.NodeProvisioner;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The default {@link PlannedNodeBuilder} implementation, in case there is other registered.
 */
public class StandardPlannedNodeBuilder extends PlannedNodeBuilder {
    private static final Logger LOGGER = Logger.getLogger(StandardPlannedNodeBuilder.class.getName());
    private static ExecutorService EXECUTOR = Executors.newFixedThreadPool(100);

    @Override
    public NodeProvisioner.PlannedNode build() {
        LOGGER.info("Entering build");
        long start = System.currentTimeMillis();
        KubernetesCloud cloud = getCloud();
        PodTemplate t = getTemplate();
        Future f;
        f = EXECUTOR.submit(() -> {
            long insideThreadStart = System.currentTimeMillis();
            LOGGER.info("actually creating slave");
            KubernetesSlave agent = KubernetesSlave
                    .builder()
                    .podTemplate(cloud.getUnwrappedTemplate(t))
                    .cloud(cloud)
                    .build();
            LOGGER.info("Created slave in " + (System.currentTimeMillis() - insideThreadStart) + " milliseconds");
            return agent;
            }
        );
        LOGGER.info("future created after " + (System.currentTimeMillis() - start) + " milliseconds");
        NodeProvisioner.PlannedNode result = new NodeProvisioner.PlannedNode(Util.fixNull("Kubernetes Agent"), f, getNumExecutors());
        LOGGER.info("Exiting build after " + (System.currentTimeMillis() - start) + " milliseconds");
        return result;
    }
}
