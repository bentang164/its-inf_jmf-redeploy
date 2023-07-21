public class Computer {
    String serialNumber, name;

    public Computer(String serialNumber, String assetNumber) {
        this.serialNumber = serialNumber;
        this.name = assetNumber;
    }

    public String getSerial() {
        return serialNumber;
    }

    public String getAsset() {
        return name;
    }
}
