package codeeater.com.eyereadapp.fragment

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CaptureRequest.*
import android.media.ImageReader
import android.media.MediaActionSound
import android.net.Uri
import android.os.*
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.checkSelfPermission
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import codeeater.com.eyereadapp.R
import codeeater.com.eyereadapp.activity.AnonymousActivity
import codeeater.com.eyereadapp.activity.LoginActivity
import codeeater.com.eyereadapp.activity.MainActivity
import codeeater.com.eyereadapp.activity.ResponseListActivity
import codeeater.com.eyereadapp.model.Response
import codeeater.com.eyereadapp.utils.OutputFilter
import codeeater.com.eyereadapp.utils.PackageManagerUtils
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequest
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class VisionFragment : Fragment(){
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    // Create a Database Instance
    private val database = FirebaseDatabase.getInstance()!!
    // Create a Database Reference container
    private var dbRef: DatabaseReference? = null
    // Create a Storage Reference from Firebase
    private val mStorageRef = FirebaseStorage.getInstance()
    // Create reference for TTSpeech
    private var tts: TextToSpeech? = null

    // CAMERA2
    private var textureView: TextureView? = null
    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null

    private var error_request = "Cloud Vision API request failed. Check logs for details."
    // Array for the result
    private val arrResponse = ArrayList<String>()
    private val arrFaceResponse = ArrayList<String>()
    // WIDGETS
    private var imgSignout: ImageView? = null
    private var btnImage: Button? = null
    private var btnText: Button? = null
    private var btnList: Button? = null
    private var textResults: TextView? = null
    private var textRecognizer: TextRecognizer? = null
    private var frame: Frame? = null
    private var click : Boolean? = null
    private val outputFilter = OutputFilter()
    private var stringUri: Uri? = null
    private var dialog: ProgressDialog? = null
    private lateinit var sound: MediaActionSound
    private lateinit var vibe: Vibrator // HAHAHAHA

    override fun onCreateView(
            inflater: LayoutInflater?,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater!!.inflate(R.layout.fragment_vision, container, false)
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = ProgressDialog(context)
        dialog!!.setMessage("Please wait...")
        dialog!!.setCancelable(false)

        sound = MediaActionSound()
        // initialize the TTS
        initTextToSpeech()
        // initialize the buttons
        textResults = view!!.findViewById(R.id.textResult)

        btnImage = view.findViewById(R.id.btnImage)
        btnText = view.findViewById(R.id.btnText)
        btnList = view.findViewById(R.id.btnList)

        textureView = view.findViewById(R.id.cameraPreview)
        assert(textureView != null)

        textureView!!.surfaceTextureListener = textureListener

        btnImage!!.setOnClickListener {

            click = true

            btnText!!.isEnabled = false
            btnImage!!.isEnabled = false
            btnList!!.isEnabled = false
            vibrateOnClick()
            dialog!!.show()
            takePicture()
        }
        btnText!!.setOnClickListener {
            click = false

            btnText!!.isEnabled = false
            btnImage!!.isEnabled = false
            btnList!!.isEnabled = false
            vibrateOnClick()
            dialog!!.show()
            takePicture()
            dialog!!.hide()
            btnImage!!.isEnabled = true
            btnText!!.isEnabled = true
            btnList!!.isEnabled = true
        }
        btnList!!.setOnClickListener {
            vibrateOnClick()
            startActivity(Intent(context, ResponseListActivity::class.java))
        }

        imgSignout = view.findViewById(R.id.imgSignout)

        imgSignout!!.setOnClickListener {
            vibrateOnClick()
            mAuth.signOut()

            startActivity(Intent(context, LoginActivity::class.java))
            activity.finish()
        }

        /** STORE THIS TO ANOTHER FUNCTION **/
        //Text Recognition
        textRecognizer = TextRecognizer.Builder(context).build()

        if (!(textRecognizer)!!.isOperational)  {
            Toast.makeText(context, "Dependencies are not available", Toast.LENGTH_SHORT).show()
        }

        textResults!!.setOnClickListener {
            tts!!.speak(textResults!!.text.toString(), TextToSpeech.QUEUE_ADD, null)
        }

    }

    /** CENTER CROPPING IMAGE **/
    private fun centerCropImage(bitmap: Bitmap): Bitmap {
        return if (bitmap.width >= bitmap.height) Bitmap.createBitmap(bitmap, bitmap.width /2 - bitmap.height /2, 0, bitmap.height, bitmap.height)
        else {
            Bitmap.createBitmap(bitmap, 0, bitmap.height /2 - bitmap.width /2, bitmap.width, bitmap.width)
        }
    }
    /** INITIALIZATION FOR VIBRATOR SERVICE **/
    private fun vibrateOnClick() {
        vibe = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibe.vibrate(100)

    }

    /** INITIALIZATION FOR TEXT TO SPEECH **/
    private fun initTextToSpeech() {
        // initialize the tts
        tts = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                tts!!.language = Locale.ENGLISH
                tts!!.setSpeechRate(0.9f)
                tts!!.setPitch(0.8f)
            }
        })
    }

    /** FRAGMENT LIFECYCLE **/
    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
        startBackgroundThread()
        if (textureView!!.isAvailable) {
            openCamera()
            initTextToSpeech()
        } else {
            textureView!!.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        Log.e(TAG, "onPause")
        closeCamera()
        stopBackgroundThread()
        tts!!.shutdown()
        super.onPause()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        tts!!.stop()
    }

    override fun onStart() {
        super.onStart()
        initTextToSpeech()
    }
    override fun onStop() {
        super.onStop()
        tts!!.stop()
    }

    /** TEXTURE AND STATECALLBACK OF CAMERA2 API**/
    private var textureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            //open your camera here
            openCamera()
        }
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            // Transform you image captured size according to the surface width and height
        }
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }
    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened")
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice!!.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }

    /** HANDLERS FOR CAMERA2 **/
    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }
    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }
    private fun openCamera() {
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.e(TAG, "is camera open")
        try {
            cameraId = manager.cameraIdList[0]
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            // Add permission for camera and let user grant the permission
            if (checkSelfPermission(context, CAMERA) != PERMISSION_GRANTED && checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE), CAMERA_PERMISSIONS_REQUEST)
                return
            }
            manager.openCamera(cameraId, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        Log.e(TAG, "openCamera X")
    }
    private fun closeCamera() {
        if (cameraDevice != null  ) {
            cameraDevice!!.close()
            cameraDevice = null
        }
        if (imageReader != null) {
            imageReader!!.close()
            imageReader = null
        }
    }

    /** CAMERA2 TakePicture Event and PREVIEW**/
    private fun takePicture() {

        tts!!.stop()
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null")
            return
        }
        try {
            val width = 960
            val height = 720

            val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurfaces = ArrayList<Surface>(2)
            outputSurfaces.add(reader.surface)
            outputSurfaces.add(Surface(textureView!!.surfaceTexture))

            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            // CHANGE THE CAPTURE REQUEST OR WHAT
            // Parameters for.set(CameraRequest, CameraMetadata)
            captureBuilder.set(CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            captureBuilder.set(CONTROL_AE_ANTIBANDING_MODE, CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_AUTO)
            captureBuilder.set(STATISTICS_LENS_SHADING_MAP_MODE, CameraMetadata.STATISTICS_LENS_SHADING_MAP_MODE_ON)
            captureBuilder.set(CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

            // Orientation
            val rotation = activity.windowManager.defaultDisplay.rotation
            // can be delete the orientation
            captureBuilder.set(JPEG_ORIENTATION, ORIENTATIONS.get(rotation))

            val file = getCameraFile()

            @Suppress("NAME_SHADOWING")
            val readerListener = object : ImageReader.OnImageAvailableListener {
                override fun onImageAvailable(reader: ImageReader) {
                    val image: android.media.Image? = reader.acquireLatestImage()
                    try {
                        val buffer = image!!.planes[0].buffer
                        val bytes = ByteArray(buffer.capacity())
                        buffer.get(bytes)
                        save(bytes)

                        //converting byteArray to bitmap for the API's
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                        //setting converted byteArray to frame builder
                        frame = Frame.Builder().setBitmap(centerCropImage(bitmap)).build()

                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        image?.close()
                    }
                }

                @Throws(IOException::class)
                private fun save(bytes: ByteArray) {
                    var output: OutputStream? = null
                    try {
                        output = FileOutputStream(file)
                        output.write(bytes)
                    } finally {
                        if (null != output) {
                            output.close()
                        }
                    }
                }
            }
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)

            val captureListener = object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureStarted(session: CameraCaptureSession?, request: CaptureRequest?, timestamp: Long, frameNumber: Long) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber)

                    // camera shutter click effect
                    sound.play(MediaActionSound.SHUTTER_CLICK)
                }
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    super.onCaptureCompleted(session, request, result)

                    // expression for determining the button clicked by the user
                    when(click) {
                        true -> {
                            val photoUri = FileProvider.getUriForFile(context, activity.applicationContext.packageName + ".provider", getCameraFile())

                            stringUri = photoUri // this is for the Firebase Database and Storage
                            uploadImage(photoUri) // this is for the CallVisionAPI
                            // Toast.makeText(context, "Saved:" + file, Toast.LENGTH_SHORT).show()

                            if(textResults!!.text == error_request ) {
                                tts!!.speak("I can't connect to the internet", TextToSpeech.QUEUE_ADD, null)
                            }
                        }
                        else -> {

                            val items = textRecognizer!!.detect(frame)

                            if (items.size() != 0) {
                                textResults!!.post {
                                    val stringBuilder = StringBuilder()
                                    for (i in 0 until items.size()) {
                                        val item = items.valueAt(i)
                                        stringBuilder.append(item.value)
                                        stringBuilder.append("\n")
                                    }
                                    textResults!!.text = stringBuilder.toString()
                                }
                                tts!!.speak("Text Detected, tap to read", TextToSpeech.QUEUE_ADD, null)
                            } else {
                                tts!!.speak("No text detected!", TextToSpeech.QUEUE_ADD, null)
                            }
                        }
                    }
                    createCameraPreview()
                }
            }
            cameraDevice!!.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }
    private fun createCameraPreview() {
        try {
            val texture = textureView!!.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureRequestBuilder!!.addTarget(surface)
            cameraDevice!!.createCaptureSession(Arrays.asList(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return
                    }
                    try {
                        // When the session is ready, we start displaying the preview.
                        cameraCaptureSessions = cameraCaptureSession
                        captureRequestBuilder!!.set(CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START)
                        captureRequestBuilder!!.set(CONTROL_AE_ANTIBANDING_MODE, CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_AUTO)
                        captureRequestBuilder!!.set(STATISTICS_LENS_SHADING_MAP_MODE, CameraMetadata.STATISTICS_LENS_SHADING_MAP_MODE_ON)
                        captureRequestBuilder!!.set(CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

                        cameraCaptureSessions!!.setRepeatingRequest(captureRequestBuilder!!.build(), null, mBackgroundHandler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }
                override fun onConfigureFailed( cameraCaptureSession: CameraCaptureSession) {
                    Toast.makeText(context, "Configuration change", Toast.LENGTH_SHORT).show()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    /** GETTING THE PICTURE FROM THE LOCAL DEVICE **/
    fun getCameraFile(): File {
        val dir = activity.getExternalFilesDir(DIRECTORY_PICTURES)
        return File(dir, FILE_NAME)
    }

    /** PERMISSION **/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /** UPLOADING THE IMAGE TO THE FIREBASE and FOR CALLING THE CLOUD API**/
    // uploading the image as reference then get the dlurl for reference in database
    // no use yet. since no duplication checking
    fun uploadImage(uri: Uri?) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                val srcBmp = scaleBitmapDown(
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri),
                        1000)
                // request the image
                callCloudVision(centerCropImage(srcBmp))

            } catch (e: IOException) {
                Log.d(TAG, "Image Upload failed because " + e.message)
                Toast.makeText(
                        this.context,
                        R.string.image_picker_error,
                        Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d(TAG, "Image gave us a null image.")
            Toast.makeText(
                    this.context,
                    R.string.image_picker_error,
                    Toast.LENGTH_LONG).show()

        }
    }
    /** Uploads the data to the firebase database and storage **/
    @SuppressLint("SimpleDateFormat")
    private fun postProcessUpload() {
        if (stringUri != null) {

            val mn = MainActivity()
            val mn2 = AnonymousActivity()
            // CHECK FOR BUGS
            dbRef = if (mn.giveUser() == null) {
                database.getReference(MainActivity.FB_DATABASE_PATH).child(mn2.giveUser())
            } else {
                database.getReference(MainActivity.FB_DATABASE_PATH).child(mn.giveUser())
            }

            //Get the storage reference
            val ref = mStorageRef.reference.child(MainActivity.FB_STORAGE_PATH + System.currentTimeMillis() + ".jpg")
            // create an custom metaData
            val metadata = StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build()

            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm:ss a")
            val date = Date()
            val formattedDate = dateFormat.format(date).toString()
            val formattedTime = timeFormat.format(date).toString()

            // upload the file to the firebase
            ref.putFile(stringUri!!, metadata).addOnSuccessListener { taskSnapshot ->
                //Dismiss dialog when success
                //Display success toast msg
                Toast.makeText(context, "Image uploaded", Toast.LENGTH_SHORT).show()
                val imageUpload = Response(
                        formattedDate,
                        taskSnapshot.downloadUrl!!.toString(),
                        formattedTime,
                        arrResponse.toString(),
                        arrFaceResponse.toString()
                )
                //Save image info in to firebase database
                try {
                    val uploadId = dbRef!!.push().key
                    dbRef!!.child(uploadId).setValue(imageUpload)
                } catch (e: NullPointerException) {
                    Toast.makeText(context, "Error: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.d(TAG, "Image gave us a null image.")
            Toast.makeText(
                    this.context,
                    R.string.image_picker_error,
                    Toast.LENGTH_LONG).show()

        }
    }


    /** REQUESTING IN CLOUD VISION **/
    // requesting to use the Vision API
    @Throws(IOException::class)
    private fun callCloudVision(bitmap: Bitmap) {
        // Switch text to loading
        //mImageDetails!!.setText(R.string.loading_message)
        // Made it an internal class
        // Do the real work in an async task, because we need to use the network anyway
        class Async : AsyncTask<Any, Void, String>() {
            override fun doInBackground(vararg params: Any): String {
                try {
                    val httpTransport = AndroidHttp.newCompatibleTransport()
                    val jsonFactory = GsonFactory.getDefaultInstance()

                    val requestInitializer = object : VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                        /**
                         * We override this so we can inject important identifying fields into the HTTP
                         * headers. This enables use of a restricted cloud platform API key.
                         */
                        override fun initializeVisionRequest(visionRequest: VisionRequest<*>) {
                            super.initializeVisionRequest(visionRequest)

                            val packageName = activity.packageName
                            visionRequest.requestHeaders.set(ANDROID_PACKAGE_HEADER, packageName)

                            val sig = PackageManagerUtils.getSignature(activity.packageManager, packageName)

                            visionRequest.requestHeaders.set(ANDROID_CERT_HEADER, sig)
                        }
                    }

                    val builder = Vision.Builder(httpTransport, jsonFactory, null)
                    builder.setVisionRequestInitializer(requestInitializer)

                    val vision = builder.build()

                    val batchAnnotateImagesRequest = BatchAnnotateImagesRequest()

                    batchAnnotateImagesRequest.requests = object : ArrayList<AnnotateImageRequest>() {
                        init {
                            val annotateImageRequest = AnnotateImageRequest()

                            // Add the image
                            val base64EncodedImage = Image()
                            // Convert the bitmap to a JPEG
                            // Just in case it's a format that Android understands but Cloud Vision
                            val byteArrayOutputStream = ByteArrayOutputStream()

                            bitmap.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    70,
                                    byteArrayOutputStream)

                            val imageBytes = byteArrayOutputStream.toByteArray()

                            // Base64 encode the JPEG
                            base64EncodedImage.encodeContent(imageBytes)
                            annotateImageRequest.image = base64EncodedImage

                            // add the features we want
                            annotateImageRequest.features = object : ArrayList<Feature>() {
                                init
                                {
                                    val labelDetection = Feature()
                                    labelDetection.type = "LABEL_DETECTION"
                                    labelDetection.maxResults = 15

                                    add(labelDetection)

                                    val faceDetection = Feature()
                                    faceDetection.type = "FACE_DETECTION"
                                    faceDetection.maxResults = 5
                                    add(faceDetection)
                                }
                            }
                            // Add the list of one thing to the request
                            add(annotateImageRequest)
                        }
                    }

                    val annotateRequest = vision.images().annotate(batchAnnotateImagesRequest)
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.disableGZipContent = true
                    Log.d(TAG, "created Cloud Vision request object, sending request")

                    val response = annotateRequest.execute()
                    return convertResponseToString(response)
                } catch (e: GoogleJsonResponseException) {
                    Log.d(TAG, "failed to make API request because " + e.content)
                } catch (e: IOException) {
                    Log.d(TAG, "failed to make API request because of other IOException " + e.message)
                }

                return error_request
            }

            @SuppressLint("SimpleDateFormat")
            override fun onPostExecute(result: String) {
                dialog!!.hide()
                // upload the data to the firebase
                postProcessUpload()

                // Displays the results
                if (result.isNotEmpty()) {
                    textResults!!.visibility = View.VISIBLE
                    textResults!!.text = result
                }

                btnText!!.isEnabled = true
                btnImage!!.isEnabled = true
                btnList!!.isEnabled = true
            }
        }
        // just gonna call the Async class (not the object)
        // to replace avoid the leak on the object
        Async().execute()
    }

    /** SCALING THE IMAGE TO A SCALE **/
    // conversion of the image to the small scale
    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var resizedWidth = maxDimension
        var resizedHeight = maxDimension

        when {
            originalHeight > originalWidth -> {
                resizedHeight = maxDimension
                resizedWidth = (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
            }
            originalWidth > originalHeight -> {
                resizedWidth = maxDimension
                resizedHeight = (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
            }
            originalHeight == originalWidth -> {
                resizedHeight = maxDimension
                resizedWidth = maxDimension
            }
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
    }
    // This for the output part
    // must be pass to the Filtration System

    /** CONVERTING AND STORING THE DATA TO THE ARRAY **/
    private fun convertResponseToString(response: BatchAnnotateImagesResponse): String {

        // every time this will be called it will clear the arraylist
        arrFaceResponse.clear()
        arrResponse.clear()

        val faceDetected = StringBuilder("")
        val output = StringBuilder("")
        val ttsSpeak = StringBuilder("I found. \n\n")

        // Getting the label_detection result
        val labels = response.responses[0].labelAnnotations
        val toBeF = arrayListOf<String>()

        if (labels != null) labels.mapTo(toBeF) { it.description } else {
            ttsSpeak.append("nothing")
        }

        if(labels != null) {
            // ADD A AND FOR THE SPEECH SINCE IT WILL SAY IT STRAIGHT
            ttsSpeak.append(String.format(Locale.UK, "%s", outputFilter.filterResponse(toBeF)))
            output.append(outputFilter.filterResponse(toBeF))
            arrResponse += outputFilter.filterResponse(toBeF)
        }

        // Getting the result for the face detection
        val faces = response.responses[0].faceAnnotations

        if (faces != null) for (face in faces) {
            var emotion = ""
            if(face.angerLikelihood != "VERY_UNLIKELY"
                    && (face.angerLikelihood == "VERY_LIKELY" || face.angerLikelihood == "POSSIBLE")) {
                emotion = "The face is angry"
            }
            if(face.joyLikelihood != "VERY_UNLIKELY"
                    && (face.joyLikelihood == "VERY_LIKELY" || face.joyLikelihood == "POSSIBLE")) {
                emotion = "The face is happy"
            }
            if(face.sorrowLikelihood != "VERY_UNLIKELY"
                    && (face.sorrowLikelihood== "VERY_LIKELY" || face.sorrowLikelihood == "POSSIBLE")) {
                emotion = "The face is sad"
            }
            if(face.surpriseLikelihood != "VERY_UNLIKELY"
                    && (face.surpriseLikelihood == "VERY_LIKELY" || face.surpriseLikelihood == "POSSIBLE")) {
                emotion = "The face is surprised"
            }

            faceDetected.append(emotion)
            faceDetected.append("\n")

            arrFaceResponse.add(0, "Anger: " + face.angerLikelihood)
            arrFaceResponse.add(1, "Joy: " + face.joyLikelihood)
            arrFaceResponse.add(2, "Sorrow: " + face.sorrowLikelihood)
            arrFaceResponse.add(3, "Surprise: " + face.surpriseLikelihood)
        }

        if (!arrResponse.isEmpty()) {
            if (ttsSpeak.equals("[]")) {
                texttospeechoutput("I found no object")
            } else {
                texttospeechoutput(ttsSpeak.toString())

            }
        } else {
            texttospeechoutput("I found no object")
        }
        // delay this after this after uploading the file into firebase
        texttospeechoutput(" " + faceDetected.toString())

        val vfResult = StringBuilder(faceDetected.toString() + "\n" + output.toString())

        return vfResult.toString()
    }

    // [END] filterResponse
    /** Text-to-Speech output**/
    private fun texttospeechoutput(iString: String) = tts!!.speak("" + iString, TextToSpeech.QUEUE_ADD,null)

    companion object {
        private val CLOUD_VISION_API_KEY = "AIzaSyDcVnb8Rx5-OIfpJKE62RD6ZND5UAvJZIo"

        private val ANDROID_CERT_HEADER = "X-Android-Cert"
        private val ANDROID_PACKAGE_HEADER = "X-Android-Package"

        private val TAG = VisionFragment::class.java.simpleName

        val FILE_NAME = "temp.jpg"
        val CAMERA_PERMISSIONS_REQUEST = 2

        var ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }
}