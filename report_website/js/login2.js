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
// console.log(firebase)
/** [START] admin_page.html **/
const btnSignOut = document.getElementById('btnSignOut');

// Logout event
function signOut() {
  firebase.auth().signOut();
  document.location = "\login_page.html";
}

// check the user is sign in or not
firebase.auth().onAuthStateChanged(function(user) {
  if (user) {
    // console.log(user.email);
    var email = document.getElementById('userEmail');
    email.innerHTML = user.email;
    //document.location = "\admin_page.html";
  } else {
    alert("No User Detect, Redirecting to Login Page");
    document.location = "\login_page.html";
  }
});
/** [END] admin_page.html **/
