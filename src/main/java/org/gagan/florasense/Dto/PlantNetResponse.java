package org.gagan.florasense.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlantNetResponse(
        String bestMatch,  //The API's best-match species name.
        List<Candidate> results, //A list of possible plant species.
        Integer remainingIdentificationRequests //Remaining identification requests reported by the API.
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(
            Double score, //score is 0.95, representing a confidence score of 95%.
            Species species //species contains the details of that candidate.
    ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Species(
            String scientificName,
            String scientificNameWithoutAuthor,
            List<String> commonNames //The List<String> is used because a species can have multiple common names.
    ){}

}
/*
* it combines Java records, nested records, and Jackson JSON deserialization.
* These concepts are commonly used when integrating an external API like Pl@ntNet with Spring Boot.
* */