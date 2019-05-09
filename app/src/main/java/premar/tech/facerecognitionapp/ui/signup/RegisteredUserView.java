package premar.tech.facerecognitionapp.ui.signup;

/**
 * Class exposing registered user details to the UI.
 */
class RegisteredUserView {
    private String responseMessage;
    private Boolean success;
    //... other data fields that may be accessible to the UI

    RegisteredUserView(Boolean success, String responseMessage) {
        this.success = success;
        this.responseMessage = responseMessage;
    }

    String getResponseMessage() {
        return responseMessage;
    }
}