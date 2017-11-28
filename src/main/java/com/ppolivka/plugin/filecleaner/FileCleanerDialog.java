package com.ppolivka.plugin.filecleaner;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FileCleanerDialog extends DialogWrapper {

    private JPanel mainView;
    private JTextArea filesToDelete;

    private FileCleanerWorker fileCleanerWorker;

    private Collection<PsiFile> files;

    public FileCleanerDialog(FileCleanerWorker fileCleanerWorker) {
        super(fileCleanerWorker.getProject());
        this.fileCleanerWorker = fileCleanerWorker;
        init();
    }

    @Override
    protected void init() {
        super.init();
        setTitle("Files to delete");
        setHorizontalStretch(3f);
        setVerticalStretch(1f);
        setOKButtonText("Delete");
    }

    @Override
    protected void doOKAction() {
        String s[] = filesToDelete.getText().split("\\r?\\n");
        List<String> linesUnprocessed = new ArrayList<>(Arrays.asList(s));
        List<String> lines = linesUnprocessed.stream().filter(line -> !StringUtils.containsWhitespace(line)).collect(Collectors.toList());
        this.files = fileCleanerWorker.deleteFiles(lines);
        super.doOKAction();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainView;
    }

    public Collection<PsiFile> getFiles() {
        return files;
    }
}
