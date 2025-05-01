async function logout() {
    const shouldLogout = confirm("Вы уверены, что хотите выйти из системы?");
    if (!shouldLogout) return;

    try {
        // 1. Сначала получаем CSRF токен через специальный endpoint
        const tokenResponse = await fetch("/csrf-token", {
            method: "GET",
            credentials: "include"
        });

        if (!tokenResponse.ok) {
            throw new Error('Не удалось получить CSRF токен');
        }

        // 2. Отправляем запрос на выход
        const response = await fetch("/logout", {
            method: "POST",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
                // CSRF токен автоматически подставится из куки
            },
            credentials: "include"
        });

        if (response.ok) {
            window.location.href = "/";
        } else {
            throw new Error('Ошибка при выходе');
        }
    } catch (error) {
        console.error('Ошибка при выходе:', error);
        alert("Не удалось выйти из системы");
    }
}