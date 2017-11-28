package com.ppolivka.plugin.filecleaner;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.actions.VirtualFileDeleteProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.SafeDeleteRefactoring;
import com.intellij.usageView.UsageInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class FileCleanerAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {

        Project project = event.getProject();
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);

        FileCleanerWorker fileCleanerWorker = FileCleanerWorker.create(project, file);
        if(fileCleanerWorker != null) {
            FileCleanerDialog branchCleanerDialog = new FileCleanerDialog(fileCleanerWorker);
            branchCleanerDialog.show();
            ApplicationManager.getApplication().invokeLater(() -> {
                Collection<PsiFile> filesToDelete = branchCleanerDialog.getFiles();
                int filesToDeleteCount = filesToDelete.size();
                int filesToDeleteCountOld = 0;
                int attempts = 0;
                while (filesToDeleteCount > 0 && attempts < 100 && filesToDeleteCount != filesToDeleteCountOld) {
                    try {
                        filesToDeleteCountOld = filesToDeleteCount;
                        for (PsiFile fileToDelete : filesToDelete) {
                            List<PsiElement> elements = new ArrayList<>();
                            elements.add(fileToDelete);
                            elements.addAll(Arrays.asList(fileToDelete.getChildren()));
                            SafeDeleteRefactoring safeDeleteRefactoring =
                                    RefactoringFactory.getInstance(project)
                                            .createSafeDelete(
                                                    elements.toArray(new PsiElement[elements.size()])
                                            );
                            safeDeleteRefactoring.setSearchInComments(true);
                            safeDeleteRefactoring.setSearchInNonJavaFiles(true);
                            UsageInfo[] usageInfos = safeDeleteRefactoring.findUsages();
                            if (usageInfos == null || usageInfos.length == 0) {
                                final VirtualFileDeleteProvider provider = new VirtualFileDeleteProvider();
                                try {
                                    fileToDelete.getVirtualFile().delete(provider);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                filesToDeleteCount--;
                            }
                        }

                    } catch (Exception e) {
                        //noop
                    } finally {
                        attempts++;
                    }
                }

            });
        }
    }


}
