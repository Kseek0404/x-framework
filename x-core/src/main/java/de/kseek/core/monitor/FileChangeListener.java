package de.kseek.core.monitor;

import java.io.File;

/**
 * @author kseek
 * @date 2024/3/22
 */
public interface FileChangeListener {
    String getFileName();

    void onChange(File file);
}
