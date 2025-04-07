(() => {
    document.addEventListener('DOMContentLoaded', function() {
        fetch('/user-info')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                var isAuthenticated = data.isAuthenticated;
                var roles = data.roles;

                if (isAuthenticated) {
                    // Показать элементы для авторизованного пользователя
                    document.getElementById('logout-div').style.display = 'block';
                    document.getElementById('profile-div').style.display = 'block';
                    if (roles.includes('ROLE_ADMIN')) {
                        document.getElementById('admin-div').style.display = 'block';
                    }
                } else {
                    // Показать элементы для неавторизованного пользователя
                    document.getElementById('login-div').style.display = 'block';
                    document.getElementById('registration-div').style.display = 'block';
                }
            })
            .catch(error => console.error('Error fetching user info:', error));
    });
})();