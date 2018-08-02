package codeeater.com.eyereadapp.utils

import java.util.*
import kotlin.collections.ArrayList

/**
 * This will remove all the non-required output
 * **/
open class OutputFilter {
    private val listOfShape = ArrayList(Arrays.asList(
            "circle",
            "oval",
            "heart",
            "triangle",
            "square",
            "trapezium",
            "diamond",
            "rhombus",
            "parallelogram",
            "rectangle",
            "star",
            "pentagon",
            "hexagon",
            "heptagon",
            "octagon",
            "nonagon",
            "decagon"
    ))

    private val listOfColor = ArrayList(Arrays.asList(
            "red",
            "orange",
            "blue",
            "yellow",
            "gray",
            "black",
            "pink",
            "white"
    ))

    private val listOfAnimals = ArrayList(Arrays.asList(
            "cat",
            "dog",
            "sheep",
            "bird",
            "snake",
            "ant",
            "alligator",
            "lion",
            "tiger",
            "parrot",
            "leopard",
            "koala",
            "lobster",
            "jellyfish",
            "horse",
            "mouse",
            "hamster",
            "monkey",
            "gorilla",
            "panda",
            "bear",
            "turtle",
            "pig",
            "wolf",
            "puppy",
            "cub",
            "kitten",
            "owl",
            "zebra"

    ))
    private val listOfOutput = ArrayList(Arrays.asList(
            "car",
            "ball",
            "coin",
            "bottle",
            "water bottle",
            "laptop",
            "text",
            "display device",
            "slipper",
            "shoe",
            "girl",
            "boy",
            "lady",
            "gentlemen",
            "sign",
            "lotion",
            "football",
            "printer",
            "photograph",
            "pen",
            "ball pen",
            "mouse",
            "dice",
            "anime",
            "cartoon",
            "doodle",
            "trash",
            "tissue",
            "watch",
            "clock",
            "pillow",
            "key",
            "id",
            "picture",
            "frame",
            "door knob",
            "mirror",
            "furniture",
            "jacket"
    ))

    private val listOfFruits = ArrayList(Arrays.asList(
            "apple",
            "banana",
            "orange",
            "mango",
            "grapes",
            "lemon",
            "kiwi",
            "melon",
            "watermelon",
            "avocado",
            "cherry",
            "coconut",
            "cucumber",
            "durian",
            "guava",
            "papaya",
            "strawberry",
            "tamarind",
            "pear",
            "peach",
            "jackfruit"
    ))

    private val listOfVegetables = ArrayList(Arrays.asList(
            "broccoli",
            "cabbage",
            "celery",
            "lettuce",
            "spinach",
            "bell pepper",
            "cucumber",
            "eggplant",
            "squash",
            "pumpkin",
            "tomato",
            "carrots",
            "cauliflower",
            "pea",
            "peanut",
            "soybean",
            "garlic",
            "onion",
            "potato",
            "ginger",
            "radish"
    ))

    private val listOfSnacks = ArrayList(Arrays.asList(
            "pie",
            "cake",
            "burger",
            "sandwich",
            "fries",
            "jams",
            "chocolate",
            "shake",
            "smoothies",
            "ice cream",
            "pizza",
            "rice",
            "spaghetti",
            "fried chicken",
            "bread",
            "coffee",
            "hotdog",
            "donut",
            "softdrinks",
            "cookies"
    ))

    private val listOfTechnologies = ArrayList(Arrays.asList(
            "personal computer",
            "desktop computer",
            "laptop",
            "printer",
            "projector",
            "refrigerator",
            "oven",
            "camera",
            "smartphone",
            "headphone",
            "headset",
            "charger",
            "keyboard",
            "monitor",
            "aircon"
    ))

    private val listOfFurniture = ArrayList(Arrays.asList(
            "table",
            "desk",
            "cabinet",
            "bed",
            "sofa",
            "rocking chair",
            "bench",
            "couch",
            "mattress",
            "billiard table",
            "chess table",
            "closet",
            "shelf",
            "chair",
            "plywood"
    ))

    private val listOfBodyParts = ArrayList(Arrays.asList(
            "eyebrow",
            "nose",
            "cheek",
            "face",
            "chin",
            "chest",
            "beard",
            "stomach",
            "leg",
            "toe",
            "foot",
            "knee",
            "tongue",
            "mouth",
            "tooth",
            "eye",
            "ear",
            "jaw",
            "forehead",
            "elbow",
            "arm",
            "neck",
            "hair",
            "head",
            "shoulder",
            "back",
            "waist",
            "hand",
            "finger"
    ))


    // [START] filterResponse
    open fun filterResponse(arrayList: ArrayList<String>): ArrayList<String> {

        val listing = ArrayList(arrayList)

        listing.retainAll(listOfOutput)

        return listing
    }
    // [END] filterResponse
    init {
        listOfOutput += listOfColor
        listOfOutput += listOfShape
        listOfOutput += listOfAnimals
        listOfOutput += listOfFruits
        listOfOutput += listOfVegetables
        listOfOutput += listOfSnacks
        listOfOutput += listOfTechnologies
        listOfOutput += listOfBodyParts
        listOfOutput += listOfFurniture
    }

}