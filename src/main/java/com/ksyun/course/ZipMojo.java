package com.ksyun.course;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Mojo(name = "zip")
public class ZipMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "${project.basedir}", required = true)
    private File baseDirectory;

    @Parameter(property = "zipFileName", defaultValue = "${project.name}", required = true)
    private String zipFileName;

    public void execute() throws MojoExecutionException {
        getLog().info("Creating ZIP file: " + zipFileName);
        // 确保 zipFileName 以 ".zip" 结尾
        if (!zipFileName.toLowerCase().endsWith(".zip")) {
            zipFileName += ".zip";
        }
        Path zipFilePath = Paths.get(outputDirectory.getAbsolutePath(), zipFileName);
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath.toFile().toPath()))) {
            Files.walk(baseDirectory.toPath())
                    .filter(path -> !Files.isDirectory(path) && !path.toString().endsWith(".zip"))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(baseDirectory.toPath().relativize(path).toString());
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            getLog().error("Failed to add file to ZIP: " + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create ZIP file", e);
        }
        getLog().info("ZIP file created successfully at: " + zipFilePath);
    }
}
