# EyeReadApp
* Capstone project using Google Cloud Vision and Google Mobile Vision
* Programming Language: Kotlin and Java
* API's used: Using Camera2 API with Firebase SDK 



## Authors

* **Jonerec Cereno** - *Initial work* 
* **Vincent San Joaquin** - *Initial work* 


![poster](https://i.imgur.com/Gf8wOEB.png)


	CODE EATER SOURCE CODE NOTE
	October 2016 - September 2017 

Notes Changes Made:
June-August 2017
	- Added function centerCropImage() for cropping images Line 155
	- Added when expression for btnImage and btnText Line 366
	- Added auto focus on capture builder Line 308 and Line 433
	- Added Media Action Sound for shutter click effect Line 365
	- Changed CameraDevice.TEMPLATE_MANUAL to TEMPLATE_STILL_CAPTURE Line 302 and Line 420
	- Changed callCloudVision(bitmap) to callCloudVision(centerCropImage(bitmap))
	- Fixed the signing error for being null (since the new version of the gradle)
	    -> added lintOptions{} in build.gradle ->
	- Modified the fragment_vision.xml for wrapping of the <TextureView>
	- Added SplashScreen Activity
	- Note: The only problem is if the user don't have net but can access the
	    MainActivity.kt, the list and image is press caused to the app crash
Sept. 1-17, 2017
	- added function filterResponse() for filtering results Line 168
    - added condition to return tts if API request failed Line 389
    - added tts to notify user if text are recognized Line 408
    - added arrayList tobeF as storage for filtered response Line 699
    - added nested if's for the face detection api to return customize result Line 725
    - change on the body of the filterResponse()
    - Fixed SplashActivity
    - Added User model
	- Added Login and Register activity and .kt
	- fix all the lifecycle of 3 activities
	- Added AnonymousActivity for own activity but still call the same content of MainActivity
