document.addEventListener('DOMContentLoaded', function() {
    // Загружаем информацию о пользователе
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

                // Назначаем обработчик только после того, как элемент точно существует
                document.getElementById('logout-button').onclick = async function() {
                    const shouldLogout = confirm("Вы уверены, что хотите выйти из системы?");

                    if (shouldLogout) {
                        // Получаем CSRF токен из куки
                        const csrfToken = document.cookie
                            .split('; ')
                            .find(row => row.startsWith('XSRF-TOKEN='))
                            ?.split('=')[1];

                        try {
                            // Отправляем запрос на выход с CSRF токеном
                            const response = await fetch("/logout", {
                                method: "POST",
                                headers: {
                                    "X-XSRF-TOKEN": csrfToken,
                                    "X-Requested-With": "XMLHttpRequest"
                                },
                                credentials: "include"
                            });

                            if (!response.ok) {
                                throw new Error('Ошибка при выходе');
                            }

                            // Перенаправляем после успешного выхода
                            window.location.href = "/index.html";
                        } catch (error) {
                            console.error('Ошибка при выходе:', error);
                            alert("Не удалось выйти из системы");
                        }
                    }
                };
            } else {
                // Показать элементы для неавторизованного пользователя
                document.getElementById('login-div').style.display = 'block';
                document.getElementById('registration-div').style.display = 'block';
            }
        })
        .catch(error => console.error('Error fetching user info:', error));
});