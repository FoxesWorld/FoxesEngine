package org.foxesworld.engine.utils.Download;

public enum FileType {
    JAR("jar"),ZIP("zip"),PNG("png"),JSON("json"),TXT("txt"),OGG("ogg");

    private final String fileString;
    FileType(String fileString){
        this.fileString = fileString;
    }

    public String getType(){
        return fileString;
    };
}
