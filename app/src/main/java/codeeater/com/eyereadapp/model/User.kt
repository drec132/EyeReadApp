package codeeater.com.eyereadapp.model

class User {
    lateinit var name: String
    lateinit var email: String
    lateinit var age: String
    lateinit var gender: String

    constructor(name: String, email: String, age:String, gender: String) {
        this.name = name
        this.email = email
        this.age = age
        this.gender = gender
    }

    constructor() {}
}