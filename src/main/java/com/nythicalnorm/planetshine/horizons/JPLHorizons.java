package com.nythicalnorm.planetshine.horizons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.spacecraft.irlship.AbstractIrlSpacecraft;
import com.nythicalnorm.planetshine.spacecraft.irlship.ServerIrlSpacecraft;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class JPLHorizons {
    public static final Map<String, String> planetNameToJPLID = Map.of(
            "earth", "500@399",
            "luna", "500@301",
            "sun", "500@10",
            "mars", "500@499"
    );

    public static JsonObject getData(String spacecraft_name, String planetName) {
        try {
            // Spacecraft ID in Horizons (Voyager 1 = -31)

            String apiUrl = "https://ssd.jpl.nasa.gov/api/horizons.api";
            double currentJDTime = getCurrentJDTime();
            String Center = planetNameToJPLID.get(planetName);
            if (Center == null) {
                return null;
            }
            String query =
                    "format=json" +
                            "&COMMAND=" + URLEncoder.encode("'" + spacecraft_name + "'", StandardCharsets.UTF_8) +
                            "&MAKE_EPHEM='YES'" +
                            "&EPHEM_TYPE='ELEMENTS'" +
                            "&CENTER="+ URLEncoder.encode("'" + Center + "'", StandardCharsets.UTF_8) +
                            "&CSV_FORMAT='YES'" +
                            "&TLIST=" + URLEncoder.encode("'" + currentJDTime + "'", StandardCharsets.UTF_8);

            URL url = new URL(apiUrl + "?" + query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;


            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            return JsonParser.parseString(response.toString()).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            if (PSServer.get() != null) {
                PSServer.get().sendAllMessage(e.toString());
            }
        }
        return null;
    }

    private static double getCurrentJDTime() {
        double unixTime = (double) System.currentTimeMillis() / 1000d;
        return (unixTime / 86400.0) + 2440587.5;
    }

    public static long julianToUnix(double jd) {
        return (long)((jd - 2440587.5) * 86400);
    }

    public static @Nullable ServerIrlSpacecraft getSpacecraftData(String spacecraftName, String body, double mass) {
        JsonObject jsonObject = getData(spacecraftName, body);
        if (jsonObject == null) {
            return null;
        }

        JsonElement errorElement = jsonObject.get("error");
        if (errorElement != null){
            PSServer.get().sendAllMessage(errorElement.getAsString());
            return null;
        }

        JsonElement result = jsonObject.get("result");

        if (result != null) {
            String resultString = result.getAsString();

            String spacecraftDisplayName = getSpacecraftName(resultString);

            String responseEphem = getBetweenDelimiters(resultString);
            String[] ephemerisData = responseEphem.split(",");
            OrbitalElements orbitalElements = parseOrbitalElements(ephemerisData, mass);
            if (orbitalElements == null) {
                return null;
            }
            PSServer.get().sendAllMessage(responseEphem);

            AbstractIrlSpacecraft.IRLSpacecraftBuilder builder = new AbstractIrlSpacecraft.IRLSpacecraftBuilder();
            builder.setJplId(spacecraftName);
            builder.setId(OrbitId.getIdFromUTFString(spacecraftName));
            builder.setStableOrbit(false);
            builder.setOrbitalElements(orbitalElements);

            builder.setDisplayName(Component.literal(Objects.requireNonNullElse(spacecraftDisplayName, spacecraftName)));

            return (ServerIrlSpacecraft) builder.build();
        }

        return null;
    }

    public static OrbitalElements getOrbitalElementData(String spacecraftName, String body, double mass) {
        JsonObject jsonObject = getData(spacecraftName, body);
        if (jsonObject == null) {
            return null;
        }

        JsonElement errorElement = jsonObject.get("error");
        if (errorElement != null){
            PSServer.get().sendAllMessage(errorElement.getAsString());
            return null;
        }

        JsonElement result = jsonObject.get("result");

        if (result != null) {
            String resultString = result.getAsString();
            String responseEphem = getBetweenDelimiters(resultString);
            String[] ephemerisData = responseEphem.split(",");
            return parseOrbitalElements(ephemerisData, mass);
        }
        return null;
    }

    private static @Nullable OrbitalElements parseOrbitalElements(String[] ephemerisData, double parentMass) {
        try {
            double eccentricity = Double.parseDouble(ephemerisData[2]);

            double periapsisJD = Double.parseDouble(ephemerisData[7]);

            long periapsisTime = TimeCalc.timeDoubleToLong(julianToUnix(periapsisJD));

            double inclination = Math.toRadians(Double.parseDouble(ephemerisData[4]));
            double longitudeOfAscendingNode = Math.toRadians(Double.parseDouble(ephemerisData[5]));
            double argumentOfPeriapsis = Math.toRadians(Double.parseDouble(ephemerisData[6]));

            double semiMajorAxis = Double.parseDouble(ephemerisData[11]) * 1000d;

            if (eccentricity > 1.0d && semiMajorAxis > 0.0d) {
                semiMajorAxis = -semiMajorAxis;
            }

            return new OrbitalElements(semiMajorAxis, eccentricity, periapsisTime, inclination, argumentOfPeriapsis, longitudeOfAscendingNode, parentMass);
        } catch (Exception e) {
            PSServer.get().sendAllMessage("Cannot parse Orbital Elements from JPL Horizons");
        }
        return null;
    }

    private static String getSpacecraftName(String text) {
        String result = null;

        for (String line : text.split("\\R")) { // splits on any newline
            if (line.startsWith("Target body name:")) {
                result = line.substring(18);

                int index = result.indexOf("(");
                if (index >= 1) {
                    result = result.substring(0, index - 1);
                }

                return result;
            }
        }

        return null;
    }

    private static String getBetweenDelimiters(String str) {
        String startDelimiter = "$$SOE";
        String endDelimiter = "$$EOE";

        int startIndex = str.indexOf(startDelimiter);
        int endIndex = str.indexOf(endDelimiter, startIndex + startDelimiter.length());

        if (startIndex != -1 && endIndex != -1) {
            // Extract the substring between the delimiters
            return str.substring(startIndex + startDelimiter.length(), endIndex);
        } else {
            PSServer.get().sendAllMessage("Cannot parse response from JPL Horizons");
            return "";
        }
    }
}
