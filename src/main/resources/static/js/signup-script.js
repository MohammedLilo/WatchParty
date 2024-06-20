
    document.addEventListener("DOMContentLoaded", function() {
        const urlParams = new URLSearchParams(window.location.search);
        const error = urlParams.get('err');
        if (error) {
            const errorDiv = document.getElementById('error-message');
            errorDiv.textContent = error;
            errorDiv.style.display = 'block';
        }
    });
    
    
        document.getElementById('registrationForm').addEventListener('submit', function(event) {
         //   event.preventDefault(); // Prevent the form from submitting

            var email = document.getElementById('email').value;
            var password = document.getElementById('password').value;
            var fullName = document.getElementById('name').value;

            var xhr = new XMLHttpRequest();
            xhr.open('POST', '/signup', true);
            xhr.setRequestHeader('Content-Type', 'application/json');
            xhr.onreadystatechange = function() {
                if (xhr.readyState === XMLHttpRequest.DONE) {
                    if (xhr.status === 201) {
                        document.getElementById('message').innerText = 'Account Registered successfully.';
                    } else {
                        document.getElementById('message').innerText = 'Failed to register account.';
                    }
                }
            };
            console.log(email);
            xhr.send(JSON.stringify({email: email, password: password, fullName: fullName}));
        });