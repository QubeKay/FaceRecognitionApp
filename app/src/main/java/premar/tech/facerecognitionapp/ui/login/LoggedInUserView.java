package premar.tech.facerecognitionapp.ui.login;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView {
    private String displayName;
    private Boolean success;
    //... other data fields that may be accessible to the UI

    LoggedInUserView(Boolean success, String displayName) {
        this.success = success;
        this.displayName = displayName;
    }

    String getDisplayName() {
        return displayName;
    }

    public Boolean getSuccess() {
        return success;
    }
}
