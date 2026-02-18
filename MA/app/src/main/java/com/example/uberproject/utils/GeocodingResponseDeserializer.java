package com.example.uberproject.utils;

import com.example.uberproject.dto.response.GeocodingResponseDTO;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class GeocodingResponseDeserializer implements JsonDeserializer<GeocodingResponseDTO> {
    @Override
    public GeocodingResponseDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        GeocodingResponseDTO result = new GeocodingResponseDTO();

        // Handle lat as either string or double
        if (jsonObject.has("lat")) {
            JsonElement latElement = jsonObject.get("lat");
            double lat = latElement.isJsonPrimitive() ?
                    (latElement.getAsJsonPrimitive().isNumber() ? latElement.getAsDouble() : Double.parseDouble(latElement.getAsString()))
                    : 0.0;
            result.setLat(lat);
        }

        // Handle lon as either string or double
        if (jsonObject.has("lon")) {
            JsonElement lonElement = jsonObject.get("lon");
            double lon = lonElement.isJsonPrimitive() ?
                    (lonElement.getAsJsonPrimitive().isNumber() ? lonElement.getAsDouble() : Double.parseDouble(lonElement.getAsString()))
                    : 0.0;
            result.setLon(lon);
        }

        // Handle name
        if (jsonObject.has("display_name")) {
            result.setName(jsonObject.get("display_name").getAsString());
        }

        return result;
    }
}

