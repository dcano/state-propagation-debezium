package io.twba.course_management;

public record PreRequirement(String requirement) {

    public static PreRequirement from(String requirement) {
        return new PreRequirement(requirement);
    }

}
