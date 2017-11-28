package com.ppolivka.plugin.filecleaner;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.*;

public class FileCleanerWorker {

    private Project project;
    private VirtualFile virtualFile;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public void setVirtualFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }


    public Collection<PsiFile> deleteFiles(List<String> filesToDelete) {

        return WorkerUtil.computeValueInModal(project, "Searching for files...", indicator -> {
            Set<PsiFile> psiToDelete = new HashSet<>();
            for(String fileName : filesToDelete) {
                PsiFile[] files = FilenameIndex.getFilesByName(project, fileName, GlobalSearchScope.everythingScope(project));
                if(files != null && files.length > 0) {
                    psiToDelete.addAll(Arrays.asList(files));
                }
            }
            return psiToDelete;
        });
    }

    public static FileCleanerWorker create(Project project, VirtualFile virtualFile) {
        return WorkerUtil.computeValueInModal(project, "Loading file info...", indicator -> {
            FileCleanerWorker fileCleanerWorker = new FileCleanerWorker();
            fileCleanerWorker.setProject(project);
            fileCleanerWorker.setVirtualFile(virtualFile);


            return fileCleanerWorker;
        });
    }

}
