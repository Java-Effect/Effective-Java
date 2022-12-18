package org.example.item01;

/**
 * 이 클래스의 인스턴스는 #getInstance() 를 통해 사용된다.
 * @see #getInstance()
 */
public class Settings {

    private boolean userAutoSetting;
    private boolean userABS;

    private static final Settings INSTANCE = new Settings();

    public Settings() {}

    public Settings(boolean userAutoSetting, boolean userABS) {
        this.userAutoSetting = userAutoSetting;
        this.userABS = userABS;
    }

    public static Settings getInstance() {
        return INSTANCE;
    }
}
