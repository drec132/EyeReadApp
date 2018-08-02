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
/** [START] admin_login.html **/
// Text Field
const uEmail = document.getElementById('uEmail');
const uPassword = document.getElementById('uPassword');
// Button
const btnLogin = document.getElementById('btnLogin');
const btnSignOut = document.getElementById('btnSignOut');
const loginForm = document.getElementById('loginForm');
// this can be stored on the firebase database for reference on the user inteaction on the login
// var attempt = 3;

// Login Event
btnLogin.addEventListener('click', e => {
  const email = uEmail.value;
  const pass = uPassword.value;
  const auth = firebase.auth();
  if (email == "" || pass == "")
  {
    const promise = auth.signInWithEmailAndPassword(email, pass);
    promise.catch(
      e => alert ("Wrong Email/Password")
    );
    // clear?
    loginForm.reset();
  } else {
    const promise = auth.signInWithEmailAndPassword(email, pass);
    promise.catch(
      e => alert (e.message)
    );

    // clear?
    loginForm.reset();
  }
});

// check the user is sign in or not
firebase.auth().onAuthStateChanged(function(user) {
  if (user) {
    console.log(user);
    document.location = "\index.html";
  } else {
    console.log("not logged in");
  }
});
/** [END] admin_login.html **/
