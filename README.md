# FaceRecognitionApp
The stated aim of this project is very straightforward. We are creating an app that can detect a face using the camera, capture the face, and send it to a back-end for registration. Subsequently, the same face can be used to log into the app instead of a password. I used OpenCV for face detection; Retrofit and Moshi for handling API requests and responses; Nammu to manage permissions; and Picasso, Timber, and Sweet Alerts for other tasks.

Of the above, OpenCV took some time to set up because it required working with the NDK. But eventually, I compiled OpenCV native code and imported it as a module in my Android project. This saved me a lot of work later on because I only needed to change a few lines in OpenCV’s sample sources to get the bitmap of a detected face. In a nutshell, once OpenCV crops a face from the surrounding, I pass it to the function in the image below which returns it to my activity.

    private void onFaceFound(final Mat firstFoundFace) {
        if (firstFoundFace != null) {

            final Bitmap faceImageBitmap = OpenCVUtils.convertMatToBitMap(firstFoundFace);

            runOnUiThread(new Runnable() {
                @Overridepublic void run() {
                    ivSelectedImagePreview.setImageBitmap(faceImageBitmap);
                }
            });

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            faceImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();

            // Base64.DEFAULT flag adds newline after every 76 xters, wasted my day!
            String faceImageBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP);

            faceImageBase64 = "data:image/png;base64," + faceImageBase64;

            Intent resultData = new Intent();

            // this fails if string too long
//            resultData.putExtra(DETECTED_FACE_KEY, faceImageBase64);
            StaticData.base64ImageData = faceImageBase64; // use static field instead
            StaticData.bitmapImageData = faceImageBitmap;

            setResult(RESULT_OK, resultData);
            finish();
        }
    }
Once I have the face image it is trivial work to convert it to base64, and compose the JSON message which I forward to the API described in the previous article. This is where Retrofit and Moshi come in. Moshi takes care of marshaling POJO model classes into JSON whereas Retrofit is responsible for the HTTP and entire communication with the server. Both of these libraries and the others mentioned earlier can be loaded with a single line of code using Gradle in Android Studio. 

Basically, during registration, I send the name, email/username, password, and the base64 face image. In the back-end, the AI model converts the face image into a set of 128 numbers which are called faceprints. It is these prints that are saved to the database and not the image itself. For privacy reasons, I chose not to save the images at all. Below is the code that uses Retrofit to push a sign up request to the back-end, User is my registration model/POJO class.

    private void sendSignupRequest(User user) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseMessage> users = apiInterface.createUser(user);
        users.enqueue(new Callback<ResponseMessage>() {
            @Overridepublic void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
//                Toast.makeText(SignupActivity.this, "Success: Eureka! \n" + response.message(), Toast.LENGTH_SHORT).show();
                ResponseMessage responseMessage = response.body();
                if (responseMessage != null) {
                    signupResult.setValue(new SignupResult(new RegisteredUserView(responseMessage.success, responseMessage.message)));
//                    Toast.makeText(context, "Message : : " + responseMessage.message, Toast.LENGTH_SHORT).show();
                    Timber.d("RESPONSE MESSAGE : : " + responseMessage.message);
                } else {
                    signupResult.setValue(new SignupResult(R.string.signup_failed));
                }
            }

            @Overridepublic void onFailure(Call<ResponseMessage> call, Throwable t) {
                signupResult.setValue(new SignupResult(R.string.connection_error));
//                Toast.makeText(SignupActivity.this, "Failed miserably!\n"+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


Once a user is fully registered, they can log in using either their username and face or username and password, see the image below. I could have used the face alone (without the username) but that would take much longer processing time at the back-end. You see, faceprints of different images of your face are not exactly similar. So without a username with which to retrieve a given user’s faceprints, we would have to compare their current faceprint with every other faceprint in the DB until we find the one that’s most similar to it. As you can imagine, this takes a lot of time.

Add descriptionNo alt text provided for this image
The process of making this app was full of exciting discoveries. For example, I used Android's login activity template. To no one's surprise, I found that Android's SDK 28 moved things around again -- making some things harder and others easier, like always. So now changing the text on the login button based on whether a user has entered a password as in the image above is rather easy. But it took me some time to figure out how to do that because of the different way relevant classes are organized now. Mind you, the default login activity template has eight more supporting classes by itself. Nine classes just to send a login request, come on Android!

Anyway, I hope this article and the one before it give you an idea about how to structure your face recognition project. I did not get into practical details of how to implement everything because you can find very good documentation for every library and tool online. Besides I have shared the code for both the back-end API and the android app on GitHub to get you started. If you meet any challenges, please ask a question in the comments section below. There is a whole community out here ready to assist, do not waste time struggling with basic stuff unless that is just how you roll.

In conclusion, this concludes the quasi-technical part of my articles on how to complete a face recognition project. Getting a hands-on experience working with the technologies, tools, and libraries we have met so far should prepare you for more advanced AI projects. My next project involves a highly imbalanced water consumption dataset a friend of mine got from Kampala. We shall be building an AI model to detect water misuse and theft based on consumption history, etc. Check out my next article for a compilation of lessons learned so far and how this project has prepared me for the next challenge.
