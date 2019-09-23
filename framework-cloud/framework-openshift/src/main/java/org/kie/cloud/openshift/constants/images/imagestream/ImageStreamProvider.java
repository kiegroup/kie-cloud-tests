/*
 * Copyright 2018 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.cloud.openshift.constants.images.imagestream;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamBuilder;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.images.Image;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.ProjectProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates image streams from file or generates them based on system properties.
 */
public class ImageStreamProvider {

    private static final Logger logger = LoggerFactory.getLogger(ImageStreamProvider.class);

    /**
     * Creates image streams in project which will be used by OpenShift template.
     * In case image stream URL is passed as system property then it is used, otherwise image streams are generated from image tag system properties.
     * @param project Project where image streams will be deployed to.
     */
    public static void createImageStreamsInProject(Project project) {
        String kieImageStreams = OpenShiftConstants.getKieImageStreams();

        boolean anyImageStreamTagPropertyIsSet = Stream.of(Image.values())
                                                       .map(Image::getTag)
                                                       .anyMatch(Optional::isPresent);

        if (kieImageStreams != null && !kieImageStreams.isEmpty()) {
            createImagesFromImageStreamFile(project, kieImageStreams);
            if (anyImageStreamTagPropertyIsSet) {
                replaceImagesFromImageStreamTags(project);
            }
        }
        else {
            logger.info("Image stream file not found, creating image streams using image stream tags.");
            createImagesFromImageStreamTags(project);
        }
    }

    private static void createImagesFromImageStreamFile(Project project, String kieImageStreams) {
        logger.info("Creating image streams from " + kieImageStreams);
        project.createResources(kieImageStreams);
    }

    private static void replaceImagesFromImageStreamTags(Project project) {
        logger.info("Replacing image streams tags.");
        Stream.of(Image.values())
              .filter(image -> image.getTag().isPresent())
              .forEach(image -> createImageStreamForImage(project, image));
    }

    private static void createImagesFromImageStreamTags(Project project) {
        ProjectProfile projectProfile = ProjectProfile.fromSystemProperty();
        logger.info("Creating image streams for {} project.", projectProfile);
        if (projectProfile == ProjectProfile.DROOLS) {
            createImageStreamForImage(project, Image.WORKBENCH);
            createImageStreamForImage(project, Image.KIE_SERVER);
            createImageStreamForImage(project, Image.CONTROLLER);
            createImageStreamForImage(project, Image.WORKBENCH_INDEXING);
        } else if (projectProfile == ProjectProfile.JBPM) {
            createImageStreamForImage(project, Image.AMQ);
            createImageStreamForImage(project, Image.CONSOLE);
            createImageStreamForImage(project, Image.CONTROLLER);
            createImageStreamForImage(project, Image.KIE_SERVER);
            createImageStreamForImage(project, Image.MYSQL);
            createImageStreamForImage(project, Image.POSTGRESQL);
            createImageStreamForImage(project, Image.SMARTROUTER);
            createImageStreamForImage(project, Image.WORKBENCH);
            createImageStreamForImage(project, Image.WORKBENCH_INDEXING);
        }
    }

    private static void createImageStreamForImage(Project project, Image image) {
        if (!image.getTag().isPresent()) {
            throw new RuntimeException("System property for image tag '" + image.getSystemPropertyForImageTag() + "' is not defined.");
        }

        logger.info("Creating image stream for " + image.toString() + " image.");
        ImageStream imageStream = new ImageStreamBuilder().withApiVersion("v1")
                                                          .withNewMetadata()
                                                              .withName(image.getImageName())
                                                              .addToAnnotations("openshift.io/image.insecureRepository", "true")
                                                              .addToAnnotations("openshift.io/display-name", image.getImageName())
                                                              .addToAnnotations("openshift.io/provider-display-name", "Red Hat, Inc.")
                                                          .endMetadata()
                                                          .withNewSpec()
                                                              .addNewTag()
                                                                  .withName(image.getImageVersion())
                                                                  .addToAnnotations("description", image.getImageName() + " image")
                                                                  .addToAnnotations("iconClass", "icon-jboss")
                                                                  .addToAnnotations("tags", "rhpam,xpaas")
                                                                  .addToAnnotations("supports", "xpaas:1.4")
                                                                  .addToAnnotations("version", image.getImageVersion())
                                                                  .withNewFrom()
                                                                      .withKind("DockerImage")
                                                                      .withName(image.getTag().get())
                                                                  .endFrom()
                                                                  .withNewImportPolicy()
                                                                      .withInsecure(Boolean.TRUE)
                                                                  .endImportPolicy()
                                                              .endTag()
                                                          .endSpec()
                                                          .build();

        ImageStream existingImageStream = project.getOpenShift().getImageStream(image.getImageName());
        if(existingImageStream != null) {
            logger.debug("Found already existing image stream for {}. Replacing it with custom set tag.", image.getImageName());
            project.getOpenShift().deleteImageStream(existingImageStream);
        }
        project.getOpenShift().createImageStream(imageStream);
    }
}
