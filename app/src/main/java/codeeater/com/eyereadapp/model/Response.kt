package codeeater.com.eyereadapp.model

class Response {

    lateinit var date: String
    lateinit var images: String
    lateinit var time: String
    lateinit var response: String
    lateinit var face: String

    constructor(
            date: String,
            images: String,
            time: String,
            response: String,
            face: String
    ) {
        this.date = date
        this.images = images
        this.time = time
        this.response = response
        this.face= face
    }

    constructor() {}
}
