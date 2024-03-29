package com.brpc.plugin;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "proto2java", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class Proto2Java extends AbstractMojo {

    @Parameter(defaultValue = "src/main/proto")
    private String     protoPath;

    @Parameter(defaultValue = "src/main/java")
    private String     buildPath;

    /**
     * @parameter expression="${project.basedir}"
     * @required
     * @readonly
     */
    @Parameter(defaultValue="${project.basedir}")
    private File basedir;

    private List<File> allProtoFile = new ArrayList<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File deirectory = new File(basedir,protoPath);
        System.err.println(deirectory.toString());
        File build = new File(basedir,buildPath);

        listAllProtoFile(deirectory);

        Proto2ServicePojo protp2ServicePojo = Proto2ServicePojo.forConfig(deirectory.toString(), build.toString());
        for (File file : allProtoFile) {

            if (file.exists()) {
                String protoFilePath = file.getPath();
                protp2ServicePojo.generateFile(protoFilePath);
            }
        }
    }

    private File listAllProtoFile(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                File[] fileArray = file.listFiles();
                if (fileArray != null) {
                    for (int i = 0; i < fileArray.length; i++) {
                        listAllProtoFile(fileArray[i]);
                    }
                }
            } else {
                if (StringUtils.endsWith(file.getName(), "proto")) {
                    allProtoFile.add(file);
                }
            }
        }
        return null;
    }
}
