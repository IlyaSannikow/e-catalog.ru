document.getElementById('change-password-form').onsubmit = async function(e) {
    e.preventDefault();

    // 1. Получаем CSRF токен из куки (правильный способ)
    const csrfToken = document.cookie
        .split('; ')
        .find(row => row.startsWith('XSRF-TOKEN='))
        ?.split('=')[1];

    // 2. Подготавливаем данные
    const data = {
        currentPassword: this.currentPassword.value,
        newPassword: this.newPassword.value
    };

    try {
        // 3. Отправляем запрос с ВСЕМИ необходимыми заголовками
        const response = await fetch("/changePassword", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": csrfToken, // Важно: без decodeURIComponent!
                "X-Requested-With": "XMLHttpRequest" // Добавляем для Spring Security
            },
            body: JSON.stringify(data),
            credentials: "include" // Обязательно для HTTPS и кук
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }

        alert("Пароль успешно изменен!");
    } catch (error) {
        console.error('Error:', error);
        alert("Ошибка: " + error.message);
    }
};

document.getElementById('profile-image-form').onsubmit = async function(e) {
    e.preventDefault();

    // 1. Получаем CSRF токен из куки
    const csrfToken = document.cookie
        .split('; ')
        .find(row => row.startsWith('XSRF-TOKEN='))
        ?.split('=')[1];

    // 2. Подготавливаем FormData
    const formData = new FormData();
    formData.append('file', this.file.files[0]);

    try {
        // 3. Отправляем запрос с заголовками
        const response = await fetch("/upload", {
            method: "POST",
            headers: {
                "X-XSRF-TOKEN": csrfToken,
                "X-Requested-With": "XMLHttpRequest"
            },
            body: formData,
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }

        alert("Изображение успешно загружено!");
        window.location.reload();
    } catch (error) {
        console.error('Error:', error);
        alert("Ошибка загрузки: " + error.message);
    }
};