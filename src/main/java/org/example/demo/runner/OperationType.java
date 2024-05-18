package org.example.demo.runner;

public enum OperationType {

    POST_OWNER("/owner"),
    POST_PET("/pet"),
    POST_VISIT("/visit"),
    GET_OWNER("/owner/"),
    GET_PET("/pet/"),
    GET_VISIT_BY_PET("/visit/"),
    PATCH_OWNER("/owner"),
    PATCH_PET("/pet"),
    GET_PETS("/pet");

    private final String route;

    OperationType(String route){
        this.route = route;
    }

    public String getRoute() {
        return route;
    }
}
