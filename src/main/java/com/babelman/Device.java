package com.babelman;

public class Device {

    private String name, deviceId, macAddr;

    public Device(String name, String deviceId, String macAddr) {
        this.name = name;
        this.deviceId = deviceId;
        this.macAddr = macAddr;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return deviceId;
    }

    public String getMacAddr() {
        return macAddr;
    }
}
