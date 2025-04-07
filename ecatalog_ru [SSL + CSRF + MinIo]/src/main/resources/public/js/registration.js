(() => {
    document.querySelector("form#registrationForm").onsubmit = e => {
        e.preventDefault();

        const username = document.querySelector("input[name=username]").value;
        const password = document.querySelector("input[name=password]").value;

        // Отправляем данные для регистрации без CSRF токена
        fetch('/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        }).then(response => {
            if (response.ok) {
                // Переход на страницу входа при успешной регистрации
                window.location.href = '/login';
            } else {
                // Переход на главную страницу при неуспешной регистрации
                window.location.href = '/index.html';
            }
        });
    };
})();