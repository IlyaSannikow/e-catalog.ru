// Общая функция для получения CSRF токена
async function getCsrfToken() {
    const response = await fetch("/csrf-token", {
        method: "GET",
        credentials: "include"
    });
    if (!response.ok) {
        throw new Error('Failed to get CSRF token');
    }
}

document.getElementById('change-password-form').onsubmit = async function(e) {
    e.preventDefault();

    try {
        // 1. Получаем CSRF токен
        await getCsrfToken();

        // 2. Подготавливаем данные
        const data = {
            currentPassword: this.currentPassword.value,
            newPassword: this.newPassword.value
        };

        // 3. Отправляем запрос
        const response = await fetch("/changePassword", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-Requested-With": "XMLHttpRequest"
                // CSRF токен автоматически подставится из куки
            },
            body: JSON.stringify(data),
            credentials: "include"
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

    try {
        // 1. Получаем CSRF токен
        await getCsrfToken();

        // 2. Подготавливаем FormData
        const formData = new FormData();
        formData.append('file', this.file.files[0]);

        // 3. Отправляем запрос
        const response = await fetch("/upload", {
            method: "POST",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
                // CSRF токен автоматически подставится из куки
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