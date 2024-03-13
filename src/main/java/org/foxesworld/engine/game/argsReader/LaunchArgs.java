package org.foxesworld.engine.game.argsReader;

import java.util.List;

public class LaunchArgs {
    private String id;
    private String time;
    private String releaseTime;
    private String type;
    private String mainClass;
    private int minimumLauncherVersion;
    private Arguments arguments;

    public String getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public String getReleaseTime() {
        return releaseTime;
    }

    public String getType() {
        return type;
    }

    public String getMainClass() {
        return mainClass;
    }

    public int getMinimumLauncherVersion() {
        return minimumLauncherVersion;
    }

    public Arguments getArguments() {
        return arguments;
    }
}

class Arguments {
    private List<JvmArgument> jvm;
    private List<GameArgument> game;
}

class JvmArgument {
    private List<String> values;
    private List<Rule> rules;
}

class GameArgument {
    private List<String> values;
    private List<Rule> rules;
}

class Rule {
    private String action;
    private Os os;
    private Features features;
}

class Os {
    private String name;
    private String version;
}

class Features {
    private boolean is_demo_user;
    private boolean has_custom_resolution;
}