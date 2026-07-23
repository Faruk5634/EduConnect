package com.educonnect.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class AnnouncementFile {
    private String fileName;
    private String fileUrl;

    public AnnouncementFile() {
    }

    public AnnouncementFile(String fileName, String fileUrl) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}