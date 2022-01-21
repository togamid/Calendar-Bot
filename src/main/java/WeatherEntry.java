public class WeatherEntry {
    public String name;
    public Integer temp_modifier;
    public WindStrength windStrength;

    public WeatherEntry(String name, Integer temp_modifier, WindStrength windStrength) {
        this.name = name;
        this.temp_modifier = temp_modifier;
        this.windStrength = windStrength;
    }
}