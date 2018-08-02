// Initialize Firebase
var config = {
  apiKey: "AIzaSyCKGAa1tLK7khZ7RQfusrYtdpXHLsR3iLA",
  authDomain: "dev-code-eater-project.firebaseapp.com",
  databaseURL: "https://dev-code-eater-project.firebaseio.com",
  projectId: "dev-code-eater-project",
  storageBucket: "dev-code-eater-project.appspot.com",
  messagingSenderId: "873709423479"
};
firebase.initializeApp(config);
// Database reference
var mDatabase = firebase.database();
// reference
var mRef = mDatabase.ref('EyereadTest');
// TODO find a way that will get all the data.
mRef.once('value').then(function(snap){
  console.log(snap.val());
});
