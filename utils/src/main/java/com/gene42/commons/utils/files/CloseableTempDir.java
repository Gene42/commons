package com.gene42.commons.utils.files;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Temp directory which deletes itself when closed, or on jvm exit.
 *
 * @version $Id$
 */
public class CloseableTempDir implements Closeable {
    private final File dir;

    /**
     * Constructor.
     * @throws IOException if any issue occurs while creating the temp directory
     */
    public CloseableTempDir() throws IOException {
        File jvmTempFolder = new File(System.getProperty("java.io.tmpdir"));
        String folderName = getRandomName();
        this.dir = new File(jvmTempFolder, "gene42-" + folderName);
        FileUtils.forceMkdir(this.dir);
        this.dir.deleteOnExit();
    }

    /**
     * Get the temp dir as a File.
     * @return a File of the dir
     */
    @NotNull
    public File getDir() {
        return this.dir;
    }

    @Override
    public void close() {
        FileUtils.deleteQuietly(this.dir);
    }

    /**
     * Generate a random file name. This implementation uses UUID to generate the name.
     * @return a random file name
     */
    public static String getRandomName() {
        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString();
    }
}
