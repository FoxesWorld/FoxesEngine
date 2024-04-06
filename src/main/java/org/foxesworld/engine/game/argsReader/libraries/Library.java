package org.foxesworld.engine.game.argsReader.libraries;

import java.util.List;

public class Library {
    private String name;
    private List<Rule> rules;
    private Artifact artifact;

    public String getName() {
        return name;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public Artifact getArtifact() {
        return artifact;
    }
}

class Rule {
    private String action;
    private OS os;

    public String getAction() {
        return action;
    }

    public OS getOs() {
        return os;
    }
}

class OS {
    private String name;

    public String getName() {
        return name;
    }
}

class Artifact {
    private String sha1;
    private int size;
    private String path;
    private String url;

    public String getSha1() {
        return sha1;
    }

    public int getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }
}
