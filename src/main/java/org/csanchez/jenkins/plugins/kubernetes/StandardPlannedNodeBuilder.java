package org.csanchez.jenkins.plugins.kubernetes;

import hudson.Util;
import hudson.model.Descriptor;
import hudson.slaves.NodeProvisioner;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The default {@link PlannedNodeBuilder} implementation, in case there is other registered.
 */
public class StandardPlannedNodeBuilder extends PlannedNodeBuilder {
    private static final Logger LOGGER = Logger.getLogger(StandardPlannedNodeBuilder.class.getName());

    @Override
    public NodeProvisioner.PlannedNode build() {
        LOGGER.info("Entering build");
        long start = System.currentTimeMillis();
        KubernetesCloud cloud = getCloud();
        PodTemplate t = getTemplate();
        CompletableFuture f;
        String displayName;
        try {
            KubernetesSlave agent = KubernetesSlave
                    .builder()
                    .podTemplate(cloud.getUnwrappedTemplate(t))
                    .cloud(cloud)
                    .build();
            LOGGER.info("constructed agent after " + (System.currentTimeMillis() - start) + " milliseconds");
            displayName = agent.getDisplayName();
            f = CompletableFuture.completedFuture(agent);
            LOGGER.info("future created after " + (System.currentTimeMillis() - start) + " milliseconds");
        } catch (IOException | Descriptor.FormException e) {
            LOGGER.log(Level.SEVERE, "Failed to create agent", e);
            displayName = null;
            f = new CompletableFuture();
            f.completeExceptionally(e);
        }

        NodeProvisioner.PlannedNode result = new NodeProvisioner.PlannedNode(Util.fixNull(displayName), f, getNumExecutors());
        LOGGER.info("Exiting build after " + (System.currentTimeMillis() - start) + " milliseconds");
        return result;
    }
}
